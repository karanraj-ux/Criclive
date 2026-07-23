package com.example.api

import org.junit.Test
import org.junit.Assert.*

class CricketRepositoryTest {
    
    @Test
    fun testExtractNameAndScore() {
        val fullStr = "India 233/10 (43.6 ov)"
        
        val overRegex = Regex("""\(([^)]+)\)$""")
        var strToProcess = fullStr.trim()
        var overs = ""
        val overMatch = overRegex.find(strToProcess)
        if (overMatch != null) {
            overs = overMatch.groupValues[1].trim()
            strToProcess = strToProcess.substring(0, overMatch.range.first).trim()
        }
        
        val scoreRegex = Regex("""(\d+(?:/\d+)?(?:\s*(?:d|\*))?)$""")
        val match = scoreRegex.find(strToProcess)
        var namePart = strToProcess
        var scorePart = ""
        if (match != null) {
            scorePart = match.groupValues[1].trim()
            namePart = strToProcess.substring(0, match.range.first).trim()
        }
        
        println("Name: $namePart, Score: $scorePart, Overs: $overs")
        assertEquals("India", namePart)
        assertEquals("233/10", scorePart)
        assertEquals("43.6 ov", overs)
    }
}
