package com.saadbruno.gpsspeedvolumecontrol

import android.location.Location


/**
 * @Author: Abdul Rehman
 * @Date: 06/05/2024.
 */
interface LocationUpdatesCallBack {
    fun onLocationUpdate(location: Location)
    fun locationException(message: String)
}