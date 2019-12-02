package com.core.repository.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.core.base.usecases.Event

class ResponseErrorLiveData : MutableLiveData<Event<DataSourceError>>() {
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
) : Observer<Event<DataSourceError>> {
    override fun onChanged(event: Event<DataSourceError>) {
        event.getContentIfNotHandled()?.let { content ->
            if (content.serverError) {
                serverError.invoke(content)
            } else {
                content.throwable?.let {
                    internalError.invoke(it)
                }
            }

            error.invoke(content.errorMessage)
        }
    }
}