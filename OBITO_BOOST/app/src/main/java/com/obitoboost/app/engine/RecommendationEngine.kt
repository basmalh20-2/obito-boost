package com.obitoboost.app.engine

import com.obitoboost.app.data.model.*

/**
 * The "brain" of OBITO BOOST. Turns (device tier + measured performance +
 * user-selected profile) into a personalized set of game settings and safe
 * optimization actions — never a one-size-fits-all list.
 */
class RecommendationEngine {

    fun buildProfile(
        device: DeviceInfo,
        game: GameEntry?,
        profileType: GamingProfileType,
        latestPerformance: PerformanceSnapshot?
    ): OptimizationProfile {
        val baseTier = device.performanceTier
        val effectiveTier = adjustTierForProfile(baseTier, profileType)

        val recommendations = mutableListOf<GameSettingsRecommendation>()
        if (game != null) {
            val base = game.recommendationsByTier[effectiveTier]
                ?: game.recommendationsByTier[baseTier]
            base?.let { recommendations.add(refineForPerformance(it, latestPerformance)) }
        }

        val actions = buildOptimizationActions(device, profileType, latestPerformance)

        return OptimizationProfile(
            id = "profile_${System.currentTimeMillis()}",
            name = "OBITO BOOST — ${profileType.displayName}",
            profileType = profileType,
            deviceModel = "${device.manufacturer} ${device.model}",
            createdAtMs = System.currentTimeMillis(),
            recommendations = recommendations,
            optimizationActions = actions
        )
    }

    /** MAX FPS / Competitive push the *effective* tier down one notch (lighter settings);
     *  Graphics Quality pushes it up one notch, within the bounds of what the device can plausibly handle. */
    private fun adjustTierForProfile(base: PerformanceTier, profile: GamingProfileType): PerformanceTier {
        val order = listOf(
            PerformanceTier.LOW_END, PerformanceTier.ENTRY_MID,
            PerformanceTier.MID_RANGE, PerformanceTier.HIGH_END
        )
        val index = order.indexOf(base)
        return when (profile) {
            GamingProfileType.MAX_FPS, GamingProfileType.COMPETITIVE ->
                order[(index - 1).coerceAtLeast(0)]
            GamingProfileType.GRAPHICS_QUALITY ->
                order[(index + 1).coerceAtMost(order.size - 1)]
            GamingProfileType.LOW_END_DEVICE ->
                PerformanceTier.LOW_END
            GamingProfileType.BALANCED ->
                base
        }
    }

    /** If we have real measured performance showing instability, bias settings down regardless of profile — and label why. */
    private fun refineForPerformance(
        base: GameSettingsRecommendation,
        performance: PerformanceSnapshot?
    ): GameSettingsRecommendation {
        val stability = performance?.stabilityPercent?.valueOrNull()
        if (stability != null && stability < 70.0) {
            return base.copy(
                shadows = "Off",
                antiAliasing = "Off",
                effects = "Low",
                basis = RecommendationBasis.PERFORMANCE_BASED
            )
        }
        return base
    }

    private fun buildOptimizationActions(
        device: DeviceInfo,
        profile: GamingProfileType,
        performance: PerformanceSnapshot?
    ): List<OptimizationAction> {
        val actions = mutableListOf<OptimizationAction>()

        actions += OptimizationAction(
            id = "battery_optimization",
            title = "Disable battery optimization for OBITO BOOST & your game",
            description = "Lets the game keep running at full priority in the background without Android throttling it to save power.",
            canApplyDirectly = false,
            settingsIntentAction = "android.settings.APPLICATION_DETAILS_SETTINGS"
        )

        actions += OptimizationAction(
            id = "background_apps",
            title = "Review high-RAM background apps",
            description = "OBITO BOOST lists installed apps by memory footprint so you can close ones you don't need before playing — nothing is closed automatically without your confirmation.",
            canApplyDirectly = true
        )

        if (device.refreshRateHz >= 90f) {
            actions += OptimizationAction(
                id = "refresh_rate",
                title = "Confirm display refresh rate",
                description = "Your screen supports ${device.refreshRateHz.toInt()}Hz. Make sure it's not locked to 60Hz in system display settings for supported games.",
                canApplyDirectly = false,
                settingsIntentAction = "android.settings.DISPLAY_SETTINGS"
            )
        }

        val storageAvailGb = device.storageAvailableGb.valueOrNull()
        if (storageAvailGb != null && storageAvailGb < 5.0) {
            actions += OptimizationAction(
                id = "storage_cleanup",
                title = "Free up storage",
                description = "Only ${"%.1f".format(storageAvailGb)} GB free. Low storage can cause stutter and slow asset loading in games.",
                canApplyDirectly = false,
                settingsIntentAction = "android.settings.INTERNAL_STORAGE_SETTINGS"
            )
        }

        val thermal = device.thermalStatus.valueOrNull()
        if (thermal != null && thermal != "Normal") {
            actions += OptimizationAction(
                id = "thermal_warning",
                title = "Device is running warm ($thermal)",
                description = "Consider a short break, remove the case, or avoid direct sunlight before your next session to reduce thermal throttling risk.",
                canApplyDirectly = true
            )
        }

        if (profile == GamingProfileType.LOW_END_DEVICE || device.performanceTier == PerformanceTier.LOW_END) {
            actions += OptimizationAction(
                id = "low_end_mode",
                title = "Enable Android's built-in Battery Saver during play",
                description = "Counterintuitively, on low-RAM devices this can reduce background churn; test with and without to see which feels smoother for you.",
                canApplyDirectly = false,
                settingsIntentAction = "android.settings.BATTERY_SAVER_SETTINGS"
            )
        }

        return actions
    }
}
