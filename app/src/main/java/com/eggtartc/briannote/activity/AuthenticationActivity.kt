package com.eggtartc.briannote.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.eggtartc.briannote.R
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.databinding.ActivityAuthenticationBinding
import com.eggtartc.briannote.enums.PasswordAlgorithm
import com.eggtartc.briannote.util.HashUtils
import com.google.android.material.elevation.SurfaceColors

class AuthenticationActivity : BaseActivity() {
    companion object {
        public val PASSWORD_LENGTH_MINIMUM = 4
        public val PASSWORD_LENGTH_MAXIMUM = 8
    }

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var correctPasswordSha256: String
    private lateinit var passwordAlgorithm: PasswordAlgorithm

    private var shouldDisableBiometricAuthentication: Boolean = false
    private var password = ""
    private var authenticated = false
    private var settingUpPassword = false
    private var didEnteredFirstPassword = false
    private var firstPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingUpPassword = authenticationHelper.getSettingUpPassword(intent)
        if (settingUpPassword) {
            setMessage("(1/2) 请设置密码")
        } else {
            authenticationHelper.getPassword(intent)?.let {
                correctPasswordSha256 = it
            } ?: run {
                finish()
                return
            }

            passwordAlgorithm = authenticationHelper.getPasswordAlgorithm(intent)
                ?: run {
                    finish()
                    return
                }

            authenticationHelper.getAuthenticationPrompt(intent)?.let {
                setMessage(it)
            } ?: run {
                setMessage("请输入密码")
            }

            shouldDisableBiometricAuthentication =
                authenticationHelper.getShouldDisableBiometricAuthentication(intent)
        }

        binding.apply {
            buttonPad0.setOnClickListener { onPasswordPadClicked("0") }
            buttonPad1.setOnClickListener { onPasswordPadClicked("1") }
            buttonPad2.setOnClickListener { onPasswordPadClicked("2") }
            buttonPad3.setOnClickListener { onPasswordPadClicked("3") }
            buttonPad4.setOnClickListener { onPasswordPadClicked("4") }
            buttonPad5.setOnClickListener { onPasswordPadClicked("5") }
            buttonPad6.setOnClickListener { onPasswordPadClicked("6") }
            buttonPad7.setOnClickListener { onPasswordPadClicked("7") }
            buttonPad8.setOnClickListener { onPasswordPadClicked("8") }
            buttonPad9.setOnClickListener { onPasswordPadClicked("9") }
            buttonPadBackspace.setOnClickListener { onBackspaceClicked() }
            buttonPadBackspace.setOnLongClickListener { onBackspaceLongPressed(); true }
            buttonPadEnter.setOnClickListener { onEnterClicked() }
            topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this@AuthenticationActivity))
            topAppBar.setNavigationOnClickListener { setResult(RESULT_CANCELED); finish() }
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            result.resultCode
        }
    }

    override fun onResume() {
        super.onResume()
        if (!settingUpPassword && canAndShouldUseBiometrics()) {
            disablePasswordAuthentication()
            authenticateWithBiometrics()
        }
        else {
            enablePasswordAuthentication()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!authenticated && !settingUpPassword) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!authenticated && !settingUpPassword) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun onPasswordPadClicked(digit: String) {
        password += digit
        updatePasswordMask()
    }

    private fun onBackspaceClicked() {
        if (password.isNotEmpty()) {
            password = password.substring(0, password.length - 1)
            updatePasswordMask()
        }
    }

    private fun onBackspaceLongPressed() {
        clearInputPassword()
    }

    private fun clearInputPassword() {
        password = ""
        updatePasswordMask()
    }

    private fun disablePasswordAuthentication() {
        binding.linearLayoutButtonPad.visibility = View.GONE
    }

    private fun enablePasswordAuthentication() {
        binding.linearLayoutButtonPad.visibility = View.VISIBLE
    }

    private fun authenticateWithBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                enablePasswordAuthentication()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authenticatedAndFinish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                enablePasswordAuthentication()
                setMessage("生物识别验证失败，请改用密码验证")
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("生物识别验证")
            .setSubtitle("需要进行生物识别验证以解锁 " + getString(R.string.app_name))
            .setNegativeButtonText("改用密码验证")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun enrollBiometrics() {
        activityResultLauncher.launch(Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            )
        })
    }

    /**
     * Check if biometrics can be used and should be used.
     * Will jump to biometrics enrollment if biometrics is not enrolled.
     * @return true if biometrics can be used and should be used
     */
    private fun canAndShouldUseBiometrics(): Boolean {
        // Disabled from settings?
        if (!preferencesHelper.read(Keys.BIOMETRIC_AUTHENTICATION, true)) {
            return false
        }

        // Disabled from intent?
        if (!shouldDisableBiometricAuthentication) {
            return false
        }

        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Jump to biometrics enrollment, if applicable
                enrollBiometrics()
                false
            }
            else -> false
        }
    }

    private fun stateFirstPassword() {
        firstPassword = password
        didEnteredFirstPassword = true
        setMessage("(2/2) 请再次输入密码")
        clearInputPassword()
    }

    private fun stateConfirmPassword() {
        if (firstPassword == password) {
            // Password confirmed
            noteHelper.writeSecurePassword(HashUtils.sha256(password), false)
            setMessage("密码设置成功")
            setResult(RESULT_OK, Intent().apply {
                putExtra(Keys.ASSOCIATED_OPERATION, Keys.AO_PASSWORD_SETUP)
            })
            finish()
        } else {
            // Password not confirmed
            setMessage("两次输入的密码不一致，请重新输入")
            didEnteredFirstPassword = false
            clearInputPassword()
        }
    }

    private fun stateSettingUpPassword() {
        if (didEnteredFirstPassword) {
            stateConfirmPassword()
        } else {
            stateFirstPassword()
        }
    }

    private fun authenticatedAndFinish() {
        setAuthenticated(true)
        setMessage("验证通过")
        setResult(RESULT_OK, Intent().apply {
            putExtra(Keys.ASSOCIATED_OPERATION, Keys.AO_PASSWORD_VERIFY)
        })
        finish()
    }

    private fun stateAuthenticating() {
        if (HashUtils.sha256(password) == correctPasswordSha256) {
            authenticatedAndFinish()
            return
        }
        password = ""
        updatePasswordMask()
        setMessage("验证不通过")
    }

    private fun onEnterClicked() {
        if (!isPasswordLengthValid()) {
            setMessage("密码长度必须在 $PASSWORD_LENGTH_MINIMUM 到 $PASSWORD_LENGTH_MAXIMUM （含）之间")
            return
        }
        if (settingUpPassword) {
            stateSettingUpPassword()
        } else {
            stateAuthenticating()
        }
    }

    private fun isPasswordLengthValid(): Boolean {
        return password.length in PASSWORD_LENGTH_MINIMUM..PASSWORD_LENGTH_MAXIMUM
    }

    private fun setMessage(message: String) {
        binding.textViewAuthenticationPrompt.text = message
    }

    private fun setAuthenticated(authenticated: Boolean) {
        this.authenticated = authenticated
    }

    private fun updatePasswordMask() {
        binding.textViewPasswordMask.text = "#".repeat(password.length)
    }
}
