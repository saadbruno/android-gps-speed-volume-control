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

    init {
        Log.d("SpeedViewModel", "ViewModel instance created. Hash code: ${System.identityHashCode(this)}")
    }

    fun updateSpeed(newSpeed: Float) {
        Log.d("SpeedViewModel", "Updating speed to $newSpeed (Hash code: ${System.identityHashCode(this)})")
        viewModelScope.launch {
            _speed.value = newSpeed
        }
    }
}