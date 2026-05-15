/*
 * This file is a part of the NewsFeed Launcher application's source code.
 * Copyright (c) 2017 - 2026. András Oravecz - All rights reserved
 *
 * You are NOT ALLOWED to USE, DISTRIBUTE or MODIFY this code, except if the copyright owner explicitly granted for you.
 */

package com.paramsen.noise.tester

import com.paramsen.noise.Noise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

class NoiseFunctionalTest {

    @Test
    fun testReal_DC_Signal() {
        val size = 1024
        val noise = Noise.real(size)
        val src = FloatArray(size) { 1.0f } // Constant 1.0 signal
        val dst = FloatArray(size + 2)

        noise.fft(src, dst)

        // For a DC signal of 1.0, the 0-th bin should be size, others 0
        // kiss_fftr scales by 1, so magnitude at index 0 is sum(src)
        assertEquals(size.toFloat(), dst[0], 0.01f)
        assertEquals(0f, dst[1], 0.01f) // Imaginary part of DC is 0

        // Check that other bins are approximately zero
        for (i in 2 until dst.size) {
            assertEquals("Bin $i should be 0", 0f, dst[i], 0.01f)
        }
        noise.close()
    }

    @Test
    fun testReal_Impulse_Signal() {
        val size = 1024
        val noise = Noise.real(size)
        val src = FloatArray(size)
        src[0] = 1.0f // Impulse at t=0
        val dst = FloatArray(size + 2)

        noise.fft(src, dst)

        // FFT of an impulse is a constant 1.0 across all frequencies
        for (i in 0 until (size / 2 + 1)) {
            val real = dst[i * 2]
            val imag = dst[i * 2 + 1]
            val mag = sqrt(real * real + imag * imag)
            assertEquals("Magnitude at bin $i should be 1.0", 1.0f, mag, 0.01f)
        }
        noise.close()
    }

    @Test
    fun testReal_SineWave_Peak() {
        val size = 1024
        val freq = 32 // 32 cycles per window
        val noise = Noise.real(size)
        val src = FloatArray(size) { i ->
            cos(2.0 * PI * freq * i / size).toFloat()
        }
        val dst = FloatArray(size + 2)

        noise.fft(src, dst)

        // Peak should be at bin 'freq'
        var maxMag = 0f
        var maxBin = -1

        for (i in 0 until (size / 2 + 1)) {
            val mag = sqrt(dst[i * 2] * dst[i * 2] + dst[i * 2 + 1] * dst[i * 2 + 1])
            if (mag > maxMag) {
                maxMag = mag
                maxBin = i
            }
        }

        assertEquals("Peak should be at frequency $freq", freq, maxBin)
        assertTrue("Peak magnitude should be significant", maxMag > (size / 2.1f))
        noise.close()
    }
}
