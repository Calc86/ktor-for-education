package com.example.plugins

//import io.ktor.serialization.kotlinx.json.*
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.serialization.gson.*
import io.ktor.serialization.gson.gson
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.routing.*

fun Application.configureSerialization() {
    install(Pebble) {   // todo move to debug plugin??
        loader(ClasspathLoader().apply {
            prefix = "debug"
        })
    }
    install(ContentNegotiation) {
        json()
//        gson {
//        }
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
//        get("/json/gson") {
//            call.respond(mapOf("hello" to "world"))
//        }
    }
}
