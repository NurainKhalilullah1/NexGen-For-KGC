package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object CloudinaryUploader {

    private val client = OkHttpClient()

    suspend fun uploadImage(
        imageBytes: ByteArray,
        cloudName: String = BuildConfig.CLOUDINARY_CLOUD_NAME,
        uploadPreset: String = BuildConfig.CLOUDINARY_UPLOAD_PRESET
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", uploadPreset)
                .addFormDataPart(
                    "file",
                    "course_thumb_${System.currentTimeMillis()}.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (response.isSuccessful && !responseBodyString.isNullOrEmpty()) {
                val json = JSONObject(responseBodyString)
                val secureUrl = json.optString("secure_url")
                if (secureUrl.isNotEmpty()) {
                    Result.success(secureUrl)
                } else {
                    Result.failure(Exception("Cloudinary response missing secure_url parameter"))
                }
            } else {
                Result.failure(Exception("Cloudinary upload failed (${response.code}): $responseBodyString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
