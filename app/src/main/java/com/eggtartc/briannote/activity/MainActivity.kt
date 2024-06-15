package com.eggtartc.briannote.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.eggtartc.briannote.BrianNoteApplication
import com.eggtartc.briannote.R
import com.eggtartc.briannote.adapter.NoteItemAdapter
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.databinding.ActivityMainBinding
import com.eggtartc.briannote.extension.getDayOfMonth
import com.eggtartc.briannote.extension.getHourOfDay
import com.eggtartc.briannote.extension.getMonth2
import com.eggtartc.briannote.extension.getWeekOfMonth
import com.eggtartc.briannote.extension.getYear2
import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.interfaces.INoteItem
import com.eggtartc.briannote.menu.IconPopupMenu
import com.eggtartc.briannote.model.Note
import com.eggtartc.briannote.model.NoteHeader
import com.eggtartc.briannote.util.EditorUtils
import com.eggtartc.briannote.util.HashUtils
import com.google.android.material.elevation.SurfaceColors
import java.util.Date
import java.util.Stack
import java.util.function.Consumer
import kotlin.system.exitProcess


class MainActivity: BaseActivity(), NoteItemAdapter.OnNoteItemClickListener, Toolbar.OnMenuItemClickListener {
    companion object {
        private const val TAG = "MainActivity"
        private const val STATE_KEY_REQUIRE_AUTH = "shouldRequireAuthenticationOnNextStart"
    }

    private var shouldRequireAuthenticationOnNextStart = true
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteItemAdapter: NoteItemAdapter<MainActivity>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var applicationInstance: BrianNoteApplication
    private val noteItems = mutableListOf<INoteItem>()
    private var applicationStateChangeSubscriptionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            shouldRequireAuthenticationOnNextStart = it.getBoolean(STATE_KEY_REQUIRE_AUTH, true)
        }
        applicationInstance = application as BrianNoteApplication

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tryInitializeForFirstRun()

        noteItemAdapter = NoteItemAdapter(this, noteItems)

        binding.apply {
            recyclerView.adapter = noteItemAdapter
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

            // Migration button
            viewNoNote.findViewById<Button>(R.id.buttonImportFromRRNote).setOnClickListener {
                startMigrationTutorial()
            }

            // New note button
            viewNoNote.findViewById<Button>(R.id.buttonCreateNote).setOnClickListener {
                createNewNoteAndStartEditor()
            }

            // fab
            fab.setOnClickListener {
                createNewNoteAndStartEditor()
            }

            topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this@MainActivity))
            topAppBar.setOnMenuItemClickListener(this@MainActivity)
        }

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                Log.d(TAG, "onCreate: Authentication failed.")
                exitProcess(0)
            }
            Log.d(TAG, "onCreate: Authenticate succeeded.")
            shouldRequireAuthenticationOnNextStart = false
        }

        applicationStateChangeSubscriptionId = applicationInstance.subscribeToApplicationStateChange {
            when (it) {
                BrianNoteApplication.STATE_BACKGROUND -> onBackground()
                BrianNoteApplication.STATE_FOREGROUND -> onForeground()
            }
        }

        reloadNotes()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_KEY_REQUIRE_AUTH, shouldRequireAuthenticationOnNextStart)
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationStateChangeSubscriptionId?.let {
            if (!applicationInstance.unsubscribeFromApplicationStateChange(it)) {
                Log.e(TAG, "onDestroy: Failed to unsubscribe from application state change.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reloadNotes()
    }

    private fun onBackground() {
        Log.d(TAG, "onBackground")
        shouldRequireAuthenticationOnNextStart = true
    }

    private fun onForeground() {
        Log.d(TAG, "onForeground")
        if (!shouldRequireAuthenticationOnNextStart) {
            return
        }

        val allowBiometrics = preferencesHelper.read(Keys.BIOMETRIC_AUTHENTICATION, true)
        authenticationHelper.tryAuthenticate(
            "请输入密码",
            !allowBiometrics,
            {},
            activityResultLauncher
        )
    }

    private fun tryInitializeForFirstRun() {
        if (!preferencesHelper.read(Keys.FIRST_RUN, true)) {
            return
        }
        noteHelper.initializeForFirstRun()
        preferencesHelper.writeAsync(Keys.FIRST_RUN, false)
    }

    private fun notifyNoteRemoved(note: Note) {
        val index = getNoteIndex(note)
        if (index == -1) {
            return
        }
        noteItems.removeAt(index)
        noteItemAdapter.notifyItemRemoved(index)
        checkEmptyNotes()
    }

    private fun notifyNoteTitleChanged(note: Note) {
        val index = getNoteIndex(note)
        if (index == -1) {
            return
        }
        noteItems[index] = note
        noteItemAdapter.notifyItemChanged(index)
        checkEmptyNotes()
    }

    /**
     * @return The index of the note in the list, or -1 if not found.
     */
    private fun getNoteIndex(note: Note): Int {
        return noteItems.indexOfFirst { it is Note && it.id == note.id }
    }

    private fun checkEmptyNotes() {
        if (noteItems.none { !it.isHeader() }) {
            reloadNotes()
        }
    }

    private fun reloadNotes() {
        // Remove and notify old notes
        val notesDeletedCount = noteItems.size
        noteItems.clear()
        noteItemAdapter.notifyItemRangeRemoved(0, notesDeletedCount)

        // Sort notes by date descending
        val notes = noteHelper.readNotes()
        val topNotes = notes
            .filter { it.pinned }
            .sortedBy { it.updatedAt.time }
            .reversed()
        val normalNotes = notes
            .filter { !it.pinned }
            .sortedBy { it.updatedAt.time }
            .reversed()

        // Group notes by just now, today, yesterday, last week, last month, and older
        val groupedNotes = groupNotes(normalNotes)

        // Reverse notes by group so earlier groups are at the top
        val groupedNotesReversed = reverseNotesByGroup(groupedNotes)

        // Insert notes
        if (topNotes.isNotEmpty()) {
            noteItems.add(NoteHeader("置顶"))
            noteItems.addAll(topNotes)
        }
        noteItems.addAll(groupedNotesReversed)

        // Notify new notes
        noteItemAdapter.notifyItemRangeInserted(0, noteItems.size)

        // No notes?
        binding.viewNoNote.visibility = if (noteItems.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    data class DateComparisonResult(
        val yearsAway: Int,
        val monthsAway: Int,
        val weeksAway: Int,
        val daysAway: Int,
        val hoursAway: Int,
        val now: Date
    )

    private fun reverseNotesByGroup(notes: List<INoteItem>): List<INoteItem> {
        val stack = Stack<INoteItem>()
        val ret = mutableListOf<INoteItem>()
        for (item in notes.reversed()) {
            if (item.isHeader()) {
                ret.add(item)
                while (stack.isNotEmpty()) {
                    ret.add(stack.pop())
                }
            }
            else {
                stack.push(item)
            }
        }
        return ret
    }

    private fun groupNotes(notes: List<Note>): List<INoteItem> {
        val dateAndPredicate = mutableListOf<Pair<String, (DateComparisonResult) -> Boolean>>(
            "多年前" to { it.yearsAway >= 2 },
            "去年" to { it.yearsAway == 1 },
            "数月前" to { it.monthsAway >= 2 },
            "上个月" to { it.monthsAway == 1 },
            "两周前" to { it.weeksAway >= 2 },
            "上周" to { it.weeksAway == 1 },
            "七天前" to { it.daysAway in 7..8 },
            "六天前" to { it.daysAway in 6..7 },
            "五天前" to { it.daysAway in 5..6 },
            "四天前" to { it.daysAway in 4..5 },
            "三天前" to { it.daysAway in 3..4 },
            "前天" to { it.daysAway == 2 },
            "昨天" to { it.daysAway == 1 },
            "六小时前" to { it.hoursAway in 6..7 },
            "五小时前" to { it.hoursAway in 5..6 },
            "四小时前" to { it.hoursAway in 4..5 },
            "三小时前" to { it.hoursAway in 3..4 },
            "两小时前" to { it.hoursAway in 2..3 },
            "一小时前" to { it.hoursAway in 1..2 },
            "刚刚" to { it.hoursAway in 0..1 },
        )
        val ret = mutableListOf<INoteItem>()

        for ((title, predicate) in dateAndPredicate) {
            var didAddHeader = false
            for (note in notes) {
                val now = Date()
                val yearsAway = now.getYear2() - note.updatedAt.getYear2()
                val monthsAway = now.getMonth2() - note.updatedAt.getMonth2()
                val weeksAway = now.getWeekOfMonth() - note.updatedAt.getWeekOfMonth()
                val daysAway = now.getDayOfMonth() - note.updatedAt.getDayOfMonth()
                val hoursAway = now.getHourOfDay() - note.updatedAt.getHourOfDay()

                val comparisonResult = DateComparisonResult(
                    yearsAway = yearsAway,
                    monthsAway = monthsAway,
                    weeksAway = weeksAway,
                    daysAway = daysAway,
                    hoursAway = hoursAway,
                    now = now
                )
                if (predicate(comparisonResult)
                    && ret.none { !it.isHeader() && (it as Note) == note }) {
                    if (!didAddHeader) {
                        ret.add(NoteHeader(title))
                        didAddHeader = true
                    }
                    ret.add(note)
                }
            }
        }
        return ret
    }

    override fun onNoteItemClicked(note: Note, view: View) {
        startEditingNode(note.id)
    }

    override fun onNoteItemLongClicked(note: Note, view: View): Boolean {
        showPopupMenu(R.menu.menu_context_note, view, note) {
            when (it.itemId) {
                R.id.contextMenuItemPinUnpin -> onNotePinUnpinClicked(note)
                R.id.contextMenuItemChangeTitle -> onNoteChangeTitleClicked(note)
                R.id.contextMenuItemDelete -> onNoteDeleteClicked(note)
            }
            true
        }
        return true
    }

    private fun onNotePinUnpinClicked(note: Note) {
        noteHelper.updateNote(
            NoteHelper.UpdateBuilder(note.id)
                .pinned(!note.pinned)
                .build()
        )
        reloadNotes()
    }

    private fun onNoteChangeTitleClicked(note: Note) {
        EditorUtils.promptForChangeNoteTitle(activityHelper, noteHelper, note) {
            notifyNoteTitleChanged(note)
        }
    }

    private fun onNoteDeleteClicked(note: Note) {
        EditorUtils.promptForDeleteNote(activityHelper, noteHelper, note) {
            notifyNoteRemoved(note)
        }
    }

    private fun startEditingNode(id: Long) {
        startActivity(Intent(this, EditorActivity::class.java).apply {
            putExtra("note_id", id)
        })
    }

    private fun startMigrationTutorial() {
        startActivity(Intent(this, MigrationUtilityActivity::class.java))
    }

    private fun createNewNoteAndStartEditor() {
        val id = noteHelper.createNote()
        startEditingNode(id)
    }

    private fun showPopupMenu(@MenuRes menuRes: Int, onView: View, note: Note, onClickListener: MenuItem.OnMenuItemClickListener) {
        val popupMenu = IconPopupMenu(this, onView, menuRes).apply {
            setOnMenuItemClickListener(onClickListener)
            setDestructiveTitlePredicate { arrayOf("删除").contains(it) }
            setMenuItemPostProcessor { item ->
                if (item.title == "置顶" && note.pinned) {
                    item.title = "取消置顶"
                }
            }
        }
        popupMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemImportFromRRNote -> startMigrationTutorial()
            R.id.menuItemSettings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
        return true
    }
}
