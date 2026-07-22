package com.example.notification

import java.util.UUID

enum class NotificationCategory(val label: String) {
    LEARNING_TIPS("Learning & Tips"),
    NEW_COURSE("New Course Added"),
    NEW_LESSON("New Lesson Added"),
    COURSE("Course Updates"),
    ASSIGNMENT("Assignments & Quizzes"),
    ANNOUNCEMENT("Announcements"),
    SYSTEM("System & Account"),
    REMINDER("Study Reminders")
}

data class PushNotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: NotificationCategory = NotificationCategory.SYSTEM,
    val isRead: Boolean = false,
    val deepLinkAction: String? = null
)
