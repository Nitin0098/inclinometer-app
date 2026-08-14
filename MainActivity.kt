package com.claude.digitallevel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Digital level. Uses the accelerometer as a gravity sensor: at rest, its
 * reading in the phone's coordinate frame (gx, gy, gz) points opposite
 * gravity. From that vector we derive:
 *
 *  - overall angle between the phone's flat face and level (horizontal),
 *    i.e. how far the surface the phone is resting on deviates from
 *    perpendicular to the gravity vector:  acos(gz / |g|)
 *  - left-right tilt (rotation around the phone's vertical axis)
 *  - front-back tilt (rotation around the phone's horizontal axis)
 *
 * A low-pass filter smooths raw accelerometer noise so the reading doesn't
 * jitter.
 */
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Low-pass filtered gravity vector, seeded pointing "down" in a flat phone.
    private val gravity = floatArrayOf(0f, 0f, SensorManager.GRAVITY_EARTH)
    private val smoothing = 0.85f // 0..1, higher = smoother but slower to react

    private lateinit var levelView: LevelView
    private lateinit var tvLeftRight: TextView
    private lateinit var tvFrontBack: TextView
    private lateinit var tvOverall: TextView
    private lateinit var btnCalibrate: Button
    private lateinit var btnReset: Button

    private var leftRightOffset = 0f
    private var frontBackOffset = 0f

    private var currentLeftRight = 0f
    private var currentFrontBack = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        levelView = findViewById(R.id.levelView)
        tvLeftRight = findViewById(R.id.tvLeftRight)
        tvFrontBack = findViewById(R.id.tvFrontBack)
        tvOverall = findViewById(R.id.tvOverall)
        btnCalibrate = findViewById(R.id.btnCalibrate)
        btnReset = findViewById(R.id.btnReset)

        // "Calibrate zero" lets the user zero out a surface they know is
        // level/flat, or compensate for a slightly warped phone case.
        btnCalibrate.setOnClickListener {
            leftRightOffset = currentLeftRight
            frontBackOffset = currentFrontBack
        }

        btnReset.setOnClickListener {
            leftRightOffset = 0f
            frontBackOffset = 0f
        }

        if (accelerometer == null) {
            tvOverall.text = "N/A"
            tvSubtitleFallback()
        }
    }

    private fun tvSubtitleFallback() {
        findViewById<TextView>(R.id.tvSubtitle).text = "No accelerometer found on this device"
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        // Low-pass filter to smooth sensor noise.
        gravity[0] = smoothing * gravity[0] + (1 - smoothing) * event.values[0]
        gravity[1] = smoothing * gravity[1] + (1 - smoothing) * event.values[1]
        gravity[2] = smoothing * gravity[2] + (1 - smoothing) * event.values[2]

        val gx = gravity[0]
        val gy = gravity[1]
        val gz = gravity[2]
        val norm = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
        if (norm < 1f) return // guard against divide-by-near-zero in free fall

        // Left-right tilt (rotation around the phone's Y axis)
        val leftRight = Math.toDegrees(atan2(gx.toDouble(), gz.toDouble())).toFloat()
        // Front-back tilt (rotation around the phone's X axis)
        val frontBack = Math.toDegrees(atan2(gy.toDouble(), gz.toDouble())).toFloat()
        // Combined angle between the phone's flat face and level (0..90)
        val overall = Math.toDegrees(acos((gz / norm).coerceIn(-1f, 1f).toDouble())).toFloat()

        currentLeftRight = leftRight
        currentFrontBack = frontBack

        val displayLeftRight = leftRight - leftRightOffset
        val displayFrontBack = frontBack - frontBackOffset

        tvLeftRight.text = String.format("%.1f°", displayLeftRight)
        tvFrontBack.text = String.format("%.1f°", displayFrontBack)
        tvOverall.text = String.format("%.1f°", overall)

        levelView.setTilt(displayLeftRight, displayFrontBack)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op: not needed for this use case.
    }
}
