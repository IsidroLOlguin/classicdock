package cu.axel.smartdock.components

import android.app.ActivityManager
import android.content.Context
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import cu.axel.smartdock.R
import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.models.WINDOWING_MODE_FULLSCREEN
import cu.axel.smartdock.utils.AppUtils
import cu.axel.smartdock.utils.Utils
import cu.axel.smartdock.wrappers.ActivityManagerWrapper
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

data class ForegroundTask(
    val id: Int,
    val packageName: String,
    val windowingMode: Int,
    val displayId: Int,
    val activity: String = ""
)

enum class CloseResult { CLOSED, NO_TARGET, UNAVAILABLE, FAILED }

object WindowControlsPolicy {
    const val FALLBACK_ID = -1
    const val RECENTS_ACTIVITY = "com.android.quickstep.RecentsActivity"

    fun targetTask(tasks: List<ForegroundTask>, displayId: Int, launcher: String, own: String): ForegroundTask? {
        val top = tasks.firstOrNull { it.displayId == displayId } ?: return null
        if (top.windowingMode == WINDOWING_MODE_FREEFORM || top.activity == RECENTS_ACTIVITY) return null
        if (top.packageName == launcher || top.packageName == own || top.packageName.startsWith("com.android.systemui"))
            return null
        return top
    }

    // Sin Shizuku solo se conoce el paquete en primer plano; el id -1 impide cerrar y el modo se asume pantalla completa.
    fun fallbackTask(packageName: String?, displayId: Int) =
        packageName?.let { ForegroundTask(FALLBACK_ID, it, WINDOWING_MODE_FULLSCREEN, displayId) }

    fun close(available: Boolean, task: ForegroundTask?, remove: (Int) -> Boolean): CloseResult {
        if (!available || task?.id == FALLBACK_ID) return CloseResult.UNAVAILABLE
        if (task == null || task.id <= 0) return CloseResult.NO_TARGET
        return try {
            if (remove(task.id)) CloseResult.CLOSED else CloseResult.FAILED
        } catch (e: SecurityException) {
            CloseResult.UNAVAILABLE
        } catch (e: RemoteException) {
            CloseResult.UNAVAILABLE
        } catch (e: Exception) {
            CloseResult.FAILED
        }
    }
}

class WindowControlsBar(
    private val context: Context,
    private val windowManager: WindowManager,
    secondaryDisplay: Boolean,
    private val activityManager: () -> ActivityManagerWrapper?,
    private val foregroundPackage: (Int) -> String?,
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
    @Volatile private var fallbackPackage: String? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val queued = AtomicBoolean(false)
    private val buttons = view.findViewById<View>(R.id.window_buttons)
    private val notice = view.findViewById<TextView>(R.id.window_notice)
    @Volatile private var warning = false
    private val hideNotice = Runnable {
        if (destroyed) return@Runnable
        endNotice()
        update()
    }
    @Volatile private var destroyed = false

    init {
        view.findViewById<View>(R.id.window_minimize_btn).setOnClickListener { goHome() }
        view.findViewById<View>(R.id.window_close_btn).setOnClickListener { closeForeground() }
        val params = Utils.makeWindowParams(-2, -2, context, secondaryDisplay, true)
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        windowManager.addView(view, params)
    }

    private fun availableManager() = activityManager()?.takeIf { it.isAlive() }

    private fun runInBackground(block: () -> Unit) {
        try {
            executor.execute(block)
        } catch (_: RejectedExecutionException) {
        }
    }

    // La accesibilidad solo se puede consultar desde el hilo principal; el executor lee el resultado ya resuelto.
    private fun refreshFallbackPackage() {
        fallbackPackage = runCatching { foregroundPackage(displayId) }
            .onFailure { Log.w(TAG, "No se pudo leer el paquete en primer plano por accesibilidad", it) }
            .getOrNull()
    }

    private fun shizukuTasks(am: ActivityManagerWrapper): List<ForegroundTask>? {
        val field = displayIdField ?: return null
        return try {
            am.getRunningTasks(MAX_TASKS).filter { it.isRunning && it.topActivity != null }.map {
                ForegroundTask(
                    it.id,
                    it.topActivity!!.packageName,
                    getWindowingMode!!.invoke(it) as Int,
                    field.getInt(it),
                    it.topActivity!!.className
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "getRunningTasks fallo, uso accesibilidad", e)
            null
        }
    }

    private fun resolveTarget(am: ActivityManagerWrapper?): ForegroundTask? = try {
        WindowControlsPolicy.targetTask(
            am?.let { shizukuTasks(it) }
                ?: listOfNotNull(WindowControlsPolicy.fallbackTask(fallbackPackage, displayId)),
            displayId,
            AppUtils.getCurrentLauncher(context.packageManager),
            context.packageName
        )
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo resolver la tarea en primer plano", e)
        null
    }

    private fun closeForeground() {
        if (destroyed) return
        val am = availableManager()
        refreshFallbackPackage()
        runInBackground {
            val result = WindowControlsPolicy.close(am != null, resolveTarget(am)) {
                try {
                    am!!.removeTask(it)
                } catch (e: Exception) {
                    Log.w(TAG, "removeTask($it) lanzo", e)
                    throw e
                }
            }
            when (result) {
                CloseResult.UNAVAILABLE -> view.post { warn(R.string.close_needs_shizuku) }
                CloseResult.FAILED -> view.post { warn(R.string.close_failed) }
                else -> Unit
            }
        }
    }

    // Samsung suprime los Toast de apps con notificaciones bloqueadas (medido en SM-X910), así que el aviso también va dentro de la barra.
    private fun warn(@StringRes message: Int) {
        if (destroyed || view.visibility != View.VISIBLE) return
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        warning = true
        notice.setText(message)
        buttons.visibility = View.GONE
        notice.visibility = View.VISIBLE
        view.removeCallbacks(hideNotice)
        view.postDelayed(hideNotice, NOTICE_MS)
    }

    private fun endNotice() {
        view.removeCallbacks(hideNotice)
        warning = false
        notice.visibility = View.GONE
        buttons.visibility = View.VISIBLE
    }

    fun update() {
        if (destroyed) return
        val am = availableManager()
        refreshFallbackPackage()
        if (!queued.compareAndSet(false, true)) return
        runInBackground {
            queued.set(false)
            val visible = resolveTarget(am) != null
            view.post {
                if (destroyed) return@post
                if (warning && !visible) endNotice()
                if (!warning) view.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }
    }

    fun destroy() {
        destroyed = true
        view.removeCallbacks(hideNotice)
        executor.shutdownNow()
        windowManager.removeViewImmediate(view)
    }

    private companion object {
        const val TAG = "WindowControlsBar"
        const val MAX_TASKS = 20
        const val NOTICE_MS = 3000L
    }
}
