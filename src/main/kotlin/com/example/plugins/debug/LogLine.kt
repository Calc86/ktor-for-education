package com.example.plugins.debug

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class LogLine(
    val date: String,
    val time: String,
    val from: String,
    val requestId: String,
    val device: String,
    val deviceId: String,
    val method: String,
    val path: String,
    val answer: String,
    val processing: String,
) {
    companion object {
        fun from(file: File, bufferSize: Int = 512) =
            file.bufferedReader(bufferSize = bufferSize).use { it.readLine() }?.let { from(it) }

        private fun from(line: String): LogLine {
            /* date(0)    time(1)  from(2)    requestId(3) device(4)      device_id(5)
             * 2023-02-14 16:20:04 localhost  123          myRandomDevice randomUuidOrHash
             * (6) method(7) path(8)           (9) answer(10) processing(11)
             * --> POST      /api/v1/user/auth <-- 200        145ms
             */
            return line.split(' ').let { fields ->
                LogLine(
                    date = fields[0],
                    time = fields[1],
                    from = fields[2],
                    requestId = fields[3],
                    device = fields[4],
                    deviceId = fields[5],
                    method = fields[7],
                    path = fields[8],
                    answer = fields[10],
                    processing = fields[11],
                )
            }
        }
    }
}
