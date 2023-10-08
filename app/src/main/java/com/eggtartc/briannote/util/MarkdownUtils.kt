package com.eggtartc.briannote.util

import android.content.Context
import android.net.Uri
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.ImageItem
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.SchemeHandler
import java.util.Collections

object MarkdownUtils {
    fun createImagesPlugin(context: Context): ImagesPlugin {
        val configure = ImagesPlugin.ImagesConfigure {
            it.addSchemeHandler(object : SchemeHandler() {
                override fun handle(raw: String, uri: Uri): ImageItem {
                    val assetName = raw.substring("assets://".length)
                    return ImageItem.withDecodingNeeded("image/png", context.assets.open(assetName))
                }

                override fun supportedSchemes(): MutableCollection<String> {
                    return Collections.singleton("assets")
                }

            })
        }
        return ImagesPlugin.create(configure)
    }

    fun createThemePlugin(): AbstractMarkwonPlugin {
        return object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {

            }
        }
    }
}
