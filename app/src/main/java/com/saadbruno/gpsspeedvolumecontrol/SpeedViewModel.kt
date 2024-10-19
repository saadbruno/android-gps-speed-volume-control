package com.saadbruno.gpsspeedvolumecontrol

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpeedViewModel : ViewModel() {
    private val _speed = MutableStateFlow(0f) // Initial speed
    val speed: StateFlow<Float> get() = _speed

    fun updateSpeed(newSpeed: Float) {
        Log.d("SpeedViewModel", "Updating speed to $newSpeed")
        viewModelScope.launch {
            _speed.value = newSpeed
        }
    }
}