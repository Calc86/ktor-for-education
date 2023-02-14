package com.example.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.Config
import com.example.api.models.requests.Auth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level
import java.lang.Exception
import java.util.*

fun Application.configureApi() {
    routing {
        route("/api") {
            get("/throw") {
                throw ApiException(HttpStatusCode.NotImplemented, "/throw route")
            }
            get("/throw500") {
                throw Exception("/throw500")
            }
            route("/v1") {
                route("/user") {
                    auth()
                    profile()
                }
            }
        }
    }
}

val Route.jwt
    get() = Config.jwt(this.application)
val Route.v1Api: Static.Api
    get() = Static.preload(this.application)

fun Route.auth() {
    post("/auth") {
        val auth = call.receive<Auth>()
        val user = this@auth.v1Api.auth.firstOrNull {
            it.login == auth.login && it.password == auth.password
        } ?: throw UserNotFound()
        val token = JWT.create()
            .withAudience(this@auth.jwt.audience)
            .withIssuer(this@auth.jwt.issuer)
            .withClaim("id", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + this@auth.jwt.ttl))
            .sign(Algorithm.HMAC256(this@auth.jwt.secret))
        call.respond(hashMapOf("token" to token))   // {"token": "some value"}
    }
}

fun Route.profile() {
    authenticate(jwt.name) {
        route("/profile") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val id = principal.payload.getClaim("id")?.asInt()
                val profile = this@profile.v1Api.profiles.firstOrNull {
                    it.id == id
                } ?: throw ProfileNotFound()
                call.respond(profile)
            }
            post {
                throw NotImplementedException()
            }
        }
    }
}
