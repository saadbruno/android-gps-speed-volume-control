package com.saadbruno.gpsspeedvolumecontrol

import android.Manifest
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.saadbruno.gpsspeedvolumecontrol.ui.theme.GPSSpeedVolumeControlTheme

class MainActivity : ComponentActivity() {
    private val tag = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_SERVICE_START
            startService(this)
        }
        setContent {
            GPSSpeedVolumeControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.Black
                ) {
                    App()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPerm()) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            } else {
                checkLocationPerm()
            }
        } else {
            checkLocationPerm()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "APP DESTROYED")
        Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_SERVICE_STOP
            startService(this)
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
            if (it.key == "android.permission.POST_NOTIFICATIONS" && it.value) {
                askForBGPermission()
            }
        }
    }

    private val requestLocationPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            askForBGPermission()
        } else {
            Log.d(tag, "Permission is not true")
        }
    }

    private val requestBGLocationPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(tag, "Background Permission is true")
        } else {
            Log.d(tag, "Background Permission is not true")
        }
    }

    private fun checkLocationPerm() {
        if (!hasLocationPermission()) {
            requestLocationPerm.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            askForBGPermission()
        }
    }

    private fun askForBGPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasBGLocationPermission()) {
                requestBGLocationPerm.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }
}






