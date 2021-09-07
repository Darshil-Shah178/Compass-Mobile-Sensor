package com.example.internalsensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var compass: ImageView
    private val readingAccelerometer = FloatArray(3)
    private val readingMagnetometer = FloatArray(3)
    private val matrixRotation = FloatArray(9)
    private val orientationAngles = FloatArray(9)
    private var currentDegree = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        compass = findViewById(R.id.imageCompass)
    }

     override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED)
            ?.also { accelerometer ->
                sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_UI,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val info: PackageInfo = packageManager.getPackageInfo(
                this.packageName, PackageManager.GET_PERMISSIONS
            )
            val permissions = info.requestedPermissions
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissions, 1)
            return
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { //unused
    }

    override fun onSensorChanged(p0: SensorEvent) {
        when (p0.sensor.type) {
            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> {
                System.arraycopy(p0.values, 0, readingAccelerometer, 0, readingAccelerometer.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(p0.values, 0, readingMagnetometer, 0, readingMagnetometer.size)
            }
        }
        updateOrientation()
    }

    private fun updateOrientation() {
        SensorManager.getRotationMatrix(
            matrixRotation,
            null,
            readingAccelerometer,
            readingMagnetometer
        )
        val arrayOrientation = SensorManager.getOrientation(matrixRotation, orientationAngles)
        val compassFloat: Float = arrayOrientation[0] * (180 / Math.PI.toFloat())
        val compassRotation = RotateAnimation(
            currentDegree,
            -compassFloat,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        compassRotation.duration = 500
        compassRotation.fillAfter = true
        compass.startAnimation(compassRotation)
        currentDegree = -compassFloat
    }

}