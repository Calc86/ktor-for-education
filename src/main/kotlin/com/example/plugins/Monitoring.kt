package com.example.plugins

import com.example.plugins.debug.CallLoggingFilter
import com.example.plugins.debug.DebugLog
import com.example.plugins.debug.LogEntry
import io.ktor.server.plugins.callloging.*
import org.slf4j.event.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.micrometer.prometheus.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking

fun Application.configureMonitoring() {
    install(DebugLog.plugin) // {}
    install(DoubleReceive) {
        // if you do not need to receive different types from the same request or receive a stream or channel
        //cacheRawRequest = false
    }
    install(CallLogging, DebugLog::configCallLogging)
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate(16, "abcdefghijklmnopqrstuvwxyz0123456789")    // generate if no call id in header
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

//    install(DropwizardMetrics) {
//        Slf4jReporter.forRegistry(registry)
//            .outputTo(this@module.log)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build()
//            .start(10, TimeUnit.SECONDS)
//    }
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // ...
    }
    routing {
        get("/metrics-micrometer") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}

