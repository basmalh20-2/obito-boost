package com.obitoboost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obitoboost.app.ui.components.ObitoCard
import com.obitoboost.app.ui.components.SectionHeader
import com.obitoboost.app.ui.theme.*

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader("Settings", "About OBITO BOOST")

        ObitoCard {
            Text("OBITO BOOST", style = MaterialTheme.typography.headlineMedium, color = CyanGlow, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Gaming Performance Suite", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            Text("Version 1.0", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }

        Spacer(Modifier.height(16.dp))
        Text("Why OBITO BOOST shows \"N/A\" sometimes", style = MaterialTheme.typography.titleMedium, color = Gold)
        Spacer(Modifier.height(8.dp))
        ObitoCard {
            Text(
                "Android deliberately restricts what any app — including OBITO BOOST — can read or change about other apps and the system, for your security and privacy. When a metric or setting isn't accessible through an official Android API, OBITO BOOST tells you plainly instead of guessing or faking a number.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Permissions Used", style = MaterialTheme.typography.titleMedium, color = Gold)
        Spacer(Modifier.height(8.dp))
        ObitoCard {
            PermissionRow("Battery Stats", "Reads battery level, temperature and charging status")
            PermissionRow("Package Visibility (Games)", "Detects whether a supported game is installed — reads no game data")
        }

        Spacer(Modifier.height(16.dp))
        Text("Safety Commitment", style = MaterialTheme.typography.titleMedium, color = Gold)
        Spacer(Modifier.height(8.dp))
        ObitoCard {
            listOf(
                "Never fakes FPS or temperature readings",
                "Never claims to modify a setting it cannot actually modify",
                "Never deletes user files or modifies system files",
                "Never requires root access"
            ).forEach {
                Text("•  $it", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionRow(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}
