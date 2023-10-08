package com.eggtartc.briannote.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.eggtartc.briannote.R
import com.eggtartc.briannote.activity.BackupRestoreActivity
import com.eggtartc.briannote.activity.SettingsActivity
import com.eggtartc.briannote.annotation.PreferenceBind
import com.eggtartc.briannote.annotation.processor.PreferenceBinder
import com.eggtartc.briannote.constants.Keys
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class SettingsFragment(private val activity: SettingsActivity): PreferenceFragmentCompat() {
    @PreferenceBind("preference_switch_password_lock")
    private lateinit var preferenceSwitchPasswordLock: SwitchPreferenceCompat

    @PreferenceBind("preference_switch_allow_biometric_authentications")
    private lateinit var preferenceSwitchAllowBiometricAuthentications: SwitchPreferenceCompat

    @PreferenceBind("preference_switch_disable_screenshot")
    private lateinit var preferenceSwitchDisableScreenshot: SwitchPreferenceCompat

    @PreferenceBind("preference_backup")
    private lateinit var preferenceBackup: Preference

    @PreferenceBind("preference_backup_more")
    private lateinit var preferenceBackupMore: Preference

    @PreferenceBind("preference_licenses")
    private lateinit var preferenceLicenses: Preference

    @PreferenceBind("preference_version")
    private lateinit var preferenceVersion: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey)
        PreferenceBinder.bind(this)

        preferenceLicenses.setOnPreferenceClickListener {
            showLicenses()
            true
        }

        preferenceSwitchPasswordLock.isChecked =
            activity.preferencesHelper.read(Keys.PASSWORD_LOCK, false)
        preferenceSwitchPasswordLock.setOnPreferenceChangeListener { _, enabling ->
            onPasswordLockPreferenceChanged(enabling as Boolean)
        }

        preferenceSwitchAllowBiometricAuthentications.isChecked =
            activity.preferencesHelper.read(Keys.BIOMETRIC_AUTHENTICATION, true)
        preferenceSwitchAllowBiometricAuthentications.setOnPreferenceChangeListener { _, allowing ->
            onAllowBiometricAuthenticationsPreferenceChanged(allowing as Boolean)
        }

        preferenceSwitchDisableScreenshot.isChecked =
            activity.preferencesHelper.read(Keys.FLAG_SECURE, false)
        preferenceSwitchDisableScreenshot.setOnPreferenceChangeListener { _, disabling ->
            onDisableScreenshotPreferenceChanged(disabling as Boolean)
        }

        preferenceBackupMore.setOnPreferenceClickListener {
            onLearnMoreAboutBackupPreferenceClicked()
        }
    }

    private fun onLearnMoreAboutBackupPreferenceClicked(): Boolean {
        startActivity(Intent(activity, BackupRestoreActivity::class.java))
        return true
    }

    private fun onDisableScreenshotPreferenceChanged(disableScreenshot: Boolean): Boolean {
        activity.preferencesHelper.writeAsync(Keys.FLAG_SECURE, disableScreenshot)
        return true
    }

    private fun onAllowBiometricAuthenticationsPreferenceChanged(allowBiometricAuthentications: Boolean): Boolean {
        activity.preferencesHelper.writeAsync(Keys.BIOMETRIC_AUTHENTICATION, allowBiometricAuthentications)
        return true
    }

    private fun onPasswordLockPreferenceChanged(enabledPassword: Boolean): Boolean {
        if (enabledPassword) {
            activity.authenticationHelper.setupPassword(
                activity.applicationResultLauncher)
        } else {
            activity.authenticationHelper.tryAuthenticate(
                "关闭密码前需要验证密码",
                true,
                { onPasswordVerificationResultForDisablingPassword(it) },
                activity.applicationResultLauncher
            )
        }
        return false
    }

    private fun onPasswordVerificationResultForDisablingPassword(success: Boolean) {
        if (success) {
            activity.preferencesHelper.writeAsync(Keys.PASSWORD_LOCK, false)
            preferenceSwitchPasswordLock.isChecked = false
        }
    }

    public fun onActivityResult(success: Boolean, associatedOperation: String) {
        when (associatedOperation) {
            Keys.AO_PASSWORD_SETUP -> {
                activity.preferencesHelper.writeAsync(Keys.PASSWORD_LOCK, success)
                preferenceSwitchPasswordLock.isChecked = success
            }
            Keys.AO_PASSWORD_VERIFY -> {
                activity.preferencesHelper.writeAsync(Keys.PASSWORD_LOCK, !success)
                preferenceSwitchPasswordLock.isChecked = !success
            }
        }
    }

    private fun showLicenses() {
        val notices = Notices().apply {
            addNotice(Notice("Test1", "https://www.google.com", "Test1", ApacheSoftwareLicense20()))
            addNotice(Notice("Test1", "https://www.google.com", "Test1", ApacheSoftwareLicense20()))
            addNotice(Notice("Test1", "https://www.google.com", "Test1", ApacheSoftwareLicense20()))
        }
        LicensesDialog.Builder(activity)
            .setNotices(notices)
            .setIncludeOwnLicense(true)
            .build()
            .show()
    }
}
