package com.example.plugins.debug

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
class DebugEntries(
    val debug: List<String>,
    val request: List<String>
) {
    val entries: List<Entry>

    init {
        entries = parse()
    }

    private fun parse(): List<Entry> {
        return request.map { File("$dir/$it") }.map { file -> Entry.make(file).also { entry ->
            if(entry == null) file.delete() // todo delete debug entry
        } }.filterNotNull()
            .sortedByDescending { it.timestamp }
    }

    @Serializable
    data class Entry(
        val timestamp: Long,
        val request: String,
        val debug: String,
        val status: Int,
        val id: String,
        val date: String,
        val time: String,
        val host: String,
        val method: String,
    ) {
        companion object {
            fun make(file: File): Entry? {
                return file.bufferedReader(bufferSize = 512).use {
                    it.readLine()
                }?.split(' ')?.let { fields ->
                    Entry(
                        timestamp = file.lastModified(),
                        request = fields[6],
                        debug = "debug_url",
                        status = 0,
                        id = fields[3],
                        date = fields[0],
                        time = fields[1],
                        host = fields[2],
                        method = fields[5],
                    )
                }
            }
        }
    }

    companion object {
        const val dir = "./runtime/debug"
        fun create(): DebugEntries {

            val exclude = "$dir/"
            val files = File(dir).walk()

            fun mapName(name: String) = name.replace(exclude, "")

            return DebugEntries(
                debug = files.map { it.path }.filter { it.contains(".debug.") }.map(::mapName).toList(),
                request = files.map { it.path }.filter { it.contains(".request.") }.map(::mapName).toList()
            )
        }
    }
}

fun Route.debug() {
    get("/debug") {
        val entries = DebugEntries.create()
        call.respond(entries)
    }
    get("/debug/{id}") {
        val id = call.parameters["id"]!!
        call.respondFile(File(DebugEntries.dir + "/" +id))
    }
}
