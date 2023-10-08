package com.eggtartc.briannote.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import java.io.File
import java.time.Instant
import java.util.Base64

object FileUtils {
    /**
     * Creates a temporary file from the given [uri].
     * The file will be deleted when the application exits.
     */
    fun createTemporaryFileFromUri(context: Context, uri: Uri): File {
        val file = File.createTempFile("temp", ".db", context.cacheDir).apply {
            deleteOnExit()
        }
        // The [file] returned by [createTempFile] is guaranteed to exist
        // so we don't need to catch [FileNotFoundException].
        context.contentResolver.openInputStream(uri).use {
            it?.copyTo(file.outputStream())
        }
        return file
    }

    fun createPersistentFileFromUri(context: Context, uri: Uri): File {
        val filePath = uri.path
            ?: throw IllegalArgumentException("Uri $uri does not have a path")
        val fileName = Instant.now().epochSecond.toString() + filePath.substringAfterLast('/')
        val file = File(context.filesDir, fileName)
        context.contentResolver.openInputStream(uri).use {
            it?.copyTo(file.outputStream())
        }
        return file
    }

    fun encodeUriFileToBase64Binary(context: Context, uri: Uri): String {
        context.contentResolver.openInputStream(uri).use {
            return Base64.getEncoder().encodeToString(it?.readBytes())
        }
    }
}
