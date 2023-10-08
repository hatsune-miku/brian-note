package com.eggtartc.briannote.menu

import android.app.Activity
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import com.eggtartc.briannote.inflater.IconMenuInflater
import java.util.function.Consumer

class IconPopupMenu(
    activity: Activity,
    view: View,
    @MenuRes private val menuRes: Int,
) : PopupMenu(activity, view) {
    private val inflater = IconMenuInflater(activity)

    fun setOnMenuItemClickListener(listener: MenuItem.OnMenuItemClickListener) {
        inflater.setOnMenuItemClickListener(listener)
    }

    fun setDestructiveTitlePredicate(predicate: (CharSequence) -> Boolean) {
        inflater.setDestructiveTitlePredicate(predicate)
    }

    fun setMenuItemPostProcessor(processor: Consumer<MenuItem>) {
        inflater.setMenuItemPostProcessor(processor)
    }

    override fun show() {
        inflater.inflateMenuWithIcon(menuInflater, menuRes, menu)
        super.show()
    }
}
