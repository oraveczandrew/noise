package com.paramsen.noise.sample.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.paramsen.noise.sample.R
import kotlin.math.hypot

/**
 * @author Pär Amsen 07/2017
 */

class InfoView : ConstraintLayout {
    @JvmField
    var showed = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("SetTextI18n")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT)

        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        elevation = 4f.px
        padding(16f.px.toInt())
    }

    /**
     * Uses alpha=0 and post to render hierarchy before starting reveal animation, to avoid fugly
     * animation glitches
     */
    fun onShow() {
        if (isVisible) return onClose()

        clearAnimation()
        visibility = VISIBLE
        alpha = 0f
        requestLayout()

        post {
            showed = true

            if (alpha != 1.0f) alpha = 1f

            ViewAnimationUtils
                .createCircularReveal(this, width - (12f + 8f).px.toInt(), 0, width / 20f, hypot(width.toFloat(), height.toFloat()))
                    .setDuration(300)
                    .start()
        }
    }

    fun onClose() {
        clearAnimation()

        val oldY = y

        animate().alpha(0f)
                .yBy((-20f).px)
                .setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .onTerminate {
                    visibility = INVISIBLE
                    alpha = 1f
                    y = oldY
                }.start()
    }
}