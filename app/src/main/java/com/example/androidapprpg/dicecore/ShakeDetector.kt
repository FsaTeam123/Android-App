package com.example.androidapprpg.dicecore

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detecta "sacudir" usando o acelerômetro.
 * Ajuste [threshold] e [minIntervalMs] para calibrar sensibilidade.
 */
class ShakeDetector(
    private val onShake: () -> Unit,
    private val threshold: Float = 13.5f,   // menor = mais sensível
    private val minIntervalMs: Long = 650L  // evita disparos seguidos
) : SensorEventListener {

    private var lastTs = 0L

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val g = sqrt(ax*ax + ay*ay + az*az)
        val delta = abs(g - SensorManager.GRAVITY_EARTH)

        val now = System.currentTimeMillis()
        if (delta > threshold && (now - lastTs) > minIntervalMs) {
            lastTs = now
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
