package com.t3ddyss.clother.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager = context
        .getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun observeConnectivityStatus() = callbackFlow {
        trySend(isNetworkAvailable())

        val connectivityListener = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        connectivityManager?.registerDefaultNetworkCallback(connectivityListener)

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(connectivityListener)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return with(networkCapabilities) {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    }
}