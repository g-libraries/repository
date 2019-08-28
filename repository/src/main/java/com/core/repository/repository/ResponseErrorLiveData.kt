package com.core.repository.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class ResponseErrorLiveData<T> : MutableLiveData<DataSourceError<T>>() {
    fun observe(
        owner: LifecycleOwner,
        error: (String) -> Unit,
        serverError: (Int) -> Unit = {},
        internalError: (Throwable) -> Unit = {}
    ) {
        super.observe(owner, ResponseErrorObserver(error, serverError, internalError))
    }
}

class ResponseErrorObserver<T>(
    val error: (String) -> Unit,
    val serverError: (Int) -> Unit = {},
    val internalError: (Throwable) -> Unit = {}
) : Observer<DataSourceError<T>> {
    override fun onChanged(t: DataSourceError<T>) {
        if (t.serverError) {
            serverError.invoke(t.errorCode)
        } else {
            t.throwable?.let {
                internalError.invoke(it)
            }
        }

        error.invoke(t.errorMessage)
    }
}