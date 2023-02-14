package com.example.plugins.debug

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val status: Int,
    val method: String,
    val uri: String,
    val headers: String,
    val body: String?,
    val processing: Long,
) {
    override fun toString(): String {
        return "--> " + "$method $uri" + " <-- $status ${processing}ms\n\n" +
                "$headers\n\n" +
                (if (body != null) "$body\n\n" else "") +
                "<-- $status; ${uri}; ${processing}ms"
    }

    companion object {
        suspend fun make(call: ApplicationCall) = LogEntry(
            status = call.response.status()?.value ?: 0,
            method = call.request.httpMethod.value,
            uri = call.request.uri,
            headers = call.request.headers.entries().joinToString("\n") { entry ->
                entry.value.joinToString("\n") { value ->
                    "${entry.key}: $value"
                }
            },
            body = if (call.request.httpMethod == HttpMethod.Get)   // todo other requests without body
                null
            else
                call.receiveText(),
            processing = call.processingTimeMillis()
        )
    }
}
