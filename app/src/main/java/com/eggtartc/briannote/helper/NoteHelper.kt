package com.eggtartc.briannote.helper

import android.content.ContentValues
import android.content.Context
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.enums.Role
import com.eggtartc.briannote.interfaces.IDatabaseHelper
import com.eggtartc.briannote.model.Note
import java.time.Instant

class NoteHelper {
    private val databaseHelper: IDatabaseHelper

    constructor(context: Context) {
        this.databaseHelper = DatabaseHelper(context)
    }

    constructor(databaseHelper: IDatabaseHelper) {
        this.databaseHelper = databaseHelper
    }

    fun initializeForFirstRun() {
        databaseHelper.initializeForFirstRun()
    }

    fun isRealPasswordSet(): Boolean {
        return readSecurePassword(false).isNotEmpty()
    }

    fun isFakePasswordSet(): Boolean {
        return readSecurePassword(true).isNotEmpty()
    }

    fun authenticateWithPassword(password: String): Role {
        val realPassword = readSecurePassword(false)
        val fakePassword = readSecurePassword(true)

        if (realPassword.isEmpty() || password == realPassword) {
            return Role.USER
        }

        if (fakePassword.isNotEmpty() && password == fakePassword) {
            return Role.GUEST
        }

        return Role.UNAUTHORIZED
    }

    fun readSecurePassword(fake: Boolean): String {
        databaseHelper.getReadableDatabase().apply {
            val cursor = rawQuery(
                "SELECT password FROM secure WHERE is_fake = ?",
                arrayOf(if (fake) 1 else 0)).apply {
                moveToFirst()
            }
            val securePassword = cursor.getString(0)
            cursor.close()
            return securePassword
        }
    }

    @Throws
    fun writeSecurePassword(passwordSha256: String, fake: Boolean) {
        databaseHelper.getWritableDatabase().apply {
            beginTransaction()
            try {
                execSQL("UPDATE secure SET password = ? WHERE is_fake = ?",
                    arrayOf(passwordSha256, if (fake) 1 else 0))
                setTransactionSuccessful()
            }
            finally {
                endTransaction()
            }
        }
    }

    @Throws
    fun readNotes(): List<Note> {
        databaseHelper.getReadableDatabase().apply {
            val notes = mutableListOf<Note>()
            rawQuery(
                "SELECT id, title, substr(html_content, 0, 100), is_pinned, created_at, updated_at FROM notes" +
                        " ORDER BY updated_at DESC", null).use { cursor ->
                while (cursor.moveToNext()) {
                    notes.add(Note.fromCursor(cursor))
                }
            }
            return notes
        }
    }

    fun readNote(id: Long, shouldLoadFullContent: Boolean): Note {
        databaseHelper.getReadableDatabase().apply {
            val queryPartHtmlContent = if (shouldLoadFullContent) {
                "html_content"
            }
            else {
                "substr(html_content, 0, 100) as html_content"
            }
            val cursor = rawQuery(
                "SELECT id, title, $queryPartHtmlContent, is_pinned, created_at, updated_at FROM notes" +
                        " WHERE id = ?", arrayOf(id)).apply {
                            moveToFirst()
            }
            val note = Note.fromCursor(cursor)
            cursor.close()
            return note
        }
    }

    fun readNoteFullContent(id: Long): String? {
        databaseHelper.getReadableDatabase().apply {
            val cursor = rawQuery(
                "SELECT html_content FROM notes WHERE id = ?",
                arrayOf(id)).apply {
                    moveToFirst()
            }
            val fullContent = cursor.getString(0)
            cursor.close()
            return fullContent
        }
    }

    @Throws
    fun updateNote(updateQuery: UpdateQuery) {
        databaseHelper.getWritableDatabase().apply {
            beginTransaction()
            try {
                execSQL(updateQuery.sql, updateQuery.arguments)
                setTransactionSuccessful()
            }
            finally {
                endTransaction()
            }
        }
    }

    /**
     * @return The ID of the newly created note
     */
    @Throws
    fun createNote(): Long {
        databaseHelper.getWritableDatabase().apply {
            beginTransaction()
            try {
                val now = Instant.now()
                val id = insert("notes", null, ContentValues().apply {
                    put("title", "无标题文档")
                    put("html_content", "")
                    put("is_pinned", 0)
                    put("created_at", now.epochSecond)
                    put("updated_at", now.epochSecond)
                })
                setTransactionSuccessful()
                return id
            }
            finally {
                endTransaction()
            }
        }
    }

    @Throws
    fun addNote(note: Note) {
        val noteId = createNote()
        updateNote(
            UpdateBuilder(noteId)
                .title(note.title)
                .htmlContent(note.htmlContent)
                .pinned(note.pinned)
                .build()
        )
    }

    fun importNotes(notes: List<Note>): List<Long> {
        databaseHelper.getWritableDatabase().apply {
            val ids = mutableListOf<Long>()
            beginTransaction()
            try {
                for (note in notes) {
                    ids.add(insert("notes", null, ContentValues().apply {
                        put("title", note.title)
                        put("html_content", note.htmlContent)
                        put("is_pinned", if (note.pinned) 1 else 0)
                        put("created_at", note.createdAt.time / 1000)
                        put("updated_at", note.updatedAt.time / 1000)
                    }))
                }
                setTransactionSuccessful()
                return ids
            }
            finally {
                endTransaction()
            }
        }
    }

    @Throws
    fun deleteNote(id: Long) {
        databaseHelper.getWritableDatabase().apply {
            beginTransaction()
            try {
                execSQL("DELETE FROM notes WHERE id = ?", arrayOf(id))
                setTransactionSuccessful()
            }
            finally {
                endTransaction()
            }
        }
    }

    data class UpdateQuery(val sql: String, val arguments: Array<Any>)

    class UpdateBuilder(private val id: Long) {
        private var shouldUpdateTitle = false
        private var title: String? = null

        private var shouldUpdateHtmlContent = false
        private var htmlContent: String? = null

        private var shouldUpdatePinned = false
        private var pinned: Boolean? = null

        fun title(title: String): UpdateBuilder {
            shouldUpdateTitle = true
            this.title = title
            return this
        }

        fun htmlContent(htmlContent: String): UpdateBuilder {
            shouldUpdateHtmlContent = true
            this.htmlContent = htmlContent
            return this
        }

        fun pinned(pinned: Boolean): UpdateBuilder {
            shouldUpdatePinned = true
            this.pinned = pinned
            return this
        }

        fun build(): UpdateQuery {
            val sql = StringBuilder("UPDATE notes SET ")
            val arguments = mutableListOf<Any>()
            if (shouldUpdateTitle) {
                sql.append("title = ?, ")
                arguments.add(title!!)
            }
            if (shouldUpdateHtmlContent) {
                sql.append("html_content = ?, ")
                arguments.add(htmlContent!!)
            }
            if (shouldUpdatePinned) {
                sql.append("is_pinned = ?, ")
                arguments.add(if (pinned!!) 1 else 0)
            }
            sql.append("updated_at = ? WHERE id = ?")
            arguments.add(Instant.now().epochSecond.toInt())
            arguments.add(id)

            return UpdateQuery(sql.toString(), arguments.toTypedArray())
        }
    }
}
