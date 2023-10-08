package com.eggtartc.briannote.helper

import android.content.Context
import android.util.Log
import com.eggtartc.briannote.constants.Keys
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

class DatabaseHelper(
    private val context: Context
) : SQLiteOpenHelper(
    /* context */                   context,
    /* name */                      Keys.DATABASE_NAME,
    /* factory */                   null,
    /* version */                   1,
    /* minimumSupportedVersion */   //1,
    /* databaseHook */              //null,
    /* errorHandler */              //DatabaseErrorHandler {  }
) {
    companion object {
        const val TAG = "DatabaseHelper";

        /**
         * The call to System.loadLibrary("sqlcipher") must occur before any other database operation.
         */
        init {
            System.loadLibrary("sqlcipher")
            Log.d(TAG, "Loaded sqlcipher library")
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.apply {
            beginTransaction()
            try {
                execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                            "notes (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT," +
                            "html_content TEXT," +
                            "is_pinned INTEGER," +
                            "created_at INTEGER," +
                            "updated_at INTEGER" +
                            ")"
                )
                execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                            "secure (" +
                            "password TEXT," +
                            "is_fake INTEGER" +
                            ")"
                )
                Log.d(TAG, "Tried to create tables if they do not exist")
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.apply {
            if (!needUpgrade(newVersion)) {
                return
            }
            upgrade()
        }
    }

    @Throws(Exception::class)
    fun initializeForFirstRun() {
        getWritableDatabase(Keys.DATABASE_PASSWORD).apply {
            beginTransaction()
            try {
                execSQL("DELETE FROM secure")
                execSQL("INSERT INTO secure (password, is_fake) VALUES ('', 0)")
                execSQL("INSERT INTO secure (password, is_fake) VALUES ('', 1)")
                Log.d(TAG, "Initialized secure table for first run")
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    private fun upgrade() {
        // Left blank intentionally
    }
}
