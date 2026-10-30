package com.example.service

import com.example.data.model.DeveloperNotification
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.data.repository.AppLogRepository
import com.example.data.repository.ConfigRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SFRFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ConfigRepository.getInstance(applicationContext).setFcmToken(token)
        AppLogRepository.getInstance().addLog(
            EventLogItem(
                type = LogEventType.SYSTEM_EVENT,
                title = "Token FCM Diperbarui",
                details = "Token registrasi push notification aktif: ${token.take(20)}...",
                success = true
            )
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: remoteMessage.data["judul"]
            ?: "Pesan dari Developer SFR"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: remoteMessage.data["pesan"]
            ?: remoteMessage.data["body"]
            ?: ""

        val url = remoteMessage.data["url"] ?: remoteMessage.data["link"]
        val notifId = remoteMessage.messageId ?: "fcm_${System.currentTimeMillis()}"

        val notifItem = DeveloperNotification(
            id = notifId,
            title = title,
            message = body,
            timestamp = System.currentTimeMillis(),
            url = url,
            type = "fcm_push"
        )

        AppLogRepository.getInstance().addLog(
            EventLogItem(
                type = LogEventType.DEV_UPDATE_RECEIVED,
                title = "Push FCM Diterima: $title",
                details = body,
                success = true
            )
        )

        DeveloperUpdateChecker.postDeveloperNotification(applicationContext, notifItem)
    }
}
