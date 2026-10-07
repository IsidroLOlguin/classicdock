package cu.axel.smartdock.components

import android.app.ActivityManager
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.view.ContextThemeWrapper
import cu.axel.smartdock.R
import cu.axel.smartdock.models.WINDOWING_MODE_FREEFORM
import cu.axel.smartdock.utils.AppUtils
import cu.axel.smartdock.utils.Utils
import cu.axel.smartdock.wrappers.ActivityManagerWrapper

data class ForegroundTask(val id: Int, val packageName: String, val windowingMode: Int)

object WindowControlsPolicy {
    fun targetTask(top: ForegroundTask?, launcher: String, own: String): ForegroundTask? {
        if (top == null || top.windowingMode == WINDOWING_MODE_FREEFORM) return null
        if (top.packageName == launcher || top.packageName == own || top.packageName.startsWith("com.android.systemui"))
            return null
        return top
    }

    fun runOnTask(task: ForegroundTask?, action: (Int) -> Unit) {
        if (task != null && task.id > 0) action(task.id)
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
    private val getWindowingMode =
        ActivityManager.RunningTaskInfo::class.java.getMethod("getWindowingMode")
    private var target: ForegroundTask? = null

    init {
        view.findViewById<View>(R.id.window_minimize_btn).setOnClickListener { goHome() }
        view.findViewById<View>(R.id.window_close_btn).setOnClickListener {
            WindowControlsPolicy.runOnTask(target) { activityManager()?.removeTask(it) }
        }
        val params = Utils.makeWindowParams(-2, -2, context, secondaryDisplay, true)
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        windowManager.addView(view, params)
    }

    fun update() {
        target = try {
            val top = activityManager()?.getRunningTasks(5)
                ?.firstOrNull { it.isRunning && it.topActivity != null }
            WindowControlsPolicy.targetTask(
                top?.let {
                    ForegroundTask(it.id, it.topActivity!!.packageName, getWindowingMode.invoke(it) as Int)
                },
                AppUtils.getCurrentLauncher(context.packageManager),
                context.packageName
            )
        } catch (_: Exception) {
            null
        }
        view.visibility = if (target != null) View.VISIBLE else View.GONE
    }

    fun destroy() {
        windowManager.removeViewImmediate(view)
    }
}
