package com.eggtartc.briannote.activity

import android.os.Bundle
import com.eggtartc.briannote.R
import com.eggtartc.briannote.databinding.ActivityBackupRestoreBinding
import com.eggtartc.briannote.util.MarkdownUtils
import com.google.android.material.elevation.SurfaceColors
import io.noties.markwon.Markwon

class BackupRestoreActivity: BaseActivity() {
    private lateinit var binding: ActivityBackupRestoreBinding
    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val migrationTutorialStream = resources.openRawResource(R.raw.learn_about_backup)
        val migrationTutorialRaw = String(migrationTutorialStream.readBytes(), Charsets.UTF_8)

        markwon = Markwon.builder(this)
            .usePlugin(MarkdownUtils.createImagesPlugin(this))
            .usePlugin(MarkdownUtils.createThemePlugin())
            .build()
        markwon.setMarkdown(binding.textViewLearnMoreAboutBackup, migrationTutorialRaw)

        binding.topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

    }
}
