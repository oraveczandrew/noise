package com.paramsen.noise

/**
 * JNI interface
 *
 * @author Pär Amsen 06/2017
 */
@Suppress("unused")
object NoiseNativeBridge {
    init {
        System.loadLibrary("noise")
    }

    external fun realConfig(inputLength: Int): Long
    external fun realConfigDispose(cfgPointer: Long)
    external fun real(`in`: FloatArray, out: FloatArray, cfgPointer: Long)

    external fun imaginaryConfig(inputLength: Int): Long
    external fun imaginaryConfigDispose(cfgPointer: Long)
    external fun imaginary(`in`: FloatArray, out: FloatArray, cfgPointer: Long)
}
