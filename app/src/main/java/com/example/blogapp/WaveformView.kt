package com.example.blogapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View


class WaveformView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var point = Paint()
    private var amplitudes = ArrayList<Float>()
    private var soikes = ArrayList<RectF>()

    private var radius = 3f  // Giảm kích thước radius
    private var w = 2f  // Giảm chiều rộng của cột xuống
    private var d = 3f  // Giảm khoảng cách giữa các cột

    private var sw = 0f
    private var sh = 100f  // Tăng chiều cao để các cột có không gian hiển thị lớn hơn


    private var maxSpikes = 0

    init {
        point.color = Color.rgb(244,81,30)

        sw = resources.displayMetrics.widthPixels.toFloat()

        maxSpikes = (sw / (w+d)).toInt()
    }

    fun addAmplitude(amp: Float) {
        val norm = Math.min(amp.toInt() / 7, 100).toFloat()
        amplitudes.add(norm)

        soikes.clear()
        val amps = amplitudes.takeLast(maxSpikes)
        for (i in amps.indices) {
            val left = sw - i * (w + d)
            val top = sh / 2 - amps[i] / 2
            val right = left + w
            val bottom = top + amps[i]
            soikes.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }


    fun clear() : ArrayList<Float>{
        var amps = amplitudes.clone() as ArrayList<Float>
        amplitudes.clear()
        soikes.clear()
        invalidate()

        return amps
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        soikes.forEach {
            canvas?.drawRoundRect(it,radius,radius,point)
        }
    }
}