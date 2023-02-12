package com.example.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: Int = 0,
    val message: String = "Error",
    val trace: String? = null,
) {
    companion object {
        fun Throwable.error(withTrace: Boolean = false): ApiError {
            return when (this) {
                is ApiException -> ApiError(
                    code = this.code,
                    message = this.message ?: "Error",
                    trace = if (withTrace) this.stackTraceToString() else null
                )

                else -> ApiError(
                    code = 0,
                    message = this.message ?: "Error",
                    trace = if (withTrace) this.stackTraceToString() else null
                )
            }
        }
    }
}