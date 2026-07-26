fun main() {
    val html = """uselImageLayout\":true},\"currentMatchesList\":{\"typeMatches\":[{\"matchType\":\"International\""""
    var searchStr = "\"typeMatches\":["
    println(html.indexOf(searchStr))
    searchStr = "\\\"typeMatches\\\":["
    println(html.indexOf(searchStr))
}
