package com.core.repository.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class ResponseErrorLiveData : MutableLiveData<DataSourceError>() {
    fun observe(
        owner: LifecycleOwner,
        error: (String) -> Unit,
        serverError: (DataSourceError) -> Unit = {},
        internalError: (Throwable) -> Unit = {}
    ) {
        super.observe(owner, ResponseErrorObserver(error, serverError, internalError))
    }
}

class ResponseErrorObserver(
    val error: (String) -> Unit,
    val serverError: (DataSourceError) -> Unit = {},
    val internalError: (Throwable) -> Unit = {}
) : Observer<DataSourceError> {
    override fun onChanged(t: DataSourceError) {
        if (t.serverError) {
            serverError.invoke(t)
        } else {
            t.throwable?.let {
                internalError.invoke(it)
            }
        }

        error.invoke(t.errorMessage)
    }
}