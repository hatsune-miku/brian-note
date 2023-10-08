package com.eggtartc.briannote

import android.app.Application
import com.google.android.material.color.DynamicColors

class BrianNoteApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        applyDynamicColors()
        initializeSQLiteCipher()
    }

    private fun applyDynamicColors() {
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initializeSQLiteCipher() {
        // Nothing to do currently
    }
}
