package com.core.repository.repository

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.core.repository.repository.DataSourceError
import com.core.repository.repository.IErrorHandler
import retrofit2.Response

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
        error = DataSourceError(code, message, serverError)
        return this
    }

    fun successful(body: T): DataSourceResponse<T> {
        result = body
        return this
    }

    fun getResultSafe(
        resultSuccessful: (DataSourceResponse<T>) -> Unit,
        resultUnsuccessful: (DataSourceError) -> Unit,
        resultIsNull: (Int) -> Unit = {},
        errorIsNull: (Int) -> Unit = {}
    ) {
        if (isSuccessful) {
            result?.let {
                resultSuccessful(this)
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

class ResponseLiveData<T : Any> : MutableLiveData<DataSourceResponse<T>>() {
    fun observe(
        owner: LifecycleOwner,
        success: Success<T>,
        error: Error,
        errorHandler: IErrorHandler
    ) {
        val lifecycleOwner = (owner as? Fragment)?.viewLifecycleOwner ?: owner
        super.observe(lifecycleOwner, ResponseObserver(success, error, errorHandler))
    }
}

class ResponseObserver<T : Any>(
    private val success: Success<T>,
    private val error: Error,
    private val errorHandler: IErrorHandler
) : Observer<DataSourceResponse<T>> {
    override fun onChanged(response: DataSourceResponse<T>?) {
        response?.let {
            if (response.isSuccessful) {
                success(response)
            } else {
                response.error?.let {
                    error.invoke(response.error!!)
                    errorHandler.handleServerError(response.error!!.errorCode)
                }
            }
        }
    }
}

typealias Success<T> = (data: DataSourceResponse<T>) -> Unit

typealias Error = (error: DataSourceError) -> Unit