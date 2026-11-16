package com.wyrix.wallpaper.network

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

data class RssItem(val title: String?, val link: String?, val enclosureUrl: String?)

/** Tiny RSS 2.0 reader: only <item><title>, <link> and <enclosure url="..."> are used. */
object RssParser {

    fun parse(stream: InputStream): List<RssItem> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, null)

        val items = ArrayList<RssItem>()
        var inItem = false
        var title: String? = null
        var link: String? = null
        var enclosure: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "item" -> {
                        inItem = true
                        title = null
                        link = null
                        enclosure = null
                    }
                    "title" -> if (inItem) title = parser.nextText()
                    "link" -> if (inItem) link = parser.nextText()
                    "enclosure" -> if (inItem) enclosure = parser.getAttributeValue(null, "url")
                }
            } else if (event == XmlPullParser.END_TAG && parser.name == "item") {
                items.add(RssItem(title, link, enclosure))
                inItem = false
            }
            event = parser.next()
        }
        return items
    }
}
