package com.example.plugins.debug

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.AbstractMatcherFilter
import ch.qos.logback.core.spi.FilterReply

class CallLoggingFilter(
    private val filterName: String = name
) : AbstractMatcherFilter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        if (!isStarted) {
            return FilterReply.NEUTRAL
        }

        return if (event.loggerName == filterName) {
            onMatch
        } else {
            onMismatch
        }
    }

    companion object {
        const val name = "CallLogging"
    }
}
