package com.eggtartc.briannote.activity

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.ViewDataBinding
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.helper.ActivityHelper
import com.eggtartc.briannote.helper.AuthenticationHelper
import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.helper.PreferencesHelper
import com.google.android.material.elevation.SurfaceColors

open class BaseActivity: AppCompatActivity() {
    lateinit var preferencesHelper: PreferencesHelper
    lateinit var activityHelper: ActivityHelper
    lateinit var noteHelper: NoteHelper
    lateinit var authenticationHelper: AuthenticationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesHelper = PreferencesHelper(this)
        activityHelper = ActivityHelper(this)
        noteHelper = NoteHelper(this)
        authenticationHelper = AuthenticationHelper(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        activityHelper.setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this))
    }

    override fun onResume() {
        super.onResume()
        tryApplyFlagSecure()
    }

    private fun tryApplyFlagSecure() {
        when (preferencesHelper.read(Keys.FLAG_SECURE, false)) {
            true -> activityHelper.setFlagSecure()
            false -> activityHelper.clearFlagSecure()
        }
    }
}
