import org.json.JSONObject

fun main() {
    val json = JSONObject("{\"test\": 1}")
    println(json.getInt("test"))
}
