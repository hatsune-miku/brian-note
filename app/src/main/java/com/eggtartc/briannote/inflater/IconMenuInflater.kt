package com.eggtartc.briannote.inflater

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import com.eggtartc.briannote.R
import java.util.function.Consumer

class IconMenuInflater(private val context: Context) {
    private var onItemClickListener: MenuItem.OnMenuItemClickListener? = null
    private var destructiveTitlePredicate: ((CharSequence) -> Boolean)? = null
    private var menuItemPostProcessor: Consumer<MenuItem>? = null

    fun setOnMenuItemClickListener(listener: MenuItem.OnMenuItemClickListener) {
        onItemClickListener = listener
    }

    fun setDestructiveTitlePredicate(predicate: (CharSequence) -> Boolean) {
        destructiveTitlePredicate = predicate
    }

    fun setMenuItemPostProcessor(processor: Consumer<MenuItem>) {
        menuItemPostProcessor = processor
    }

    @SuppressLint("RestrictedApi")
    fun inflateMenuWithIcon(menuInflater: MenuInflater, @MenuRes menuRes: Int, menu: Menu) {
        menuInflater.inflate(menuRes, menu)
        if (menu is MenuBuilder) {
            menu.apply {
                setOptionalIconsVisible(true)
                for (item in visibleItems) {
                    item.setOnMenuItemClickListener(onItemClickListener)
                    item.title?.let {
                        if (destructiveTitlePredicate?.invoke(it) == true) {
                            item.iconTintList = context.getColorStateList(R.color.dangerous)
                            item.title = SpannableString(it).apply {
                                setSpan(
                                    ForegroundColorSpan(context.getColor(R.color.dangerous)),
                                    0, it.length, SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }

                    val iconMarginPixels = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 16.0f, context.resources.displayMetrics).toInt()
                    if (item.icon != null) {
                        item.icon = object : InsetDrawable(item.icon, iconMarginPixels, 0, iconMarginPixels, 0) {
                            override fun getIntrinsicWidth(): Int {
                                return intrinsicHeight + 2 * iconMarginPixels
                            }
                        }
                    }
                    menuItemPostProcessor?.accept(item)
                }
            }
        }
    }
}
