package com.mohit.videoskipper.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.mohit.videoskipper.domain.repository.AutoScrollDetectionRepository
import com.mohit.videoskipper.domain.repository.KeywordRepository
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import com.mohit.videoskipper.domain.repository.TextDetectionRepository
import com.mohit.videoskipper.events.AutoScrollDecision
import jakarta.inject.Inject

class AutoScrollDetectionRepositoryImpl @Inject constructor(
    private val keywordRepository: KeywordRepository,
    private val textDetectionRepository: TextDetectionRepository,
    private val monitoringRepository: MonitoringRepository
) : AutoScrollDetectionRepository {
    private val TAG = "HEEEEWaooo"

    override suspend fun decide(bitmap: Bitmap): AutoScrollDecision {
        val isEnabled = monitoringRepository.getTextDetectionEnabledOnce()
        Log.d(TAG, "Text detection enabled flag = $isEnabled")
        if (!isEnabled) return AutoScrollDecision.DetectionDisabled

        val activeKeywords = keywordRepository.getActiveKeywordTexts()
        Log.d(TAG, "Active keywords from DB: $activeKeywords")
        if (activeKeywords.isEmpty()) return AutoScrollDecision.Stay

        val match = textDetectionRepository.findMatchingKeyword(bitmap, activeKeywords)
        return if (match != null) AutoScrollDecision.Skip(match) else AutoScrollDecision.Stay
    }
}