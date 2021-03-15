package com.aglushkov.coroutinesrxjavabenchmark.benchmark

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import kotlin.math.max
import kotlin.math.min

class SystemMonitor(
    private val context: Context,
    private val delay: Long = 100
) {
    private var handler: Handler
    private var measureRunnable = Runnable {
        updateAvailableMemory()
        updateActiveThreadCount()
        measureDelayed()
    }

    var time: Long = 0
    var maxThreadCount: Long = 0
    var minAvailableMemory: Long = Long.MAX_VALUE
    var minAvailableMemoryOnStart: Long = 1

    private var startedCount: Int = 0

    init {
        val thread = HandlerThread("SystemMonitor Thread")
        thread.start()
        handler = Handler(thread.looper)
    }

    fun updateAvailableMemoryOnStart(completion: Runnable) {
        requestMemoryInfo { memoryInfo ->
            minAvailableMemoryOnStart = memoryInfo.availMem
            completion.run()
        }
    }

    fun startRecording() {
        if (startedCount == 0) {
            time = System.currentTimeMillis()
            maxThreadCount = 0
            minAvailableMemory = Long.MAX_VALUE

            measureDelayed()
        }
        ++startedCount
    }

    fun stopRecording() {
        --startedCount
        if (startedCount == 0) {
            time = System.currentTimeMillis() - time
            handler.removeCallbacks(measureRunnable)
        }
    }

    private fun measureDelayed() {
        handler.postDelayed(measureRunnable, delay)
    }

    private fun updateAvailableMemory() {
        requestMemoryInfo { memoryInfo ->
            minAvailableMemory = min(minAvailableMemory, memoryInfo.availMem)
        }
    }

    private fun requestMemoryInfo(result: (ActivityManager.MemoryInfo) -> Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
            result(memoryInfo)
        }
    }

    private fun updateActiveThreadCount() {
        //maxThreadCount = max(maxThreadCount, Thread.getAllStackTraces().keys.size.toLong())
        maxThreadCount = max(maxThreadCount, Thread.activeCount().toLong())
    }
}