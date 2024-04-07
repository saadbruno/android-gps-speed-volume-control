package com.saadbruno.gpsspeedvolumecontrol

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.saadbruno.gpsspeedvolumecontrol.ui.theme.GPSSpeedVolumeControlTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSSpeedVolumeControlTheme {
                Speedometer()
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
    var autoVolumeEnabled by remember { mutableStateOf(true) }

    val locationListener = LocationListener { location ->
        speed.floatValue = location.speed
        if (autoVolumeEnabled) {
            adjustVolume(context, location.speed)
        }
    }

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

    SpeedometerLayout(
        "${(speed.floatValue * 2.23694f * 10).roundToInt() / 10.0}",
        "mph",
        "${speed.floatValue}",
        autoVolumeEnabled,
        onCheckedChange = { autoVolumeEnabled = it }
    )

}

fun adjustVolume(context: Context, speed: Float) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val volume = when {
        speed < 4.0 -> (maxVolume * 0.2).toInt()
        speed > 8.0 -> maxVolume
        else -> ((speed - 4.0) / (8.0 - 4.0) * maxVolume).toInt()
    }

    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}

// MAIN LAYOUT
@Composable
fun SpeedometerLayout(speed: String, unit: String, speedDebug: String, isEnabled: Boolean, onCheckedChange: (Boolean) -> Unit) {

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            BigSpeedMeter(speed, unit)
            SmallSpeedMeter(speedDebug, "m/s")
            ToggleSwitch(
                isEnabled,
                onCheckedChange
            )
        }
    }
}

// COMPONENTS
@Composable
fun BigSpeedMeter(value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 128.sp,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Text(
            text = unit,
            fontSize = 48.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SmallSpeedMeter(value: String, unit: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.DarkGray
        )
        Text(
            text = unit,
            fontSize = 10.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun ToggleSwitch(isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) }
        )
        Text(
            text = "Volume automático",
            color = Color.White,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

// LAYOUT PREVIEWS

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SpeedometerPreview() {
    var foo = false
    GPSSpeedVolumeControlTheme {
        SpeedometerLayout(
            "55.5",
            "mph",
            "1234.4",
            foo,
            onCheckedChange = { foo = it }
            )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true, device = "spec:parent=pixel_5,orientation=landscape"
)
@Composable
fun SpeedometerPreviewLandscape() {
    var foo = false
    GPSSpeedVolumeControlTheme {
        SpeedometerLayout(
            "55.5",
            "mph",
            "1234.4",
            foo,
            onCheckedChange = { foo = it }
        )
    }
}