package com.core.repository.exceptions

import java.io.IOException

class NoConnectivityException : IOException() {
    override val message: String
        get() = "No connectivity exception"
}

class ParseException(override val message: String = "Parse exception") : IOException()