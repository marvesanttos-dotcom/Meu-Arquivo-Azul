package com.meuarquivoazul.app

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class WhatsAppNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (sbn.packageName != "com.whatsapp" && sbn.packageName != "com.whatsapp.w4b") return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val sender = extras.getCharSequence("android.title")?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString()?.trim().orEmpty()
        if (sender.isBlank() || text.isBlank()) return

        // Android/WhatsApp supplies the notification's message count when available.
        // Some notification styles omit it, so a new notification is represented as 1.
        val unread = notification.number.takeIf { it > 0 } ?: 1
        ArchiveStore.add(applicationContext, sender, text, unread)
    }
}
