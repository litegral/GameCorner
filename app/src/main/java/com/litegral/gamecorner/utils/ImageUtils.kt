package com.litegral.gamecorner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Utility class for image operations and Firebase Storage uploads
 * Handles image compression, rotation correction, and storage management
 */
object ImageUtils {
    
    private const val TAG = "ImageUtils"
    private const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    private const val COMPRESSION_QUALITY = 85
    private const val MAX_DIMENSION = 512 // Max width/height in pixels
    
    /**
     * Compress and prepare image for upload
     */
    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode image")
                return null
            }
            
            // Correct orientation
            val correctedBitmap = correctImageOrientation(context, imageUri, originalBitmap)
            
            // Resize if needed
            val resizedBitmap = resizeImage(correctedBitmap, MAX_DIMENSION)
            
            // Compress to byte array
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            val compressedData = outputStream.toByteArray()
            
            // Clean up
            if (correctedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            if (resizedBitmap != correctedBitmap) {
                correctedBitmap.recycle()
            }
            resizedBitmap.recycle()
            outputStream.close()
            
            Log.d(TAG, "Image compressed from ${originalBitmap.byteCount} to ${compressedData.size} bytes")
            compressedData
            
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            null
        }
    }
    
    /**
     * Correct image orientation based on EXIF data
     */
    private fun correctImageOrientation(context: Context, imageUri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            inputStream.close()
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bitmap
            }
            
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            Log.w(TAG, "Could not get image orientation", e)
            bitmap
        }
    }
    
    /**
     * Resize image to fit within max dimensions while maintaining aspect ratio
     */
    private fun resizeImage(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / aspectRatio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * aspectRatio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Upload image to Firebase Storage
     */
    suspend fun uploadProfileImage(userId: String, imageData: ByteArray): Result<String> {
        return try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val profileImagesRef = storageRef.child("profile_images/$userId.jpg")
            
            // Upload the image
            val uploadTask = profileImagesRef.putBytes(imageData).await()
            
            // Get download URL
            val downloadUrl = profileImagesRef.downloadUrl.await()
            
            Log.d(TAG, "Profile image uploaded successfully: $downloadUrl")
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload profile image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete profile image from Firebase Storage
     */
    suspend fun deleteProfileImage(userId: String): Result<Unit> {
        return try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val profileImageRef = storageRef.child("profile_images/$userId.jpg")
            
            profileImageRef.delete().await()
            
            Log.d(TAG, "Profile image deleted successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete profile image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate unique filename for profile image
     */
    fun generateImageFileName(userId: String): String {
        val timestamp = System.currentTimeMillis()
        return "profile_${userId}_${timestamp}.jpg"
    }
    
    /**
     * Check if image size is within limits
     */
    fun isImageSizeValid(context: Context, imageUri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val size = inputStream?.available() ?: 0
            inputStream?.close()
            
            val isValid = size <= MAX_IMAGE_SIZE
            if (!isValid) {
                Log.w(TAG, "Image size ($size bytes) exceeds maximum allowed (${MAX_IMAGE_SIZE} bytes)")
            }
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error checking image size", e)
            false
        }
    }
} 