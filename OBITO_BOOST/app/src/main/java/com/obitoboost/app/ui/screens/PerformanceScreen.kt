package com.obitoboost.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obitoboost.app.AppViewModel
import com.obitoboost.app.data.model.PerformanceSnapshot
import com.obitoboost.app.ui.components.*
import com.obitoboost.app.ui.theme.*

@Composable
fun PerformanceScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot: PerformanceSnapshot? = state.afterSnapshot ?: state.beforeSnapshot

    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader("Gaming Performance", "Results from your most recent OBITO BOOST benchmark run")

        if (snapshot == null) {
            ObitoCard {
                Text(
                    "No benchmark run yet. Head to the Benchmark screen to measure real on-device frame performance.",
                    color = TextSecondary
                )
            }
            return@Column
        }

        ObitoCard {
            MetricRow("Average FPS", snapshot.averageFps) { "%.1f".format(it) }
            MetricRow("Minimum FPS", snapshot.minFps) { "%.1f".format(it) }
            MetricRow("Maximum FPS", snapshot.maxFps) { "%.1f".format(it) }
            MetricRow("Stability", snapshot.stabilityPercent) { "%.0f%%".format(it) }
            MetricRow("Frame-time Consistency", snapshot.frameTimeConsistencyMs) { "±%.1f ms".format(it) }
        }

        Spacer(Modifier.height(16.dp))
        ObitoCard {
            Text("Resource Usage", style = MaterialTheme.typography.titleLarge, color = Gold)
            Spacer(Modifier.height(8.dp))
            MetricRow("CPU Usage", snapshot.cpuUsagePercent) { "%.0f%%".format(it) }
            MetricRow("RAM Usage", snapshot.ramUsagePercent) { "%.0f%%".format(it) }
            MetricRow("Device Temperature", snapshot.temperatureC) { "%.1f°C".format(it) }
            MetricRow("Battery Temperature", snapshot.batteryTemperatureC) { "%.1f°C".format(it) }
            MetricRow("Thermal Throttling", snapshot.thermalThrottling) { if (it) "Yes" else "No" }
        }

        Spacer(Modifier.height(16.dp))
        ObitoCard {
            Text(
                "Why is CPU usage unavailable?",
                style = MaterialTheme.typography.titleMedium, color = CyanGlow
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Since Android 8, apps can no longer read per-app or system-wide CPU usage from other processes — this is an OS-level privacy restriction, not a limitation of OBITO BOOST. We show \"N/A\" instead of a fabricated number.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
