with open('app/src/main/java/com/example/ui/CricketApp.kt', 'r') as f:
    content = f.read()

target1 = """    val fundingDismissed by viewModel.fundingDismissed.collectAsState(initial = false)
    val suggestedPlayers by viewModel.suggestedPlayers.collectAsState()"""
replacement1 = """    val fundingDismissed by viewModel.fundingDismissed.collectAsState(initial = false)
    val appOpensCount by viewModel.appOpensCount.collectAsState(initial = 0)
    val feedbackDismissed by viewModel.feedbackDismissed.collectAsState(initial = false)
    val suggestedPlayers by viewModel.suggestedPlayers.collectAsState()"""

if target1 in content:
    content = content.replace(target1, replacement1)

target2 = """    LaunchedEffect(Unit) {
        // Checking for a real update from GitHub Releases"""
replacement2 = """    LaunchedEffect(Unit) {
        viewModel.incrementAppOpens()
        // Checking for a real update from GitHub Releases"""

if target2 in content:
    content = content.replace(target2, replacement2)

target3 = """    if (appUpdate != null && !isPipMode) {"""
replacement3 = """    // Feedback Dialog Logic
    if (appOpensCount >= 2 && !feedbackDismissed && !isPipMode) {
        var showFeedbackDialog by remember { mutableStateOf(true) }
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showFeedbackDialog = false 
                    viewModel.dismissFeedback()
                },
                title = { Text("Next Phase Plan: Local Scorer", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Are you enjoying CricLive? We are planning to add a manual easy scorer for local tournaments (like gully cricket) with sharing options.", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Would you find this feature useful?", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        showFeedbackDialog = false
                        viewModel.dismissFeedback()
                        // Optional: trigger some analytics or open play store if positive
                    }) {
                        Text("Yes, I'd love it!")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showFeedbackDialog = false
                        viewModel.dismissFeedback()
                    }) {
                        Text("No, keep it simple")
                    }
                }
            )
        }
    }

    if (appUpdate != null && !isPipMode) {"""

if target3 in content:
    content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/CricketApp.kt', 'w') as f:
    f.write(content)
print("Updated CricketApp")
