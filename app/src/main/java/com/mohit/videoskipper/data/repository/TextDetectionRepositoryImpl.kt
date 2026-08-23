package com.mohit.videoskipper.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mohit.videoskipper.domain.repository.TextDetectionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class TextDetectionRepositoryImpl @Inject constructor() : TextDetectionRepository {
    private val TAG = "HEEEEWaooo"

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(bitmap: Bitmap): String =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (cont.isActive) cont.resume(visionText.text) { cause, _, _ -> }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ ML Kit OCR failed: ${e.message}", e)
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }

    override suspend fun findMatchingKeyword(bitmap: Bitmap, keywords: List<String>): String? {
        if (keywords.isEmpty()) {
            return null
        }

        val detectedText = recognizeText(bitmap).lowercase()
        val match = keywords.firstOrNull { keyword -> detectedText.contains(keyword.lowercase()) }
        return match
    }
}