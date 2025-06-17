package com.paramsen.noise.tester

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.paramsen.noise.Noise.Companion.imaginary
import com.paramsen.noise.Noise.Companion.real
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Reminder: org.junit and android.support.test imports fail if Build Variant == release
 */
@RunWith(AndroidJUnit4::class)
class NoiseInstrumentationTest {

    @Test
    @Throws(Exception::class)
    fun testReal_Profile() {
        val noise = real(4096)
        val src = FloatArray(4096)
        val dst = FloatArray(4096 + 2)
        val runs = loopFor(
            time = 1,
            unit = TimeUnit.SECONDS
        ) {
            noise.fft(src, dst)
        }
        noise.close()

        println("============")
        System.out.printf("=== ROS: %.2fms\n", runs.toFloat() / 1000f)
        println("============")
    }

    /**
     * Assert that the output on a predefined signal of 4096 from kissfft is exactly the same for
     * this library as it is for kissfft. The "prerecorded" FFT is created through the
     * cpp_test_data_suite which simply compiles and runs kissfft on the input dataset in assets.
     *
     *
     * Hence, if this test is green kissfft works as intended.
     */
    @Test
    @Throws(Exception::class)
    fun testReal_Assert_kissfft_compare_result() {
        val noise = real(4096)
        val input = readFloatsFromFile("test/sample_signal_4096.dat")
        val output = FloatArray(input.size + 2)
        val kissfftPrerecordedFFT = readFloatsFromFile("test/sample_signal_4096_result_real.dat")

        loopFor(
            time = 100,
            unit = TimeUnit.MILLISECONDS
        ) {
            val fft = noise.fft(input, output)
            for (i in input.indices) {
                assertEquals(kissfftPrerecordedFFT[i], fft[i])
            }
        }

        noise.close()
    }

    @Test
    @Throws(Exception::class)
    fun testImaginary_Profile() {
        val noise = imaginary(4096)
        val src = FloatArray(4096)
        val dst = FloatArray(4096)
        val runs = loopFor(
            time = 1,
            unit = TimeUnit.SECONDS
        ) {
            noise.fft(src, dst)
        }
        noise.close()

        println("============")
        System.out.printf("=== IOS: %.2fms\n", (runs.toFloat()) / 1000)
        println("============")
    }

    /**
     * Assert that the output on a predefined signal of 4096 from kissfft is exactly the same for
     * this library as it is for kissfft. The "prerecorded" FFT is created through the
     * cpp_test_data_suite which simply compiles and runs kissfft on the input dataset in assets.
     *
     *
     * Hence, if this test is green kissfft works as intended.
     */
    @Test
    @Throws(Exception::class)
    fun testImaginary_Assert_kissfft_compare_result() {
        val noise = imaginary(4096 * 2)
        val inputFromFile = readFloatsFromFile("test/sample_signal_4096.dat")
        val input = FloatArray(4096 * 2)
        val output = FloatArray(4096 * 2)
        val kissfftPrerecordedFFT = readFloatsFromFile("test/sample_signal_4096_result_imag.dat")

        for (i in inputFromFile.indices) {
            input[i * 2] = inputFromFile[i]
            input[i * 2 + 1] = inputFromFile[i]
        }

        loopFor(
            time = 100,
            unit = TimeUnit.MILLISECONDS
        ) {
            val fft = noise.fft(input, output)
            for (i in input.indices) {
                assertEquals(kissfftPrerecordedFFT[i], fft[i])
            }
        }

        noise.close()
    }

    /**
     * Run Runnable *each* repeatedly during long *time* of TimeUnit *unit* and return how many
     * times Runnable *each* was run. For profiling.
     */
    private inline fun loopFor(time: Long, unit: TimeUnit, crossinline each: () -> Unit): Long {
        val start = System.currentTimeMillis()
        var runs = 0L
        while (System.currentTimeMillis() - start <= unit.toMillis(time)) {
            each()
            runs++
        }
        return runs
    }

    private fun readFloatsFromFile(fileName: String): FloatArray {
        return InstrumentationRegistry.getInstrumentation().targetContext.assets.open(fileName).use {
            FloatsSource(it).get()
        }
    }
}
