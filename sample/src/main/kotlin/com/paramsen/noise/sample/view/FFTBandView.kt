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
    val size = 4096
    val bands = 64
    val bandSize = size / bands
    val maxConst = 1750000000 //reference max value for accum magnitude
    var average = .0f

    val fft: FloatArray = FloatArray(4096)
    val paintBandsFill: Paint = Paint()
    val paintBands: Paint = Paint()
    val paintAvg: Paint = Paint()
    val paintText: Paint = textPaint()

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

    fun drawAudio(canvas: Canvas): Canvas {
        canvas.drawColor(Color.DKGRAY)

        val height = height
        val width = width
        val bandsF = bands.toFloat()

        for (i in 0..bands - 1) {
            var acc = .0f

            synchronized(fft) {
                for (j in 0..bandSize - 1 step 2) {
                    //convert real and imag part to get energy
                    acc += (fft[j + (i * bandSize)].toDouble().pow(2) +
                            fft[j + 1 + (i * bandSize)].toDouble().pow(2)).toFloat()
                }

                acc /= bandSize / 2
            }

            average += acc

            canvas.drawRect(width * (i / bandsF), height - (height * min(acc / maxConst.toDouble(), 1.0).toFloat()) - height * .02f, width * (i / bandsF) + width / bandsF, height.toFloat(), paintBandsFill)
            canvas.drawRect(width * (i / bandsF), height - (height * min(acc / maxConst.toDouble(), 1.0).toFloat()) - height * .02f, width * (i / bandsF) + width / bandsF, height.toFloat(), paintBands)
        }

        average /= bands

        canvas.drawLine(0f, height - (height * (average / maxConst)) - height * .02f, width.toFloat(), height - (height * (average / maxConst)) - height * .02f, paintAvg)
        canvas.drawText("FFT BANDS", 16f.px, 24f.px, paintText)

        return canvas
    }

    override fun onFFT(fft: FloatArray) {
        synchronized(this.fft) {
            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
            drawSurface(this::drawAudio)
        }
    }
}