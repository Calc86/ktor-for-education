package com.example.plugins

import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.Config
import com.example.api.configureApi
import io.ktor.http.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {

    authentication {
        basic(name = "myauth1") {
            realm = "Ktor Server"
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }

        form(name = "myauth2") {
            userParamName = "user"
            passwordParamName = "password"
            challenge {
                /**/
            }
        }
    }
    authentication {
        val jwt = Config.jwt(this@configureSecurity)
        jwt(jwt.name) {
            realm = jwt.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwt.secret))
                    .withAudience(jwt.audience)
                    .withIssuer(jwt.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwt.audience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        authenticate("myauth1") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        authenticate("myauth2") {
            get("/protected/route/form") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}
