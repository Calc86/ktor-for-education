package com.example.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.Config
import com.example.api.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureApi() {
    val jwt = Config.jwt(this)
    val static = Static.preload(this)

    routing {
        post("/api/auth") {
            val user = call.receive<User>()
            static.users.firstOrNull {
                it.name == user.name && it.password == user.password
            }?.let { authUser ->
                val token = JWT.create()
                    .withPayload(hashMapOf("id" to authUser.id))
                    .withAudience(jwt.audience)
                    .withIssuer(jwt.issuer)
                    .withClaim("username", user.name)
                    .withExpiresAt(Date(System.currentTimeMillis() + jwt.ttl))
                    .sign(Algorithm.HMAC256(jwt.secret))
                call.respond(hashMapOf("token" to token))   // {"token": "some value"}
            } ?: call.respond(HttpStatusCode.Unauthorized, "user not found")
        }
    }
}
