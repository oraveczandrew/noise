package com.paramsen.noise

import java.io.Closeable

/**
 * Instances should be closed when no longer in use to free native allocations.
 *
 * @author Pär Amsen 11/2019
 */
class Noise private constructor(
    private var configPointer: Long,
    private val isReal: Boolean
) : Closeable {

    /** @return dst for convenience */
    fun fft(src: FloatArray, dst: FloatArray): FloatArray {
        check(configPointer != 0L) { "Noise instance is closed" }
        if (isReal) {
            require(dst.size == src.size + 2) { "Cannot compute FFT, dst length must equal src length + 2" }
            NoiseNativeBridge.real(src, dst, configPointer)
        } else {
            require(src.size == dst.size) { "Cannot compute FFT, dst length must equal src length" }
            NoiseNativeBridge.imaginary(src, dst, configPointer)
        }

        return dst
    }

    override fun close() {
        if (configPointer != 0L) {
            if (isReal) {
                NoiseNativeBridge.realConfigDispose(configPointer)
            } else {
                NoiseNativeBridge.imaginaryConfigDispose(configPointer)
            }
            configPointer = 0L
        }
    }

    companion object {
        /** @param inputLength fixed input length to compute FFT for */
        @JvmStatic
        fun real(inputLength: Int): Noise {
            require(inputLength % 2 == 0) { "inputLength must be even" }
            val pointer = NoiseNativeBridge.realConfig(inputLength)
            if (pointer == 0L) throw RuntimeException("Could not initialize native FFT config")
            return Noise(pointer, true)
        }

        /** @param inputLength fixed input length to compute FFT for */
        @JvmStatic
        fun imaginary(inputLength: Int): Noise {
            require(inputLength % 2 == 0) { "inputLength must be even (real/imaginary pairs)" }
            val pointer = NoiseNativeBridge.imaginaryConfig(inputLength)
            if (pointer == 0L) throw RuntimeException("Could not initialize native FFT config")
            return Noise(pointer, false)
        }
    }
}