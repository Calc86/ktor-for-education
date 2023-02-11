package com.example.api

import com.example.api.models.User
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.mamoe.yamlkt.Yaml

object Static {
    fun preload(app: Application): Api {
        val inputStream = app.environment.classLoader.getResourceAsStream("app/static_api.yaml")
        val data = String(inputStream.readAllBytes())
        return Yaml().decodeFromString<Api>(data)
    }

    @Serializable
    data class Api(
        val users: List<User>
    )
}
