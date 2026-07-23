fun main() {
    val context = "Kohli 45 (30)"
    val batMatch = Regex("\\b(\\d{1,3})\\s*\\\\(\\s*(\\d{1,3})\\s*\\\\)").find(context)
    if (batMatch != null) {
        println("Bat: ${batMatch.groupValues[1]}* (${batMatch.groupValues[2]}b)")
    } else {
        println("Bat mismatch")
    }
}
