package com.obitoboost.app.data.database

import com.obitoboost.app.data.model.GameEntry
import com.obitoboost.app.data.model.GameSettingsRecommendation
import com.obitoboost.app.data.model.PerformanceTier
import com.obitoboost.app.data.model.RecommendationBasis

/**
 * Central, structured game database. Add a new game by appending a new
 * GameEntry — nothing else in the app needs to change (GameDetector and the
 * recommendation engine both read from this list).
 */
object GameDatabase {

    val games: List<GameEntry> = listOf(
        GameEntry(
            displayName = "Free Fire",
            packageName = "com.dts.freefireth",
            supportedGraphicsOptions = listOf("Low", "Medium", "High", "Ultra HDR"),
            supportedFpsOptions = listOf("30", "60", "90", "120 (device-dependent)"),
            recommendationsByTier = mapOf(
                PerformanceTier.LOW_END to GameSettingsRecommendation(
                    graphicsQuality = "Low", fpsOption = "30", shadows = "Off",
                    antiAliasing = "Off", effects = "Low", textureQuality = "Low",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.ENTRY_MID to GameSettingsRecommendation(
                    graphicsQuality = "Medium", fpsOption = "30", shadows = "Off",
                    antiAliasing = "Off", effects = "Medium", textureQuality = "Medium",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.MID_RANGE to GameSettingsRecommendation(
                    graphicsQuality = "High", fpsOption = "60", shadows = "On",
                    antiAliasing = "On", effects = "High", textureQuality = "High",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.HIGH_END to GameSettingsRecommendation(
                    graphicsQuality = "Ultra HDR", fpsOption = "90-120", shadows = "On",
                    antiAliasing = "On", effects = "Ultra", textureQuality = "Ultra",
                    basis = RecommendationBasis.SPEC_BASED
                )
            )
        ),
        GameEntry(
            displayName = "Free Fire MAX",
            packageName = "com.dts.freefiremax",
            supportedGraphicsOptions = listOf("Low", "Medium", "High", "Ultra HDR"),
            supportedFpsOptions = listOf("30", "60", "90 (device-dependent)"),
            recommendationsByTier = mapOf(
                PerformanceTier.LOW_END to GameSettingsRecommendation(
                    graphicsQuality = "Low", fpsOption = "30", shadows = "Off",
                    antiAliasing = "Off", effects = "Low", textureQuality = "Low",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.ENTRY_MID to GameSettingsRecommendation(
                    graphicsQuality = "Medium", fpsOption = "30", shadows = "Off",
                    antiAliasing = "Off", effects = "Medium", textureQuality = "Medium",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.MID_RANGE to GameSettingsRecommendation(
                    graphicsQuality = "High", fpsOption = "60", shadows = "On",
                    antiAliasing = "On", effects = "High", textureQuality = "High",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.HIGH_END to GameSettingsRecommendation(
                    graphicsQuality = "Ultra HDR", fpsOption = "90", shadows = "On",
                    antiAliasing = "On", effects = "Ultra", textureQuality = "Ultra",
                    basis = RecommendationBasis.SPEC_BASED
                )
            )
        ),
        GameEntry(
            displayName = "PUBG Mobile (Global)",
            packageName = "com.tencent.ig",
            supportedGraphicsOptions = listOf("Smooth", "Balanced", "HD", "HDR", "Ultra HD"),
            supportedFpsOptions = listOf("Low", "Medium", "High", "Ultra", "Extreme (90)"),
            recommendationsByTier = mapOf(
                PerformanceTier.LOW_END to GameSettingsRecommendation(
                    graphicsQuality = "Smooth", fpsOption = "Medium", shadows = "Off",
                    antiAliasing = "Off", effects = "Low", textureQuality = "Low",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.ENTRY_MID to GameSettingsRecommendation(
                    graphicsQuality = "Balanced", fpsOption = "High", shadows = "Off",
                    antiAliasing = "Off", effects = "Medium", textureQuality = "Medium",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.MID_RANGE to GameSettingsRecommendation(
                    graphicsQuality = "HD", fpsOption = "Ultra", shadows = "On",
                    antiAliasing = "On", effects = "High", textureQuality = "High",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.HIGH_END to GameSettingsRecommendation(
                    graphicsQuality = "HDR", fpsOption = "Extreme (90)", shadows = "On",
                    antiAliasing = "On", effects = "Ultra", textureQuality = "Ultra HD",
                    basis = RecommendationBasis.SPEC_BASED
                )
            )
        ),
        GameEntry(
            displayName = "PUBG Mobile (India / BGMI-style build)",
            packageName = "com.pubg.imobile",
            supportedGraphicsOptions = listOf("Smooth", "Balanced", "HD", "HDR", "Ultra HD"),
            supportedFpsOptions = listOf("Low", "Medium", "High", "Ultra", "Extreme (90)"),
            recommendationsByTier = mapOf(
                PerformanceTier.LOW_END to GameSettingsRecommendation(
                    graphicsQuality = "Smooth", fpsOption = "Medium", shadows = "Off",
                    antiAliasing = "Off", effects = "Low", textureQuality = "Low",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.ENTRY_MID to GameSettingsRecommendation(
                    graphicsQuality = "Balanced", fpsOption = "High", shadows = "Off",
                    antiAliasing = "Off", effects = "Medium", textureQuality = "Medium",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.MID_RANGE to GameSettingsRecommendation(
                    graphicsQuality = "HD", fpsOption = "Ultra", shadows = "On",
                    antiAliasing = "On", effects = "High", textureQuality = "High",
                    basis = RecommendationBasis.SPEC_BASED
                ),
                PerformanceTier.HIGH_END to GameSettingsRecommendation(
                    graphicsQuality = "HDR", fpsOption = "Extreme (90)", shadows = "On",
                    antiAliasing = "On", effects = "Ultra", textureQuality = "Ultra HD",
                    basis = RecommendationBasis.SPEC_BASED
                )
            )
        )
    )

    fun findByPackage(packageName: String): GameEntry? = games.find { it.packageName == packageName }
}
