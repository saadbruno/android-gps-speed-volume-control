package com.saadbruno.gpsspeedvolumecontrol

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * @Author: Abdul Rehman
 * @Date: 06/05/2024.
 */
@Preview
@Composable
fun App() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        BigSpeedMeter()

        Button(onClick = {
            //Start Service
            Toast.makeText(context, "Service Start button clicked", Toast.LENGTH_SHORT).show()
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_SERVICE_START
                context.startService(this)
            }
        }) {
            Text(text = "Start Service")
        }
        Spacer(modifier = Modifier.padding(12.dp))
        Button(onClick = {
            //Stop Service
            Toast.makeText(context, "Service Stop button clicked", Toast.LENGTH_SHORT).show()
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_SERVICE_STOP
                context.startService(this)
            }
        }) {
            Text(text = "Stop Service")

        }
    }
}

@Composable
fun BigSpeedMeter(speedViewModel: SpeedViewModel = viewModel()) {
    val speed = speedViewModel.speed.collectAsState()
    val speedMph = "${(speed.value * 2.23694f * 10).roundToInt() / 10.0}"
    Log.d("BigSpeedMeter", "BigSpeedMeter: ${speed.value} | $speedMph")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${speed.value}",
            fontSize = 128.sp,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "mph",
            fontSize = 48.sp,
            color = Color.Gray
        )
    }
}