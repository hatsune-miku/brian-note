package com.eggtartc.briannote.helper

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.function.Consumer

class ActivityHelper(private val activity: Activity) {
    fun showAlertDialog(
        message: String?,
        title: String?,
        primaryButtonText: String? = "好",
        primaryButtonAction: Runnable? = null,
        secondaryButtonText: String? = null,
        secondaryButtonAction: Runnable? = null
    ) {
        val builder = MaterialAlertDialogBuilder(activity)
        val dialog = builder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(primaryButtonText) { _, _ -> primaryButtonAction?.run() }
            .setNegativeButton(secondaryButtonText) { _, _ -> secondaryButtonAction?.run() }
            .create()
        dialog.show()
    }

    fun promptForString(
        prompt: String, initialValue: String, hint: String,
        positiveButtonText: String, positiveButtonAction: Consumer<TextInputEditText>? = null,
        negativeButtonText: String? = null, negativeButtonAction: Runnable? = null
    ) {
        val textInputLayout = TextInputLayout(activity).apply {
            this.hint = hint
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            updateLayoutParams<ViewGroup.LayoutParams> {
                setPadding(128, 64, 128, 64)
            }
        }
        val textInputEditText = TextInputEditText(activity).apply {
            setText(initialValue, TextView.BufferType.EDITABLE)
        }
        textInputLayout.addView(textInputEditText)

        val alertDialog = MaterialAlertDialogBuilder(activity)
            .setTitle(prompt)
            .setView(textInputLayout)
            .setCancelable(false)
            .setPositiveButton(positiveButtonText) { _, _ -> positiveButtonAction?.accept(textInputEditText) }
            .setNegativeButton(negativeButtonText) { _, _ -> negativeButtonAction?.run() }
            .create()
        alertDialog.show()
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        activity.window.statusBarColor = color
    }

    fun setFlagSecure() {
        activity.window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun clearFlagSecure() {
        activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun makeViewRaiseAlongWithKeyboard(view: View) {
        view.setOnApplyWindowInsetsListener { v, insets ->
            val imeInsets = insets.getInsets(WindowInsets.Type.ime())
            v.setPadding(0, 0, 0, imeInsets.bottom)
            insets
        }
    }
}
