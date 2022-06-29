package com.aglushkov.coroutinesrxjavabenchmark.benchmark

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object IOSources {
    val sites = listOf(
//        <Add here websites you want to load>
        "https://www.ya.ru",
        "https://www.google.com",
    )

    private val timeout = 5L
    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(timeout, TimeUnit.SECONDS)
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .callTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .build()

    var delayStep = 250L
    var delays = calcDelays()

    private fun calcDelays() = sites.mapIndexed { index, s ->
        s to index * delayStep
    }.toMap()

    fun updateDelayStep(step: Long) {
        delayStep = step
        delays = calcDelays()
    }

    fun buildRequest(url: String) = okHttpClient.newCall(
            Request.Builder().url(url).build()
        )

    fun shutdownClient() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
        okHttpClient.cache?.close();
    }
}