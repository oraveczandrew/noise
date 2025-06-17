package com.paramsen.noise.sample.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import java.util.ArrayDeque
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

private const val TAG: String = "FFTSpectogramView"

/**
 * @author Pär Amsen 06/2017
 */
class FFTSpectogramView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {

    private val sec = 10
    private val hz = 44100 / 4096
    private val fps = 80
    private val history = hz * sec
    private var resolution = 512
    private val minResolution = 64
    private val ffts = ArrayDeque<FloatArray>()

    private val paintSpectogram: Paint = Paint()
    private val paintText: Paint = textPaint()
    private val paintMsg: Paint = errTextPaint()
    private val bg = Color.rgb(20, 20, 25)

    private val hotThresholds = hashMapOf(512 to 15000, 256 to 15000, 128 to 30000, 64 to 45000)

    private val drawTimes = ArrayDeque<Long>()
    private var msg: Pair<Long, String>? = null

    private data class LastAverage(
        @JvmField
        val now: Long,
        @JvmField
        val average: Long,
    )

    private var lastAvg: LastAverage = LastAverage(System.currentTimeMillis(), 0)

    private val textX = 16f.px
    private val textY = 24f.px

    init {
        paintSpectogram.color = 0xFFFF2C00.toInt()
        paintSpectogram.style = Paint.Style.FILL
    }

    private fun drawTitle(canvas: Canvas) = canvas.drawText("FFT SPECTOGRAM", textX, textY, paintText)

    private fun drawIndicator(canvas: Canvas) {
        val height = height
        for (i in 0..height) {
            val f = i / height.toDouble()
            paintSpectogram.color = Spectogram.color(1.0 - f)

            canvas.drawRect(0f, i.toFloat(), 10f, i + 1f, paintSpectogram)
        }
    }

    private fun drawMsg(canvas: Canvas) {
        val msg = msg ?: return

        if (msg.first > System.currentTimeMillis()) {
            canvas.drawText(msg.second, (width - paintMsg.measureText(msg.second)) / 2, height - 16f.px, paintMsg)
        }
    }

    private fun drawSpectogram(canvas: Canvas) {
        val width = width
        val height = height

        val fftW = width / history.toFloat()
        val bandWH = height / resolution.toFloat()

        var x: Float
        var y: Float
        var band: FloatArray?

        val hot = hotThresholds[resolution] ?: 0

        for (i in 0 until ffts.size) {
            synchronized(ffts) {
                band = ffts.elementAtOrNull(i)
            }

            x = width - (fftW * i)

            for (j in 0 until resolution) {
                y = height - (bandWH * j)
                val mag = band?.get(j) ?: .0f

                paintSpectogram.color = Spectogram.color((mag / hot.toDouble()).coerceAtMost(1.0))
                canvas.drawRect(x - fftW, y - bandWH, x, y, paintSpectogram)
            }
        }
    }

    private fun canDrawSpectogram() = resolution >= minResolution

    private fun drawGraphic(canvas: Canvas): Canvas {
        canvas.drawColor(bg)

        if (!canDrawSpectogram()) {
            val msg = "GPU MEM TOO LOW"
            drawTitle(canvas)
            canvas.drawText(msg, (width - paintMsg.measureText(msg)) / 2, height - 16f.px, paintMsg)

            return canvas
        }

        // If rendering is causing backpressure [and thus fps drop], lower resolution + show message
        if (resolution >= minResolution && drawTimes.size >= history / 5 && avgDrawTime() > fps) {
            synchronized(ffts) {
                ffts.clear()
            }

            drawTimes.clear()
            resolution /= 2
            msg = Pair(System.currentTimeMillis() + 10000, "DOWNSAMPLE DUE TO LOW GPU MEMORY")
            Log.w(TAG, "Draw hz exceeded 60 (${avgDrawTime()}), downsampled to $resolution")

            return canvas
        }

        drawTimes.addLast(measureTimeMillis {
            drawSpectogram(canvas)
            drawIndicator(canvas)
            drawTitle(canvas)
            drawMsg(canvas)
        })

        println("===p4: ${drawTimes.last}")

        while (drawTimes.size > history) drawTimes.removeFirst()

        return canvas
    }

    override fun onFFT(fft: FloatArray) {
        if (canDrawSpectogram()) {
            val bands = FloatArray(resolution)
            var accum: Float
            var avg = 0f

            for (i in 0 until resolution) {
                accum = .0f

                for (j in 0 until fft.size / resolution step 2) {
                    val index = i * j
                    accum += (sqrt(fft[index].toDouble().pow(2.0) + fft[index + 1].toDouble().pow(2.0))).toFloat() //magnitudes
                }

                accum /= resolution
                bands[i] = accum
                avg += accum
            }

            avg /= resolution

            for (i in 0 until resolution) {
                if (bands[i] < avg / 2) bands[i] * 1000f
            }

            synchronized(ffts) {
                ffts.addFirst(bands)

                while (ffts.size > history) {
                    ffts.removeLast()
                }
            }
        }

        drawSurface(this::drawGraphic)
    }

    private fun avgDrawTime(): Long {
        val now = System.currentTimeMillis()
        if (now - lastAvg.now > 1000) {
            lastAvg = LastAverage(now, if (drawTimes.isNotEmpty()) drawTimes.average().toLong() else 0L)
        }

        return lastAvg.average
    }
}