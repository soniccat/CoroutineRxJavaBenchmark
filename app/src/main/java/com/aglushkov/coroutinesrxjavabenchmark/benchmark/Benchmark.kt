package com.aglushkov.coroutinesrxjavabenchmark.benchmark

interface Benchmark {

    fun runBenchmark(completion: Runnable)
}