package com.core.repository.repository

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.core.repository.repository.DataSourceError
import com.core.repository.repository.IErrorHandler
import retrofit2.Response
import timber.log.Timber


class DataSourceResponse<T> {
    var result: T? = null
    var isSuccessful: Boolean = true
    var error: DataSourceError? = null

    fun convertToDataSource(responseAPI: Response<T>): DataSourceResponse<T> {
        return if (responseAPI.isSuccessful) {
            successful(responseAPI.body()!!)
        } else {
            unSuccessful(responseAPI.code(), responseAPI.message(), true)
        }
    }

    fun unSuccessful(code: Int, message: String, serverError: Boolean): DataSourceResponse<T> {
        isSuccessful = false
        error = DataSourceError(code, message, serverError, null)
        return this
    }

    fun successful(body: T): DataSourceResponse<T> {
        result = body
        return this
    }

    fun getResultSafe(
        resultSuccessful: (T) -> Unit,
        resultUnsuccessful: (DataSourceError) -> Unit,
        resultIsNull: (Int) -> Unit = {},
        errorIsNull: (Int) -> Unit = {}
    ) {
        if (isSuccessful) {
            result?.let {
                resultSuccessful(it)
            } ?: resultIsNull(-1)
        } else {
            error?.let {
                Timber.e(it.throwable)
                resultUnsuccessful(it)
            } ?: errorIsNull(-1)
        }
    }
}