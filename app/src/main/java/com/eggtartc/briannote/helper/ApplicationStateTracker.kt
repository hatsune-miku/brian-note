package com.eggtartc.briannote.helper

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

object ApplicationStateTracker {
    const val STATE_FOREGROUND: Int = 0
    const val STATE_BACKGROUND: Int = 1

    var currentState: Int = 0
        private set

    fun track(application: Application, appStateChangeListener: AppStateChangeListener) {
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            private var resumeActivityCount = 0

            override fun onActivityStarted(activity: Activity) {
                if (resumeActivityCount == 0) {
                    currentState = STATE_FOREGROUND
                    appStateChangeListener.appTurnIntoForeground()
                }

                resumeActivityCount++
            }


            override fun onActivityStopped(activity: Activity) {
                resumeActivityCount--

                if (resumeActivityCount == 0) {
                    currentState = STATE_BACKGROUND
                    appStateChangeListener.appTurnIntoBackGround()
                }
            }
        })
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackGround()
    }

    private open class SimpleActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }
}
