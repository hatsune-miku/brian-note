package com.eggtartc.briannote.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.eggtartc.briannote.R
import com.eggtartc.briannote.databinding.ActivityRrnoteImportBinding
import com.eggtartc.briannote.enums.PasswordAlgorithm
import com.eggtartc.briannote.helper.RRNoteHelper
import com.eggtartc.briannote.util.MarkdownUtils
import com.eggtartc.briannote.util.RRNoteUtils
import com.google.android.material.elevation.SurfaceColors
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.SchemeHandler
import java.util.Collections
import java.util.concurrent.Executors

class MigrationUtilityActivity: BaseActivity() {
    private lateinit var binding: ActivityRrnoteImportBinding
    private lateinit var markwon: Markwon
    private lateinit var activityResultLauncherDatabaseUri: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherAuthentication: ActivityResultLauncher<Intent>
    private lateinit var rrNoteHelper: RRNoteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRrnoteImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val migrationTutorialStream = resources.openRawResource(R.raw.migration_tutorial)
        val migrationTutorialRaw = String(migrationTutorialStream.readBytes(), Charsets.UTF_8)

        markwon = Markwon.builder(this)
            .usePlugin(MarkdownUtils.createImagesPlugin(this))
            .usePlugin(MarkdownUtils.createThemePlugin())
            .build()
        markwon.setMarkdown(binding.textViewMigrationTutorial, migrationTutorialRaw)

        binding.buttonStartMigration.setOnClickListener {
            startMigration()
        }

        binding.topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        activityResultLauncherDatabaseUri = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { handleRRNoteDatabaseUri(it) }
            }
        }

        activityResultLauncherAuthentication = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                activityHelper.showAlertDialog("需要正确的密码方可解密这份 RRNote 备份", "密码错误")
                return@registerForActivityResult
            }
            onAuthenticated()
        }
    }

    private fun onAuthenticated() {
        Executors.newSingleThreadExecutor().submit {
            try {
                val notes = rrNoteHelper.readNotes()
                val ids = mutableListOf<Long>()
                ids.addAll(noteHelper.importNotes(notes))
                runOnUiThread {
                    activityHelper.showAlertDialog("成功导入了所有 ${ids.size} 条记事！", "导入完成")
                }
            }
            catch (e: Exception) {
                runOnUiThread {
                    activityHelper.showAlertDialog(
                        "截图如下失败原因可帮助排查问题。\n\n${e.message}", "导入失败")
                }
            }
            finally {
                rrNoteHelper.close()
            }
        }
    }

    private fun handleRRNoteDatabaseUri(uri: Uri) {
        if (!(uri.path?.endsWith(".db") ?: run {
            activityHelper.showAlertDialog("发生了没有预料到的错误，截图如下信息可帮助排查问题。\n\nURI路径为空", "未知错误")
            return
        })) {
            activityHelper.showAlertDialog("所选的文件不是数据库文件。", "文件类型不匹配")
            return
        }

        rrNoteHelper = try {
            RRNoteHelper(this, uri)
        } catch (e: Exception) {
            activityHelper.showAlertDialog(
                "截图如下失败原因可帮助排查问题。\n\n${e.message}", "无法打开 RRNote 备份数据库")
            return
        }

        if (!rrNoteHelper.isPasswordSet()) {
            onAuthenticated()
            return
        }

        val passwordMd5 = rrNoteHelper.readSecurePassword()
        authenticationHelper.authenticateWithPassword(
            prompt = "输入原先 RRNote 的密码",
            password = passwordMd5,
            passwordAlgorithm = PasswordAlgorithm.MD5,
            shouldDisableBiometricAuthentication = true,
            activityResultLauncher = activityResultLauncherAuthentication
        )
    }

    private fun startMigration() {
        val intent = RRNoteUtils.createBackupDatabaseReadIntent()
        activityResultLauncherDatabaseUri.launch(intent)
    }
}
