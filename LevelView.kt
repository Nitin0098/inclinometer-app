package com.claude.digitallevel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Draws a bullseye-style bubble level. The bubble's position is driven by
 * two tilt angles (left-right and front-back), in degrees. It turns green
 * when the device is within LEVEL_THRESHOLD degrees of flat on both axes.
 */
class LevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var tiltX = 0f // left-right, degrees
    private var tiltY = 0f // front-back, degrees

    // Degrees of tilt that correspond to the bubble reaching the outer ring.
    private val maxAngle = 30f
    private val levelThreshold = 0.5f

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#3A4552")
    }

    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#3A4552")
    }

    private val bubblePaintOff = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFC107")
    }

    private val bubblePaintLevel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00E676")
    }

    fun setTilt(leftRight: Float, frontBack: Float) {
        tiltX = leftRight
        tiltY = frontBack
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - 24f

        // Outer ring and crosshair
        canvas.drawCircle(cx, cy, radius, ringPaint)
        canvas.drawLine(cx - radius, cy, cx + radius, cy, crosshairPaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, crosshairPaint)
        canvas.drawCircle(cx, cy, radius * 0.15f, crosshairPaint)

        // Raw bubble position from tilt angles
        var bx = cx + (tiltX / maxAngle) * radius
        var by = cy + (tiltY / maxAngle) * radius

        // Clamp the bubble so it never leaves the ring at extreme tilts
        val dx = bx - cx
        val dy = by - cy
        val dist = sqrt(dx * dx + dy * dy)
        val maxDist = radius - 40f
        if (dist > maxDist && dist > 0f) {
            val scale = maxDist / dist
            bx = cx + dx * scale
            by = cy + dy * scale
        }

        val isLevel = abs(tiltX) < levelThreshold && abs(tiltY) < levelThreshold
        canvas.drawCircle(bx, by, 40f, if (isLevel) bubblePaintLevel else bubblePaintOff)
    }
}
