package com.core.repository.repository

import retrofit2.Response


class DataSourceResponse<T> {
    var result: T? = null
    var isSuccessful: Boolean = true
    var error: DataSourceError<T>? = null

    fun convertToDataSource(responseAPI: Response<T>): DataSourceResponse<T> {
        return if (responseAPI.isSuccessful) {
            successful(responseAPI.body()!!)
        } else {
            unSuccessful(responseAPI.code(), responseAPI.message(), true)
        }
    }

    fun unSuccessful(code: Int, message: String, serverError: Boolean): DataSourceResponse<T> {
        isSuccessful = false
        error = DataSourceError(code, message, serverError)
        return this
    }

    fun successful(body: T): DataSourceResponse<T> {
        result = body
        return this
    }


    fun getResultSafe(
        resultSuccessful: (T) -> Unit,
        resultUnsuccessful: (DataSourceError<T>) -> Unit,
        resultIsNull: (Int) -> Unit = {},
        errorIsNull: (Int) -> Unit = {}
    ) {
        if (isSuccessful) {
            result?.let {
                resultSuccessful(it)
            } ?: resultIsNull(-1)
        } else {
            error?.let {
                resultUnsuccessful(it)
            } ?: errorIsNull(-1)
        }
    }
}

