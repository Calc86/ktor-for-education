package com.example.api

import io.ktor.http.*

open class ApiException(val status: HttpStatusCode, message: String?, val code: Int = 0) : Exception(message)
open class NotFoundException(message: String? = "not found", code: Int = 0) : ApiException(HttpStatusCode.NotFound, message, code)
class UserNotFound : ApiException(HttpStatusCode.Forbidden, "user not found")
class ProfileNotFound : NotFoundException("profile not found")
class NotImplementedException(message: String? = null) :
    ApiException(HttpStatusCode.NotImplemented, message ?: "Not implemented yet")
