package com.core.repository.network

import android.content.Context
import android.net.ConnectivityManager
import com.core.repository.exceptions.NoConnectivityException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}

fun CoroutineScope.launchSafe(
    onError: (Throwable) -> Unit = {},
    onSuccess: suspend () -> Unit
) {
    val handler = CoroutineExceptionHandler { _, throwable ->
        //todo handle other exceptions
        if (throwable is NoConnectivityException)
            onError(throwable)
    }

    launch(handler) {
        onSuccess()
    }
}