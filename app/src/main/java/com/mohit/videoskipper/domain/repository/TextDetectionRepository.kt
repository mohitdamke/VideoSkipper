package com.mohit.videoskipper.domain.repository

import android.graphics.Bitmap

interface TextDetectionRepository {
    suspend fun recognizeText(bitmap: Bitmap): String
    suspend fun findMatchingKeyword(bitmap: Bitmap, keywords: List<String>): String?
}