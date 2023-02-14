package com.example.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: Int = 0,
    val message: String = "Error",
) {
    companion object {
        fun Throwable.error(): ApiError {
            return when (this) {
                is ApiException -> ApiError(
                    code = this.code,
                    message = this.message ?: "Error",
                )

                else -> ApiError(
                    code = 0,
                    message = this.message ?: "Error",
                )
            }
        }
    }
}
