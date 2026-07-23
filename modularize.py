import os
import re

with open('app/src/main/java/com/example/ui/CricketApp.kt', 'r') as f:
    lines = f.readlines()

def get_block(start_line_idx):
    # This is rudimentary, assuming we know exact line ranges from the previous grep
    pass

# We will just slice the lines array based on line numbers (1-indexed, so -1 for array index)

def write_file(path, pkg, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    imports = """import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Match
import com.example.viewmodel.CricketUiState
import com.example.viewmodel.CricketViewModel
import kotlin.math.abs
"""
    if pkg != "com.example.ui":
        imports += "import com.example.ui.*\n"
        imports += "import com.example.ui.components.*\n"
        imports += "import com.example.ui.screens.*\n"
    
    with open(path, 'w') as f:
        f.write(f"package {pkg}\n\n{imports}\n")
        f.writelines(content)

# MatchListScreen: 132-335
write_file('app/src/main/java/com/example/ui/screens/MatchListScreen.kt', 'com.example.ui.screens', lines[131:336])

# IdolHeader: 337-387
write_file('app/src/main/java/com/example/ui/components/IdolHeader.kt', 'com.example.ui.components', lines[336:388])

# MatchCard: 389-516
write_file('app/src/main/java/com/example/ui/components/MatchCard.kt', 'com.example.ui.components', lines[388:517])

# TeamScoreRow: 518-578
write_file('app/src/main/java/com/example/ui/components/TeamScoreRow.kt', 'com.example.ui.components', lines[517:578])

# OnboardingScreen: 579-903
write_file('app/src/main/java/com/example/ui/screens/OnboardingScreen.kt', 'com.example.ui.screens', lines[578:903])

# SettingsBottomSheet: 904-984
write_file('app/src/main/java/com/example/ui/screens/SettingsBottomSheet.kt', 'com.example.ui.screens', lines[903:984])

# MatchDetailScreen: 985-1055
write_file('app/src/main/java/com/example/ui/screens/MatchDetailScreen.kt', 'com.example.ui.screens', lines[984:1055])

# PipScoreCard: 1056-1076
write_file('app/src/main/java/com/example/ui/components/PipScoreCard.kt', 'com.example.ui.components', lines[1055:])

# Now update CricketApp.kt to only include the first 131 lines
with open('app/src/main/java/com/example/ui/CricketApp.kt', 'w') as f:
    f.writelines(lines[:38])
    f.write("import com.example.ui.screens.*\nimport com.example.ui.components.*\n")
    f.writelines(lines[38:131])

