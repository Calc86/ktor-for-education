package com.example.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.Config
import com.example.api.models.requests.Auth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureApi() {
    val jwt = Config.jwt(this)
    val static = Static.preload(this)


    routing {
        post("/api/auth") {
            val auth = call.receive<Auth>()
            static.auth.firstOrNull {
                it.login == auth.login && it.password == auth.password
            }?.let { user ->
                val token = JWT.create()
                    .withAudience(jwt.audience)
                    .withIssuer(jwt.issuer)
                    .withClaim("id", user.id)
                    .withExpiresAt(Date(System.currentTimeMillis() + jwt.ttl))
                    .sign(Algorithm.HMAC256(jwt.secret))
                call.respond(hashMapOf("token" to token))   // {"token": "some value"}
            } ?: call.respond(HttpStatusCode.Unauthorized, "user not found")
        }
        authenticate(jwt.name) {
            get("/api/profile") {
                val principal = call.principal<JWTPrincipal>()!!
                val id = principal.payload.getClaim("id")?.asInt()
                static.profiles.firstOrNull {
                    it.id == id
                }?.let {
                    call.respond(it)
                } ?: call.respond(HttpStatusCode.NotFound, "profile not found")

                //val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                //call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
        }
    }
}
