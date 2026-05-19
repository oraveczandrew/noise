/*
 * This file is a part of the NewsFeed Launcher application's source code.
 * Copyright (c) 2017 - 2026. András Oravecz - All rights reserved
 *
 * You are NOT ALLOWED to USE, DISTRIBUTE or MODIFY this code, except if the copyright owner explicitly granted for you.
 */

package com.paramsen.noise.tester

import com.paramsen.noise.Noise
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class NoiseConcurrencyTest {

    @Test
    fun testParallelFFT() {
        val numThreads = 4
        val iterations = 1000
        val executor = Executors.newFixedThreadPool(numThreads)
        val errorCount = AtomicInteger(0)
        val size = 1024

        repeat(numThreads) {
            executor.execute {
                try {
                    val noise = Noise.real(size)
                    val src = FloatArray(size) { i -> i.toFloat() }
                    val dst = FloatArray(size + 2)

                    repeat(iterations) {
                        noise.fft(src, dst)
                        // Simple sanity check: DC component should be sum(0..1023)
                        val expectedDC = (size * (size - 1) / 2).toFloat()
                        if (abs(dst[0] - expectedDC) > 1.0f) {
                            errorCount.incrementAndGet()
                        }
                    }
                    noise.close()
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                    e.printStackTrace()
                }
            }
        }

        executor.shutdown()
        val finished = executor.awaitTermination(10, TimeUnit.SECONDS)

        assertEquals("Should finish all tasks", true, finished)
        assertEquals("Should have no errors during parallel execution", 0, errorCount.get())
    }
}
