package com.obitoboost.app.data.model

/**
 * OBITO BOOST never silently guesses. Every metric that could be limited by
 * Android's sandboxing is wrapped in this type so the UI can label it
 * correctly instead of presenting an estimate as a hard measurement.
 */
sealed class MetricValue<out T> {
    data class Measured<T>(val value: T) : MetricValue<T>()
    data class Estimated<T>(val value: T, val reason: String) : MetricValue<T>()
    data class Unavailable(val reason: String = "Not available on this device/Android version") : MetricValue<Nothing>()

    fun valueOrNull(): T? = when (this) {
        is Measured -> value
        is Estimated -> value
        is Unavailable -> null
    }

    val label: String get() = when (this) {
        is Measured -> "Measured"
        is Estimated -> "Estimated"
        is Unavailable -> "Unavailable"
    }
}

enum class PerformanceTier(val displayName: String) {
    LOW_END("Low-End"),
    ENTRY_MID("Entry / Mid-Range"),
    MID_RANGE("Mid-Range"),
    HIGH_END("High-End")
}

enum class GamingProfileType(val displayName: String, val tagline: String) {
    MAX_FPS("MAX FPS", "Maximum frame rate for competitive play"),
    BALANCED("Balanced Gaming", "Balances FPS, temperature, battery and graphics"),
    GRAPHICS_QUALITY("Graphics Quality", "Prioritizes visual fidelity"),
    LOW_END_DEVICE("Low-End Device", "Optimized for limited hardware"),
    COMPETITIVE("Competitive Gaming", "Stability and input responsiveness first")
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: MetricValue<String>,
    val cpuAbi: String,
    val cpuCores: Int,
    val cpuInfoRaw: MetricValue<String>,
    val ramTotalMb: MetricValue<Long>,
    val ramAvailableMb: MetricValue<Long>,
    val glRenderer: MetricValue<String>,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val refreshRateHz: Float,
    val densityDpi: Int,
    val storageTotalGb: MetricValue<Double>,
    val storageAvailableGb: MetricValue<Double>,
    val batteryPercent: MetricValue<Int>,
    val batteryTemperatureC: MetricValue<Float>,
    val isCharging: Boolean,
    val thermalStatus: MetricValue<String>,
    val performanceTier: PerformanceTier
)

data class FpsSample(val timestampMs: Long, val fps: Double)

data class PerformanceSnapshot(
    val averageFps: MetricValue<Double>,
    val minFps: MetricValue<Double>,
    val maxFps: MetricValue<Double>,
    val stabilityPercent: MetricValue<Double>,
    val frameTimeConsistencyMs: MetricValue<Double>,
    val cpuUsagePercent: MetricValue<Double>,
    val ramUsagePercent: MetricValue<Double>,
    val temperatureC: MetricValue<Float>,
    val batteryTemperatureC: MetricValue<Float>,
    val batteryDrainPercent: MetricValue<Int>,
    val thermalThrottling: MetricValue<Boolean>,
    val samples: List<FpsSample> = emptyList(),
    val capturedAtMs: Long = System.currentTimeMillis()
)

data class GameEntry(
    val displayName: String,
    val packageName: String,
    val supportedGraphicsOptions: List<String>,
    val supportedFpsOptions: List<String>,
    val recommendationsByTier: Map<PerformanceTier, GameSettingsRecommendation>
)

data class GameSettingsRecommendation(
    val graphicsQuality: String,
    val fpsOption: String,
    val shadows: String,
    val antiAliasing: String,
    val effects: String,
    val textureQuality: String,
    val basis: RecommendationBasis
)

enum class RecommendationBasis(val explanation: String) {
    DEVICE_DETECTED("Directly detected from the device/game"),
    SPEC_BASED("Recommended based on device specifications"),
    PERFORMANCE_BASED("Recommended based on measured performance")
}

data class InstalledGame(val entry: GameEntry, val isInstalled: Boolean)

data class OptimizationAction(
    val id: String,
    val title: String,
    val description: String,
    val canApplyDirectly: Boolean,
    val settingsIntentAction: String? = null // Android Settings action to deep-link to, if not directly applicable
)

data class OptimizationProfile(
    val id: String,
    val name: String,
    val profileType: GamingProfileType,
    val deviceModel: String,
    val createdAtMs: Long,
    val recommendations: List<GameSettingsRecommendation>,
    val optimizationActions: List<OptimizationAction>
)

data class BenchmarkComparison(
    val before: PerformanceSnapshot,
    val after: PerformanceSnapshot
) {
    fun fpsDeltaPercent(): MetricValue<Double> {
        val b = before.averageFps.valueOrNull()
        val a = after.averageFps.valueOrNull()
        return if (b != null && a != null && b > 0.0) {
            MetricValue.Measured(((a - b) / b) * 100.0)
        } else {
            MetricValue.Unavailable("Insufficient measured data to compute a valid percentage")
        }
    }
}

data class BackupEntry(
    val id: String,
    val createdAtMs: Long,
    val label: String,
    val restorableSettings: Map<String, String>
)
