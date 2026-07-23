package com.example.api

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class RssParserTest {
    @Test
    fun testParse() = runBlocking {
        try {
            val matches = RssParser.fetchLiveMatches()
            println("Matches found: ${matches.size}")
            var scoresFound = 0
            matches.forEach {
                println(it.title)
                if (it.title.contains("/")) {
                    scoresFound++
                }
            }
            assertTrue(matches.isNotEmpty())
            assertTrue(scoresFound > 0)
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }
}
