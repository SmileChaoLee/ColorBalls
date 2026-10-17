package com.smile.reversi.network

import android.Manifest
import android.os.Build

object NetPermissions {
    /** Runtime permissions needed by Nearby Connections on this Android version. */
    fun nearby(): Array<String> = when {
        // Android 17+ (API 37) needs ACCESS_LOCAL_NETWORK for Nearby's Wi-Fi LAN medium
        Build.VERSION.SDK_INT >= 37 -> arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            "android.permission.ACCESS_LOCAL_NETWORK"
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        else -> arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
