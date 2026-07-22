package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole {
    STUDENT,
    TUTOR,
    SUPER_ADMIN
}

enum class CourseStatus {
    DRAFT,
    PENDING_APPROVAL,
    PUBLISHED,
    REJECTED
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // Email or UUID
    val email: String,
    val fullName: String,
    val role: UserRole,
    val avatarUrl: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tutorId: String,
    val tutorName: String,
    val title: String,
    val description: String,
    val category: String, // e.g., "Web Development", "Python", "UI/UX", "Robotics"
    val priceNgn: Double,
    val thumbnailUrl: String = "",
    val status: CourseStatus = CourseStatus.PENDING_APPROVAL,
    val rejectionReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val youtubeUrl: String, // YouTube unlisted or standard link
    val durationMinutes: Int,
    val orderIndex: Int
)

@Entity(tableName = "enrollments")
data class Enrollment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val courseId: String,
    val enrolledAt: Long = System.currentTimeMillis(),
    val progressPercentage: Int = 0,
    val completedLessonsJson: String = "[]", // Comma-separated or JSON list of completed lesson IDs
    val notes: String = ""
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val studentEmail: String,
    val courseId: String,
    val courseTitle: String,
    val amountNgn: Double,
    val remitaServiceFeeNgn: Double = 100.0,
    val totalAmountNgn: Double = amountNgn + 100.0,
    val remitaRrr: String, // 12-digit RRR e.g. "2804-9831-4192"
    val paymentMethod: String = "Remita Online (Card / USSD / Bank Transfer)",
    val status: String = "SUCCESSFUL",
    val timestamp: Long = System.currentTimeMillis()
)
