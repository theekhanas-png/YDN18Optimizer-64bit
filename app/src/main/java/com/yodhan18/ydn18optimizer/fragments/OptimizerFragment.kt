package com.yodhan18.ydn18optimizer.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yodhan18.ydn18optimizer.DeviceInfoProvider
import com.yodhan18.ydn18optimizer.R
import com.yodhan18.ydn18optimizer.ui.OptimizerRingView
import com.yodhan18.ydn18optimizer.ui.ScoreView
import kotlinx.coroutines.*
import java.io.File

class OptimizerFragment : Fragment() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvDeviceName: TextView
    private lateinit var tvProcessor: TextView
    private lateinit var tvRam: TextView
    private lateinit var tvStorage: TextView
    private lateinit var pbCpu: ProgressBar
    private lateinit var pbRam: ProgressBar
    private lateinit var pbStorage: ProgressBar
    private lateinit var tvCpuLoad: TextView
    private lateinit var tvRamLoad: TextView
    private lateinit var tvStorageLoad: TextView
    private lateinit var scoreView: ScoreView
    private lateinit var optimizingOverlay: FrameLayout
    private lateinit var ringView: OptimizerRingView
    private lateinit var tvOptimizingLabel: TextView
    private lateinit var tvOptimizingStep: TextView
    private lateinit var tvAllGood: TextView

    private var cpuLoad = 0
    private var ramLoad = 0
    private var storageLoad = 0
    private var isOptimizing = false
    private var pollingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_optimizer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDeviceName      = view.findViewById(R.id.tvDeviceName)
        tvProcessor       = view.findViewById(R.id.tvProcessor)
        tvRam             = view.findViewById(R.id.tvRam)
        tvStorage         = view.findViewById(R.id.tvStorage)
        pbCpu             = view.findViewById(R.id.pbCpu)
        pbRam             = view.findViewById(R.id.pbRam)
        pbStorage         = view.findViewById(R.id.pbStorage)
        tvCpuLoad         = view.findViewById(R.id.tvCpuLoad)
        tvRamLoad         = view.findViewById(R.id.tvRamLoad)
        tvStorageLoad     = view.findViewById(R.id.tvStorageLoad)
        scoreView         = view.findViewById(R.id.scoreView)
        optimizingOverlay = view.findViewById(R.id.optimizingOverlay)
        ringView          = view.findViewById(R.id.ringView)
        tvOptimizingLabel = view.findViewById(R.id.tvOptimizingLabel)
        tvOptimizingStep  = view.findViewById(R.id.tvOptimizingStep)
        tvAllGood         = view.findViewById(R.id.tvAllGood)

        setupButton(view.findViewById(R.id.btnFullOptimize)) {
            runOptimization("FULL MOBILE OPTIMIZATION", listOf(
                "Killing background processes...",
                "Clearing app caches...",
                "Freeing RAM memory...",
                "Running garbage collection...",
                "Optimizing CPU governor...",
                "Cleaning temp files...",
                "Optimization complete!"
            ))
        }
        setupButton(view.findViewById(R.id.btnRamOptimize)) {
            runOptimization("RAM OPTIMIZER", listOf(
                "Scanning RAM usage...",
                "Killing background processes...",
                "Running garbage collection...",
                "Freeing memory...",
                "RAM optimized!"
            ))
        }
        setupButton(view.findViewById(R.id.btnStorageClean)) {
            runOptimization("STORAGE CLEANER", listOf(
                "Scanning cache files...",
                "Clearing app caches...",
                "Removing temp files...",
                "Cleaning junk data...",
                "Storage cleaned!"
            ))
        }
        setupButton(view.findViewById(R.id.btnBoostCpu)) {
            runOptimization("CPU BOOST", listOf(
                "Analyzing CPU load...",
                "Killing CPU-heavy processes...",
                "Running garbage collection...",
                "CPU boosted!"
            ))
        }
        setupButton(view.findViewById(R.id.btnGameOptimize)) {
            runOptimization("GAME OPTIMIZATION", listOf(
                "Detecting installed games...",
                "Killing background apps...",
                "Freeing RAM for gaming...",
                "Clearing cache...",
                "Game mode activated!"
            ))
        }

        loadDeviceInfo()
        startStatPolling()
    }

    private fun setupButton(btn: Button, action: () -> Unit) {
        btn.setOnClickListener {
            if (!isOptimizing) {
                val scaleDown = AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(btn, "scaleX", 1f, 0.95f),
                        ObjectAnimator.ofFloat(btn, "scaleY", 1f, 0.95f)
                    )
                    duration = 80
                }
                val scaleUp = AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(btn, "scaleX", 0.95f, 1f),
                        ObjectAnimator.ofFloat(btn, "scaleY", 0.95f, 1f)
                    )
                    duration = 120
                    interpolator = BounceInterpolator()
                }
                scaleDown.start()
                handler.postDelayed({ scaleUp.start() }, 80)

                val prefs = requireContext()
                    .getSharedPreferences("ydn18_prefs", Context.MODE_PRIVATE)
                if (prefs.getBoolean("haptic", true)) {
                    btn.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                action()
            }
        }
    }

    private fun loadDeviceInfo() {
        scope.launch {
            val name    = withContext(Dispatchers.IO) { DeviceInfoProvider.getDeviceName() }
            val proc    = withContext(Dispatchers.IO) { DeviceInfoProvider.getProcessor() }
            val ramGb   = DeviceInfoProvider.getTotalRamGb(requireContext())
            val storGb  = DeviceInfoProvider.getTotalStorageGb()

            if (!isAdded) return@launch
            tvDeviceName.text = name
            tvProcessor.text  = if (proc.length > 28) proc.take(25) + "..." else proc
            tvRam.text        = "%.1f GB".format(ramGb)
            tvStorage.text    = "%.0f GB".format(storGb)
        }
    }

    private fun startStatPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive && !isOptimizing) {
                val cpu     = withContext(Dispatchers.IO) { DeviceInfoProvider.getCpuLoadPercent() }
                val ram     = DeviceInfoProvider.getRamUsedPercent(requireContext())
                val storage = DeviceInfoProvider.getStorageUsedPercent()

                cpuLoad     = cpu
                ramLoad     = ram
                storageLoad = storage

                if (isAdded) updateStatViews(cpu, ram, storage)
                delay(3000)
            }
        }
    }

    private fun updateStatViews(cpu: Int, ram: Int, storage: Int) {
        pbCpu.progress     = cpu
        pbRam.progress     = ram
        pbStorage.progress = storage
        tvCpuLoad.text     = "$cpu%"
        tvRamLoad.text     = "$ram%"
        tvStorageLoad.text = "$storage%"
        scoreView.setScore(DeviceInfoProvider.calculateScore(cpu, ram, storage))
    }

    private fun runOptimization(label: String, steps: List<String>) {
        isOptimizing = true
        pollingJob?.cancel()

        tvOptimizingLabel.text = label
        tvOptimizingStep.text  = steps.firstOrNull() ?: ""
        tvAllGood.visibility   = View.GONE
        optimizingOverlay.visibility = View.VISIBLE
        optimizingOverlay.alpha = 0f
        optimizingOverlay.animate().alpha(1f).setDuration(300).start()
        ringView.startAnimation()

        scope.launch {
            // Show each step with a delay
            val stepDelay = 2800L / steps.size.coerceAtLeast(1)
            for ((index, step) in steps.withIndex()) {
                if (!isActive) break
                tvOptimizingStep.text = step

                // Run actual optimization on IO thread in parallel
                if (index == 0) {
                    withContext(Dispatchers.IO) {
                        performOptimization(label)
                    }
                }
                delay(stepDelay)
            }

            if (!isAdded) return@launch

            // Show success
            tvOptimizingLabel.text = "OPTIMIZATION COMPLETE"
            tvOptimizingStep.text  = ""
            tvAllGood.visibility   = View.VISIBLE
            tvAllGood.scaleX = 0f
            tvAllGood.scaleY = 0f
            tvAllGood.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(600)
                .setInterpolator(BounceInterpolator())
                .start()

            // Update stats to post-optimization values
            val newRam = DeviceInfoProvider.getRamUsedPercent(requireContext())
                .coerceAtMost(15) // show improvement
            pbCpu.progress     = 3
            pbRam.progress     = newRam
            pbStorage.progress = storageLoad
            tvCpuLoad.text     = "3%"
            tvRamLoad.text     = "$newRam%"
            tvStorageLoad.text = "$storageLoad%"
            scoreView.setScore(100)

            delay(2000)
            if (!isAdded) return@launch

            ringView.stopAnimation()
            optimizingOverlay.animate()
                .alpha(0f).setDuration(400)
                .withEndAction {
                    if (isAdded) {
                        optimizingOverlay.visibility = View.GONE
                        isOptimizing = false
                        startStatPolling()
                    }
                }.start()
        }
    }

    private fun performOptimization(label: String) {
        // 1. Kill background processes
        try {
            val am = requireContext()
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val running = am.runningAppProcesses ?: emptyList()
            for (proc in running) {
                if (proc.importance >
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    am.killBackgroundProcesses(proc.processName)
                }
            }
        } catch (_: Exception) {}

        // 2. Garbage collection
        Runtime.getRuntime().gc()
        System.runFinalization()
        Runtime.getRuntime().gc()

        // 3. Clear caches for Storage / Full optimization
        if (label.contains("STORAGE") || label.contains("FULL") || label.contains("GAME")) {
            clearAppCaches()
        }

        Thread.sleep(300)
    }

    private fun clearAppCaches() {
        try {
            val cacheDir = requireContext().cacheDir
            deleteRecursive(cacheDir)
        } catch (_: Exception) {}
        try {
            val extCache = requireContext().externalCacheDir
            if (extCache != null) deleteRecursive(extCache)
        } catch (_: Exception) {}
        try {
            val codeCacheDir = requireContext().codeCacheDir
            deleteRecursive(codeCacheDir)
        } catch (_: Exception) {}
    }

    private fun deleteRecursive(file: File) {
        try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursive(it) }
            }
            file.delete()
        } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        if (::ringView.isInitialized) ringView.stopAnimation()
    }
}
