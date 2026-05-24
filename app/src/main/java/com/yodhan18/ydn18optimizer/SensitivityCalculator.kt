package com.yodhan18.ydn18optimizer

import android.os.Build

data class SensitivityProfile(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniper: Int,
    val fireButtonSize: Int,
    val dpi: Int,
    val brandGroup: String
)

object SensitivityCalculator {

    private val RAM_STEPS = intArrayOf(2, 3, 4, 6, 8, 12, 16)

    enum class BrandGroup(val label: String) {
        REDMI_POCO_XIAOMI("Redmi/Poco/Xiaomi/Mi"),
        VIVO_OPPO("Vivo/Oppo"),
        IQOO("iQOO"),
        ONEPLUS_SAMSUNG("OnePlus/Samsung"),
        INFINIX_REALME("Infinix/Realme")
    }

    fun detectBrandGroup(): BrandGroup {
        val mfr = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        return when {
            mfr.contains("xiaomi") || mfr.contains("redmi") || mfr.contains("poco") || mfr.contains("mi") -> BrandGroup.REDMI_POCO_XIAOMI
            mfr.contains("iqoo") -> BrandGroup.IQOO
            mfr.contains("vivo") || mfr.contains("oppo") -> BrandGroup.VIVO_OPPO
            mfr.contains("oneplus") || mfr.contains("samsung") -> BrandGroup.ONEPLUS_SAMSUNG
            mfr.contains("infinix") || mfr.contains("realme") -> BrandGroup.INFINIX_REALME
            model.contains("poco") || model.contains("redmi") -> BrandGroup.REDMI_POCO_XIAOMI
            else -> BrandGroup.REDMI_POCO_XIAOMI
        }
    }

    fun mapRam(ramGb: Float): Int {
        return RAM_STEPS.minByOrNull { Math.abs(it - ramGb) } ?: 4
    }

    fun calculate(ramGb: Float): SensitivityProfile {
        val group = detectBrandGroup()
        val mappedRam = mapRam(ramGb)
        val ramIdx = RAM_STEPS.indexOf(mappedRam)

        val generalBase: Int
        val dpiBase: Int
        val fireMap: IntArray
        val generalAdj: IntArray
        val dpiAdj = intArrayOf(8, 6, 4, 2, -2, -4, -6)

        when (group) {
            BrandGroup.REDMI_POCO_XIAOMI -> {
                generalBase = 180
                dpiBase = 456
                fireMap = intArrayOf(44, 43, 46, 48, 48, 50, 52)
                generalAdj = intArrayOf(12, 8, 4, 0, -4, -12, -18)
            }
            BrandGroup.VIVO_OPPO -> {
                generalBase = 172
                dpiBase = 460
                fireMap = intArrayOf(42, 42, 44, 46, 48, 48, 50)
                generalAdj = intArrayOf(16, 14, 10, 0, -4, -10, -12)
            }
            BrandGroup.IQOO -> {
                generalBase = 190
                dpiBase = 460
                fireMap = intArrayOf(42, 42, 44, 46, 48, 48, 50)
                generalAdj = intArrayOf(8, 6, -8, -10, -18, -20, -25)
            }
            BrandGroup.ONEPLUS_SAMSUNG -> {
                generalBase = 160
                dpiBase = 460
                fireMap = intArrayOf(42, 42, 44, 46, 48, 48, 50)
                generalAdj = intArrayOf(26, 24, 20, 15, 8, 5, 4)
            }
            BrandGroup.INFINIX_REALME -> {
                generalBase = 175
                dpiBase = 447
                fireMap = intArrayOf(42, 42, 44, 46, 48, 48, 50)
                generalAdj = intArrayOf(12, 8, 4, 1, -4, -5, -6)
            }
        }

        val idx = if (ramIdx >= 0) ramIdx else 2
        val general = generalBase + generalAdj[idx]
        val dpi = dpiBase + dpiAdj[idx]
        val fire = fireMap[idx]

        return SensitivityProfile(
            general = general,
            redDot = maxOf(0, general - 87),
            scope2x = maxOf(0, general - 56),
            scope4x = maxOf(0, general - 66),
            sniper = maxOf(0, general - 82),
            fireButtonSize = fire,
            dpi = dpi,
            brandGroup = group.label
        )
    }
}
