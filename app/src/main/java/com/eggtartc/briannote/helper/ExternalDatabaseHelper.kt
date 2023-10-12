package com.eggtartc.briannote.helper

import android.content.Context
import android.net.Uri
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.interfaces.IDatabaseHelper
import com.eggtartc.briannote.util.FileUtils
import net.sqlcipher.database.SQLiteDatabase

class ExternalDatabaseHelper(context: Context, databaseFileUri: Uri): IDatabaseHelper {
    private val database: SQLiteDatabase

    init {
        val databaseFile = FileUtils.createTemporaryFileFromUri(
            context, databaseFileUri)
        database = SQLiteDatabase.openOrCreateDatabase(
            databaseFile, Keys.DATABASE_PASSWORD, null, null)
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        return database
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return database
    }

    override fun initializeForFirstRun() {
        // Left blank intentionally
    }

    fun close() {
        database.close()
    }
}
