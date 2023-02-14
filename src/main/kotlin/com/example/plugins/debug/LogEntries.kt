package com.example.plugins.debug

import io.ktor.util.logging.*
import java.io.File

class LogEntries(private val dir: String) {
    private val log = KtorSimpleLogger("LogEntries")
    private val _requests = mutableSetOf<Entry>()
    val requests: Set<Entry>
        get() = synchronized(this) {
            return _requests
        }

    data class Entry(
        val id: String,
        private val dir: String
    ) {
        private val log = KtorSimpleLogger("LogEntries.Entry")
        private var _line: LogLine? = null

        val request: File
        val debug: File
        val timestamp: Long
        val line: LogLine?
            get() {
                if (_line == null) _line = LogLine.from(request)
                return _line
            }

        init {
            request = File("$dir/$id.request.log")
            timestamp = request.lastModified()
            debug = File(request.path.replace(".request.", ".debug."))
        }

        fun isProper() = request.exists() && debug.exists() && line != null

        fun clean() {
            if (!isProper()) delete()
        }

        fun delete() {
            log.trace("delete $id")
            request.delete()
            debug.delete()
        }
    }

    fun clean(limit: Int): Unit = synchronized(this) {
        if (_requests.size < limit) return
        log.trace("clean for limit: $limit")

        val requests = _requests
            .sortedByDescending { it.timestamp }
            .subList(0, limit)
        _requests
            .sortedByDescending { it.timestamp }
            .subList(limit, _requests.size - 1)
            .forEach(Entry::delete)
        _requests.clear()
        _requests.addAll(requests)
    }

    fun update() = synchronized(this) {
        log.trace("update")
        val exclude = "${dir}/"
        fun filter(name: String) = name.contains(".request.")
                && !name.startsWith("app.")
                && !name.startsWith(".")
        fun mapName(name: String) = name.replace(exclude, "")

        val requests = File(dir).walk()
            .filter { !it.isDirectory }
            .map { it.path }
            .map(::mapName)
//            .filter(::filter)
            .filter {name ->
                name.contains(".request.")
                        && !name.startsWith("app.")
                        && !name.startsWith(".")
            }
            .map {
                it.split('.')[0]
            }
            .toSet()
            .map { Entry(it, dir) }
            .filter {
                if (it.isProper())
                    true
                else {
                    it.delete() // remove unused log
                    false
                }
            }
        this._requests.addAll(requests)
    }
}
