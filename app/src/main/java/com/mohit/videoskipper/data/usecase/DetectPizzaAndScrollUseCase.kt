package com.mohit.videoskipper.data.usecase

import android.graphics.Bitmap
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.TextDetectionRepository
import jakarta.inject.Inject

class DetectPizzaAndScrollUseCase @Inject constructor(
    private val keywordRepository: KeywordRepository,
    private val textDetectionRepository: TextDetectionRepository
) {
    suspend operator fun invoke(screenBitmap: Bitmap): String? {
        val activeKeywords = keywordRepository.getActiveKeywordTexts()
        return textDetectionRepository.findMatchingKeyword(screenBitmap, activeKeywords)
        // returns the matched keyword (e.g. "pizza") or null — caller decides to auto-scroll
    }
}