package com.core.repository.repository

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DataSourceError(
    @SerializedName("code")
    @Expose
    var errorCode: Int = -1,
    @SerializedName("errors")
    @Expose
    var errorMessage: String = "",
    var serverError: Boolean = true,
    var throwable: Throwable?
) {
    constructor(errorCode: Int, serverError: Boolean, throwable: Throwable) : this(
        errorCode,
        throwable.message ?: "",
        serverError,
        throwable
    )
}