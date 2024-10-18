package com.saadbruno.gpsspeedvolumecontrol

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.util.Log

private const val DEBUG = true
private const val PREFERENCES_FILE_NAME = "volume_preferences"
private const val HIGH_SPEED_THRESHOLD = 4.0 // m/s
private const val VOLUME_PREFERENCE_KEY = "volume_preference"

private var setVolume = 0
private var targetVolume = 0
private var volumeChangeInProgress = false
private var launched = false

class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSSpeedVolumeControlTheme {
                Speedometer()
            }
        }
        // Request permissions at runtime
        requestPermissions()
    }

    private fun requestPermissions() {
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            startForegroundService()
        } else {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startForegroundService()
        } else {
            // Handle permission denial
        }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
        } else {
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundService()
    }

    private fun stopForegroundService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        stopService(serviceIntent)
    }
}

@Composable
fun Speedometer() {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val speed = remember { mutableFloatStateOf(0f) }
    var autoVolumeEnabled by rememberSaveable { mutableStateOf(true) }

    val locationListener = LocationListener { location ->
        speed.floatValue = location.speed
        if (autoVolumeEnabled) {
            adjustVolume(context, location.speed)
        }
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

    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    targetVolume = if (speed >= HIGH_SPEED_THRESHOLD) {
        getHighSpeedVolume(context)
    } else {
        getLowSpeedVolume(context)
    }

    if (DEBUG) Log.d("Speedometer", "Speed: $speed, Current Volume: $setVolume, Current System Volume: $currentVolume, Target Volume: $targetVolume, MaxVolume: $maxVolume, Volume Change In Progress: $volumeChangeInProgress")

    if (!volumeChangeInProgress && setVolume != targetVolume) {
        CoroutineScope(Dispatchers.Main).launch {
            if (DEBUG) Log.d("Speedometer", "Starting volume change")
            volumeChangeInProgress = true
            while (setVolume != targetVolume) {
                if (setVolume < targetVolume) {
                    setVolume++
                } else {
                    setVolume--
                }
                setSystemVolume(context, setVolume)
                delay(500)
            }
            volumeChangeInProgress = false
        }
    }

    if (setVolume != currentVolume) {
        if (launched) {
            if (DEBUG) Log.d("Speedometer", "User changed volume manually. Updating preferences, and also setting setVolume to currentVolume")
            setVolume = currentVolume
            storeUserVolume(context, currentVolume, speed)
        } else {
            launched = true
        }
    }
}
// Get the stored volume for the high speed state
private fun getHighSpeedVolume(context: Context): Int {
    //return (maxVolume * 0.9).roundToInt()

    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getInt("${VOLUME_PREFERENCE_KEY}_high", 14)
}

// Get the stored volume for the low speed state
private fun getLowSpeedVolume(context: Context): Int {
    //return (maxVolume * 0.1).roundToInt()
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getInt("${VOLUME_PREFERENCE_KEY}_low", 2)
}

// Set the system volume and store the new volume for the current state
private fun setSystemVolume(context: Context, volume: Int) {
    if(DEBUG) Log.d("Speedometer", "Setting system volume to $volume")
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Set the actual system volume
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}

private fun storeUserVolume(context: Context, volume: Int, speed: Float) {
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    if (speed >= HIGH_SPEED_THRESHOLD) {
        if(DEBUG) Log.d("Speedometer", "Storing high speed volume: $volume")
        sharedPreferences.edit().putInt("${VOLUME_PREFERENCE_KEY}_high", volume).apply()
    } else {
        if(DEBUG) Log.d("Speedometer", "Storing low speed volume: $volume")
        sharedPreferences.edit().putInt("${VOLUME_PREFERENCE_KEY}_low", volume).apply()
    }
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