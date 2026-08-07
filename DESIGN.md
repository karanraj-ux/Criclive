# CricZen: Architecture & Design Documentation

## 1. Core Architecture
CricZen is built using a modern Android tech stack following the **MVVM (Model-View-ViewModel)** pattern.
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **State Management**: StateFlow & Coroutines
- **Data Persistence**: DataStore Preferences (`OnboardingManager`)
- **Local Caching**: Room Database (`AppDatabase`, `MatchDao`)

## 2. Data Extraction Engine (How it works)
The application avoids paid APIs by employing a hybrid scraping and parsing engine targeted at Cricbuzz data sources. This logic resides in `RssParser.kt` and `CricketRepository.kt`.

### Phase 1: RSS Feed Parsing
- The app first pings the Cricbuzz J2ME legacy RSS feed (`http://synd.cricbuzz.com/j2me/1.0/livematches.xml`).
- Using `XmlPullParser`, it extracts the `<title>` and `<link>` tags.
- The title string (e.g., "India 150/4 vs Australia 149/10, Final - India won by 6 wickets") is processed using Regex to separate Team 1, Team 2, Scores, Overs, and Match Status.

### Phase 2: Embedded JSON Extraction (Scraping)
- To get richer, more reliable data (especially for recent/upcoming matches), the app fetches the raw HTML from Cricbuzz (`https://www.cricbuzz.com/cricket-match/live-scores`).
- It scans the HTML body for a specific embedded JSON block: `"typeMatches":[{...}]`.
- Using a custom bracket-matching algorithm, it extracts the raw JSON string from the HTML document.
- It parses this JSON using `org.json.JSONObject`, navigating through `seriesMatches` -> `seriesAdWrapper` -> `matches` -> `matchInfo` & `matchScore`.
- This provides precise, structured data for Runs, Wickets, Overs, and Status without relying on brittle string splitting.

### Phase 3: Player & Squad Verification
- To support the "Favorite Players" feature, the app checks if a specific player is playing.
- It attempts to fetch team squads by querying the **Wikipedia API** (e.g., querying the "India_national_cricket_team" page and scraping links in the "Squad" section).
- If Wikipedia parsing fails, it falls back to a hardcoded `topPlayersFallback` map containing the Top 10-15 players for major international teams.

## 3. Personalization & Onboarding
User preferences are central to the app's feed algorithm. This is managed by `OnboardingManager.kt` using Android's `DataStore`.

### Filtering Algorithm
1. **My Teams**: Users select their favorite International or T20 League teams during Onboarding (or via Settings). 
2. **Tabbed Navigation**: The `MatchListScreen` has two tabs: "All Matches" and "My Teams". 
   - When on "My Teams", the app filters the extracted JSON/RSS feed, cross-referencing `team1` and `team2` against the user's `preferredTeams` set.
3. **Idol & Fan Zone**: Users can upload a custom Wallpaper URI and set an "Idol Name". This is saved in DataStore and displayed dynamically in the `IdolHeader` Composable at the top of the feed.

## 4. UI / UX Breakdown (Phase 1 Refactor)
- **Light Mode Enforced**: The app intentionally forces a clean, high-contrast white canvas (Cricbuzz style) by overriding the system Dark Mode in `Theme.kt`.
- **Top 10 Indian Players**: A horizontally scrolling `<Row>` displays a hardcoded list of Fan Favorites (Virat Kohli, Rohit Sharma, MS Dhoni, etc.) acting as a visual anchor.
- **Match Cards**: Designed with stark white backgrounds, dark structural borders (1dp stroke, `Color(0xFFE5E7EB)`), and prominent typography (`FontWeight.Bold`). Badges differentiate Live (Red), Upcoming (Grey), and Complete statuses.
- **Picture-in-Picture (PiP)**: Managed in `MainActivity.kt`. When the user leaves the app while viewing a `MatchDetailScreen`, the app triggers `enterPictureInPictureMode`, switching to a minimalist `PipScoreCard` Composable.
