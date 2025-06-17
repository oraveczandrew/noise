package com.paramsen.noise.sample.view

import android.util.Log
import android.util.SparseIntArray
import androidx.core.util.forEach

class Profiler(val tag: String) {

    @Suppress("PropertyName", "UNNECESSARY_NOT_NULL_ASSERTION")
    val TAG: String = javaClass.simpleName!!

    private var count = 0L
    private var time = 0L

    private val hashes = SparseIntArray()

    fun next() {
        if (time == 0L) time = System.currentTimeMillis()

        if (System.currentTimeMillis() - time > 1000L) {
            time = System.currentTimeMillis()
            Log.d(TAG, "===$tag: $count/1000ms")
            count = 0
        } else {
            ++count
        }
    }

    fun next(hash: Int) {
        val now = System.currentTimeMillis()

        if (time == 0L) {
            time = now
        }

        hashes.put(hash, hashes[hash] + 1)

        if (now - time > 1000L) {
            time = now
            hashes.forEach { _, value -> if (value > 1) Log.d(TAG, "===$tag: $value") }
            Log.d(TAG, "===$tag: $count/1000ms")
            count = 0
            hashes.clear()
        } else {
            ++count
        }
    }
}