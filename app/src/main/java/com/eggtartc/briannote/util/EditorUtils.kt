package com.eggtartc.briannote.util

import com.eggtartc.briannote.helper.ActivityHelper
import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.model.Note
import java.util.function.Consumer

object EditorUtils {
    fun promptForChangeNoteTitle(
        activityHelper: ActivityHelper,
        noteHelper: NoteHelper,
        note: Note,
        onChanged: Consumer<Note>?
    ) {
        activityHelper.promptForString(
            "修改标题", note.title, "标题",
            "修改", {
                val title = it.text.toString()
                note.title = title
                onChanged?.accept(note)
                noteHelper.updateNote(
                    NoteHelper.UpdateBuilder(note.id)
                        .title(note.title)
                        .build()
                )
            },
            "取消"
        )
    }

    fun promptForDeleteNote(
        activityHelper: ActivityHelper,
        noteHelper: NoteHelper,
        note: Note,
        onDeleted: Runnable?
    ) {
        activityHelper.showAlertDialog(
            "确定要删除这篇记事吗？", "删除记事",
            "删除", {
                noteHelper.deleteNote(note.id)
                onDeleted?.run()
            }, "取消"
        )
    }
}
