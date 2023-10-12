package com.eggtartc.briannote.interfaces

import net.sqlcipher.database.SQLiteDatabase

interface IDatabaseHelper {
    fun getReadableDatabase(): SQLiteDatabase
    fun getWritableDatabase(): SQLiteDatabase

    fun initializeForFirstRun()
}
