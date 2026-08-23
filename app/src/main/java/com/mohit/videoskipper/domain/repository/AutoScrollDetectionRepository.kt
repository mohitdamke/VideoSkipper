package com.mohit.videoskipper.domain.repository

import android.graphics.Bitmap
import com.mohit.videoskipper.events.AutoScrollDecision

interface AutoScrollDetectionRepository {
    suspend fun decide(bitmap: Bitmap): AutoScrollDecision
}