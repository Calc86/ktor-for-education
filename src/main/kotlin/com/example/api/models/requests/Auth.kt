package com.example.api.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class Auth (
    val login: String,
    val password: String,
    val id: Int = 0
)
