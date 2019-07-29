package com.core.repository.repository

data class DataSourceError(var errorCode: Int = -1, var errorMessage: String = "", var serverError: Boolean = true)