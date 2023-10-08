package com.eggtartc.briannote.extension

import com.github.mr5.icarus.Callback
import com.github.mr5.icarus.Icarus
import com.github.mr5.icarus.entity.Html
import com.google.gson.Gson

fun Icarus.getRawHtmlContent(callback: Callback) {
    this.getContent { content ->
        callback.run(
            (Gson().fromJson(content, Html::class.java) as Html)
                .content
        )
    }
}

fun Icarus.renderHooked() {
    // Line 1: editorReady = false;
    val fieldEditorReady = this.javaClass.getDeclaredField("editorReady")
    fieldEditorReady.isAccessible = true
    fieldEditorReady.set(this, false)

    // Line 2: initialize();
    val fieldInitialize = this.javaClass.getDeclaredMethod("initialize")
    fieldInitialize.isAccessible = true
    fieldInitialize.invoke(this)

    // Line 3: webView.loadUrl("file://android_asset/icarus-editor/editor.html");
    val fieldWebView = this.javaClass.getDeclaredField("webView")
    fieldWebView.isAccessible = true
    val webView = fieldWebView.get(this) as android.webkit.WebView
    webView.loadUrl("file:///android_asset/icarus-editor-hook/editor-hook.html")

    // Line 4: toolbar.resetButtonsStatus();
    val fieldToolbar = this.javaClass.getDeclaredField("toolbar")
    fieldToolbar.isAccessible = true
    val toolbar = fieldToolbar.get(this) as com.github.mr5.icarus.Toolbar
    toolbar.resetButtonsStatus()
}
