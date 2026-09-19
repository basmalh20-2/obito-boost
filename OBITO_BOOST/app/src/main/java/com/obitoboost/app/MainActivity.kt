package com.obitoboost.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.obitoboost.app.ui.Screen
import com.obitoboost.app.ui.screens.*
import com.obitoboost.app.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ObitoBoostTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    ObitoBoostApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObitoBoostApp(viewModel: AppViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route
    val currentLabel = Screen.drawerItems.find { it.route == currentRoute }?.label ?: "OBITO BOOST"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = NavySurface) {
                Column(Modifier.padding(20.dp)) {
                    Text("OBITO BOOST", style = MaterialTheme.typography.headlineMedium, color = CyanGlow)
                    Text("Gaming Performance Suite", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                HorizontalDivider(color = NavyCardBorder)
                Screen.drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.label, color = TextPrimary) },
                        icon = { Icon(screen.icon, contentDescription = null, tint = if (currentRoute == screen.route) CyanGlow else TextSecondary) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                popUpTo(Screen.Home.route) { saveState = true }
                                restoreState = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = CyanGlow.copy(alpha = 0.12f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = NavyDeep,
            topBar = {
                TopAppBar(
                    title = { Text(currentLabel, color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = CyanGlow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                composable(Screen.Home.route) { HomeScreen(viewModel, navController) }
                composable(Screen.DeviceAnalysis.route) { DeviceAnalysisScreen(viewModel) }
                composable(Screen.Performance.route) { PerformanceScreen(viewModel) }
                composable(Screen.Games.route) { GamesScreen(viewModel) }
                composable(Screen.SmartBoost.route) { SmartBoostScreen(viewModel) }
                composable(Screen.Profiles.route) { ProfilesScreen(viewModel) }
                composable(Screen.Benchmark.route) { BenchmarkScreen(viewModel) }
                composable(Screen.Backup.route) { BackupScreen(viewModel) }
                composable(Screen.Export.route) { ExportScreen(viewModel) }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}
