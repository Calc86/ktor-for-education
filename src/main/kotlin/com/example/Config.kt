package com.example

import io.ktor.server.application.*

object Config {
    private const val jwtAudience = "jwt.audience"
    private const val jwtRealm = "jwt.realm"
    private const val jwtIssuer = "jwt.domain"
    private const val jwtSecret = "jwt.secret"
    private const val jwtTtl = "jwt.ttl"

    fun jwt(app: Application) = JWT(
        name = "auth-jwt",
        audience = get(app, jwtAudience),
        realm = get(app, jwtRealm),
        issuer = get(app, jwtIssuer),
        secret = get(app, jwtSecret),
        ttl = getInt(app, jwtTtl)
    )

    fun get(app: Application, name: String): String {
        return app.environment.config.property(name).getString()
    }

    fun getInt(app: Application, name: String) = get(app, name).toInt()

    data class JWT(
        val name: String,
        val audience: String,
        val realm: String,
        val issuer: String,
        val secret: String,
        val ttl: Int,
    )
}
