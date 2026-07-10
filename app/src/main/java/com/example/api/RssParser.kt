package com.example.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object RssParser {
    private const val RSS_URL = "https://static.cricinfo.com/rss/livescores.xml"

    suspend fun fetchLiveMatches(): List<String> = withContext(Dispatchers.IO) {
        val matches = mutableListOf<String>()
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(RSS_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(inputStream, null)

                var eventType = parser.eventType
                var insideItem = false
                var currentTitle = ""

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name.equals("item", ignoreCase = true)) {
                                insideItem = true
                            } else if (parser.name.equals("title", ignoreCase = true)) {
                                if (insideItem) {
                                    currentTitle = parser.nextText().trim()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name.equals("item", ignoreCase = true)) {
                                insideItem = false
                                if (currentTitle.isNotEmpty()) {
                                    matches.add(currentTitle)
                                    currentTitle = ""
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            connection?.disconnect()
        }
        matches
    }
}
