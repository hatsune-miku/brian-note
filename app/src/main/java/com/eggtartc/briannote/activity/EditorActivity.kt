package com.eggtartc.briannote.activity

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updateLayoutParams
import com.eggtartc.briannote.R
import com.eggtartc.briannote.databinding.ActivityEditorBinding
import com.eggtartc.briannote.extension.ImageButton
import com.eggtartc.briannote.extension.format
import com.eggtartc.briannote.extension.getRawHtmlContent
import com.eggtartc.briannote.extension.renderHooked
import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.model.Note
import com.eggtartc.briannote.util.EditorUtils
import com.eggtartc.briannote.util.FileUtils
import com.eggtartc.briannote.util.PermissionUtils
import com.github.mr5.icarus.Icarus
import com.github.mr5.icarus.TextViewToolbar
import com.github.mr5.icarus.button.Button
import com.github.mr5.icarus.button.FontScaleButton
import com.github.mr5.icarus.button.TextViewButton
import com.github.mr5.icarus.entity.Options
import com.github.mr5.icarus.popover.FontScalePopoverImpl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Date


class EditorActivity : BaseActivity(), ImageButton.IImagePicker, Toolbar.OnMenuItemClickListener {
    companion object {
        private const val TAG = "EditorActivity"
    }

    private lateinit var binding: ActivityEditorBinding
    private lateinit var icarusToolbar: TextViewToolbar
    private lateinit var icarusOptions: Options
    private lateinit var icarus: Icarus
    private lateinit var note: Note
    private lateinit var activityResultLauncherImagePicking: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteId = getNoteId(intent)
        note = noteHelper.readNote(noteId, true)

        binding.topAppBar.apply {
            setOnClickListener { onChangeTitle() }
            setNavigationOnClickListener { saveAndFinish() }
            setTitle(note.title)
            setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this@EditorActivity))
            setOnMenuItemClickListener(this@EditorActivity)
            setDestructiveTitlePredicate { it == "删除" }
            reInflateMenu()
        }

        binding.horizontalScrollView.setBackgroundColor(
            SurfaceColors.SURFACE_2.getColor(this@EditorActivity))
        window.navigationBarColor =
            SurfaceColors.SURFACE_2.getColor(this@EditorActivity)
        binding.root.setBackgroundColor(
            SurfaceColors.SURFACE_2.getColor(this@EditorActivity))

        activityResultLauncherImagePicking = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    onImagePicked(uri)
                }
            }
        }

        prepareIcarus()
        activityHelper.makeViewRaiseAlongWithKeyboard(binding.root)
    }

    override fun onImagePickStart() {
        activityResultLauncherImagePicking.launch(
            PermissionUtils.createImagePickingIntent()
        )
    }

    private fun onImagePicked(uri: Uri) {
        // insert base64
        val base64 = FileUtils.encodeUriFileToBase64Binary(this, uri)
        icarus.insertHtml("<img src=\"data:image/png;base64,$base64\" />")
    }

    private fun save(completion: (() -> Unit)? = null) {
        icarus.getRawHtmlContent { content ->
            noteHelper.updateNote(
                NoteHelper.UpdateBuilder(note.id)
                    .title(note.title)
                    .htmlContent(content)
                    .build()
            )
            completion?.let { it() }
        }
    }

    private fun saveAndFinish() {
        save { finish() }
    }

    private fun getNoteId(intent: Intent): Long {
        return intent.getLongExtra("note_id", -1).let {
            if (it == -1L) {
                throw Exception("Note ID is not provided.")
            }
            it
        }
    }

    private fun prepareIcarus() {
        icarusToolbar = TextViewToolbar()
        icarusOptions = Options().apply {
            placeholder = "今天是 " + Date().format("yyyy/MM/dd HH:mm:ss")
            addAllowedAttributes(
                "img",
                listOf(
                    "data-type",
                    "data-id",
                    "class",
                    "src",
                    "alt",
                    "width",
                    "height",
                    "data-non-image"
                )
            )
            addAllowedAttributes(
                "iframe",
                listOf("data-type", "data-id", "class", "src", "width", "height")
            )
            addAllowedAttributes(
                "a",
                listOf("data-type", "data-id", "class", "href", "target", Button.NAME_TITLE)
            )
        }

        icarus = Icarus(icarusToolbar, icarusOptions, binding.webViewEditor).apply {
            setContent(note.htmlContent)
        }
        icarusToolbar.apply {
            val simditorFont = Typeface.createFromAsset(assets, "simditor.ttf")
            val buttonNameViewMap = hashMapOf(
                Button.NAME_BOLD to binding.buttonBold,
                Button.NAME_OL to binding.buttonListOl,
                Button.NAME_BLOCKQUOTE to binding.buttonBlockquote,
                Button.NAME_HR to binding.buttonHr,

                // 全选 底色 字号 行间距
                Button.NAME_SELECT_ALL to binding.buttonSelectAll,
                Button.NAME_MARK to binding.buttonBackgroundColor,
                Button.NAME_FONT_SCALE to binding.buttonFontScale,

                Button.NAME_UL to binding.buttonListUl,
                Button.NAME_ALIGN_LEFT to binding.buttonAlignLeft,
                Button.NAME_ALIGN_CENTER to binding.buttonAlignCenter,
                Button.NAME_ALIGN_RIGHT to binding.buttonAlignRight,

                Button.NAME_ITALIC to binding.buttonItalic,
                Button.NAME_INDENT to binding.buttonIndent,
                Button.NAME_OUTDENT to binding.buttonOutdent,
                Button.NAME_UNDERLINE to binding.buttonUnderline,
                Button.NAME_STRIKETHROUGH to binding.buttonStrikeThrough,
            )
            for ((name, textView) in buttonNameViewMap.entries) {
                textView.setTypeface(simditorFont)
                addButton(TextViewButton(textView, icarus).apply {
                    this.name = name
                })
            }

            // Prepare font scale button.
            binding.buttonFontScale.setTypeface(simditorFont)
            addButton(FontScaleButton(binding.buttonFontScale, icarus).apply {
                name = Button.NAME_FONT_SCALE
                popover = FontScalePopoverImpl(binding.buttonFontScale, icarus)
            })

            // Prepare image button.
            binding.buttonImage.setTypeface(simditorFont)
            addButton(ImageButton(this@EditorActivity, binding.buttonImage, icarus).apply {
                name = Button.NAME_IMAGE
            })
        }

        // Custom CSS.
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                icarus.loadCSS("file:///android_asset/editor.css")
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                icarus.loadCSS("file:///android_asset/editor_dark.css")
            }
        }

        // Render.
        icarus.render()
    }

    private fun onChangeTitle() {
        EditorUtils.promptForChangeNoteTitle(activityHelper, noteHelper, note) {
            binding.topAppBar.title = it.title
        }
    }

    private fun onDeleteNote() {
        EditorUtils.promptForDeleteNote(activityHelper, noteHelper, note) {
            finish()
        }
    }

    private fun onExitWithoutSaving() {
        activityHelper.showAlertDialog(
            "确定要放弃更改，直接退出吗？", "不保存退出",
            "放弃", { finish() }, "取消"
        )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemExit -> onExitWithoutSaving()
            R.id.menuItemSaveAndExit -> saveAndFinish()
            R.id.menuItemSave -> save()
            R.id.menuItemDelete -> onDeleteNote()
        }
        return true
    }
}
