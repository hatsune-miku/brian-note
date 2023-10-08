package com.eggtartc.briannote.model

import com.eggtartc.briannote.helper.NoteHelper
import com.eggtartc.briannote.interfaces.INoteItem
import com.eggtartc.briannote.util.RRNoteUtils
import net.sqlcipher.Cursor
import java.sql.Timestamp
import java.util.Date

data class Note(
    val id: Long,
    var title: String,
    var htmlContent: String,
    var pinned: Boolean,
    val createdAt: Date,
    var updatedAt: Date
): INoteItem {
    var didLoadFullContent: Boolean = false

    companion object {
        fun sample(): Note {
            return Note(
                id = 0,
                title = "Sample Note",
                htmlContent = "This is a sample note.",
                pinned = false,
                createdAt = Date(),
                updatedAt = Date(),
            )
        }

        fun fromCursor(cursor: Cursor): Note {
            return Note(
                id = cursor.getLong(0),
                title = cursor.getString(1),
                htmlContent = cursor.getString(2),
                pinned = cursor.getInt(3) == 1,
                createdAt = Timestamp(cursor.getLong(4) * 1000),
                updatedAt = Timestamp(cursor.getLong(5) * 1000),
            )
        }

        fun fromRRNoteCursor(cursor: Cursor): Note {
            val id = cursor.getLong(0)
            val timestamp = RRNoteUtils.timestampFromRRNoteStyleDate(cursor.getString(1))
            val content = cursor.getString(2)

            return Note(
                id = id,
                title = "无标题",
                htmlContent = content,
                pinned = false,
                createdAt = timestamp,
                updatedAt = timestamp,
            )
        }
    }

    override fun isHeader(): Boolean {
        return false
    }

    /**
     * @return true if the full content was loaded, false otherwise
     */
    fun loadFullContent(helper: NoteHelper): Boolean {
        if (didLoadFullContent) {
            return true
        }
        htmlContent = helper.readNoteFullContent(id)
            ?: return false
        didLoadFullContent = true
        return true
    }
}
