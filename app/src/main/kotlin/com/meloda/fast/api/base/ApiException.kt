package com.meloda.fast.api.base

import com.meloda.fast.base.RestApiErrorDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okio.IOException



data class ApiException(override val message: String) : IOException()
