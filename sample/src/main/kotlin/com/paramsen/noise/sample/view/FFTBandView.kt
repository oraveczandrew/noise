package com.paramsen.noise.sample.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import java.lang.System.arraycopy
import kotlin.math.min
import kotlin.math.pow

/**
 * @author Pär Amsen 06/2017
 */
class FFTBandView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {
    private val size = 4096
    private val bands = 64
    private val bandSize = size / bands
    private val maxConst = 1750000000 //reference max value for accum magnitude
    private var average = .0f

    private val fft: FloatArray = FloatArray(4096)
    private val paintBandsFill: Paint = Paint()
    private val paintBands: Paint = Paint()
    private val paintAvg: Paint = Paint()
    private val paintText: Paint = textPaint()

    init {
        paintBandsFill.color = 0x33FF2C00
        paintBandsFill.style = Paint.Style.FILL

        paintBands.color = 0xAAFF2C00.toInt()
        paintBands.strokeWidth = 1f
        paintBands.style = Paint.Style.STROKE

        paintAvg.color = 0x33FFFFFF
        paintAvg.strokeWidth = 1f
        paintAvg.style = Paint.Style.STROKE
    }

    private fun drawAudio(canvas: Canvas): Canvas {
        canvas.drawColor(Color.DKGRAY)

        val bandSize = bandSize
        val height = height
        val width = width
        val bandsF = bands.toFloat()

        for (i in 0 until bands) {
            val bandMulI = i * bandSize
            var acc = .0f

            synchronized(fft) {
                var j = 0
                while (j < bandSize) {
                    //convert real and imag part to get energy
                    acc += (fft[j + bandMulI].toDouble().pow(2.0) +
                            fft[j + 1 + bandMulI].toDouble().pow(2.0)).toFloat()
                    j += 2
                }

                acc /= bandSize / 2
            }

            average += acc

            val left = width * (i / bandsF)
            val top = height - (height * min(acc / maxConst, 1f)) - height * .02f
            val right = left + width / bandsF
            val bottom = height.toFloat()
            canvas.drawRect(left, top, right, bottom, paintBandsFill)
            canvas.drawRect(left, top, right, bottom, paintBands)
        }

        average /= bands

        val startY = height - (height * (average / maxConst)) - height * .02f
        canvas.drawLine(0f, startY, width.toFloat(), startY, paintAvg)
        canvas.drawText("FFT BANDS", 16f.px, 24f.px, paintText)

        return canvas
    }

    override fun onFFT(fft: FloatArray) {
        synchronized(this.fft) {
            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
            drawSurface(::drawAudio)
        }
    }
}