package com.eggtartc.briannote.helper


import android.content.Context
import android.net.Uri
import com.eggtartc.briannote.model.Note

class RRNoteHelper(context: Context, databaseFileUri: Uri) {
    private val databaseHelper = RRNoteDatabaseHelper(context, databaseFileUri)

    @Throws
    fun readNotes(): List<Note> {
        databaseHelper.getReadableDatabase().apply {
            val notes = mutableListOf<Note>()
            val cursor = rawQuery(
                "SELECT id, time, content FROM _data_;", null)
            while (cursor.moveToNext()) {
                notes.add(Note.fromRRNoteCursor(cursor).apply {
                    didLoadFullContent = true
                })
            }
            cursor.close()
            return notes
        }
    }

    fun isPasswordSet(): Boolean {
        return readSecurePassword().isNotEmpty()
    }

    /**
     * @return The password MD5 hash
     */
    fun readSecurePassword(): String {
        databaseHelper.getReadableDatabase().apply {
            val cursor = rawQuery("SELECT pwd FROM _secure_;", null).apply {
                moveToFirst()
            }
            val password = cursor.getString(0)
            cursor.close()
            return password
        }
    }

    fun close() {
        databaseHelper.close()
    }
}
