package cu.axel.smartdock.wrappers

import android.app.ActivityManager
import android.app.IActivityManager
import android.graphics.Rect
import android.os.IBinder
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class ActivityManagerWrapper {

    private var binder: ShizukuBinderWrapper? = null
    private var activityManager: IActivityManager? = null

    private val deathRecipient = IBinder.DeathRecipient {
        synchronized(this) {
            activityManager = null
            binder = null
        }
    }

    init {
        init()
    }

    fun init() {
        synchronized(this) {
            if (activityManager != null)
                return
            binder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("activity"))
            binder?.linkToDeath(deathRecipient, 0)
            activityManager = IActivityManager.Stub.asInterface(binder)
        }
    }

    fun isAlive() = binder?.isBinderAlive == true && activityManager != null

    fun getRunningTasks(max: Int): List<ActivityManager.RunningTaskInfo> {
        val tasks = activityManager?.getTasks(max)
        return tasks ?: emptyList()
    }

    fun resizeTask(taskId: Int, bounds: Rect) {
        activityManager?.resizeTask(taskId, bounds, 0)
    }

    // Android 16 no trae setTaskWindowingMode en IActivityManager ni IActivityTaskManager (medido contra
    // AOSP android16-release); resizeTask solo actua sobre tareas freeform o multiventana y no cambia su modo.
    fun setTaskFullscreen(taskId: Int) {
        activityManager?.resizeTask(taskId, Rect(), 0)
    }

    fun removeTask(taskId: Int): Boolean {
        return activityManager?.removeTask(taskId) ?: false
    }
}