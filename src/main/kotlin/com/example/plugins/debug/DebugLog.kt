package com.example.plugins.debug

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.logging.*

/**
 * https://ktor.io/docs/custom-plugins-base-api.html
 */
class DebugLog(configuration: DebugLogConfiguration) {
    private val log = configuration.log ?: KtorSimpleLogger(DebugLog::class.java.toString())

    class DebugLogConfiguration {
        val route = "/debug"
        var directory: String = "./runtime/debug"
        var enabled: Boolean = true
        var log: Logger? = null
    }

    private fun onCallReceive(call: ApplicationCall, body: Any? = null) {
        log.trace("call 0")
        call.request.headers.forEach { name, values ->
            log.trace(call.request.uri)
            log.trace("Request headers:")
            values.forEach { value -> log.trace("$name: $value") }
        }
    }

    private fun onCallRespond(call: ApplicationCall, body: Any? = null) {
        log.trace("call 1")
    }

    companion object {
        val plugin = createApplicationPlugin(name = "DebugLogPlugin", DebugLog::DebugLogConfiguration) {
            pluginConfig.log = KtorSimpleLogger(DebugLog::class.java.toString())
            val instance = DebugLog(pluginConfig)

            onCall { call ->
                instance.onCallReceive(call, null)
            }

            onCallReceive { call ->
                instance.onCallReceive(call, null)
            }

            onCallReceive { call, body ->
                instance.onCallReceive(call, body)
            }

            onCallRespond { call ->
                instance.onCallRespond(call)
            }

            onCallRespond { call, body ->
                instance.onCallRespond(call, body)
            }
        }
    }
}
