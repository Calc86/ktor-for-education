package com.example.plugins

import com.example.Config
import com.example.api.ApiError.Companion.error
import com.example.api.ApiException
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.resources.*
import io.ktor.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*

fun Application.configureRouting() {
    install(AutoHeadResponse)
    install(Resources)

    install(StatusPages) {
        val api = Config.api(this@configureRouting)
        exception<ApiException> { call, cause ->
            call.respond(cause.status, cause.error(false))
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.error(api.traceError))
            //call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get<Articles> { article ->
            // Get all articles ...
//            call.respond(article)
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
