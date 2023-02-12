package com.example.api

import io.ktor.http.*

open class HttpException(val status: HttpStatusCode, message: String?) : Exception(message)
open class NotFoundException(message: String? = "not found") : HttpException(HttpStatusCode.NotFound, message)
class UserNotFound() : HttpException(HttpStatusCode.Forbidden, "user not found")
class ProfileNotFound() : NotFoundException("profile not found")
