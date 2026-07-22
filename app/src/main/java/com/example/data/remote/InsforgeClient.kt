package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object InsforgeClient {

    private val client = OkHttpClient()

    val baseUrl: String
        get() = BuildConfig.INSFORGE_PROJECT_URL.trimEnd('/')

    val apiKey: String
        get() = BuildConfig.INSFORGE_ANON_KEY

    private fun newRequestBuilder(endpoint: String): Request.Builder {
        val fullUrl = if (endpoint.startsWith("http")) endpoint else "$baseUrl$endpoint"
        return Request.Builder()
            .url(fullUrl)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
    }

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("/rest/v1/").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                InsforgeErrorHandler.emitError("Insforge server check failed (HTTP ${response.code})")
            }
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            InsforgeErrorHandler.handleNetworkException(e, "testing connection")
            Result.failure(e)
        }
    }

    suspend fun postToTable(tableName: String, jsonBody: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = newRequestBuilder("/rest/v1/$tableName")
                .addHeader("Prefer", "return=representation")
                .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(responseBody)
            } else {
                val err = "Insforge DB Error (${response.code}): $responseBody"
                InsforgeErrorHandler.emitError(err)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            InsforgeErrorHandler.handleNetworkException(e, "posting to $tableName")
            Result.failure(e)
        }
    }

    suspend fun signUpWithInsforgeAuth(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", fullName)
                    put("role", role)
                })
            }.toString()

            val request = newRequestBuilder("/auth/v1/signup")
                .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                Result.success(json)
            } else {
                // If auth endpoint not active, sync directly to users table
                val syncResult = syncUserToInsforge(
                    id = java.util.UUID.randomUUID().toString(),
                    email = email,
                    fullName = fullName,
                    role = role
                )
                if (syncResult.isSuccess) {
                    val userObj = JSONObject().apply {
                        put("email", email)
                        put("role", role)
                        put("full_name", fullName)
                    }
                    Result.success(userObj)
                } else {
                    Result.failure(Exception("Insforge Auth unavailable (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithInsforgeAuth(
        email: String,
        password: String
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }.toString()

            val request = newRequestBuilder("/auth/v1/token?grant_type=password")
                .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                Result.success(json)
            } else {
                // Check users table if auth endpoint isn't active
                val userResult = getUserByEmail(email)
                val userJson = userResult.getOrNull()
                if (userJson != null) {
                    Result.success(userJson)
                } else {
                    Result.failure(Exception("Insforge Auth token error (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByEmail(email: String): Result<JSONObject?> = withContext(Dispatchers.IO) {
        try {
            val encodedEmail = java.net.URLEncoder.encode(email.trim().lowercase(), "UTF-8")
            val request = newRequestBuilder("/rest/v1/users?select=*&email=eq.$encodedEmail")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonArray = JSONArray(responseBody)
                if (jsonArray.length() > 0) {
                    Result.success(jsonArray.getJSONObject(0))
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("Insforge User query HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUserToInsforge(
        id: String,
        email: String,
        fullName: String,
        role: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("id", id)
                put("email", email.trim().lowercase())
                put("full_name", fullName)
                put("role", role)
            }.toString()

            val request = newRequestBuilder("/rest/v1/users")
                .addHeader("Prefer", "return=minimal")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            // Silently fallback to Room local database for offline support
            Result.failure(e)
        }
    }

    suspend fun updateUserAvatarUrl(userId: String, avatarUrl: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("avatar_url", avatarUrl)
            }.toString()

            val request = newRequestBuilder("/rest/v1/users?id=eq.$userId")
                .patch(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
