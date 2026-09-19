package com.obitoboost.app.util

import com.obitoboost.app.data.model.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Hand-rolled JSON (de)serialization for local persistence via SharedPreferences.
 * Kept dependency-free on purpose — this is beginner-friendly and avoids
 * pulling in Room/Gson/Moshi for what is a small amount of local data.
 */
object JsonUtil {

    fun profileToJson(profile: OptimizationProfile): JSONObject = JSONObject().apply {
        put("id", profile.id)
        put("name", profile.name)
        put("profileType", profile.profileType.name)
        put("deviceModel", profile.deviceModel)
        put("createdAtMs", profile.createdAtMs)
        put("recommendations", JSONArray(profile.recommendations.map { r ->
            JSONObject().apply {
                put("graphicsQuality", r.graphicsQuality)
                put("fpsOption", r.fpsOption)
                put("shadows", r.shadows)
                put("antiAliasing", r.antiAliasing)
                put("effects", r.effects)
                put("textureQuality", r.textureQuality)
                put("basis", r.basis.name)
            }
        }))
        put("optimizationActions", JSONArray(profile.optimizationActions.map { a ->
            JSONObject().apply {
                put("id", a.id)
                put("title", a.title)
                put("description", a.description)
                put("canApplyDirectly", a.canApplyDirectly)
                put("settingsIntentAction", a.settingsIntentAction ?: JSONObject.NULL)
            }
        }))
    }

    fun profileFromJson(json: JSONObject): OptimizationProfile {
        val recArray = json.getJSONArray("recommendations")
        val recommendations = (0 until recArray.length()).map { i ->
            val r = recArray.getJSONObject(i)
            GameSettingsRecommendation(
                graphicsQuality = r.getString("graphicsQuality"),
                fpsOption = r.getString("fpsOption"),
                shadows = r.getString("shadows"),
                antiAliasing = r.getString("antiAliasing"),
                effects = r.getString("effects"),
                textureQuality = r.getString("textureQuality"),
                basis = RecommendationBasis.valueOf(r.getString("basis"))
            )
        }
        val actionsArray = json.getJSONArray("optimizationActions")
        val actions = (0 until actionsArray.length()).map { i ->
            val a = actionsArray.getJSONObject(i)
            OptimizationAction(
                id = a.getString("id"),
                title = a.getString("title"),
                description = a.getString("description"),
                canApplyDirectly = a.getBoolean("canApplyDirectly"),
                settingsIntentAction = a.optString("settingsIntentAction").takeIf { it.isNotBlank() && it != "null" }
            )
        }
        return OptimizationProfile(
            id = json.getString("id"),
            name = json.getString("name"),
            profileType = GamingProfileType.valueOf(json.getString("profileType")),
            deviceModel = json.getString("deviceModel"),
            createdAtMs = json.getLong("createdAtMs"),
            recommendations = recommendations,
            optimizationActions = actions
        )
    }

    fun backupToJson(entry: BackupEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("createdAtMs", entry.createdAtMs)
        put("label", entry.label)
        put("restorableSettings", JSONObject(entry.restorableSettings as Map<*, *>))
    }

    fun backupFromJson(json: JSONObject): BackupEntry {
        val settingsJson = json.getJSONObject("restorableSettings")
        val settings = mutableMapOf<String, String>()
        settingsJson.keys().forEach { key -> settings[key] = settingsJson.getString(key) }
        return BackupEntry(
            id = json.getString("id"),
            createdAtMs = json.getLong("createdAtMs"),
            label = json.getString("label"),
            restorableSettings = settings
        )
    }
}
