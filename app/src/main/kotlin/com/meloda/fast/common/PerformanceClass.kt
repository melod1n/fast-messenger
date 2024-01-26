package com.meloda.fast.common

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.meloda.fast.BuildConfig
import java.io.RandomAccessFile
import java.util.Locale
import kotlin.math.ceil

sealed class PerformanceClass {


    data object Low : PerformanceClass()
    data object Average : PerformanceClass()
    data object High : PerformanceClass()


    companion object {

        var devicePerformanceClass: PerformanceClass? = null

        fun getDevicePerformanceClass(applicationContext: Context): PerformanceClass {
            val performanceClass = devicePerformanceClass ?: run {
                val androidVersion = Build.VERSION.SDK_INT
                val cpuCount = Runtime.getRuntime().availableProcessors()
                val memoryClass =
                    (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
                var totalCpuFreq = 0
                var freqResolved = 0
                for (i in 0 until cpuCount) {
                    try {
                        val reader = RandomAccessFile(
                            String.format(
                                Locale.ENGLISH,
                                "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq",
                                i
                            ), "r"
                        )
                        val line = reader.readLine()
                        if (line != null) {
                            totalCpuFreq += line.toInt() / 1000
                            freqResolved++
                        }
                        reader.close()
                    } catch (ignore: Throwable) {
                    }
                }
                val maxCpuFreq =
                    if (freqResolved == 0) {
                        -1
                    } else {
                        ceil((totalCpuFreq / freqResolved.toFloat()).toDouble()).toInt()
                    }

                // TODO: 26/01/2024, Danil Nikolaev: review
                val perfClass =
                    if (androidVersion < 21 || cpuCount <= 2 || memoryClass <= 100 || cpuCount <= 4 && maxCpuFreq != -1 && maxCpuFreq <= 1250 || cpuCount <= 4 && maxCpuFreq <= 1600 && memoryClass <= 128 && androidVersion <= 21 || cpuCount <= 4 && maxCpuFreq <= 1300 && memoryClass <= 128 && androidVersion <= 24) {
                        Low
                    } else if (cpuCount < 8 || memoryClass <= 160 || maxCpuFreq != -1 && maxCpuFreq <= 2050 || maxCpuFreq == -1 && cpuCount == 8 && androidVersion <= 23) {
                        Average
                    } else {
                        High
                    }

                devicePerformanceClass = perfClass

                if (BuildConfig.DEBUG) {
                    Log.d(
                        "PerformanceClass",
                        "device performance info (cpu_count = $cpuCount, freq = $maxCpuFreq, memoryClass = $memoryClass, android version $androidVersion)"
                    )
                    Log.d("PerformanceClass", "device performance class: $perfClass")
                }

                perfClass
            }

            return performanceClass
        }
    }
}
