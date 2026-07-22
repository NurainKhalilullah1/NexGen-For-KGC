package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Course
import com.example.data.model.CourseStatus
import com.example.data.model.Enrollment
import com.example.data.model.Lesson
import com.example.data.model.Transaction
import com.example.data.model.User
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: UserRole)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE status = 'PUBLISHED' ORDER BY createdAt DESC")
    fun getAllPublishedCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE tutorId = :tutorId ORDER BY createdAt DESC")
    fun getCoursesByTutor(tutorId: String): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE status = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    fun getPendingCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: String): Flow<Course?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("UPDATE courses SET status = :status, rejectionReason = :rejectionReason WHERE id = :courseId")
    suspend fun updateCourseStatus(courseId: String, status: CourseStatus, rejectionReason: String)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourse(courseId: String)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLesson(lessonId: String)
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE studentId = :studentId ORDER BY enrolledAt DESC")
    fun getEnrollmentsForStudent(studentId: String): Flow<List<Enrollment>>

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId AND courseId = :courseId LIMIT 1")
    fun getEnrollment(studentId: String, courseId: String): Flow<Enrollment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: Enrollment)

    @Query("UPDATE enrollments SET progressPercentage = :progressPercentage, completedLessonsJson = :completedLessonsJson WHERE id = :enrollmentId")
    suspend fun updateProgress(enrollmentId: String, progressPercentage: Int, completedLessonsJson: String)

    @Query("UPDATE enrollments SET notes = :notes WHERE id = :enrollmentId")
    suspend fun updateNotes(enrollmentId: String, notes: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getTransactionsForStudent(studentId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
}
