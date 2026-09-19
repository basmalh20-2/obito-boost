package com.obitoboost.app.monitor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import com.obitoboost.app.data.model.DeviceInfo
import com.obitoboost.app.data.model.MetricValue
import com.obitoboost.app.data.model.PerformanceTier
import java.io.RandomAccessFile

/**
 * Collects everything Android's public APIs expose about the device.
 * Anything the OS does not expose (varies a lot by OEM/version) is returned
 * as MetricValue.Unavailable rather than guessed.
 */
class DeviceInfoCollector(private val context: Context) {

    fun collect(): DeviceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        val ramTotalMb = memInfo.totalMem / (1024 * 1024)
        val ramAvailMb = memInfo.availMem / (1024 * 1024)

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics().also {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(it)
        }
        @Suppress("DEPRECATION")
        val refreshRate = windowManager.defaultDisplay.refreshRate

        val statFs = StatFs(context.filesDir.path)
        val storageTotalGb = (statFs.blockCountLong * statFs.blockSizeLong) / (1024.0 * 1024 * 1024)
        val storageAvailGb = (statFs.availableBlocksLong * statFs.blockSizeLong) / (1024.0 * 1024 * 1024)

        val batteryIntent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryPct = batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100 / scale) else null
        }
        val batteryTempC = batteryIntent?.let {
            val tempTenths = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            if (tempTenths != Int.MIN_VALUE) tempTenths / 10f else null
        }
        val isCharging = batteryIntent?.let {
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false

        val thermalStatus = getThermalStatus()
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuInfo = readCpuInfoSummary()

        val tier = classifyTier(
            ramTotalMb = ramTotalMb,
            cpuCores = cpuCores,
            refreshRate = refreshRate,
            sdkInt = Build.VERSION.SDK_INT
        )

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                MetricValue.Measured(Build.VERSION.SECURITY_PATCH ?: "Unknown")
            else MetricValue.Unavailable("Requires Android 6.0+"),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            cpuCores = cpuCores,
            cpuInfoRaw = cpuInfo,
            ramTotalMb = MetricValue.Measured(ramTotalMb),
            ramAvailableMb = MetricValue.Measured(ramAvailMb),
            glRenderer = MetricValue.Unavailable("Read via a GL context on the Benchmark screen"),
            screenWidthPx = displayMetrics.widthPixels,
            screenHeightPx = displayMetrics.heightPixels,
            refreshRateHz = refreshRate,
            densityDpi = displayMetrics.densityDpi,
            storageTotalGb = MetricValue.Measured(storageTotalGb),
            storageAvailableGb = MetricValue.Measured(storageAvailGb),
            batteryPercent = batteryPct?.let { MetricValue.Measured(it) } ?: MetricValue.Unavailable(),
            batteryTemperatureC = batteryTempC?.let { MetricValue.Measured(it) } ?: MetricValue.Unavailable(),
            isCharging = isCharging,
            thermalStatus = thermalStatus,
            performanceTier = tier
        )
    }

    private fun getThermalStatus(): MetricValue<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val status = when (pm.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normal"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light throttling"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate throttling"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe throttling"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown imminent"
                else -> "Unknown"
            }
            MetricValue.Measured(status)
        } else {
            MetricValue.Unavailable("Thermal status API requires Android 10+")
        }
    }

    /** /proc/cpuinfo is world-readable on virtually all devices; treated as a supplementary summary, not a guarantee. */
    private fun readCpuInfoSummary(): MetricValue<String> {
        return try {
            val reader = RandomAccessFile("/proc/cpuinfo", "r")
            val lines = mutableListOf<String>()
            var line: String?
            var count = 0
            while (reader.readLine().also { line = it } != null && count < 6) {
                line?.let { lines.add(it) }
                count++
            }
            reader.close()
            if (lines.isEmpty()) MetricValue.Unavailable("/proc/cpuinfo not readable on this device")
            else MetricValue.Estimated(lines.joinToString("\n"), "Parsed from /proc/cpuinfo; format varies by chipset vendor")
        } catch (e: Exception) {
            MetricValue.Unavailable("/proc/cpuinfo not accessible on this device")
        }
    }

    private fun classifyTier(ramTotalMb: Long, cpuCores: Int, refreshRate: Float, sdkInt: Int): PerformanceTier {
        // Transparent, measurable heuristic — documented so it can be tuned later.
        var score = 0
        score += when {
            ramTotalMb >= 8192 -> 4
            ramTotalMb >= 6144 -> 3
            ramTotalMb >= 4096 -> 2
            else -> 1
        }
        score += when {
            cpuCores >= 8 -> 3
            cpuCores >= 6 -> 2
            else -> 1
        }
        score += when {
            refreshRate >= 120f -> 2
            refreshRate >= 90f -> 1
            else -> 0
        }
        score += if (sdkInt >= Build.VERSION_CODES.TIRAMISU) 1 else 0

        return when {
            score >= 9 -> PerformanceTier.HIGH_END
            score >= 6 -> PerformanceTier.MID_RANGE
            score >= 4 -> PerformanceTier.ENTRY_MID
            else -> PerformanceTier.LOW_END
        }
    }
}
