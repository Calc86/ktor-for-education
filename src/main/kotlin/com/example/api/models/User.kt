package com.example.api.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val name: String,
    val surname: String,
)
