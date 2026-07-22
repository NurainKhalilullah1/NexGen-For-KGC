package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.Course
import com.example.data.model.CourseStatus
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.STUDENT
    }

    @TypeConverter
    fun fromCourseStatus(value: CourseStatus): String = value.name

    @TypeConverter
    fun toCourseStatus(value: String): CourseStatus = try {
        CourseStatus.valueOf(value)
    } catch (e: Exception) {
        CourseStatus.PENDING_APPROVAL
    }
}

@Database(
    entities = [
        User::class,
        Course::class,
        Lesson::class,
        Enrollment::class,
        Transaction::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexgen_lms_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
