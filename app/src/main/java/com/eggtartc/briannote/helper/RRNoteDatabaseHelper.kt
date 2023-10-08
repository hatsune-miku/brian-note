package com.eggtartc.briannote.helper

import android.content.Context
import android.net.Uri
import android.util.Log
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.util.FileUtils
import net.sqlcipher.database.SQLiteDatabase

class RRNoteDatabaseHelper(private val context: Context, private val databaseFileUri: Uri) {
    companion object {
        const val TAG = "RRNoteDatabaseHelper";
    }

    private val database: SQLiteDatabase

    init {
        val file = FileUtils.createTemporaryFileFromUri(context, databaseFileUri)
        Log.d(TAG, "Created temporary file from uri: $file")
        database = SQLiteDatabase.openOrCreateDatabase(file, Keys.RRNOTE_DATABASE_PASSWORD, null, null)
        Log.d(TAG, "Opened database from uri: $databaseFileUri")
    }

    fun getReadableDatabase(): SQLiteDatabase {
        return database
    }

    fun close() {
        database.close()
    }
}
