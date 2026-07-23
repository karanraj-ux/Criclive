import org.json.JSONObject
fun main() {
    val obj = JSONObject("{\"runs\":233,\"wickets\":10,\"overs\":43.6}")
    val r = obj.optString("runs", "")
    println("r=$r")
}
