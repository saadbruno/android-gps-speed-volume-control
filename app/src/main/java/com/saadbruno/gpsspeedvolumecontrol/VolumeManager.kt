package com.saadbruno.gpsspeedvolumecontrol

import android.content.Context
import android.media.AudioManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author: Bruno Saad Marques
 * @Date: 10/19/2024.
 * This file will take the speed provided by GPS and manage the volume of the media on the device
 */

private const val DEBUG = false
private const val PREFERENCES_FILE_NAME = "volume_preferences"
private const val HIGH_SPEED_THRESHOLD = 4.0 // m/s
private const val VOLUME_PREFERENCE_KEY = "volume_preference"

private var setVolume = 0
private var targetVolume = 0
private var volumeChangeInProgress = false
private var launched = false

class VolumeManager(private val context: Context) {

    fun updateVolume(speed: Float) {

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Determine the target volume based on the speed state
        targetVolume = if (speed >= HIGH_SPEED_THRESHOLD) {
            // High speed state
            getHighSpeedVolume(context)
        } else {
            // Low speed state
            getLowSpeedVolume(context)
        }

        Log.d("Volume Manager", "Speed: $speed, Current Volume: $setVolume, Current System Volume: $currentVolume, Target Volume: $targetVolume, MaxVolume: $maxVolume, Volume Change In Progress: $volumeChangeInProgress")


        // Start a coroutine to gradually change the volume if necessary
        if (!volumeChangeInProgress && setVolume != targetVolume) {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("Volume Manager", "Starting volume change")
                volumeChangeInProgress = true
                while (setVolume != targetVolume) {
                    if (setVolume < targetVolume) {
                        setVolume++
                    } else {
                        setVolume--
                    }
                    // Update the actual volume
                    setSystemVolume(context, setVolume)
                    delay(500) // Adjust the delay as needed for a smooth transition
                }
                volumeChangeInProgress = false
            }
        }

        // handles user changing the volume manually, but only after the app has been launched
        if (setVolume != currentVolume) {
            if (launched) {
                Log.d("Speedometer", "User changed volume manually. Updating preferences, and also setting setVolume to currentVolume")
                setVolume = currentVolume // this prevents the volume from going back to the previous value when the user changes it manually
                storeUserVolume(context, currentVolume, speed) // stores the current volume for the current state
            } else {
                launched = true
            }
        }

    }
}

// Function to set the system volume
// This function will be called whenever the device crosses the speed threshold.
// It will be called every 500ms until we reach the target volume
private fun setSystemVolume(context: Context, volume: Int) {
    Log.d("Volume Manager", "Setting system volume to $volume")
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Set the actual system volume
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}

// this function will store the volume in the device preferences whenever the user manually clicks the volume buttons
private fun storeUserVolume(context: Context, volume: Int, speed: Float) {
    if (speed >= HIGH_SPEED_THRESHOLD) {
        setHighSpeedVolume(context, volume)

        // if low speed volume is higher than high speed volume, lower it to match
        val lowSpeedVolume = getLowSpeedVolume(context)
        if (volume < lowSpeedVolume) {
            setLowSpeedVolume(context, volume)
        }
    } else {
        setLowSpeedVolume(context, volume)

        // if the high speed volume is lower than the low speed volume, raise it to match
        val highSpeedVolume = getHighSpeedVolume(context)
        if (volume > highSpeedVolume) {
            setHighSpeedVolume(context, volume)
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

// stores the high speed volume
private fun setHighSpeedVolume(context: Context, volume: Int) {
    Log.d("Volume Manager", "Storing high speed volume: $volume")
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    sharedPreferences.edit().putInt("${VOLUME_PREFERENCE_KEY}_high", volume).apply()
}

// stores the low speed volume
private fun setLowSpeedVolume(context: Context, volume: Int) {
    Log.d("Volume Manager", "Storing low speed volume: $volume")
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    sharedPreferences.edit().putInt("${VOLUME_PREFERENCE_KEY}_low", volume).apply()
}