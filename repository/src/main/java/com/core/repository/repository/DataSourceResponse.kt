package com.core.repository.repository

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
            val dataSourceError = try {
                Gson().fromJson(
                    responseAPI.errorBody()?.charStream(),
                    DataSourceError::class.java
                )
            } catch (e: Exception) {
                unSuccessful(-1, e.localizedMessage, false)
                error?.throwable = e
                error
            }

            unSuccessful(dataSourceError!!, true)
        }
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