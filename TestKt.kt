import java.util.regex.Pattern

fun main() {
    val html = "some html \\\"typeMatches\\\":[{\"test\":1}]}]} other stuff"
    val typeMatchesRegex = Regex("""\\\"typeMatches\\\":(.*?)]}]\}""")
    val result = typeMatchesRegex.find(html)
    if (result != null) {
        var str = result.groupValues[1] + "]}]"
        str = str.replace("\\\"", "\"")
        println(str)
    } else {
        println("Not found")
    }
}
