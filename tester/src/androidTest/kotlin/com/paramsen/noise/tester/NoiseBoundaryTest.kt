/*
 * This file is a part of the NewsFeed Launcher application's source code.
 * Copyright (c) 2017 - 2026. András Oravecz - All rights reserved
 *
 * You are NOT ALLOWED to USE, DISTRIBUTE or MODIFY this code, except if the copyright owner explicitly granted for you.
 */

package com.paramsen.noise.tester

import com.paramsen.noise.Noise
import org.junit.Assert.assertThrows
import org.junit.Test

class NoiseBoundaryTest {

    @Test
    fun testReal_InvalidInputLength_Throws() {
        assertThrows(IllegalArgumentException::class.java) {
            Noise.real(1023) // Odd length
        }
    }

    @Test
    fun testImaginary_InvalidInputLength_Throws() {
        assertThrows(IllegalArgumentException::class.java) {
            Noise.imaginary(1023) // Odd length
        }
    }

    @Test
    fun testReal_IncorrectDstSize_Throws() {
        val noise = Noise.real(1024)
        val src = FloatArray(1024)
        val dst = FloatArray(1024) // Should be 1026

        assertThrows(IllegalArgumentException::class.java) {
            noise.fft(src, dst)
        }
        noise.close()
    }

    @Test
    fun testImaginary_IncorrectDstSize_Throws() {
        val noise = Noise.imaginary(1024)
        val src = FloatArray(1024)
        val dst = FloatArray(512) // Should be 1024

        assertThrows(IllegalArgumentException::class.java) {
            noise.fft(src, dst)
        }
        noise.close()
    }

    @Test
    fun testMultipleClose_DoesNotCrash() {
        val noise = Noise.real(1024)
        noise.close()
        noise.close() // Should be safe now
    }

    @Test
    fun testUseAfterClose_Throws() {
        val noise = Noise.real(1024)
        val src = FloatArray(1024)
        val dst = FloatArray(1026)
        noise.close()
        assertThrows(IllegalStateException::class.java) {
            noise.fft(src, dst)
        }
    }
}
