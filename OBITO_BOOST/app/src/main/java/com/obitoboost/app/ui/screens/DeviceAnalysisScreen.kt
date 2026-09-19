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
import com.obitoboost.app.ui.components.*
import com.obitoboost.app.ui.theme.*

@Composable
fun DeviceAnalysisScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val device = state.deviceInfo

    Column(
        modifier = Modifier.fillMaxSize().obitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        SectionHeader("Device Analysis", "Everything Android's public APIs expose about your hardware")

        if (device == null) {
            ObitoCard { Text("Scanning device…", color = TextSecondary) }
            return@Column
        }

        SectionSubHeader("System")
        ObitoCard {
            InfoRow("Manufacturer", device.manufacturer)
            InfoRow("Model", device.model)
            InfoRow("Android Version", device.androidVersion)
            InfoRow("SDK Level", device.sdkInt.toString())
            MetricRow("Security Patch", device.securityPatch) { it }
        }

        Spacer(Modifier.height(16.dp))
        SectionSubHeader("CPU")
        ObitoCard {
            InfoRow("Architecture (ABI)", device.cpuAbi)
            InfoRow("Cores", device.cpuCores.toString())
            MetricRow("Raw /proc/cpuinfo Summary", device.cpuInfoRaw) { it.lines().firstOrNull() ?: it }
        }

        Spacer(Modifier.height(16.dp))
        SectionSubHeader("Memory & Storage")
        ObitoCard {
            MetricRow("Total RAM", device.ramTotalMb) { "${it / 1024} GB" }
            MetricRow("Available RAM", device.ramAvailableMb) { "$it MB" }
            MetricRow("Total Storage", device.storageTotalGb) { "%.1f GB".format(it) }
            MetricRow("Available Storage", device.storageAvailableGb) { "%.1f GB".format(it) }
        }

        Spacer(Modifier.height(16.dp))
        SectionSubHeader("Display")
        ObitoCard {
            InfoRow("Resolution", "${device.screenWidthPx} × ${device.screenHeightPx} px")
            InfoRow("Refresh Rate", "${device.refreshRateHz} Hz")
            InfoRow("Density", "${device.densityDpi} dpi")
        }

        Spacer(Modifier.height(16.dp))
        SectionSubHeader("Battery & Thermal")
        ObitoCard {
            MetricRow("Battery Level", device.batteryPercent) { "$it%" }
            MetricRow("Battery Temperature", device.batteryTemperatureC) { "%.1f°C".format(it) }
            InfoRow("Charging", if (device.isCharging) "Yes" else "No")
            MetricRow("Thermal Status", device.thermalStatus) { it }
        }

        Spacer(Modifier.height(16.dp))
        SectionSubHeader("Performance Classification")
        ObitoCard {
            TierBadge(device.performanceTier.displayName)
            Spacer(Modifier.height(8.dp))
            Text(
                "Calculated from RAM, CPU core count, display refresh rate and Android version — not from the phone's marketing name.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionSubHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = Gold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    }
}
