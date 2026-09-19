package com.obitoboost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.OptimizationProfile
import com.obitoboost.app.ui.components.ObitoCard
import com.obitoboost.app.ui.components.SectionHeader
import com.obitoboost.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().obitoBackground().padding(20.dp)) {
        SectionHeader("Saved Profiles", "Your OBITO BOOST optimization profiles for this device")

        if (state.savedProfiles.isEmpty()) {
            ObitoCard {
                Text(
                    "No profiles saved yet. Generate one on the Smart Boost screen, then tap \"Save This Profile\".",
                    color = TextSecondary
                )
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.savedProfiles, key = { it.id }) { profile ->
                ProfileCard(profile, dateFormat) { viewModel.deleteSavedProfile(profile.id) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProfileCard(profile: OptimizationProfile, dateFormat: SimpleDateFormat, onDelete: () -> Unit) {
    ObitoCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(profile.deviceModel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(dateFormat.format(Date(profile.createdAtMs)), style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete profile", tint = StatusDanger)
            }
        }
        if (profile.recommendations.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val rec = profile.recommendations.first()
            Text(
                "${rec.graphicsQuality} graphics · ${rec.fpsOption} FPS",
                style = MaterialTheme.typography.bodyMedium, color = Gold
            )
        }
        Text(
            "${profile.optimizationActions.size} optimization action(s)",
            style = MaterialTheme.typography.bodySmall, color = TextSecondary
        )
    }
}
