import java.util.regex.Pattern

fun main() {
    val rawTitle = "Guyana Amazon Warriors 131/7 (20.0 ov) vs San Francisco Unicorns 156/6 (19.4 ov), Final - San Francisco Unicorns won by 4 wkts"
    val title = rawTitle.replace("via rss", "", ignoreCase = true).trim()
    
    var team1Full = title
    var team2Full = ""
    var status = "Live Match"
    
    if (title.contains(" vs ", ignoreCase = true) || title.contains(" v ", ignoreCase = true)) {
        // First try to split by comma to separate teams from status/desc
        val commaSplit = title.split(",", limit = 2)
        val teamsPart = commaSplit[0].trim()
        val descPart = if (commaSplit.size > 1) commaSplit[1].trim() else ""
        
        status = descPart
        
        val vsDelimiter = if (teamsPart.contains(" vs ", ignoreCase = true)) " vs " else " v "
        val teamParts = teamsPart.split(Regex(vsDelimiter, RegexOption.IGNORE_CASE), limit = 2)
        team1Full = teamParts.getOrNull(0)?.trim() ?: teamsPart
        team2Full = teamParts.getOrNull(1)?.trim() ?: ""
    }
    println("team1Full: $team1Full")
    println("team2Full: $team2Full")
    println("status: $status")
}
