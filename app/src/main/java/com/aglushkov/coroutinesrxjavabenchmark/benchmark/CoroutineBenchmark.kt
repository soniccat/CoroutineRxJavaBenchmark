package com.aglushkov.coroutinesrxjavabenchmark.benchmark

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.lang.Runnable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoroutineBenchmark(
    private val monitor: SystemMonitor,
    val useSuspended: Boolean,
    val useDefaultDispatcher: Boolean
): Benchmark {
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun runBenchmark(completion: Runnable) {
        monitor.startRecording()

        val dispatcher = if (useDefaultDispatcher) {
            Dispatchers.Default
        } else {
            Dispatchers.IO
        }

        mainScope.launch {
            withContext(dispatcher) {
                supervisorScope {
                    IOSources.sites.forEach { site ->
                        launch(CoroutineExceptionHandler { context, throwable ->
                            Log.e("webResult", throwable.message.orEmpty())
                        }) {
                            delay(IOSources.delays[site] ?: 0)
                            val resp = if (useSuspended) {
                                IOSources.buildRequest(site).customAwait()
                            } else {
                                IOSources.buildRequest(site).execute()//.customAwait().body
                            }
                            resp.body?.let { it ->
                                val text = it.byteStream().reader().readText()
                                it.close()
                                Log.d("webResult", " " + text.length)
                            }
                        }
                    }
                }

//                IOSources.shutdownClient()
            }
            monitor.stopRecording()
            completion.run()
        }
    }
}

suspend fun Call.customAwait(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body
                    if (body == null) {
                        continuation.resumeWithException(CustomAwaitException)
                    } else {
                        continuation.resume(response)
                    }
                } else {
                    continuation.resumeWithException(CustomAwaitException)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}

object CustomAwaitException: Exception("CustomAwaitException")