package com.eggtartc.briannote.util

import android.content.Intent

object PermissionUtils {
    fun createImagePickingIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    }
}
