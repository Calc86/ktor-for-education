package com.example.plugins.debug

import com.example.api.ApiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level
import java.io.File

/**
 * https://ktor.io/docs/custom-plugins-base-api.html
 */
class DebugLog(
    private val configuration: DebugLogConfiguration,
    private val application: Application,
) {
    private val log = configuration.log ?: KtorSimpleLogger("DebugLog")

    private var logrotateDebounce = 0

    private val entries = LogEntries(configuration.directory)

    class DebugLogConfiguration {
        /** Провайдер авторизации */
        val auth = "debug"

        /** Создать провайдер авторизации по умолчанию */
        val authCreate = true

        /** Логин доступа к дебаг логу по web */
        val user = "admin"

        /** Пароль доступа к дебаг логу по web */
        val password = "debug"

        /** Путь к дашборду */
        val route = "/debug"

        /** Logback путь, где лежат "раздельные" логи */
        var directory: String = "./runtime/debug"

        /** Ограничение количества лог файлов */
        var logLimit = 10

        /** Как часто очищать лог файлы (раз в количество запросов) */
        var logRotateDebounce = 10
        var log: Logger? = null
    }

    private fun config() = with(application) {
        authentication {
            if (configuration.authCreate) basic(name = configuration.auth) {
                realm = "Ktor Server Debug"
                validate { credentials ->
                    if (credentials.name == configuration.user
                        && credentials.password == configuration.password
                    ) {
                        UserIdPrincipal(configuration.user)
                    } else {
                        null
                    }
                }
            }
        }
        routing {
            fun entry(call: ApplicationCall): LogEntries.Entry {
                val id = call.parameters["id"] ?: throw ApiException(HttpStatusCode.BadRequest, "/id not set")
                return entries.requests.firstOrNull {
                    it.id == id
                } ?: throw ApiException(HttpStatusCode.NotFound, "request not found")
            }
            route(configuration.route) {
                authenticate(configuration.auth) {
                    get("/test") {
                        entries.update()
                        entries.clean(configuration.logLimit)
                        call.respond(PebbleContent("requests.html", mapOf("requests" to entries.requests.map { it.line }.toList())))
                        //call.respond(PebbleContent("index.html", mapOf("content" to "content here")))
                    }
                    get {
                        entries.update()
                        entries.clean(configuration.logLimit)
                        call.respond(PebbleContent("requests.html", mapOf("requests" to entries.requests.map { it.line }.toList())))
                    }
                    get("/{id}") {
                        call.respondFile(entry(call).request)
                    }
                    get("/{id}/trace") {
                        call.respondFile(entry(call).debug)
                    }
                }
            }
        }
    }

    private fun logRotate() {
        logrotateDebounce++
        if (logrotateDebounce % configuration.logRotateDebounce != 0) return
        entries.update()
        entries.clean(configuration.logLimit) // limit log files
    }

    companion object {
        val plugin = createApplicationPlugin(name = "DebugLogPlugin", DebugLog::DebugLogConfiguration) {
            pluginConfig.log = KtorSimpleLogger(DebugLog::class.java.toString())
            val instance = DebugLog(pluginConfig, application).apply { config() }

            on(ResponseSent) {
                instance.logRotate()
            }
        }

        private val logger = KtorSimpleLogger(CallLoggingFilter.name)

        private fun CallLoggingConfig.mdcHeader(name: String, header: String, default: String? = null) =
            mdc(name) { call ->
                call.request.header(header) ?: (default ?: "unknown")
            }

        private fun CallLoggingConfig.mdcDevice() = mdcHeader("device", "x-device")
        private fun CallLoggingConfig.mdcDeviceId() = mdcHeader("device_id", "x-device-id")

        private fun CallLoggingConfig.formatDebug() = format { call ->
            runBlocking { LogEntry.make(call).toString() }
        }

        private fun CallLoggingConfig.filterDebug() = filter { call ->
            call.request.path().startsWith("/")
                    && !call.request.path().startsWith("/debug")    // disable for development
        }

        private fun CallLoggingConfig.mdcFrom() = mdc("from") { call -> call.request.origin.remoteHost }

        fun configCallLogging(config: CallLoggingConfig) = with(config) {
            logger = DebugLog.logger
            level = Level.TRACE
            filterDebug()
            callIdMdc("call-id")
            mdcFrom()
            mdcDevice()
            mdcDeviceId()
            formatDebug()
        }
    }
}
