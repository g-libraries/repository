package com.core.repository.repository

import android.content.res.Resources
import com.core.base.usecases.Event
import com.core.repository.R
import com.core.repository.exceptions.ParseException
import retrofit2.Response
import timber.log.Timber
import com.google.gson.Gson

sealed class Result<T> {

    data class Success<T>(val data: T) : Result<T>()
    data class Error(val dataSourceError: DataSourceError) : Result<DataSourceError>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$dataSourceError]"
            Loading -> "Loading"
        }
    }
}

fun <T : Any> Response<T>.convert() {
    fun throwParseException() {
        Timber.e("Server error object was different")
        throw ParseException("Server error")
    }

    fun unSuccessful(code: Int, message: String, serverError: Boolean): Result.Error {
        return Result.Error(DataSourceError(code, message, serverError, null))
    }

    fun unSuccessful(
        dataSourceError: DataSourceError,
        serverError: Boolean
    ): Result.Error {
        dataSourceError.serverError = serverError

        return Result.Error(dataSourceError)
    }

    fun successful(body: T): Result<T> {
        return Result.Success(body)
    }

    fun convert(responseAPI: Response<T>) {
        if (responseAPI.isSuccessful) {
            successful(responseAPI.body()!!)
        } else {
            val dataSourceError: DataSourceError = try {
                responseAPI.errorBody()?.string()?.let {
                    //Checking for errors field. If no "errors" throw ParseException
                    if (!it.contains("errors")) {
                        throwParseException()
                    } else
                        Gson().fromJson(
                            it,
                            DataSourceError::class.java
                        )
                } ?: throwParseException()
            } catch (e: Exception) {
                unSuccessful(-1, e.localizedMessage, true)
            } as DataSourceError

            unSuccessful(dataSourceError, true)
        }
    }

}