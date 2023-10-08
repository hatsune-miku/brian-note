package com.eggtartc.briannote.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import androidx.appcompat.view.SupportMenuInflater
import com.eggtartc.briannote.inflater.IconMenuInflater
import com.google.android.material.appbar.MaterialToolbar

class IconMaterialToolbar: MaterialToolbar {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private val iconMenuInflater = IconMenuInflater(context)
    private var menuRes = 0

    override fun inflateMenu(i: Int) {
        super.inflateMenu(i)
        menuRes = i
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        iconMenuInflater.setOnMenuItemClickListener { listener?.onMenuItemClick(it); true }
    }

    fun setDestructiveTitlePredicate(predicate: (CharSequence) -> Boolean) {
        iconMenuInflater.setDestructiveTitlePredicate(predicate)
    }

    @SuppressLint("RestrictedApi")
    fun reInflateMenu() {
        menu.clear()
        val menuInflater = SupportMenuInflater(context)
        iconMenuInflater.inflateMenuWithIcon(menuInflater, menuRes, menu)
    }
}
