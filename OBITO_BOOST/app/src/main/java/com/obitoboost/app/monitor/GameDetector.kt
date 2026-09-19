package com.obitoboost.app.monitor

import android.content.Context
import android.content.pm.PackageManager
import com.obitoboost.app.data.database.GameDatabase
import com.obitoboost.app.data.model.InstalledGame

class GameDetector(private val context: Context) {

    /**
     * Checks each entry in GameDatabase against installed packages.
     * We never read a game's internal settings/files — only whether Android
     * reports the package as installed, which is all a normal app is allowed to see.
     */
    fun detectInstalledGames(): List<InstalledGame> {
        val pm = context.packageManager
        return GameDatabase.games.map { entry ->
            val installed = try {
                pm.getPackageInfo(entry.packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            InstalledGame(entry = entry, isInstalled = installed)
        }
    }
}
