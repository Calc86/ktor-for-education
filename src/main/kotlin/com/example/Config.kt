package com.example

import io.ktor.server.application.*

object Config {
    private const val jwtAudience = "jwt.audience"
    private const val jwtRealm = "jwt.realm"
    private const val jwtIssuer = "jwt.domain"
    private const val jwtSecret = "jwt.secret"
    private const val jwtTtl = "jwt.ttl"
    private const val traceError = "api.trace_500_error"

    fun jwt(app: Application) = JWT(
        name = "auth-jwt",
        audience = get(app, jwtAudience),
        realm = get(app, jwtRealm),
        issuer = get(app, jwtIssuer),
        secret = get(app, jwtSecret),
        ttl = getInt(app, jwtTtl)
    )

    fun api(app: Application) = Api(
        traceError = getBoolean(app, traceError)
    )

    private fun get(app: Application, name: String): String {
        return app.environment.config.property(name).getString()
    }

    private fun getInt(app: Application, name: String) = get(app, name).toInt()
    private fun getBoolean(app: Application, name: String) = get(app, name).toBoolean()

    data class JWT(
        val name: String,
        val audience: String,
        val realm: String,
        val issuer: String,
        val secret: String,
        val ttl: Int,
    )

    data class Api(
        val traceError: Boolean
    )
}
