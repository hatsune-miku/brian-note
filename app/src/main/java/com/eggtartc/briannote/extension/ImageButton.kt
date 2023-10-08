package com.eggtartc.briannote.extension

import android.app.Activity
import android.widget.TextView
import com.github.mr5.icarus.Icarus
import com.github.mr5.icarus.button.Button
import com.github.mr5.icarus.button.TextViewButton

class ImageButton<ActivityType>(val activity: ActivityType, textView: TextView, icarus: Icarus)
    : TextViewButton(textView, icarus) where ActivityType: Activity, ActivityType: ImageButton.IImagePicker  {
    override fun command() {
        if (name != Button.NAME_IMAGE) {
            super.command()
            return
        }
        activity.onImagePickStart()
    }

    interface IImagePicker {
        fun onImagePickStart()
    }
}
