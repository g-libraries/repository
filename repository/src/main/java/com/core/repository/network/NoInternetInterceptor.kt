package com.core.repository.network

import android.content.Context
import com.core.repository.exceptions.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

class NoInternetInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isOnline(context))
            throw NoConnectivityException()

        return chain.proceed(chain.request())
    }
}