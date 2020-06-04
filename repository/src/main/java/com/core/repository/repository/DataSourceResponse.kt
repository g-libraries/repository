package com.core.repository.repository

import android.content.res.Resources
import com.core.base.usecases.Event
import com.core.repository.R
import com.core.repository.exceptions.ParseException
import retrofit2.Response
import timber.log.Timber
import com.google.gson.Gson


class DataSourceResponse<T> {
    var result: T? = null
    var isSuccessful: Boolean = true
    var error: DataSourceError? = null

    fun convertToDataSource(responseAPI: Response<T>): DataSourceResponse<T> {
        return if (responseAPI.isSuccessful) {
            successful(responseAPI.body()!!)
        } else {
            val dataSourceError: DataSourceError = try {
                responseAPI.errorBody()?.string()?.let {
                    //Checking for errors field. If no "errors" throw ParseException
                    if (!it.contains("errors")) {
                        throwParseException()
                    } else
                        Gson().fromJson(
                            responseAPI.errorBody()?.charStream(),
                            DataSourceError::class.java
                        )
                } ?: throwParseException()
            } catch (e: Exception) {
                unSuccessful(-1, e.localizedMessage, false)
                error?.throwable = e
                error
            } as DataSourceError

            unSuccessful(dataSourceError, true)
        }
    }

    fun throwParseException() {
        Timber.e("Server error object was different")
        throw ParseException(Resources.getSystem().getString(R.string.error_default))
    }

    fun unSuccessful(code: Int, message: String, serverError: Boolean): DataSourceResponse<T> {
        isSuccessful = false
        error = DataSourceError(code, message, serverError, null)
        return this
    }

    fun unSuccessful(
        dataSourceError: DataSourceError,
        serverError: Boolean
    ): DataSourceResponse<T> {
        isSuccessful = false
        dataSourceError.serverError = serverError
        error = dataSourceError
        return this
    }

    fun successful(body: T): DataSourceResponse<T> {
        result = body
        return this
    }

    fun getResultSafe(
        resultSuccessful: (T) -> Unit,
        resultUnsuccessful: (Event<DataSourceError>) -> Unit,
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
                resultUnsuccessful(Event(it))
            } ?: errorIsNull(-1)
        }
    }
}