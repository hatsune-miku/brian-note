package com.eggtartc.briannote.helper

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.eggtartc.briannote.activity.AuthenticationActivity
import com.eggtartc.briannote.activity.BaseActivity
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.enums.PasswordAlgorithm
import com.eggtartc.briannote.util.HashUtils
import java.util.function.Consumer

class AuthenticationHelper(private val activity: BaseActivity) {
    fun authenticateWithPassword(
        prompt: String,
        password: String,
        passwordAlgorithm: PasswordAlgorithm,
        shouldDisableBiometricAuthentication: Boolean,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(activity, AuthenticationActivity::class.java).apply {
            putPassword(this, password)
            putAuthenticationPrompt(this, prompt)
            putPasswordAlgorithm(this, passwordAlgorithm)
            putShouldDisableBiometricAuthentication(this, shouldDisableBiometricAuthentication)
        }
        activityResultLauncher.launch(intent)
    }

    fun tryAuthenticate(
        prompt: String,
        shouldDisableBiometricAuthentication: Boolean,
        callback: Consumer<Boolean>,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ) {
        if (!activity.noteHelper.isRealPasswordSet()) {
            callback.accept(true)
            return
        }

        val passwordSha256 = activity.noteHelper.readSecurePassword(false)
        authenticateWithPassword(prompt, passwordSha256, PasswordAlgorithm.SHA256,
            shouldDisableBiometricAuthentication, activityResultLauncher)
    }

    fun setupPassword(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(activity, AuthenticationActivity::class.java).apply {
            putSettingUpPassword(this, true)
        }
        activityResultLauncher.launch(intent)
    }

    private fun putPassword(intent: Intent, passwordSha256: String) {
        intent.putExtra("password_sha256", passwordSha256)
    }

    fun getPassword(intent: Intent): String? {
        return intent.getStringExtra("password_sha256")
    }

    private fun putSettingUpPassword(intent: Intent, settingUpPassword: Boolean) {
        intent.putExtra("setting_up_password", settingUpPassword)
    }

    fun getSettingUpPassword(intent: Intent): Boolean {
        return intent.getBooleanExtra("setting_up_password", false)
    }

    private fun putAuthenticationPrompt(intent: Intent, prompt: String) {
        intent.putExtra("authentication_prompt", prompt)
    }

    fun getAuthenticationPrompt(intent: Intent): String? {
        return intent.getStringExtra("authentication_prompt")
    }

    private fun putPasswordAlgorithm(intent: Intent, passwordAlgorithm: PasswordAlgorithm) {
        intent.putExtra("password_algorithm", passwordAlgorithm.rawValue)
    }

    fun getPasswordAlgorithm(intent: Intent): PasswordAlgorithm? {
        val rawValue = intent.getStringExtra("password_algorithm") ?: return null
        return PasswordAlgorithm.fromRawValue(rawValue)
    }

    private fun putShouldDisableBiometricAuthentication(intent: Intent, shouldDisableBiometricAuthentication: Boolean) {
        intent.putExtra("should_disable_biometric_authentication", shouldDisableBiometricAuthentication)
    }

    fun getShouldDisableBiometricAuthentication(intent: Intent): Boolean {
        return intent.getBooleanExtra("should_disable_biometric_authentication", false)
    }
}
