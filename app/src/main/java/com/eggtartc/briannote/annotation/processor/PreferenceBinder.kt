package com.eggtartc.briannote.annotation.processor

import androidx.preference.PreferenceFragmentCompat
import com.eggtartc.briannote.annotation.PreferenceBind

object PreferenceBinder {
    fun bind(fragment: PreferenceFragmentCompat) {
        fragment.javaClass.declaredFields.forEach { field ->
            field.declaredAnnotations
                .find { it.annotationClass == PreferenceBind::class }
                ?.let {
                    val key = (it as PreferenceBind).key
                    field.isAccessible = true
                    field.set(fragment, fragment.findPreference(key))
                }
        }
    }
}
