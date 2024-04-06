package com.saadbruno.gpsspeedvolumecontrol

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.saadbruno.gpsspeedvolumecontrol.ui.theme.GPSSpeedVolumeControlTheme

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSSpeedVolumeControlTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SpeedometerScreen()
                }
            }
        }
        ActivityCompat.requestPermissions(this, permissions, 0)
    }
}

@Composable
fun Speedometer() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val speed = remember { mutableFloatStateOf(0f) }

    val locationListener = LocationListener { location -> speed.floatValue = location.speed }

    BackHandler {
        locationManager.removeUpdates(locationListener)
    }

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener
        )
    } else {
        // Handle permission denial
    }

    Column {
        Text(text = "Current Speed: ${speed.floatValue} m/s")
    }
}

@Composable
fun SpeedometerScreen() {
    Column {
        Speedometer()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SpeedometerPreview() {
    GPSSpeedVolumeControlTheme {
        SpeedometerScreen()
    }
}