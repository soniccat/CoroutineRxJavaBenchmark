package com.aglushkov.coroutinesrxjavabenchmark.benchmark

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.schedulers.ComputationScheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import retrofit2.awaitResponse
import java.lang.Runnable
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random.Default.nextLong

class RxJavaBenchmark(
    private val monitor: SystemMonitor,
    val useComputationScheduler: Boolean
): Benchmark {

    override fun runBenchmark(completion: Runnable) {
        monitor.startRecording()

        val scheduler = if (useComputationScheduler) {
            Schedulers.computation()
        } else {
            Schedulers.io()
        }

        Observable.fromArray(*IOSources.sites.toTypedArray())
            .concatMapEager { site ->
                Single.just(Unit)
                    .delay(IOSources.delays[site] ?: 0, TimeUnit.MILLISECONDS)
                    .map {
                        val body = IOSources.buildRequest(site).execute().body
                        val text = body?.byteStream()?.reader()?.readText().orEmpty()
                        body?.close()
                        Log.d("webResult",  " " + text.length)
                    }
                    .subscribeOn(scheduler)
                    .doOnError { throwable ->
                        Log.e("webResult", throwable.message.orEmpty())
                    }
                    .onErrorComplete()
                    .toObservable()
            }
            .ignoreElements()
            .subscribeOn(scheduler)
            .doOnComplete {
//                IOSources.shutdownClient()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                monitor.stopRecording()
                completion.run()
            }
    }
}