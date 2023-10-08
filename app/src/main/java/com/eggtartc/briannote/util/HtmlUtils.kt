package com.eggtartc.briannote.util

import org.jsoup.Jsoup

object HtmlUtils {
    fun extractVisibleTextContent(html: String): String {
        return Jsoup.parse(html).body().text()
    }
}
