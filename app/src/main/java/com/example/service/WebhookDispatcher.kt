package com.example.service

import android.content.Context
import com.example.data.model.ApiCallResult
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.data.repository.AppLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

object WebhookDispatcher {

    suspend fun postJson(
        context: Context,
        endpoint: String,
        token: String,
        payload: JSONObject,
        eventTitle: String = "Notifikasi Diteruskan",
        isTest: Boolean = false
    ): ApiCallResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null
        val payloadStr = payload.toString()

        if (endpoint.isBlank()) {
            val result = ApiCallResult(
                isSuccess = false,
                httpCode = 0,
                message = "URL Endpoint belum dikonfigurasi.",
                latencyMs = 0,
                sentPayload = payloadStr
            )
            AppLogRepository.getInstance().addLog(
                EventLogItem(
                    type = if (isTest) LogEventType.TEST_PING else LogEventType.WEBHOOK_FAILED,
                    title = "Pengiriman Gagal",
                    details = "URL endpoint kosong. Harap atur endpoint terlebih dahulu.",
                    success = false
                )
            )
            return@withContext result
        }

        try {
            val targetUrl = URL(endpoint.trim())
            connection = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 12000
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("User-Agent", "SFR-Droid/1.0 (Android; id.sfrdroid.app)")
                if (token.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
            }

            val bodyBytes = payloadStr.toByteArray(StandardCharsets.UTF_8)
            connection.setFixedLengthStreamingMode(bodyBytes.size)

            connection.outputStream.use { os: OutputStream ->
                os.write(bodyBytes)
                os.flush()
            }

            val responseCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime

            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val responseBody = try {
                responseStream?.let { stream ->
                    BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                        val sb = java.lang.StringBuilder()
                        var line = reader.readLine()
                        while (line != null && sb.length < 1500) {
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                        sb.toString().trim()
                    }
                } ?: ""
            } catch (_: Exception) {
                ""
            }

            val isSuccess = responseCode in 200..299
            val statusMessage = when (responseCode) {
                200 -> "HTTP 200 OK — Server berhasil menerima payload."
                201 -> "HTTP 201 Created — Data berhasil dibuat di server."
                204 -> "HTTP 204 No Content — Permintaan diterima tanpa respons konten."
                400 -> "HTTP 400 Bad Request — Format payload tidak diterima server."
                401 -> "HTTP 401 Unauthorized — Token API salah atau tidak valid."
                403 -> "HTTP 403 Forbidden — Akses ditolak oleh server penerima."
                404 -> "HTTP 404 Not Found — Alamat endpoint tidak ditemukan."
                500 -> "HTTP 500 Internal Server Error — Kesalahan pada server backend."
                502 -> "HTTP 502 Bad Gateway — Server proxy/gateway bermasalah."
                503 -> "HTTP 503 Service Unavailable — Server sedang offline/sibuk."
                else -> "HTTP $responseCode — Respons dari server diterima."
            }

            val result = ApiCallResult(
                isSuccess = isSuccess,
                httpCode = responseCode,
                message = statusMessage,
                latencyMs = latency,
                responseBody = responseBody,
                sentPayload = payloadStr
            )

            AppLogRepository.getInstance().addLog(
                EventLogItem(
                    type = if (isTest) LogEventType.TEST_PING else (if (isSuccess) LogEventType.WEBHOOK_SENT else LogEventType.WEBHOOK_FAILED),
                    title = if (isSuccess) eventTitle else "$eventTitle (Gagal)",
                    details = "$statusMessage (Waktu: ${latency}ms)\nPayload: ${payloadStr.take(120)}...",
                    httpCode = responseCode,
                    success = isSuccess
                )
            )

            result
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val errorMsg = when {
                e is java.net.UnknownHostException -> "Host tidak ditemukan: ${e.message}. Periksa koneksi internet & domain URL."
                e is java.net.ConnectException -> "Gagal terhubung: Server menolak koneksi atau port tertutup."
                e is java.net.SocketTimeoutException -> "Koneksi timeout setelah 12 detik. Server penerima terlalu lambat merespons."
                e is javax.net.ssl.SSLException -> "Kesalahan SSL/TLS: ${e.message}. Pastikan sertifikat HTTPS server valid."
                else -> "Kesalahan jaringan: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }

            val result = ApiCallResult(
                isSuccess = false,
                httpCode = 0,
                message = errorMsg,
                latencyMs = latency,
                sentPayload = payloadStr
            )

            AppLogRepository.getInstance().addLog(
                EventLogItem(
                    type = if (isTest) LogEventType.TEST_PING else LogEventType.WEBHOOK_FAILED,
                    title = if (isTest) "Tes Endpoint Gagal" else "Gagal Mengirim Webhook",
                    details = "$errorMsg (Waktu: ${latency}ms)",
                    httpCode = 0,
                    success = false
                )
            )

            result
        } finally {
            connection?.disconnect()
        }
    }
}
