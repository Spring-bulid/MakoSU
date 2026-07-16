package com.sukisu.ultra.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false

    // VALIDATED depends on Android reaching its own probe endpoints. On VPNs,
    // private DNS, and some regional networks that can be false even when the
    // configured module catalog is reachable. Let the actual HTTP request make
    // the final reachability decision instead.
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
