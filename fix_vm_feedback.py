with open('app/src/main/java/com/example/viewmodel/CricketViewModel.kt', 'r') as f:
    content = f.read()

target1 = """    val fundingDismissed = onboardingManager.fundingDismissed"""
replacement1 = """    val fundingDismissed = onboardingManager.fundingDismissed
    val appOpensCount = onboardingManager.appOpensCount
    val feedbackDismissed = onboardingManager.feedbackDismissed"""

if target1 in content:
    content = content.replace(target1, replacement1)

target2 = """    fun dismissFunding() {
        viewModelScope.launch {
            onboardingManager.saveFundingDismissed(true)
        }
    }"""
replacement2 = """    fun dismissFunding() {
        viewModelScope.launch {
            onboardingManager.saveFundingDismissed(true)
        }
    }
    
    fun incrementAppOpens() {
        viewModelScope.launch {
            onboardingManager.incrementAppOpens()
        }
    }

    fun dismissFeedback() {
        viewModelScope.launch {
            onboardingManager.saveFeedbackDismissed(true)
        }
    }"""

if target2 in content:
    content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/viewmodel/CricketViewModel.kt', 'w') as f:
    f.write(content)
print("Updated CricketViewModel")
