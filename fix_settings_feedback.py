with open('app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'r') as f:
    content = f.read()

target = """            OutlinedButton(
                onClick = { showFaq = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("App FAQ & Limitations", fontWeight = FontWeight.Bold)
            }"""

replacement = """            OutlinedButton(
                onClick = { showFaq = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("App FAQ & Limitations", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* Open Play Store or similar */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                border = BorderStroke(1.dp, Color(0xFFD97706)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Rate App / Give Feedback", fontWeight = FontWeight.Bold)
            }"""

if target in content:
    content = content.replace(target, replacement)
    print("Fixed SettingsBottomSheet")
else:
    print("Failed")

with open('app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'w') as f:
    f.write(content)
