package com.core.repository.repository

import android.content.res.Resources
import com.core.base.usecases.Event
import com.core.repository.R
import com.core.repository.exceptions.ParseException
import retrofit2.Response
import timber.log.Timber
import com.google.gson.Gson

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val dataSourceError: DataSourceError) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$dataSourceError]"
            Loading -> "Loading"
        }
    }

    fun check(
        loading: () -> Unit,
        resultSuccessful: (R) -> Unit,
        resultUnsuccessful: (Event<DataSourceError>) -> Unit
    ) {
        when (this) {
            is Loading -> {
                loading.invoke()
            }
            is Success -> {
                resultSuccessful.invoke(data)
            }
            is Error -> {
                resultUnsuccessful.invoke(Event(dataSourceError))
            }
        }
    }
}

fun <T : Any> Response<T>.convert(): Result<T> {
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

    return if (this.isSuccessful) {
        Result.Success(this.body()!!)
    } else {
        val dataSourceError: DataSourceError = try {
            this.errorBody()?.string()?.let {
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

fun <T, R> Result<T>.map(map: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> {
            Result.Success(map(this.data))
        }
        is Result.Loading -> {
            Result.Loading
        }
        is Result.Error -> {
            Result.Error(this.dataSourceError)
        }
    }
}

fun <T : Any, R> Response<T>.convertAndMap(map: (T) -> R): Result<R> {
    return this.convert().map(map)
}