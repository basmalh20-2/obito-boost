package com.obitoboost.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.GamingProfileType
import com.obitoboost.app.data.model.OptimizationAction
import com.obitoboost.app.data.model.RecommendationBasis
import com.obitoboost.app.ui.components.*
import com.obitoboost.app.ui.theme.*

@Composable
fun SmartBoostScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader("Smart Boost", "Pick a gaming style — recommendations are tuned to your exact device")

        Text("Gaming Profile", style = MaterialTheme.typography.titleMedium, color = Gold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(GamingProfileType.entries.toList()) { type ->
                ProfileChip(
                    type = type,
                    selected = state.selectedProfileType == type,
                    onClick = { viewModel.selectProfileType(type) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            state.selectedGame?.displayName?.let { "Target game: $it" } ?: "No game selected — recommendations will be device-level only",
            style = MaterialTheme.typography.bodySmall, color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))
        PrimaryGlowButton("Generate Recommendations", onClick = { viewModel.generateRecommendation() })

        val profile = state.currentProfile
        if (profile != null) {
            Spacer(Modifier.height(20.dp))
            Text("Recommended Game Settings", style = MaterialTheme.typography.titleLarge, color = CyanGlow)
            Spacer(Modifier.height(8.dp))
            profile.recommendations.forEach { rec ->
                ObitoCard {
                    SettingRow("Graphics Quality", rec.graphicsQuality)
                    SettingRow("FPS Option", rec.fpsOption)
                    SettingRow("Shadows", rec.shadows)
                    SettingRow("Anti-Aliasing", rec.antiAliasing)
                    SettingRow("Effects", rec.effects)
                    SettingRow("Texture Quality", rec.textureQuality)
                    Spacer(Modifier.height(8.dp))
                    BasisTag(rec.basis)
                }
                Spacer(Modifier.height(12.dp))
            }
            if (profile.recommendations.isEmpty()) {
                ObitoCard {
                    Text(
                        "Select a supported installed game on the Games screen to get game-specific settings. Device-level optimizations are still listed below.",
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Text("Safe Optimizations", style = MaterialTheme.typography.titleLarge, color = CyanGlow)
            Spacer(Modifier.height(8.dp))
            profile.optimizationActions.forEach { action ->
                OptimizationActionCard(action) {
                    action.settingsIntentAction?.let { intentAction ->
                        try {
                            val intent = Intent(intentAction)
                            if (intentAction == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                                intent.data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) { /* Settings screen not available on this OEM build */ }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))
            PrimaryGlowButton("Save This Profile", onClick = { viewModel.saveCurrentProfile() })
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileChip(type: GamingProfileType, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) CyanGlow else NavyCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            type.displayName,
            color = if (selected) NavyDeep else TextPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BasisTag(basis: RecommendationBasis) {
    Text(
        "ℹ ${basis.explanation}",
        style = MaterialTheme.typography.bodySmall,
        color = Gold
    )
}

@Composable
private fun OptimizationActionCard(action: OptimizationAction, onOpenSettings: () -> Unit) {
    ObitoCard {
        Text(action.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(action.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        if (!action.canApplyDirectly && action.settingsIntentAction != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onOpenSettings)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open Android Settings", color = CyanGlow, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
