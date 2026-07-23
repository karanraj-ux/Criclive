package com.example.data

object CricketConstants {
    val MAJOR_KEYWORDS: List<String> = listOf(
        "India", "Australia", "England", "South Africa", "New Zealand",
        "Pakistan", "Sri Lanka", "West Indies", "Bangladesh", "Afghanistan",
        "Ireland", "Zimbabwe", "Netherlands", "Scotland", "Nepal", "USA",
        "Oman", "UAE", "Namibia", "Uganda", "Papua New Guinea",
        "Chennai", "Mumbai", "Royal Challengers", "Kolkata", "Delhi", "Gujarat", 
        "Rajasthan", "Sunrisers", "Lucknow", "Punjab", "Super Kings", "Capitals", "Titans", "Indians",
        "Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide", "Hobart", "Renegades", "Scorchers",
        "Lahore", "Karachi", "Islamabad", "Peshawar", "Quetta", "Multan",
        "Trinbago", "Jamaica", "Barbados", "Guyana", "St Lucia", "St Kitts", "Antigua",
        "Pretoria", "MI Cape Town", "Paarl", "Durban", "Joburg",
        "Oval", "Trent", "Welsh", "Southern", "London", "Manchester", "Birmingham", "Northern",
        "MLC", "Major League", "LPL", "Global T20", "BPL", "WPL", "Super Smash", "The Hundred",
        "Texas", "Washington", "Los Angeles", "San Francisco", "Seattle", "New York", "Unicorns", "Orcas",
        "Knight Riders"
    )

    val EXCLUDED_KEYWORDS: List<String> = listOf("Under-19", "U19", "County", "Shield", "Trophy", "2nd XI", "Club", "Warm-up", "Practice")

    val GLOBAL_STARS: List<String> = listOf(
        "Virat Kohli", "Rohit Sharma", "MS Dhoni", "Jasprit Bumrah", "Babar Azam", 
        "Pat Cummins", "Kane Williamson", "Joe Root", "Steve Smith", "Ben Stokes", 
        "Rashid Khan", "Suryakumar Yadav", "Hardik Pandya"
    )

    val TOP_PLAYERS_FALLBACK: Map<String, List<String>> = mapOf(
        "India" to listOf("Rohit Sharma", "Virat Kohli", "Jasprit Bumrah", "Suryakumar Yadav", "Hardik Pandya", "Rishabh Pant", "Shubman Gill", "KL Rahul", "Ravindra Jadeja", "Mohammed Siraj", "MS Dhoni", "Shreyas Iyer", "Kuldeep Yadav", "Axar Patel"),
        "Australia" to listOf("Pat Cummins", "Mitchell Starc", "Steve Smith", "David Warner", "Glenn Maxwell", "Travis Head", "Josh Hazlewood", "Marnus Labuschagne", "Adam Zampa", "Mitchell Marsh", "Cameron Green"),
        "England" to listOf("Jos Buttler", "Joe Root", "Ben Stokes", "Jonny Bairstow", "Jofra Archer", "Mark Wood", "Adil Rashid", "Moeen Ali", "Sam Curran", "Liam Livingstone", "Phil Salt"),
        "Pakistan" to listOf("Babar Azam", "Shaheen Afridi", "Mohammad Rizwan", "Fakhar Zaman", "Haris Rauf", "Shadab Khan", "Naseem Shah"),
        "South Africa" to listOf("Kagiso Rabada", "Quinton de Kock", "Aiden Markram", "David Miller", "Heinrich Klaasen", "Anrich Nortje", "Marco Jansen", "Lungi Ngidi"),
        "New Zealand" to listOf("Kane Williamson", "Trent Boult", "Tim Southee", "Devon Conway", "Daryl Mitchell", "Glenn Phillips", "Rachin Ravindra", "Mitchell Santner", "Matt Henry", "Lockie Ferguson"),
        "Sri Lanka" to listOf("Wanindu Hasaranga", "Pathum Nissanka", "Maheesh Theekshana", "Charith Asalanka", "Kusal Mendis", "Dasun Shanaka", "Matheesha Pathirana", "Dushmantha Chameera"),
        "West Indies" to listOf("Nicholas Pooran", "Andre Russell", "Jason Holder", "Alzarri Joseph", "Shai Hope", "Rovman Powell", "Romario Shepherd", "Akeal Hosein"),
        "Afghanistan" to listOf("Rashid Khan", "Rahmanullah Gurbaz", "Mohammad Nabi", "Mujeeb Ur Rahman", "Fazalhaq Farooqi", "Naveen-ul-Haq", "Ibrahim Zadran", "Azmatullah Omarzai"),
        "Bangladesh" to listOf("Shakib Al Hasan", "Mustafizur Rahman", "Litton Das", "Taskin Ahmed", "Mushfiqur Rahim", "Najmul Hossain Shanto", "Mehidy Hasan Miraz"),
        "Nepal" to listOf("Sandeep Lamichhane", "Rohit Paudel", "Dipendra Singh Airee", "Kushal Malla", "Sompal Kami", "Karan KC", "Aasif Sheikh"),
        "IPL" to listOf("MS Dhoni", "Virat Kohli", "Rohit Sharma", "Hardik Pandya", "Jasprit Bumrah", "Suryakumar Yadav", "KL Rahul", "Shreyas Iyer", "Sanju Samson", "Rishabh Pant", "Ruturaj Gaikwad", "Shubman Gill")
    )
    val INTERNATIONAL_TEAMS: List<String> = listOf("India", "Australia", "England", "Pakistan", "South Africa", "New Zealand", "Sri Lanka", "West Indies", "Bangladesh", "Afghanistan")
    val T20_LEAGUES: List<String> = listOf("IPL", "BBL", "PSL", "CPL", "The Hundred", "WPL", "SA20")
}
