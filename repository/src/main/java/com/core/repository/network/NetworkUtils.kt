package com.core.repository.network

import android.content.Context
import android.net.ConnectivityManager
import com.core.repository.exceptions.NoConnectivityException
import com.core.repository.exceptions.ParseException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
}

fun CoroutineScope.launchSafe(
    onError: (Throwable) -> Unit = {},
    onSuccess: suspend () -> Unit
): Job {
    val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
        //todo handle other exceptions
        when (throwable) {
            is NoConnectivityException -> onError(throwable)
            is ParseException -> onError(throwable)
            else -> onError(throwable)
        }
    }

    return launch(handler) {
        onSuccess()
    }
}