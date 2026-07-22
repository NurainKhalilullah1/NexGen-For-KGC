package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * CloudinaryClient — Handles all media uploads for NexGen LMS.
 *
 * Used for:
 *  - Student profile avatar photos (image upload)
 *  - Course thumbnail images (image upload)
 *  - Lesson video files (video upload) — if tutors upload directly from device
 *
 * Cloudinary config is injected via BuildConfig from .env:
 *  - CLOUDINARY_CLOUD_NAME: e.g. "dlct4kt1s"
 *  - CLOUDINARY_UPLOAD_PRESET: e.g. "nexgenbykgc" (must be UNSIGNED in Cloudinary dashboard)
 */
object CloudinaryClient {

    private val cloudName: String
        get() = BuildConfig.CLOUDINARY_CLOUD_NAME

    private val uploadPreset: String
        get() = BuildConfig.CLOUDINARY_UPLOAD_PRESET

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // ─── Upload Endpoints ────────────────────────────────────────────────────

    private fun imageUploadUrl() = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
    private fun videoUploadUrl() = "https://api.cloudinary.com/v1_1/$cloudName/video/upload"
    private fun rawUploadUrl()   = "https://api.cloudinary.com/v1_1/$cloudName/raw/upload"

    // ─── Public URL Builder ──────────────────────────────────────────────────

    fun buildImageUrl(publicId: String, transformation: String = "q_auto,f_auto"): String =
        "https://res.cloudinary.com/$cloudName/image/upload/$transformation/$publicId"

    fun buildVideoUrl(publicId: String): String =
        "https://res.cloudinary.com/$cloudName/video/upload/$publicId"

    // ─── Upload: Profile Avatar (ByteArray) ──────────────────────────────────

    /**
     * Uploads a profile avatar image from a ByteArray (e.g. captured from camera or gallery).
     * Returns the secure Cloudinary CDN URL of the uploaded image.
     */
    suspend fun uploadAvatarBytes(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("public_id", "avatars/avatar_$userId")
                .addFormDataPart("folder", "nexgen_avatars")
                .addFormDataPart(
                    "file",
                    "avatar_$userId.jpg",
                    imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(imageUploadUrl())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotBlank()) {
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary returned empty URL"))
                }
            } else {
                // Graceful fallback: return a UI-Avatars URL so the profile still shows
                val fallbackUrl = "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(userId, "UTF-8")}&background=4F46E5&color=fff&size=200"
                Result.success(fallbackUrl)
            }
        } catch (e: Exception) {
            // Network error fallback
            val fallbackUrl = "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(userId, "UTF-8")}&background=4F46E5&color=fff&size=200"
            Result.success(fallbackUrl)
        }
    }

    // ─── Upload: Course Thumbnail (ByteArray) ────────────────────────────────

    /**
     * Uploads a course thumbnail image from a ByteArray.
     * Returns the secure Cloudinary CDN URL.
     */
    suspend fun uploadCourseThumbnail(
        courseId: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("public_id", "thumbnails/course_$courseId")
                .addFormDataPart("folder", "nexgen_thumbnails")
                .addFormDataPart("transformation", "q_auto,f_auto,w_800,h_450,c_fill")
                .addFormDataPart(
                    "file",
                    "thumbnail_$courseId.jpg",
                    imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(imageUploadUrl())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotBlank()) Result.success(secureUrl)
                else Result.failure(Exception("Cloudinary returned empty URL"))
            } else {
                Result.failure(Exception("Thumbnail upload failed (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Upload: Lesson Video File ────────────────────────────────────────────

    /**
     * Uploads a lesson video from a local file path.
     * For large video files. Returns secure Cloudinary video URL.
     */
    suspend fun uploadLessonVideo(
        courseId: String,
        lessonIndex: Int,
        videoFile: File,
        mimeType: String = "video/mp4"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("public_id", "lessons/course_${courseId}_lesson_$lessonIndex")
                .addFormDataPart("folder", "nexgen_lessons")
                .addFormDataPart("resource_type", "video")
                .addFormDataPart(
                    "file",
                    videoFile.name,
                    videoFile.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(videoUploadUrl())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotBlank()) Result.success(secureUrl)
                else Result.failure(Exception("Cloudinary returned empty URL"))
            } else {
                Result.failure(Exception("Video upload failed (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Upload: Lesson Video from ByteArray ─────────────────────────────────

    /**
     * Uploads a lesson video directly from bytes (e.g. picked from gallery).
     */
    suspend fun uploadLessonVideoBytes(
        courseId: String,
        lessonIndex: Int,
        videoBytes: ByteArray,
        mimeType: String = "video/mp4"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart("public_id", "lessons/course_${courseId}_lesson_$lessonIndex")
                .addFormDataPart("folder", "nexgen_lessons")
                .addFormDataPart(
                    "file",
                    "lesson_${courseId}_$lessonIndex.mp4",
                    videoBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(videoUploadUrl())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotBlank()) Result.success(secureUrl)
                else Result.failure(Exception("Cloudinary returned empty URL"))
            } else {
                Result.failure(Exception("Video upload failed (${response.code}): $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
