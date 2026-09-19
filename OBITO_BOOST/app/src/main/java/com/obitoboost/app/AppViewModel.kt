package com.obitoboost.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.obitoboost.app.data.BackupManager
import com.obitoboost.app.data.ProfileManager
import com.obitoboost.app.data.model.*
import com.obitoboost.app.engine.RecommendationEngine
import com.obitoboost.app.monitor.DeviceInfoCollector
import com.obitoboost.app.monitor.GameDetector
import com.obitoboost.app.monitor.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUiState(
    val deviceInfo: DeviceInfo? = null,
    val installedGames: List<InstalledGame> = emptyList(),
    val selectedGame: GameEntry? = null,
    val selectedProfileType: GamingProfileType = GamingProfileType.BALANCED,
    val currentProfile: OptimizationProfile? = null,
    val savedProfiles: List<OptimizationProfile> = emptyList(),
    val backups: List<BackupEntry> = emptyList(),
    val beforeSnapshot: PerformanceSnapshot? = null,
    val afterSnapshot: PerformanceSnapshot? = null,
    val isBenchmarking: Boolean = false,
    val liveFps: Double? = null
) {
    val comparison: BenchmarkComparison?
        get() {
            val b = beforeSnapshot
            val a = afterSnapshot
            return if (b != null && a != null) BenchmarkComparison(b, a) else null
        }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceInfoCollector = DeviceInfoCollector(application)
    private val gameDetector = GameDetector(application)
    private val performanceMonitor = PerformanceMonitor(application)
    private val recommendationEngine = RecommendationEngine()
    private val profileManager = ProfileManager(application)
    private val backupManager = BackupManager(application)

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        refreshDeviceAndGames()
        _uiState.value = _uiState.value.copy(
            savedProfiles = profileManager.getAll(),
            backups = backupManager.getAll()
        )
    }

    fun refreshDeviceAndGames() {
        val device = deviceInfoCollector.collect()
        val games = gameDetector.detectInstalledGames()
        _uiState.value = _uiState.value.copy(
            deviceInfo = device,
            installedGames = games,
            selectedGame = _uiState.value.selectedGame ?: games.firstOrNull { it.isInstalled }?.entry
        )
    }

    fun selectGame(game: GameEntry) {
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }

    fun selectProfileType(type: GamingProfileType) {
        _uiState.value = _uiState.value.copy(selectedProfileType = type)
    }

    fun generateRecommendation() {
        val state = _uiState.value
        val device = state.deviceInfo ?: return
        val profile = recommendationEngine.buildProfile(
            device = device,
            game = state.selectedGame,
            profileType = state.selectedProfileType,
            latestPerformance = state.afterSnapshot ?: state.beforeSnapshot
        )
        _uiState.value = state.copy(currentProfile = profile)
    }

    fun saveCurrentProfile() {
        val profile = _uiState.value.currentProfile ?: return
        profileManager.save(profile)
        _uiState.value = _uiState.value.copy(savedProfiles = profileManager.getAll())
    }

    fun deleteSavedProfile(id: String) {
        profileManager.delete(id)
        _uiState.value = _uiState.value.copy(savedProfiles = profileManager.getAll())
    }

    fun runBenchmark(isBefore: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBenchmarking = true, liveFps = null)
            val snapshot = performanceMonitor.runBenchmark(durationMs = 8000) { fps ->
                _uiState.value = _uiState.value.copy(liveFps = fps)
            }
            _uiState.value = if (isBefore) {
                _uiState.value.copy(beforeSnapshot = snapshot, isBenchmarking = false, liveFps = null)
            } else {
                _uiState.value.copy(afterSnapshot = snapshot, isBenchmarking = false, liveFps = null)
            }
        }
    }

    fun createBackup(label: String? = null) {
        val profile = _uiState.value.currentProfile
        val settings = mutableMapOf<String, String>()
        settings["profileType"] = _uiState.value.selectedProfileType.name
        profile?.recommendations?.firstOrNull()?.let { r ->
            settings["graphicsQuality"] = r.graphicsQuality
            settings["fpsOption"] = r.fpsOption
            settings["shadows"] = r.shadows
            settings["antiAliasing"] = r.antiAliasing
            settings["effects"] = r.effects
            settings["textureQuality"] = r.textureQuality
        }
        backupManager.createBackup(settings, label)
        _uiState.value = _uiState.value.copy(backups = backupManager.getAll())
    }

    fun deleteBackup(id: String) {
        backupManager.delete(id)
        _uiState.value = _uiState.value.copy(backups = backupManager.getAll())
    }

    /** Returns the restored key/value settings so the Backup screen can display them;
     *  actually re-applying them into a live profile is left to the user via Smart Boost,
     *  since OBITO BOOST does not have write access to protected system settings. */
    fun restoreBackup(id: String): Map<String, String>? = backupManager.restore(id)
}
