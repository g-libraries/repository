package com.core.repository.repository

import retrofit2.Response


class DataSourceResponse<T> {
    var result: T? = null
    var isSuccessful: Boolean = true
    var error: DataSourceError? = null

    fun convertToDataSource(responseAPI: Response<T>): DataSourceResponse<T> {
        return if (responseAPI.isSuccessful) {
            successful(responseAPI.body()!!)
        } else {
            unSuccessful(responseAPI.code(), responseAPI.message())
        }
    }

    fun unSuccessful(code: Int, message: String): DataSourceResponse<T> {
        isSuccessful = false
        error = DataSourceError(code, message)
        return this
    }

    fun successful(body: T): DataSourceResponse<T> {
        result = body
        return this
    }


    fun getResultSafe(
        resultSuccessful: (T) -> Unit,
        resultUnsuccessful: (DataSourceError) -> Unit,
        resultIsNull: (Int) -> Unit,
        errorIsNull: (Int) -> Unit
    ) {
        if (isSuccessful) {
            result?.let {
                resultSuccessful(it)
            } ?: let {
                //todo integrate base error
                resultIsNull(-1)
            }
        } else {
            error?.let {
                resultUnsuccessful(it)
            } ?: let {
                //todo integrate base error
                errorIsNull(-1)
            }
        }
    }
}

