package com.eggtartc.briannote.model

import com.eggtartc.briannote.interfaces.INoteItem

data class NoteHeader(val title: String): INoteItem {
    override fun isHeader(): Boolean {
        return true
    }
}
