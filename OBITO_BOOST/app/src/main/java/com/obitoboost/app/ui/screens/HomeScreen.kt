package com.obitoboost.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.MetricValue
import com.obitoboost.app.ui.Screen
import com.obitoboost.app.ui.components.*
import com.obitoboost.app.ui.theme.*

@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavHostController) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val device = state.deviceInfo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .obitoBackground()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        SectionHeader(
            title = "Welcome to OBITO BOOST",
            subtitle = device?.let { "${it.manufacturer} ${it.model}" } ?: "Scanning your device…"
        )

        device?.let {
            TierBadge(it.performanceTier.displayName)
            Spacer(Modifier.height(16.dp))
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                StatChip(
                    "RAM",
                    device?.ramTotalMb?.valueOrNull()?.let { "${it / 1024}GB" } ?: "—"
                )
            }
            item {
                StatChip(
                    "Refresh Rate",
                    device?.refreshRateHz?.let { "${it.toInt()}Hz" } ?: "—",
                    accent = Gold
                )
            }
            item {
                StatChip(
                    "Battery",
                    device?.batteryPercent?.valueOrNull()?.let { "$it%" } ?: "—"
                )
            }
            item {
                StatChip(
                    "Storage Free",
                    device?.storageAvailableGb?.valueOrNull()?.let { "%.0fGB".format(it) } ?: "—",
                    accent = Gold
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader(title = "Quick Gaming Status")
        ObitoCard {
            val thermal = device?.thermalStatus
            val statusText = when (thermal) {
                is MetricValue.Measured -> thermal.value
                else -> "Unknown (needs Android 10+)"
            }
            val statusColor = if (thermal is MetricValue.Measured && thermal.value != "Normal") StatusWarning else StatusGood
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Thermostat, contentDescription = null, tint = statusColor)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Thermal status: $statusText", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        if (device?.isCharging == true) "Currently charging" else "On battery power",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader(title = "Get Started")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Filled.SportsEsports,
                title = "Detect My Games",
                subtitle = "${state.installedGames.count { it.isInstalled }} supported game(s) found",
                onClick = { navController.navigate(Screen.Games.route) }
            )
            QuickActionCard(
                icon = Icons.Filled.Bolt,
                title = "Generate Smart Boost",
                subtitle = "Personalized optimization for your device",
                onClick = { navController.navigate(Screen.SmartBoost.route) }
            )
            QuickActionCard(
                icon = Icons.Filled.BarChart,
                title = "Run a Benchmark",
                subtitle = "Measure before/after performance",
                onClick = { navController.navigate(Screen.Benchmark.route) }
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ObitoCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .obitoCard(),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CyanGlow)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
