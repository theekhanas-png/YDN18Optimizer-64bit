package com.yodhan18.ydn18optimizer.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.yodhan18.ydn18optimizer.DeviceInfoProvider
import com.yodhan18.ydn18optimizer.R
import com.yodhan18.ydn18optimizer.SensitivityCalculator
import com.yodhan18.ydn18optimizer.ui.CountdownRingView

class SensitivityFragment : Fragment() {

    private lateinit var tvDeviceName: TextView
    private lateinit var tvSensRam: TextView
    private lateinit var tvSensBrandGroup: TextView
    private lateinit var btnGenerate: Button
    private lateinit var cardCountdown: CardView
    private lateinit var countdownRing: CountdownRingView
    private lateinit var tvCountdownStatus: TextView
    private lateinit var cardResults: CardView

    private lateinit var tvGeneral: TextView
    private lateinit var tvRedDot: TextView
    private lateinit var tv2x: TextView
    private lateinit var tv4x: TextView
    private lateinit var tvSniper: TextView
    private lateinit var tvFire: TextView
    private lateinit var tvDpi: TextView
    private lateinit var btnRegenerate: Button

    private val glitchMessages = listOf(
        "Analysing device…",
        "Optimizing sensitivity according to device…",
        "Almost there…"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_sensitivity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        tvDeviceName = view.findViewById(R.id.tvSensDeviceName)
        tvSensRam = view.findViewById(R.id.tvSensRam)
        tvSensBrandGroup = view.findViewById(R.id.tvSensBrandGroup)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        cardCountdown = view.findViewById(R.id.cardCountdown)
        countdownRing = view.findViewById(R.id.countdownRing)
        tvCountdownStatus = view.findViewById(R.id.tvCountdownStatus)
        cardResults = view.findViewById(R.id.cardResults)

        tvGeneral = view.findViewById(R.id.tvGeneral)
        tvRedDot = view.findViewById(R.id.tvRedDot)
        tv2x = view.findViewById(R.id.tv2x)
        tv4x = view.findViewById(R.id.tv4x)
        tvSniper = view.findViewById(R.id.tvSniper)
        tvFire = view.findViewById(R.id.tvFire)
        tvDpi = view.findViewById(R.id.tvDpi)
        btnRegenerate = view.findViewById(R.id.btnRegenerate)

        // Load device info
        tvDeviceName.text = DeviceInfoProvider.getDeviceName()
        val ramGb = DeviceInfoProvider.getTotalRamGb(requireContext())
        tvSensRam.text = "%.1f GB (mapped: %dGB)".format(
            ramGb, SensitivityCalculator.mapRam(ramGb)
        )
        tvSensBrandGroup.text = SensitivityCalculator.detectBrandGroup().label

        btnGenerate.setOnClickListener { startGeneration() }
        btnRegenerate.setOnClickListener {
            cardResults.visibility = View.GONE
            startGeneration()
        }
    }

    private fun startGeneration() {
        btnGenerate.isEnabled = false
        cardResults.visibility = View.GONE
        cardCountdown.visibility = View.VISIBLE
        cardCountdown.alpha = 0f
        cardCountdown.animate().alpha(1f).setDuration(300).start()

        var msgIdx = 0
        tvCountdownStatus.text = glitchMessages[0]

        countdownRing.startCountdown(
            totalSeconds = 10,
            onTick = { sec ->
                // Cycle through messages
                val newIdx = when {
                    sec > 7 -> 0
                    sec > 3 -> 1
                    else -> 2
                }
                if (newIdx != msgIdx) {
                    msgIdx = newIdx
                    tvCountdownStatus.animate()
                        .alpha(0f).setDuration(200)
                        .withEndAction {
                            tvCountdownStatus.text = glitchMessages[msgIdx]
                            tvCountdownStatus.animate().alpha(1f).setDuration(200).start()
                        }.start()
                }
            },
            onFinish = {
                showResults()
            }
        )
    }

    private fun showResults() {
        if (!isAdded) return

        cardCountdown.animate().alpha(0f).setDuration(300)
            .withEndAction { cardCountdown.visibility = View.GONE }.start()

        val ramGb = DeviceInfoProvider.getTotalRamGb(requireContext())
        val profile = SensitivityCalculator.calculate(ramGb)

        tvGeneral.text = "${profile.general}"
        tvRedDot.text = "${profile.redDot}"
        tv2x.text = "${profile.scope2x}"
        tv4x.text = "${profile.scope4x}"
        tvSniper.text = "${profile.sniper}"
        tvFire.text = "${profile.fireButtonSize}"
        tvDpi.text = "${profile.dpi}"

        // Show card with bounce animation
        cardResults.visibility = View.VISIBLE
        cardResults.scaleX = 0.8f
        cardResults.scaleY = 0.8f
        cardResults.alpha = 0f
        cardResults.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(600)
            .setInterpolator(BounceInterpolator())
            .start()

        // Animate individual cells with stagger
        val cells = listOf(
            view?.findViewById<View>(R.id.cellGeneral),
            view?.findViewById<View>(R.id.cellRedDot),
            view?.findViewById<View>(R.id.cell2x),
            view?.findViewById<View>(R.id.cell4x),
            view?.findViewById<View>(R.id.cellSniper),
            view?.findViewById<View>(R.id.cellFire),
            view?.findViewById<View>(R.id.cellDpi)
        )

        cells.forEachIndexed { index, cell ->
            cell?.scaleX = 0f
            cell?.scaleY = 0f
            cell?.animate()
                ?.scaleX(1f)?.scaleY(1f)
                ?.setStartDelay(index * 80L)
                ?.setDuration(400)
                ?.setInterpolator(DecelerateInterpolator())
                ?.start()
        }

        btnGenerate.isEnabled = true
    }
}
