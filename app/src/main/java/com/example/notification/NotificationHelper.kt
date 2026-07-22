package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object NotificationHelper {

    const val CHANNEL_ID_GENERAL = "nexgen_push_general"
    const val CHANNEL_ID_REMINDERS = "nexgen_push_reminders"

    private val _notifications = MutableStateFlow<List<PushNotificationItem>>(
        listOf(
            PushNotificationItem(
                title = "Welcome to NexGen LMS!",
                message = "Explore courses, access lesson notes, and track your learning progress seamlessly.",
                category = NotificationCategory.SYSTEM
            )
        )
    )
    val notifications: StateFlow<List<PushNotificationItem>> = _notifications.asStateFlow()

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "NexGen General Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General announcements, course alerts, and account updates."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "NexGen Study Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Scheduled daily study reminders and lesson prompts."
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(generalChannel)
            notificationManager?.createNotificationChannel(reminderChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendPushNotification(
        context: Context,
        title: String,
        message: String,
        category: NotificationCategory = NotificationCategory.SYSTEM,
        channelId: String = CHANNEL_ID_GENERAL
    ) {
        // Create item for in-app Notification Center
        val newItem = PushNotificationItem(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            category = category,
            isRead = false
        )
        _notifications.value = listOf(newItem) + _notifications.value

        // Check system permission before sending system status bar notification
        if (!hasNotificationPermission(context)) {
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_NOTIFICATION_ID", newItem.id)
                putExtra("EXTRA_NOTIFICATION_CATEGORY", category.name)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                newItem.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(newItem.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Permission missing at runtime
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map { item ->
            if (item.id == notificationId) item.copy(isRead = true) else item
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }
}
