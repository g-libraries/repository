package com.core.repository.repository

data class DataSourceError<T>(
    var errorCode: Int = -1,
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