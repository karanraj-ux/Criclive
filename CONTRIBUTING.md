# Contributing to CricZen 🤝

First off, thank you for considering contributing to CricZen! It's people like you that make the open-source community in India and worldwide so amazing. 

Since CricZen is heavily targeted towards students learning Android, we welcome beginners! Don't hesitate to open a Pull Request even if you're just fixing a typo or adding a comment.

## How Can I Contribute?

### 1. Code Contributions (Features & Bug Fixes)
* **Find an Issue:** Look through our GitHub Issues. Issues labeled `good first issue` are perfect if you're new to the codebase.
* **Fork & Clone:** Fork the repository to your own GitHub account, then clone it to your local machine.
* **Create a Branch:** `git checkout -b feature/your-awesome-feature`
* **Commit:** Keep your commit messages clear and descriptive.
* **Push & PR:** Push to your fork and submit a Pull Request to our `main` branch.

### 2. UI/UX & Design
If you are a designer, we always need help improving our Material 3 layouts, adding new animations, or creating assets (like placeholders for Fan Mode).

### 3. Translations
We want CricZen to be accessible across India. If you can translate the app into Hindi, Tamil, Telugu, Kannada, Malayalam, Bengali, or any other regional language, please open an issue!

## Development Guidelines
* We use **Kotlin** and **Jetpack Compose** exclusively. 
* Avoid large monolithic files; keep Composables small and reusable.
* If you add a new data layer, please use **Room** for caching so the app remains offline-friendly.
* Run `./gradlew assembleDebug` before submitting a PR to ensure there are no build errors.

## Community Respect
Please be respectful and patient with other contributors. We are all here to learn and build something cool together.

Happy Coding! 🏏
