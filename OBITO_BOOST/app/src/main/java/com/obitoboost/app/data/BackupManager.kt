package com.obitoboost.app.data

import android.content.Context
import com.obitoboost.app.data.model.BackupEntry
import com.obitoboost.app.util.JsonUtil
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Honesty note: a normal (non-root, no WRITE_SECURE_SETTINGS grant) Android
 * app cannot read or write most protected system settings. So what OBITO
 * BOOST actually backs up/restores is its OWN state — the recommended
 * settings values it generated and any in-app toggle values — not real
 * Android system settings. This is made explicit in the Backup screen UI.
 */
class BackupManager(context: Context) {
    private val prefs = context.getSharedPreferences("obito_boost_backups", Context.MODE_PRIVATE)
    private val key = "backups"
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun getAll(): List<BackupEntry> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i -> JsonUtil.backupFromJson(arr.getJSONObject(i)) }
    }

    fun createBackup(settings: Map<String, String>, label: String? = null): BackupEntry {
        val now = System.currentTimeMillis()
        val entry = BackupEntry(
            id = "backup_$now",
            createdAtMs = now,
            label = label ?: "Backup — ${dateFormat.format(Date(now))}",
            restorableSettings = settings
        )
        val current = getAll().toMutableList()
        current.add(0, entry)
        persist(current)
        return entry
    }

    fun delete(id: String) {
        persist(getAll().filterNot { it.id == id })
    }

    /** Returns the settings map to apply back; the caller (BackupScreen) is
     *  responsible for actually re-applying each value through the same
     *  in-app mechanisms used to set it originally. */
    fun restore(id: String): Map<String, String>? = getAll().find { it.id == id }?.restorableSettings

    private fun persist(entries: List<BackupEntry>) {
        val arr = JSONArray(entries.map { JsonUtil.backupToJson(it) })
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
