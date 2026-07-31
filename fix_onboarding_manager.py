with open('app/src/main/java/com/example/data/OnboardingManager.kt', 'r') as f:
    content = f.read()

target1 = """        val APP_MODE = stringPreferencesKey("app_mode")
        val FUNDING_DISMISSED = booleanPreferencesKey("funding_dismissed")"""
replacement1 = """        val APP_MODE = stringPreferencesKey("app_mode")
        val FUNDING_DISMISSED = booleanPreferencesKey("funding_dismissed")
        val APP_OPENS_COUNT = intPreferencesKey("app_opens_count")
        val FEEDBACK_DISMISSED = booleanPreferencesKey("feedback_dismissed")"""

if target1 in content:
    content = content.replace(target1, replacement1)

target2 = """    val fundingDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FUNDING_DISMISSED] ?: false
    }"""
replacement2 = """    val fundingDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FUNDING_DISMISSED] ?: false
    }
    val appOpensCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[APP_OPENS_COUNT] ?: 0
    }
    val feedbackDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FEEDBACK_DISMISSED] ?: false
    }"""

if target2 in content:
    content = content.replace(target2, replacement2)

target3 = """    suspend fun saveFundingDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FUNDING_DISMISSED] = dismissed
        }
    }"""
replacement3 = """    suspend fun saveFundingDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FUNDING_DISMISSED] = dismissed
        }
    }
    
    suspend fun incrementAppOpens() {
        context.dataStore.edit { preferences ->
            val current = preferences[APP_OPENS_COUNT] ?: 0
            preferences[APP_OPENS_COUNT] = current + 1
        }
    }

    suspend fun saveFeedbackDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FEEDBACK_DISMISSED] = dismissed
        }
    }"""

if target3 in content:
    content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/data/OnboardingManager.kt', 'w') as f:
    f.write(content)
print("Updated OnboardingManager")
