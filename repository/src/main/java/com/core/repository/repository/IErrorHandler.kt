package com.core.repository.repository

interface IErrorHandler {
    fun handleInternalError(throwable: Throwable)
    fun handleServerError(code: Int)
}