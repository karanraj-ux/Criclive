import java.util.regex.Pattern

fun main() {
    try {
        val typeMatchesRegex = Regex("""\\\"typeMatches\\\":(.*?)]}]\}""")
        println("Regex 1 works")
    } catch(e: Exception) {
        println("Regex 1 failed: ${e.message}")
    }
    
    try {
        val typeMatchesRegex2 = Regex("\\\\\"typeMatches\\\\\":(.*?)]}]}")
        println("Regex 2 works")
    } catch(e: Exception) {
        println("Regex 2 failed: ${e.message}")
    }
}
