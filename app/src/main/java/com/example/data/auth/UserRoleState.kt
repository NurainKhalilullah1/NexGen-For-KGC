package com.example.data.auth

import androidx.compose.runtime.compositionLocalOf
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.remote.InsforgeClient
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class RoleVerificationResult(
    val role: UserRole,
    val isVerifiedFromInsforge: Boolean,
    val insforgeUserId: String? = null,
    val insforgeFullName: String? = null,
    val message: String
)

class UserRoleStateProvider(private val repository: AppRepository) {

    private val _isCheckingInsforge = MutableStateFlow(false)
    val isCheckingInsforge: StateFlow<Boolean> = _isCheckingInsforge.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    /**
     * Checks the Insforge 'users' table for the specified email to determine the user's role.
     */
    suspend fun verifyAndResolveUserRole(email: String): Result<RoleVerificationResult> {
        _isCheckingInsforge.value = true
        _verificationMessage.value = "Checking Insforge database for user role..."

        try {
            val trimmedEmail = email.trim().lowercase()
            val insforgeResult = InsforgeClient.getUserByEmail(trimmedEmail)

            if (insforgeResult.isSuccess) {
                val userJson: JSONObject? = insforgeResult.getOrNull()
                if (userJson != null) {
                    val insforgeRoleStr = userJson.optString("role", "STUDENT")
                    val insforgeId = userJson.optString("id", java.util.UUID.randomUUID().toString())
                    val insforgeName = userJson.optString("full_name", userJson.optString("fullName", "NexGen Learner"))

                    val resolvedRole = parseRoleString(insforgeRoleStr, trimmedEmail)

                    _verificationMessage.value = "Role verified from Insforge: $resolvedRole"
                    _isCheckingInsforge.value = false

                    return Result.success(
                        RoleVerificationResult(
                            role = resolvedRole,
                            isVerifiedFromInsforge = true,
                            insforgeUserId = insforgeId,
                            insforgeFullName = insforgeName,
                            message = "Verified role '$resolvedRole' from Insforge database."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Network or parse exception fallback
        } finally {
            _isCheckingInsforge.value = false
        }

        _verificationMessage.value = "Insforge user record not found or offline. Using local role verification."
        return Result.failure(Exception("User record not found in Insforge users table."))
    }

    private fun parseRoleString(roleStr: String, email: String): UserRole {
        val uppercase = roleStr.uppercase()
        return when {
            uppercase.contains("ADMIN") || uppercase.contains("SUPER") -> UserRole.SUPER_ADMIN
            uppercase.contains("TUTOR") || uppercase.contains("TEACHER") || uppercase.contains("INSTRUCTOR") -> UserRole.TUTOR
            else -> UserRole.STUDENT
        }
    }
}

val LocalUserRoleState = compositionLocalOf<UserRoleStateProvider?> { null }

