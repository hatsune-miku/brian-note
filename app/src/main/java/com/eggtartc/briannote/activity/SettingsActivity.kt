package com.eggtartc.briannote.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.eggtartc.briannote.R
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.databinding.ActivitySettingsBinding
import com.eggtartc.briannote.enums.PasswordAlgorithm
import com.eggtartc.briannote.fragment.SettingsFragment
import com.eggtartc.briannote.helper.ExternalDatabaseHelper
import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.helper.RRNoteHelper
import com.eggtartc.briannote.util.RRNoteUtils
import com.google.android.material.elevation.SurfaceColors
import java.util.concurrent.Executors

class SettingsActivity: BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var activityResultLauncherDatabaseUri: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherAuthentication: ActivityResultLauncher<Intent>
    private lateinit var externalNoteHelper: NoteHelper
    lateinit var applicationResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        val fragment = SettingsFragment(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()

        applicationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            fragment.onActivityResult(
                result.resultCode == Activity.RESULT_OK,
                result.data?.getStringExtra(Keys.ASSOCIATED_OPERATION) ?: ""
            )
        }

        activityResultLauncherDatabaseUri = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { handleBrianNoteDatabaseUri(it) }
            }
        }

        activityResultLauncherAuthentication = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onAuthenticated()
            }
        }
    }

    fun startRecovery() {
        val intent = RRNoteUtils.createBackupDatabaseReadIntent()
        activityResultLauncherDatabaseUri.launch(intent)
    }

    private fun onAuthenticated() {
        Executors.newSingleThreadExecutor().submit {
            var count = 0
            try {
                val notes = externalNoteHelper.readNotes()
                for (note in notes) {
                    try {
                        noteHelper.addNote(note)
                    } catch (_: Exception) {}
                    ++count
                }
                runOnUiThread {
                    activityHelper.showAlertDialog(
                        "成功恢复了 $count 条记事！", "恢复完成")
                }
            }
            catch (e: Exception) {
                runOnUiThread {
                    activityHelper.showAlertDialog(
                        "截图如下失败原因可帮助排查问题。\n\n${e.message}", "恢复失败")
                }
            }
        }
    }

    private fun handleBrianNoteDatabaseUri(uri: Uri) {
        if (!(uri.path?.endsWith(".db") ?: run {
                activityHelper.showAlertDialog("发生了没有预料到的错误，截图如下信息可帮助排查问题。\n\nURI路径为空", "未知错误")
                return
            })) {
            activityHelper.showAlertDialog("所选的文件不是数据库文件。", "文件类型不匹配")
            return
        }

        val databaseHelper = try {
            ExternalDatabaseHelper(this, uri)
        } catch (e: Exception) {
            activityHelper.showAlertDialog(
                "截图如下失败原因可帮助排查问题。\n\n${e.message}", "无法打开 BrianNote 备份数据库")
            return
        }
        externalNoteHelper = NoteHelper(databaseHelper)
        if (!externalNoteHelper.isRealPasswordSet()) {
            onAuthenticated()
            return
        }

        val passwordSha256 = externalNoteHelper.readSecurePassword(false)
        authenticationHelper.authenticateWithPassword(
            prompt = "输入原先 BrianNote 的密码",
            password = passwordSha256,
            passwordAlgorithm = PasswordAlgorithm.SHA256,
            shouldDisableBiometricAuthentication = true,
            activityResultLauncher = activityResultLauncherAuthentication
        )
    }
}
