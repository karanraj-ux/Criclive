val regex = Regex("""(\d+(?:/\d+)?(?:\s*\*?)?)$""")
val s = "Sri Lanka Under-19s 291/9 *"
val match = regex.find(s.trim())
println(match?.groupValues?.get(1)?.trim())
println(s.substring(0, match!!.range.first).trim())
