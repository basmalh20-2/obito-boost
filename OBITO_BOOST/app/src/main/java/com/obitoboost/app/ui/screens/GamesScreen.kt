package com.obitoboost.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.InstalledGame
import com.obitoboost.app.ui.components.ObitoCard
import com.obitoboost.app.ui.components.SectionHeader
import com.obitoboost.app.ui.theme.*

@Composable
fun GamesScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().obitoBackground().padding(20.dp)) {
        SectionHeader("Games", "Supported games detected on this device")

        if (state.installedGames.isEmpty()) {
            ObitoCard { Text("Scanning installed apps…", color = TextSecondary) }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.installedGames) { game ->
                GameRow(
                    game = game,
                    isSelected = state.selectedGame?.packageName == game.entry.packageName,
                    onSelect = { viewModel.selectGame(game.entry) }
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Don't see your game? OBITO BOOST's game database is built to expand — more titles are added in future updates.",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GameRow(game: InstalledGame, isSelected: Boolean, onSelect: () -> Unit) {
    ObitoCard(
        modifier = Modifier.then(
            if (game.isInstalled) Modifier.clickable(onClick = onSelect) else Modifier
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = if (game.isInstalled) CyanGlow else TextMuted)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.entry.displayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    if (game.isInstalled) "Installed" else "Not installed on this device",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (game.isInstalled) StatusGood else TextMuted
                )
            }
            if (game.isInstalled) {
                Icon(
                    if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) CyanGlow else TextMuted
                )
            }
        }
    }
}
