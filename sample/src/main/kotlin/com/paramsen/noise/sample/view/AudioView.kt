package com.paramsen.noise.sample.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.collection.MutableFloatList
import kotlin.math.min

/**
 * @author Pär Amsen 06/2017
 */
class AudioView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs) {

    private val sec = 10
    private val hz = 44100
    private val merge = 512
    private val history = hz * sec / merge
    private val audio: MutableFloatList = MutableFloatList()

    private val paintAudio: Paint = Paint().apply {
        color = 0xFF23E830.toInt()
        strokeWidth = 0f
        style = Paint.Style.STROKE
    }

    private val paintText: Paint = textPaint()
    private val path: Path = Path()

    private val bg = 0xFF39424F.toInt()

    private val textX = 16f.px
    private val textY = 24f.px

    fun drawAudio(canvas: Canvas): Canvas {
        val width = width
        val heightF = height.toFloat()

        val path = path
        val audio = audio
        path.reset()

        synchronized(audio) {
            for (i in audio.indices) {
                val sample = audio[i]

                if (i == 0) {
                    path.moveTo(width.toFloat(), sample)
                }

                path.lineTo(width - width * i / history.toFloat(), min(sample * 0.175f + heightF / 2f, heightF))
            }

            if (audio.size in 1 until history) {
                path.lineTo(0f, heightF / 2f)
            }
        }

        canvas.drawColor(bg)
        canvas.drawPath(path, paintAudio)
        canvas.drawText("AUDIO", textX, textY, paintText)

        return canvas
    }

    @SuppressLint("Range")
    fun onWindow(window: FloatArray) {
        synchronized(audio) {
            var acc = 0f

            for (i in window.indices) {
                val sample = window[i]
                if (i > 0 && i % merge != 0) {
                    acc += sample
                } else {
                    audio.add(0, acc / merge)
                    acc = 0f
                }
            }

            while (audio.size > history) {
                audio.removeAt(audio.lastIndex)
            }
        }

        drawSurface(::drawAudio)
    }
}