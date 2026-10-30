package com.example.service

import android.content.Context
import com.example.data.model.ApiCallResult
import com.example.data.model.DispatchMode
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.data.model.WebhookFormatMode
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.example.util.EncryptionHelper
import com.example.util.ShortcodeHelper
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

    /**
     * Dispatches an incoming notification to the appropriate webhook based on user rules,
     * formatting mode (Default JSON or Custom Template), and HTTP method (POST or GET).
     */
    suspend fun dispatchNotification(
        context: Context,
        appName: String,
        packageName: String,
        title: String,
        message: String,
        subText: String = "",
        postTime: Long = System.currentTimeMillis(),
        notifKey: String = "",
        notifId: Int = 0
    ): ApiCallResult = withContext(Dispatchers.IO) {
        val configRepo = ConfigRepository.getInstance(context)
        val config = configRepo.getConfig()

        // 1. Determine target endpoint, method, template, and token
        var targetEndpoint = config.endpoint
        var targetMethod = config.httpMethod
        var targetToken = config.token
        var targetFormatMode = config.formatMode
        var targetBodyTemplate = config.customBodyTemplate

        if (config.dispatchMode == DispatchMode.MULTI_WEBHOOK) {
            val appRule = config.appWebhookRules[packageName]
            if (appRule != null && appRule.isEnabled && appRule.endpoint.isNotBlank()) {
                targetEndpoint = appRule.endpoint
                targetMethod = appRule.method
                if (appRule.token.isNotBlank()) targetToken = appRule.token
                if (appRule.customTemplate.isNotBlank()) {
                    targetFormatMode = WebhookFormatMode.CUSTOM_TEMPLATE
                    targetBodyTemplate = appRule.customTemplate
                }
            }
        }

        if (targetEndpoint.isBlank()) {
            val errResult = ApiCallResult(
                isSuccess = false,
                httpCode = 0,
                message = "Endpoint belum dikonfigurasi untuk aplikasi: $appName ($packageName)",
                latencyMs = 0
            )
            AppLogRepository.getInstance().addLog(
                EventLogItem(
                    type = LogEventType.WEBHOOK_FAILED,
                    title = "Gagal Mengirim Notifikasi: $appName",
                    details = "URL Endpoint belum diisi. Atur di Dashboard atau Multi Webhook.",
                    success = false
                )
            )
            return@withContext errResult
        }

        val notifContext = ShortcodeHelper.NotificationContext(
            appName = appName,
            packageName = packageName,
            title = title,
            message = message,
            subText = subText,
            timestamp = postTime,
            id = notifId
        )

        // Resolve URL if contains shortcodes (e.g. https://contoh.com/webhook.php?app={app}&msg={message})
        val resolvedUrlString = ShortcodeHelper.resolveUrl(targetEndpoint, notifContext)

        // Determine Body
        val (finalBodyString, contentType) = if (targetMethod.equals("GET", ignoreCase = true)) {
            Pair("", "")
        } else {
            if (targetFormatMode == WebhookFormatMode.CUSTOM_TEMPLATE) {
                val resolvedBody = ShortcodeHelper.resolveBody(targetBodyTemplate, notifContext)
                val cType = if (resolvedBody.trim().startsWith("{") || resolvedBody.trim().startsWith("[")) {
                    "application/json; charset=utf-8"
                } else {
                    "application/x-www-form-urlencoded; charset=utf-8"
                }
                Pair(resolvedBody, cType)
            } else {
                // Default JSON mode
                val payload = JSONObject()
                val fields = config.selectedFields
                if (fields.contains("app")) payload.put("app", appName)
                if (fields.contains("package")) payload.put("package", packageName)
                if (fields.contains("title")) payload.put("title", title)
                if (fields.contains("message")) payload.put("message", message)
                if (fields.contains("timestamp")) payload.put("timestamp", postTime)
                if (fields.contains("key")) payload.put("key", notifKey)
                if (fields.contains("id")) payload.put("id", notifId)
                payload.put("event", "notification")
                Pair(payload.toString(), "application/json; charset=utf-8")
            }
        }

        // Encryption if enabled and POST
        val encKey = config.encryptionKey
        val finalToken = if (config.encryptToken && encKey.isNotBlank() && targetToken.isNotBlank()) {
            EncryptionHelper.encrypt(targetToken, encKey)
        } else {
            targetToken
        }

        val (sendBody, isEncrypted) = if (config.encryptPayload && encKey.isNotBlank() && finalBodyString.isNotBlank()) {
            val encryptedData = EncryptionHelper.encrypt(finalBodyString, encKey)
            val wrapped = JSONObject().apply {
                put("encrypted", true)
                put("data", encryptedData)
                put("timestamp", System.currentTimeMillis())
            }
            Pair(wrapped.toString(), true)
        } else {
            Pair(finalBodyString, false)
        }

        executeHttpRequest(
            context = context,
            urlString = resolvedUrlString,
            method = targetMethod,
            body = sendBody,
            contentType = contentType,
            token = finalToken,
            isEncrypted = isEncrypted,
            eventTitle = "Forward Notifikasi ($appName)",
            isTest = false
        )
    }

    /**
     * Test endpoint or send custom JSON.
     */
    suspend fun postJson(
        context: Context,
        endpoint: String,
        token: String,
        payload: JSONObject,
        eventTitle: String = "Uji Coba Endpoint Webhook",
        isTest: Boolean = false
    ): ApiCallResult = withContext(Dispatchers.IO) {
        val config = ConfigRepository.getInstance(context).getConfig()
        val encKey = config.encryptionKey

        val finalToken = if (config.encryptToken && encKey.isNotBlank() && token.isNotBlank()) {
            EncryptionHelper.encrypt(token, encKey)
        } else {
            token
        }

        val (finalBodyString, isEncrypted) = if (config.encryptPayload && encKey.isNotBlank()) {
            val rawJson = payload.toString()
            val encryptedData = EncryptionHelper.encrypt(rawJson, encKey)
            val wrapped = JSONObject().apply {
                put("encrypted", true)
                put("data", encryptedData)
                put("timestamp", System.currentTimeMillis())
            }
            Pair(wrapped.toString(), true)
        } else {
            Pair(payload.toString(), false)
        }

        executeHttpRequest(
            context = context,
            urlString = endpoint,
            method = "POST",
            body = finalBodyString,
            contentType = "application/json; charset=utf-8",
            token = finalToken,
            isEncrypted = isEncrypted,
            eventTitle = eventTitle,
            isTest = isTest
        )
    }

    private fun executeHttpRequest(
        context: Context,
        urlString: String,
        method: String,
        body: String,
        contentType: String,
        token: String,
        isEncrypted: Boolean,
        eventTitle: String,
        isTest: Boolean
    ): ApiCallResult {
        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null

        if (urlString.isBlank()) {
            return ApiCallResult(
                isSuccess = false,
                httpCode = 0,
                message = "URL Endpoint belum dikonfigurasi.",
                latencyMs = 0,
                sentPayload = body
            )
        }

        try {
            val targetUrl = URL(urlString.trim())
            connection = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = method.uppercase()
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("User-Agent", "SFR-Droid/1.0 (Android; id.sfrdroid.app)")
                if (token.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                if (isEncrypted) {
                    setRequestProperty("X-SFR-Encrypted", "true")
                }

                if (method.equals("POST", ignoreCase = true) || method.equals("PUT", ignoreCase = true)) {
                    doOutput = true
                    doInput = true
                    if (contentType.isNotBlank()) {
                        setRequestProperty("Content-Type", contentType)
                    }
                    val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
                    setFixedLengthStreamingMode(bodyBytes.size)
                    outputStream.use { os: OutputStream ->
                        os.write(bodyBytes)
                        os.flush()
                    }
                } else {
                    doInput = true
                }
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
                else -> "HTTP $responseCode — Respons diterima dari server."
            }

            if (isSuccess && !isTest) {
                ConfigRepository.getInstance(context).incrementSentCount()
                StandbyForegroundService.updateStats(context)
                com.example.receiver.SFRAppWidgetProvider.updateAllWidgets(context)
            }

            val result = ApiCallResult(
                isSuccess = isSuccess,
                httpCode = responseCode,
                message = statusMessage,
                latencyMs = latency,
                responseBody = responseBody,
                sentPayload = if (body.isNotBlank()) body else urlString
            )

            val encNotice = if (isEncrypted) " [AES-256]" else ""
            AppLogRepository.getInstance().addLog(
                EventLogItem(
                    type = if (isTest) LogEventType.TEST_PING else (if (isSuccess) LogEventType.WEBHOOK_SENT else LogEventType.WEBHOOK_FAILED),
                    title = if (isSuccess) "$eventTitle$encNotice" else "$eventTitle (Gagal)",
                    details = "$statusMessage (Waktu: ${latency}ms)\nData: ${(if (body.isNotBlank()) body else urlString).take(120)}...",
                    httpCode = responseCode,
                    success = isSuccess
                )
            )

            return result
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
                sentPayload = if (body.isNotBlank()) body else urlString
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

            return result
        } finally {
            connection?.disconnect()
        }
    }
}
