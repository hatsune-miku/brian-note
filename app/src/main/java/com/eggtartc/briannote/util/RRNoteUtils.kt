package com.eggtartc.briannote.util

import android.app.Activity
import android.content.Intent
import android.os.Environment
import java.io.File
import java.util.Calendar
import java.util.Date

/**
 * Used for backward compatibility with the RRNote.
 */
object RRNoteUtils {
    fun getBackupDatabaseFile(): File {
        val directory = File(Environment.getExternalStorageDirectory(), "rrnote")
        return File(directory, "backup.db")
    }

    fun createBackupDatabaseReadIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    }

    fun timestampFromRRNoteStyleDate(date: String): Date {
        val dateGroup = date.split("\t")
        val (month, day, year) = dateGroup[0].split("/").map { it.toInt() }
        val (hour, minute, second) = dateGroup[1].split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000 + year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
        }
        return calendar.time
    }
}
