package com.obitoboost.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Filled.FlashOn)
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object DeviceAnalysis : Screen("device_analysis", "Device Analysis", Icons.Filled.Memory)
    object Performance : Screen("performance", "Gaming Performance", Icons.Filled.Speed)
    object Games : Screen("games", "Games", Icons.Filled.SportsEsports)
    object SmartBoost : Screen("smart_boost", "Smart Boost", Icons.Filled.Bolt)
    object Profiles : Screen("profiles", "Profiles", Icons.Filled.Person)
    object Benchmark : Screen("benchmark", "Benchmark", Icons.Filled.BarChart)
    object Backup : Screen("backup", "Backup", Icons.Filled.Backup)
    object Export : Screen("export", "Export", Icons.Filled.Share)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val drawerItems = listOf(
            Home, DeviceAnalysis, Performance, Games, SmartBoost,
            Profiles, Benchmark, Backup, Export, Settings
        )
    }
}
