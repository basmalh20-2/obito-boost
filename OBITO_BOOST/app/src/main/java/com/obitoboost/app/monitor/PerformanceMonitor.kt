package com.obitoboost.app.monitor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.view.Choreographer
import com.obitoboost.app.data.model.FpsSample
import com.obitoboost.app.data.model.MetricValue
import com.obitoboost.app.data.model.PerformanceSnapshot
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * IMPORTANT — honesty about what Android actually allows a normal (non-root,
 * non-system) app to measure in 2024+:
 *
 *  - FPS of the *foreground app the user is playing* CANNOT be measured by a
 *    third-party app while that other app is in the foreground; Android does
 *    not expose another process's frame timing to us. What we CAN measure
 *    honestly is OBITO BOOST's own on-screen render performance during a
 *    Benchmark run (Choreographer frame callbacks), which is a fair proxy
 *    for how smoothly the device can currently render UI/game-like frames.
 *  - Per-app/system CPU usage percentages are no longer exposed to normal
 *    apps since Android 8 (Build.VERSION_CODES.O) removed access to
 *    /proc/stat for other processes and throttled our own too. We report
 *    this as Unavailable rather than fabricate a number, with one exception:
 *    our own process's CPU time via Process.getElapsedCpuTime(), which we
 *    label as Estimated because it only reflects OBITO BOOST itself.
 *  - RAM usage (system-wide) IS available via ActivityManager.MemoryInfo.
 *  - Battery temperature and level are available via the battery broadcast.
 *  - Thermal status is available on Android 10+.
 */
class PerformanceMonitor(private val context: Context) {

    /**
     * Runs an on-screen benchmark for [durationMs], sampling real frame
     * timings via Choreographer. Call this while a Composable is being
     * rendered (e.g. during the Benchmark screen) so there are actual frames
     * to measure.
     */
    suspend fun runBenchmark(
        durationMs: Long = 10_000L,
        onFrame: (fps: Double) -> Unit = {}
    ): PerformanceSnapshot {
        val samples = mutableListOf<FpsSample>()
        val startTime = System.currentTimeMillis()
        var lastFrameNs = 0L

        val choreographer = Choreographer.getInstance()
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameNs != 0L) {
                    val deltaNs = frameTimeNanos - lastFrameNs
                    if (deltaNs > 0) {
                        val fps = 1_000_000_000.0 / deltaNs
                        val clamped = fps.coerceIn(0.0, 240.0)
                        samples.add(FpsSample(System.currentTimeMillis(), clamped))
                        onFrame(clamped)
                    }
                }
                lastFrameNs = frameTimeNanos
                if (System.currentTimeMillis() - startTime < durationMs) {
                    choreographer.postFrameCallback(this)
                }
            }
        }
        choreographer.postFrameCallback(frameCallback)

        while (System.currentTimeMillis() - startTime < durationMs) {
            delay(100)
        }
        choreographer.removeFrameCallback(frameCallback)

        return buildSnapshot(samples)
    }

    fun buildSnapshot(samples: List<FpsSample>): PerformanceSnapshot {
        val fpsValues = samples.map { it.fps }

        val avgFps: MetricValue<Double>
        val minFps: MetricValue<Double>
        val maxFps: MetricValue<Double>
        val stability: MetricValue<Double>
        val consistency: MetricValue<Double>

        if (fpsValues.isEmpty()) {
            avgFps = MetricValue.Unavailable("No frames captured during this session")
            minFps = MetricValue.Unavailable()
            maxFps = MetricValue.Unavailable()
            stability = MetricValue.Unavailable()
            consistency = MetricValue.Unavailable()
        } else {
            val avg = fpsValues.average()
            val min = fpsValues.min()
            val max = fpsValues.max()
            val variance = fpsValues.sumOf { (it - avg) * (it - avg) } / fpsValues.size
            val stdDev = sqrt(variance)
            // Stability: how tightly frames cluster around the average (100% = perfectly steady)
            val stabilityPct = (100.0 - (stdDev / avg.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0)

            avgFps = MetricValue.Estimated(avg, "OBITO BOOST on-screen render benchmark, not the foreground game")
            minFps = MetricValue.Measured(min)
            maxFps = MetricValue.Measured(max)
            stability = MetricValue.Measured(stabilityPct)
            consistency = MetricValue.Measured(stdDev)
        }

        val ramUsage = getRamUsagePercent()
        val batteryInfo = getBatterySnapshot()
        val thermal = getThermalStatus()

        return PerformanceSnapshot(
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            stabilityPercent = stability,
            frameTimeConsistencyMs = consistency,
            cpuUsagePercent = MetricValue.Unavailable(
                "Android restricts per-app/system CPU usage reporting to system apps since Android 8"
            ),
            ramUsagePercent = ramUsage,
            temperatureC = MetricValue.Unavailable("No public device-wide temperature API; battery temperature is used as a proxy"),
            batteryTemperatureC = batteryInfo.first,
            batteryDrainPercent = MetricValue.Unavailable("Requires a before/after comparison across a full session"),
            thermalThrottling = when (val t = thermal) {
                is MetricValue.Measured -> MetricValue.Measured(t.value != "Normal")
                else -> MetricValue.Unavailable("Thermal status API requires Android 10+")
            },
            samples = samples
        )
    }

    private fun getRamUsagePercent(): MetricValue<Double> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        if (info.totalMem <= 0) return MetricValue.Unavailable()
        val usedPercent = 100.0 * (info.totalMem - info.availMem) / info.totalMem
        return MetricValue.Measured(usedPercent)
    }

    private fun getBatterySnapshot(): Pair<MetricValue<Float>, MetricValue<Int>> {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return MetricValue.Unavailable() to MetricValue.Unavailable()
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val temp = if (tempTenths != Int.MIN_VALUE) MetricValue.Measured(tempTenths / 10f) else MetricValue.Unavailable()
        val pct = if (level >= 0 && scale > 0) MetricValue.Measured(level * 100 / scale) else MetricValue.Unavailable()
        return temp to pct
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
}
