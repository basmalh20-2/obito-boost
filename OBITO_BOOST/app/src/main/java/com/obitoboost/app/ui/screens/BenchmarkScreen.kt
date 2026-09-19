package com.obitoboost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.PerformanceSnapshot
import com.obitoboost.app.ui.components.*
import com.obitoboost.app.ui.theme.*

@Composable
fun BenchmarkScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader(
            "Benchmark",
            "Measures OBITO BOOST's own on-screen render performance as a smoothness proxy — Android does not let any app read another app's live FPS"
        )

        if (state.isBenchmarking) {
            ObitoCard {
                Text("Benchmarking…", style = MaterialTheme.typography.titleMedium, color = CyanGlow)
                Spacer(Modifier.height(8.dp))
                Text(
                    state.liveFps?.let { "Live: %.0f FPS".format(it) } ?: "Warming up…",
                    style = MaterialTheme.typography.displayLarge, color = TextPrimary, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = CyanGlow, trackColor = NavyCardBorder
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryGlowButton(
                text = "Run BEFORE Test",
                onClick = { viewModel.runBenchmark(isBefore = true) },
                modifier = Modifier.weight(1f),
                enabled = !state.isBenchmarking
            )
            PrimaryGlowButton(
                text = "Run AFTER Test",
                onClick = { viewModel.runBenchmark(isBefore = false) },
                modifier = Modifier.weight(1f),
                enabled = !state.isBenchmarking
            )
        }

        Spacer(Modifier.height(20.dp))

        if (state.beforeSnapshot != null || state.afterSnapshot != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    BenchmarkColumn("BEFORE", state.beforeSnapshot, TextSecondary)
                }
                Box(Modifier.weight(1f)) {
                    BenchmarkColumn("AFTER", state.afterSnapshot, CyanGlow)
                }
            }

            Spacer(Modifier.height(16.dp))
            val comparison = state.comparison
            if (comparison != null) {
                ObitoCard {
                    Text("Comparison", style = MaterialTheme.typography.titleLarge, color = Gold)
                    Spacer(Modifier.height(8.dp))
                    val delta = comparison.fpsDeltaPercent()
                    MetricRow("Average FPS Change", delta) { d ->
                        val sign = if (d >= 0) "+" else ""
                        "$sign%.1f%%".format(d)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Percentages are only shown when both runs produced valid measured data — OBITO BOOST never invents an improvement number.",
                        style = MaterialTheme.typography.bodySmall, color = TextMuted
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BenchmarkColumn(label: String, snapshot: PerformanceSnapshot?, accent: androidx.compose.ui.graphics.Color) {
    ObitoCard {
        Text(label, style = MaterialTheme.typography.labelLarge, color = accent)
        Spacer(Modifier.height(6.dp))
        if (snapshot == null) {
            Text("Not run yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        } else {
            MetricRow("Avg FPS", snapshot.averageFps) { "%.0f".format(it) }
            MetricRow("Stability", snapshot.stabilityPercent) { "%.0f%%".format(it) }
            MetricRow("RAM Usage", snapshot.ramUsagePercent) { "%.0f%%".format(it) }
        }
    }
}
