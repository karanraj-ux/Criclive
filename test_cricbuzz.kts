import java.net.URL

val json = URL("https://www.cricbuzz.com/api/cricket-match/matches").readText()
println(json)
