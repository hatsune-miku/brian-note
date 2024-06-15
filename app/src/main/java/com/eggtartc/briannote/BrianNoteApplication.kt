package com.eggtartc.briannote

import android.app.Application
import com.eggtartc.briannote.helper.ApplicationStateTracker
import com.google.android.material.color.DynamicColors
import java.util.function.Consumer

class BrianNoteApplication: Application(), ApplicationStateTracker.AppStateChangeListener {
    companion object {
        public const val STATE_FOREGROUND = 0
        public const val STATE_BACKGROUND = 1
    }

    private val subscriberIdToHandler = mutableMapOf<Int, Consumer<Int>>()
    private var subscriberIdCounter = 0

    override fun onCreate() {
        super.onCreate()
        applyDynamicColors()
        initializeSQLiteCipher()
        ApplicationStateTracker.track(this, this)
    }

    public fun subscribeToApplicationStateChange(handler: Consumer<Int>): Int {
        val id = subscriberIdCounter++
        subscriberIdToHandler[id] = handler
        return id
    }

    public fun unsubscribeFromApplicationStateChange(id: Int): Boolean {
        return subscriberIdToHandler.remove(id) != null
    }

    private fun applyDynamicColors() {
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initializeSQLiteCipher() {
        // Nothing to do currently
    }

    override fun appTurnIntoForeground() {
        subscriberIdToHandler.values.forEach {
            it.accept(STATE_FOREGROUND)
        }
    }

    override fun appTurnIntoBackGround() {
        subscriberIdToHandler.values.forEach {
            it.accept(STATE_BACKGROUND)
        }
    }
}
