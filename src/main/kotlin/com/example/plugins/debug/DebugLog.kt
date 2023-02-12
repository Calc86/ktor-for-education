package com.example.plugins.debug

import io.ktor.server.application.*

/**
 * https://ktor.io/docs/custom-plugins.html
 */
val DebugLogPlugin = createApplicationPlugin(name = "DebugLogPlugin") {
    val log = application.log

    onCall { call ->
        log.trace("onCall")
    }

    onCallReceive { call ->
        log.trace("onCallReceive 1")
    }

    onCallReceive { call, body ->
        log.trace("onCallReceive 2")
    }

    onCallRespond { call ->
        log.trace("onCallRespond 1")
    }

    onCallRespond { call, body ->
        log.trace("onCallRespond 2")
    }
}
