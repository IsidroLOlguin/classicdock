package cu.axel.smartdock.components

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import cu.axel.smartdock.R
import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.utils.AppUtils
import cu.axel.smartdock.utils.Utils
import cu.axel.smartdock.wrappers.ActivityManagerWrapper
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class ForegroundTask(
    val id: Int,
    val packageName: String,
    val windowingMode: Int,
    val displayId: Int,
    val activity: String = ""
)

enum class CloseResult { CLOSED, NO_TARGET, UNAVAILABLE }

object WindowControlsPolicy {
    const val INVALID_DISPLAY_ID = -1
    const val RECENTS_ACTIVITY = "com.android.quickstep.RecentsActivity"

    fun targetTask(tasks: List<ForegroundTask>, displayId: Int, launcher: String, own: String): ForegroundTask? {
        val top = tasks.firstOrNull { it.displayId == displayId } ?: return null
        if (top.windowingMode == WINDOWING_MODE_FREEFORM || top.activity == RECENTS_ACTIVITY) return null
        if (top.packageName == launcher || top.packageName == own || top.packageName.startsWith("com.android.systemui"))
            return null
        return top
    }

    fun runOnTask(task: ForegroundTask?, action: (Int) -> Unit) {
        if (task != null && task.id > 0) action(task.id)
    }

    fun close(available: Boolean, task: ForegroundTask?, remove: (Int) -> Unit): CloseResult {
        if (!available) return CloseResult.UNAVAILABLE
        if (task == null || task.id <= 0) return CloseResult.NO_TARGET
        return try {
            remove(task.id)
            CloseResult.CLOSED
        } catch (e: Exception) {
            CloseResult.UNAVAILABLE
        }
    }
}

class WindowControlsBar(
    private val context: Context,
    private val windowManager: WindowManager,
    secondaryDisplay: Boolean,
    private val activityManager: () -> ActivityManagerWrapper?,
    goHome: () -> Unit
) {
    private val view = LayoutInflater.from(ContextThemeWrapper(context, R.style.AppTheme_Dock))
        .inflate(R.layout.window_controls, null)
    private val displayId = runCatching { context.display.displayId }
        .getOrDefault(Display.DEFAULT_DISPLAY)
    private val getWindowingMode = runCatching {
        ActivityManager.RunningTaskInfo::class.java.getMethod("getWindowingMode")
    }.getOrNull()
    private val displayIdField = runCatching {
        ActivityManager.RunningTaskInfo::class.java.getField("displayId")
    }.getOrNull()
    private val executor = Executors.newSingleThreadExecutor()
    private val queued = AtomicBoolean(false)
    private val buttons = view.findViewById<View>(R.id.window_buttons)
    private val notice = view.findViewById<View>(R.id.window_notice)
    private var warning = false
    private val hideNotice = Runnable {
        warning = false
        notice.visibility = View.GONE
        buttons.visibility = View.VISIBLE
        update()
    }
    private var destroyed = false

    init {
        view.findViewById<View>(R.id.window_minimize_btn).setOnClickListener { goHome() }
        view.findViewById<View>(R.id.window_close_btn).setOnClickListener { closeForeground() }
        val params = Utils.makeWindowParams(-2, -2, context, secondaryDisplay, true)
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        windowManager.addView(view, params)
    }

    private fun availableManager() = activityManager()?.takeIf { it.isAlive() }

    private fun resolveTarget(am: ActivityManagerWrapper): ForegroundTask? = try {
        WindowControlsPolicy.targetTask(
            am.getRunningTasks(5).filter { it.isRunning && it.topActivity != null }.map {
                ForegroundTask(
                    it.id,
                    it.topActivity!!.packageName,
                    getWindowingMode!!.invoke(it) as Int,
                    displayIdField?.getInt(it) ?: WindowControlsPolicy.INVALID_DISPLAY_ID,
                    it.topActivity!!.className
                )
            },
            displayId,
            AppUtils.getCurrentLauncher(context.packageManager),
            context.packageName
        )
    } catch (e: Exception) {
        Log.w("WindowControlsBar", "No se pudo resolver la tarea en primer plano", e)
        null
    }

    private fun closeForeground() {
        if (destroyed) return
        val am = availableManager()
        executor.execute {
            val result = WindowControlsPolicy.close(am != null, am?.let { resolveTarget(it) }) { am?.removeTask(it) }
            if (result == CloseResult.UNAVAILABLE) view.post { warnUnavailable() }
        }
    }

    // Samsung suprime los Toast de apps con notificaciones bloqueadas (medido en SM-X910), así que el aviso también va dentro de la barra.
    private fun warnUnavailable() {
        Toast.makeText(context, R.string.close_needs_shizuku, Toast.LENGTH_LONG).show()
        warning = true
        buttons.visibility = View.GONE
        notice.visibility = View.VISIBLE
        view.removeCallbacks(hideNotice)
        view.postDelayed(hideNotice, 3000)
    }

    fun update() {
        if (destroyed || warning) return
        val am = availableManager()
        if (am == null) {
            view.visibility = View.GONE
            return
        }
        if (!queued.compareAndSet(false, true)) return
        executor.execute {
            queued.set(false)
            val visible = resolveTarget(am) != null
            view.post { if (!warning) view.visibility = if (visible) View.VISIBLE else View.GONE }
        }
    }

    fun destroy() {
        destroyed = true
        executor.shutdownNow()
        windowManager.removeViewImmediate(view)
    }
}
