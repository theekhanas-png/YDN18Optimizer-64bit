package com.yodhan18.ydn18optimizer

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

object DeviceInfoProvider {

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) model
        else "$manufacturer $model"
    }

    fun getProcessor(): String {
        return try {
            val br = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            var hardware = ""
            var processor = ""
            while (br.readLine().also { line = it } != null) {
                when {
                    line!!.startsWith("Hardware") -> hardware = line!!.substringAfter(":").trim()
                    line!!.startsWith("model name") && processor.isEmpty() ->
                        processor = line!!.substringAfter(":").trim()
                    line!!.startsWith("Processor") && processor.isEmpty() ->
                        processor = line!!.substringAfter(":").trim()
                }
            }
            br.close()
            when {
                hardware.isNotEmpty() -> hardware
                processor.isNotEmpty() -> processor
                else -> "Unknown CPU"
            }
        } catch (e: IOException) {
            Build.HARDWARE.ifEmpty { "Unknown CPU" }
        }
    }

    fun getTotalRamGb(context: Context): Float {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.totalMem / (1024f * 1024f * 1024f)
    }

    fun getTotalStorageGb(): Float {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val total = stat.blockCountLong * stat.blockSizeLong
            total / (1024f * 1024f * 1024f)
        } catch (e: Exception) {
            0f
        }
    }

    fun getStorageUsedPercent(): Int {
        return try {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            val used = total - free
            if (total == 0L) 0 else ((used * 100) / total).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun getRamUsedPercent(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        if (mi.totalMem == 0L) return 0
        val used = mi.totalMem - mi.availMem
        return ((used * 100) / mi.totalMem).toInt()
    }

    fun getCpuLoadPercent(): Int {
        return try {
            val stat1 = readCpuStat()
            Thread.sleep(200)
            val stat2 = readCpuStat()
            val idle1 = stat1[3]
            val idle2 = stat2[3]
            val total1 = stat1.sum()
            val total2 = stat2.sum()
            val totalDiff = total2 - total1
            val idleDiff = idle2 - idle1
            if (totalDiff == 0L) 0
            else (((totalDiff - idleDiff) * 100) / totalDiff).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            0
        }
    }

    private fun readCpuStat(): LongArray {
        val br = BufferedReader(FileReader("/proc/stat"))
        val line = br.readLine()
        br.close()
        val parts = line.trim().split("\\s+".toRegex())
        return LongArray(parts.size - 1) { parts[it + 1].toLong() }
    }

    fun calculateScore(cpuLoad: Int, ramLoad: Int, storageUsed: Int): Int {
        val score = 100 - (cpuLoad * 0.3 + ramLoad * 0.4 + storageUsed * 0.3)
        return score.toInt().coerceIn(0, 100)
    }
}
