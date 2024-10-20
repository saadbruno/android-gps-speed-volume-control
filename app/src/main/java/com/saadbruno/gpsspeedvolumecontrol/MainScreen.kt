package com.saadbruno.gpsspeedvolumecontrol

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

// this is the value that defines when the music volume should be high and when it should be low.
// This variable is also present on the VolumeManager.kt file. I'm too lazy to figure out how to
// share this value between the two files.
private const val HIGH_SPEED_THRESHOLD = 4.0 // m/s

/**
 * @Author: Abdul Rehman
 * @Date: 06/05/2024.
 * Modified by Bruno Saad Marques
 */
@Preview
@Composable
fun App() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SpeedMeterGroup()

    }
}

@Composable
fun SpeedMeterGroup(speedViewModel: SpeedViewModel = viewModel()) {

    val speed = speedViewModel.speed.collectAsState()
    Log.d("SpeedMeter", "SpeedMeter: ${speed.value}")

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // In landscape, place the meters side by side in a Row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeedMeter(speed.value)
            SpeedMeter(speed.value, 3.6f, "km/h", 48.sp, 28.sp)
        }
    } else {
        // In portrait, keep the meters stacked in a Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SpeedMeter(speed.value)
            Spacer(modifier = Modifier.padding(48.dp))
            SpeedMeter(speed.value, 3.6f, "km/h", 48.sp, 28.sp)
        }
    }
}

@Composable
fun SpeedMeter(
    value: Float = 0F,
    multiplier: Float = 2.23694f,
    unit: String = "mph",
    fontSizeValue: TextUnit = 128.sp,
    fontSizeUnit: TextUnit = 48.sp
) {
    val speedMph = "${(value * multiplier * 10).roundToInt() / 10.0}"
    val textColor = if (value < HIGH_SPEED_THRESHOLD) Color(0xFFFFBF00) else Color.White // Amber for values lower than 4

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = speedMph,
            fontSize = fontSizeValue,
            color = textColor,
            fontWeight = FontWeight.Black
        )
        Text(
            text = unit,
            fontSize = fontSizeUnit,
            color = Color.Gray
        )
    }
}