package com.obitoboost.app.data

import android.content.Context
import com.obitoboost.app.data.model.OptimizationProfile
import com.obitoboost.app.util.JsonUtil
import org.json.JSONArray

class ProfileManager(context: Context) {
    private val prefs = context.getSharedPreferences("obito_boost_profiles", Context.MODE_PRIVATE)
    private val key = "saved_profiles"

    fun getAll(): List<OptimizationProfile> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i -> JsonUtil.profileFromJson(arr.getJSONObject(i)) }
    }

    fun save(profile: OptimizationProfile) {
        val current = getAll().filterNot { it.id == profile.id }.toMutableList()
        current.add(0, profile)
        persist(current)
    }

    fun delete(profileId: String) {
        persist(getAll().filterNot { it.id == profileId })
    }

    private fun persist(profiles: List<OptimizationProfile>) {
        val arr = JSONArray(profiles.map { JsonUtil.profileToJson(it) })
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
