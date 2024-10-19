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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * @Author: Abdul Rehman
 * @Date: 06/05/2024.
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = speedMph,
            fontSize = fontSizeValue,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Text(
            text = unit,
            fontSize = fontSizeUnit,
            color = Color.Gray
        )
    }
}