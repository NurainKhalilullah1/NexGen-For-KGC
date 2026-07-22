package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.CloudinaryClient
import com.example.data.remote.InsforgeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository(private val db: AppDatabase, context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexgen_session", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    data class LoadingState(
        val isLoading: Boolean = false,
        val message: String = "Loading..."
    )

    private val _globalLoadingState = MutableStateFlow<LoadingState?>(null)
    val globalLoadingState: StateFlow<LoadingState?> = _globalLoadingState.asStateFlow()

    fun showGlobalLoading(message: String = "Processing...") {
        _globalLoadingState.value = LoadingState(isLoading = true, message = message)
    }

    fun hideGlobalLoading() {
        _globalLoadingState.value = null
    }

    init {
        // Load active session from SharedPreferences on launch
        val savedUserId = prefs.getString("active_user_id", null)
        if (!savedUserId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                showGlobalLoading("Verifying active session...")
                val user = db.userDao().getUserById(savedUserId).firstOrNull()
                _currentUser.value = user
                hideGlobalLoading()
            }
        }
    }

    // AUTH OPERATIONS
    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        requestedRole: UserRole,
        adminPasscode: String = ""
    ): Result<User> = withContext(Dispatchers.IO) {
        showGlobalLoading("Registering user & syncing Insforge DB...")
        try {
            val trimmedEmail = email.trim().lowercase()
            val existing = db.userDao().getUserByEmail(trimmedEmail)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with email $trimmedEmail already exists."))
            }

            val finalRole = when (requestedRole) {
                UserRole.SUPER_ADMIN -> {
                    if (adminPasscode.isNotBlank()) {
                        UserRole.SUPER_ADMIN
                    } else {
                        return@withContext Result.failure(Exception("Super Admin authorization required."))
                    }
                }
                UserRole.TUTOR -> UserRole.TUTOR
                UserRole.STUDENT -> UserRole.STUDENT
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                email = trimmedEmail,
                fullName = fullName.trim(),
                role = finalRole
            )

            // Try registering with Insforge Auth endpoint
            try {
                InsforgeClient.signUpWithInsforgeAuth(
                    email = trimmedEmail,
                    password = password,
                    fullName = fullName.trim(),
                    role = finalRole.name
                )
            } catch (e: Exception) {
                // Insforge Auth fallback handles database sync
            }

            db.userDao().insertUser(newUser)
            setActiveSession(newUser)
            Result.success(newUser)
        } finally {
            hideGlobalLoading()
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        showGlobalLoading("Authenticating & querying Insforge database...")
        try {
            val trimmedEmail = email.trim().lowercase()

            // 1. Authenticate with Insforge Auth & check 'users' table for up-to-date role
            val authResult = InsforgeClient.signInWithInsforgeAuth(trimmedEmail, password)
            val insforgeResult = InsforgeClient.getUserByEmail(trimmedEmail)
            if (insforgeResult.isSuccess || authResult.isSuccess) {
                val insforgeJson = insforgeResult.getOrNull() ?: authResult.getOrNull()
                if (insforgeJson != null) {
                    val insforgeRoleStr = insforgeJson.optString("role", "STUDENT")
                    val insforgeId = insforgeJson.optString("id", UUID.randomUUID().toString())
                    val insforgeName = insforgeJson.optString("full_name", insforgeJson.optString("fullName", "NexGen Learner"))

                    val resolvedRole = when {
                        insforgeRoleStr.equals("SUPER_ADMIN", ignoreCase = true) || insforgeRoleStr.contains("ADMIN", ignoreCase = true) -> UserRole.SUPER_ADMIN
                        insforgeRoleStr.equals("TUTOR", ignoreCase = true) || insforgeRoleStr.contains("TEACH", ignoreCase = true) -> UserRole.TUTOR
                        else -> UserRole.STUDENT
                    }

                    val existingLocal = db.userDao().getUserByEmail(trimmedEmail)
                    val updatedUser = if (existingLocal != null) {
                        existingLocal.copy(role = resolvedRole, fullName = insforgeName.ifBlank { existingLocal.fullName })
                    } else {
                        User(
                            id = insforgeId,
                            email = trimmedEmail,
                            fullName = insforgeName,
                            role = resolvedRole
                        )
                    }

                    db.userDao().insertUser(updatedUser)
                    setActiveSession(updatedUser)
                    return@withContext Result.success(updatedUser)
                }
            }

            // 2. Fallback to local Room DB if Insforge has no record or offline
            val localUser = db.userDao().getUserByEmail(trimmedEmail)
            if (localUser != null) {
                setActiveSession(localUser)
                Result.success(localUser)
            } else {
                Result.failure(Exception("No account found for $trimmedEmail in Insforge or local database. Please register first."))
            }
        } finally {
            hideGlobalLoading()
        }
    }

    private val _hasCompletedOnboardingFlow = MutableStateFlow(prefs.getBoolean("has_completed_onboarding", false))
    val hasCompletedOnboardingFlow: StateFlow<Boolean> = _hasCompletedOnboardingFlow.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _hasCompletedOnboardingFlow.value = true
    }

    fun logout() {
        prefs.edit().remove("active_user_id").apply()
        _currentUser.value = null
    }

    private fun setActiveSession(user: User) {
        prefs.edit().putString("active_user_id", user.id).apply()
        completeOnboarding()
        _currentUser.value = user
    }

    // USER MANAGEMENT (Super Admin)
    fun getAllUsers(): Flow<List<User>> = db.userDao().getAllUsers()

    suspend fun updateUserRole(userId: String, newRole: UserRole) = withContext(Dispatchers.IO) {
        db.userDao().updateUserRole(userId, newRole)
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        db.userDao().deleteUser(userId)
    }

    // COURSE OPERATIONS
    fun getPublishedCourses(): Flow<List<Course>> = db.courseDao().getAllPublishedCourses()

    fun getTutorCourses(tutorId: String): Flow<List<Course>> = db.courseDao().getCoursesByTutor(tutorId)

    fun getPendingCourses(): Flow<List<Course>> = db.courseDao().getPendingCourses()

    fun getAllCourses(): Flow<List<Course>> = db.courseDao().getAllCourses()

    fun getCourseById(courseId: String): Flow<Course?> = db.courseDao().getCourseById(courseId)

    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> = db.lessonDao().getLessonsForCourse(courseId)

    suspend fun createCourse(course: Course, lessons: List<Lesson>) = withContext(Dispatchers.IO) {
        db.courseDao().insertCourse(course)
        val lessonsWithCourseId = lessons.mapIndexed { index, lesson ->
            lesson.copy(courseId = course.id, orderIndex = index + 1)
        }
        db.lessonDao().insertLessons(lessonsWithCourseId)
    }

    suspend fun updateCourseStatus(courseId: String, status: CourseStatus, rejectionReason: String = "") = withContext(Dispatchers.IO) {
        db.courseDao().updateCourseStatus(courseId, status, rejectionReason)
    }

    suspend fun deleteCourse(courseId: String) = withContext(Dispatchers.IO) {
        db.courseDao().deleteCourse(courseId)
    }

    // ENROLLMENT & LEARNING
    fun getStudentEnrollments(studentId: String): Flow<List<Enrollment>> = db.enrollmentDao().getEnrollmentsForStudent(studentId)

    fun getEnrollment(studentId: String, courseId: String): Flow<Enrollment?> = db.enrollmentDao().getEnrollment(studentId, courseId)

    suspend fun updateLessonProgress(
        enrollmentId: String,
        completedLessonId: String,
        allLessonsCount: Int,
        currentCompletedList: List<String>
    ) = withContext(Dispatchers.IO) {
        val updatedList = if (currentCompletedList.contains(completedLessonId)) {
            currentCompletedList
        } else {
            currentCompletedList + completedLessonId
        }
        val progress = if (allLessonsCount > 0) ((updatedList.size.toFloat() / allLessonsCount) * 100).toInt() else 0
        val jsonStr = updatedList.joinToString(",")
        db.enrollmentDao().updateProgress(enrollmentId, progress, jsonStr)
    }

    suspend fun toggleLessonProgress(
        enrollmentId: String,
        lessonId: String,
        allLessonsCount: Int,
        currentCompletedList: List<String>
    ) = withContext(Dispatchers.IO) {
        val updatedList = if (currentCompletedList.contains(lessonId)) {
            currentCompletedList - lessonId
        } else {
            currentCompletedList + lessonId
        }
        val progress = if (allLessonsCount > 0) ((updatedList.size.toFloat() / allLessonsCount) * 100).toInt() else 0
        val jsonStr = updatedList.joinToString(",")
        db.enrollmentDao().updateProgress(enrollmentId, progress, jsonStr)
    }

    suspend fun updateStudentNotes(enrollmentId: String, notes: String) = withContext(Dispatchers.IO) {
        db.enrollmentDao().updateNotes(enrollmentId, notes)
    }

    // REMITA PAYMENT PIPELINE
    fun getStudentTransactions(studentId: String): Flow<List<Transaction>> = db.transactionDao().getTransactionsForStudent(studentId)

    fun getAllTransactions(): Flow<List<Transaction>> = db.transactionDao().getAllTransactions()

    fun generateRemitaRrr(): String {
        val part1 = (2000..2999).random()
        val part2 = (1000..9999).random()
        val part3 = (1000..9999).random()
        return "$part1-$part2-$part3"
    }

    suspend fun processRemitaPayment(
        student: User,
        course: Course,
        paymentMethod: String
    ): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            val rrr = generateRemitaRrr()
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                studentId = student.id,
                studentName = student.fullName,
                studentEmail = student.email,
                courseId = course.id,
                courseTitle = course.title,
                amountNgn = course.priceNgn,
                remitaServiceFeeNgn = 100.0,
                totalAmountNgn = course.priceNgn + 100.0,
                remitaRrr = rrr,
                paymentMethod = paymentMethod,
                status = "SUCCESSFUL",
                timestamp = System.currentTimeMillis()
            )

            // Save transaction record
            db.transactionDao().insertTransaction(transaction)

            // Instantly create student enrollment for immediate access
            val enrollment = Enrollment(
                id = UUID.randomUUID().toString(),
                studentId = student.id,
                courseId = course.id,
                enrolledAt = System.currentTimeMillis(),
                progressPercentage = 0,
                completedLessonsJson = ""
            )
            db.enrollmentDao().insertEnrollment(enrollment)

            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(
        email: String,
        fullName: String,
        avatarUrl: String = "",
        requestedRole: UserRole = UserRole.STUDENT
    ): Result<User> = withContext(Dispatchers.IO) {
        showGlobalLoading("Signing in with Google Account...")
        try {
            val trimmedEmail = email.trim().lowercase()
            val existing = db.userDao().getUserByEmail(trimmedEmail)

            val resolvedRole = existing?.role ?: requestedRole

            val userToSave = User(
                id = existing?.id ?: UUID.randomUUID().toString(),
                email = trimmedEmail,
                fullName = fullName.ifBlank { existing?.fullName ?: "Google Learner" },
                role = resolvedRole,
                avatarUrl = avatarUrl.ifBlank { existing?.avatarUrl ?: "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(fullName, "UTF-8")}&background=4F46E5&color=fff" }
            )

            db.userDao().insertUser(userToSave)
            InsforgeClient.syncUserToInsforge(
                id = userToSave.id,
                email = userToSave.email,
                fullName = userToSave.fullName,
                role = userToSave.role.name
            )

            setActiveSession(userToSave)
            Result.success(userToSave)
        } finally {
            hideGlobalLoading()
        }
    }

    suspend fun uploadUserProfileAvatar(
        user: User,
        imageBytes: ByteArray
    ): Result<User> = withContext(Dispatchers.IO) {
        showGlobalLoading("Uploading avatar to Cloudinary...")
        try {
            // Upload to Cloudinary CDN
            val uploadResult = CloudinaryClient.uploadAvatarBytes(user.id, imageBytes)
            val newAvatarUrl = uploadResult.getOrDefault(
                "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(user.fullName, "UTF-8")}&background=4F46E5&color=fff&size=200"
            )

            // Persist locally in Room
            val updatedUser = user.copy(avatarUrl = newAvatarUrl)
            db.userDao().insertUser(updatedUser)

            // Sync avatar URL back to InsForge users table
            InsforgeClient.updateUserAvatarUrl(user.id, newAvatarUrl)

            // Update the live session
            setActiveSession(updatedUser)
            Result.success(updatedUser)
        } finally {
            hideGlobalLoading()
        }
    }

    suspend fun uploadCourseThumbnail(
        courseId: String,
        imageBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        showGlobalLoading("Uploading thumbnail to Cloudinary...")
        try {
            CloudinaryClient.uploadCourseThumbnail(courseId, imageBytes)
        } finally {
            hideGlobalLoading()
        }
    }
}
