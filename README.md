# CricLive 🏏

![CricLive Banner](assets/feature_graphic.png)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/github/actions/workflow/status/your-username/criclive/android.yml?branch=main)](https://github.com/your-username/criclive/actions)
[![F-Droid](https://img.shields.io/badge/F--Droid-Coming%20Soon-blue?logo=f-droid)](https://f-droid.org/)

CricLive is an open-source, lightning-fast cricket live score app built specifically for Indian cricket fans and students. It tracks live matches, recent results, and upcoming series using modern Android architecture. 

Whether you just want the scores in a clean UI or want to completely customize the app for your favorite player, CricLive has you covered.

## Features ✨

* **Standard Mode:** A clean, minimal dashboard focusing entirely on live scores, match timings, and upcoming schedules. No bloat, no heavy ads.
* **Fan Zone (Fan Mode):** Personalize the app with your idol's name and wallpaper. Get a premium, custom-tailored dark theme glowing with your favorite player's aura.
* **Picture-in-Picture (PiP):** Keep an eye on the score while browsing other apps.
* **Offline-First Resilience:** Built with Room Database to cache recent scores if your network drops on the commute.
* **Light/Dark Themes:** Adapts to your system preferences naturally.

## Screenshots 📸

| Standard Mode | Fan Zone | Dark Mode |
|:---:|:---:|:---:|
| <img src="assets/screenshot_standard.png" width="250"> | <img src="assets/screenshot_fanmode.png" width="250"> | <img src="assets/screenshot_dark.png" width="250"> |

## For Students & Developers 👨‍💻

CricLive is designed to be a learning resource for modern Android development. 
* **UI:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM + Clean Architecture principles
* **Local Storage:** Room Database
* **Networking:** Retrofit + XML Pull Parsing for RSS

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/criclive.git
   ```
2. **Open in Android Studio:**
   Open the cloned directory in Android Studio (Jellyfish or newer recommended).
3. **Build & Run:**
   Sync the Gradle project, select your emulator or physical device, and hit **Run** (`Shift + F10`).

## Roadmap 🚀

- [x] Live Scores & RSS Integration
- [x] Fan Mode Personalization
- [x] Picture-in-Picture (PiP)
- [ ] F-Droid Submission
- [ ] Multi-language support (Hindi, Tamil, Telugu, etc.)
- [ ] Google Play Store Release

## License 📜

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
