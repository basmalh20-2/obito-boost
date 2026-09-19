package com.obitoboost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.BackupEntry
import com.obitoboost.app.ui.components.ObitoCard
import com.obitoboost.app.ui.components.PrimaryGlowButton
import com.obitoboost.app.ui.components.SectionHeader
import com.obitoboost.app.ui.theme.*

@Composable
fun BackupScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var restoredPreview by remember { mutableStateOf<Map<String, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize().obitoBackground().padding(20.dp)) {
        SectionHeader("Backup & Restore", "Snapshots of your OBITO BOOST recommended settings")

        ObitoCard {
            Text(
                "A non-root Android app can't read or write most protected system settings, so backups here store OBITO BOOST's own recommendation values — not raw Android system settings.",
                style = MaterialTheme.typography.bodySmall, color = TextMuted
            )
        }
        Spacer(Modifier.height(12.dp))

        PrimaryGlowButton(
            "Create Backup From Current Profile",
            onClick = { viewModel.createBackup() },
            enabled = state.currentProfile != null
        )
        if (state.currentProfile == null) {
            Spacer(Modifier.height(6.dp))
            Text("Generate a Smart Boost profile first.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }

        Spacer(Modifier.height(20.dp))
        Text("Backup History", style = MaterialTheme.typography.titleLarge, color = Gold)
        Spacer(Modifier.height(8.dp))

        if (state.backups.isEmpty()) {
            ObitoCard { Text("No backups yet.", color = TextSecondary) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.backups, key = { it.id }) { backup ->
                    BackupRow(
                        backup = backup,
                        onRestore = { restoredPreview = viewModel.restoreBackup(backup.id) },
                        onDelete = { viewModel.deleteBackup(backup.id) }
                    )
                }
            }
        }

        restoredPreview?.let { settings ->
            Spacer(Modifier.height(16.dp))
            ObitoCard {
                Text("Restored Values", style = MaterialTheme.typography.titleMedium, color = CyanGlow)
                Spacer(Modifier.height(6.dp))
                settings.forEach { (k, v) ->
                    Text("$k: $v", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Apply these manually via Smart Boost by re-selecting the matching profile.",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BackupRow(backup: BackupEntry, onRestore: () -> Unit, onDelete: () -> Unit) {
    ObitoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(backup.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${backup.restorableSettings.size} value(s) saved", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = CyanGlow)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = StatusDanger)
            }
        }
    }
}
