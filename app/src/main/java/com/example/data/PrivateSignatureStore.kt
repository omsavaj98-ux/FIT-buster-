package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Stores the user's uploaded original signature image and profile photo privately
 * in app-internal storage (MODE_PRIVATE). Never redraws, alters, or filters the signature.
 */
class PrivateSignatureStore(context: Context) {
    private val appContext = context.applicationContext
    private val privateDir: File = File(appContext.filesDir, "private_assets").apply { mkdirs() }

    private fun signatureFile(userId: String): File =
        File(privateDir, "authorized_signature_${sanitize(userId)}.bin")

    private fun photoFile(userId: String): File =
        File(privateDir, "profile_photo_${sanitize(userId)}.bin")

    private fun sanitize(uid: String): String =
        uid.replace(Regex("[^a-zA-Z0-9_-]"), "_")

    suspend fun saveSignatureFromUri(userId: String, uri: Uri): Result<Pair<ByteArray, String>> =
        withContext(Dispatchers.IO) {
            try {
                val mimeType = appContext.contentResolver.getType(uri) ?: "image/png"
                val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalArgumentException("Unable to read selected signature image"))
                if (bytes.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Selected image file is empty"))
                }
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@withContext Result.failure(IllegalArgumentException("Unsupported image format"))
                decoded.recycle()

                signatureFile(userId).writeBytes(bytes)
                Result.success(Pair(bytes, mimeType))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun saveProfilePhotoFromUri(userId: String, uri: Uri): Result<Pair<ByteArray, String>> =
        withContext(Dispatchers.IO) {
            try {
                val mimeType = appContext.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalArgumentException("Unable to read selected photo"))
                if (bytes.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Selected image file is empty"))
                }
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: return@withContext Result.failure(IllegalArgumentException("Unsupported image format"))
                decoded.recycle()

                photoFile(userId).writeBytes(bytes)
                Result.success(Pair(bytes, mimeType))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun restoreSignatureBytes(userId: String, base64: String) {
        if (base64.isBlank()) return
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        if (bytes.isNotEmpty()) {
            signatureFile(userId).writeBytes(bytes)
        }
    }

    fun restorePhotoBytes(userId: String, base64: String) {
        if (base64.isBlank()) return
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        if (bytes.isNotEmpty()) {
            photoFile(userId).writeBytes(bytes)
        }
    }

    fun loadSignatureBitmap(userId: String): Bitmap? {
        val file = signatureFile(userId)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun loadProfilePhotoBitmap(userId: String): Bitmap? {
        val file = photoFile(userId)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun hasLocalSignature(userId: String): Boolean = signatureFile(userId).exists()

    fun deleteSignature(userId: String) {
        val file = signatureFile(userId)
        if (file.exists()) file.delete()
    }

    fun deleteAllPrivateAssets(userId: String) {
        signatureFile(userId).delete()
        photoFile(userId).delete()
    }
}
