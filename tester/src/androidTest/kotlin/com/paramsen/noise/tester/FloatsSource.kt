package com.paramsen.noise.tester

import com.google.common.io.LittleEndianDataInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale

/**
 * @author Pär Amsen 07/2017
 */
class FloatsSource(
    private val input: InputStream
) {

    fun get(): FloatArray {
        try {
            LittleEndianDataInputStream(input.buffered()).use { dis ->
                val size = dis.readInt()
                val floats = FloatArray(size)

                var next: Float
                var read = 0

                while (read < size) {
                    next = dis.safeNextFloat()
                    floats[read++] = next
                }

                if (read != size) throw RuntimeException(
                    String.format(
                        Locale.US,
                        "Not correct size, expected %d, but was %d",
                        size,
                        read
                    )
                )

                return floats
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun LittleEndianDataInputStream.safeNextFloat(): Float {
        return try {
            readFloat()
        } catch (_: IOException) {
            Float.Companion.MIN_VALUE
        }
    }
}
