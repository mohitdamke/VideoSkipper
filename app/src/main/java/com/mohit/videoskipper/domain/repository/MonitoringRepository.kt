package com.mohit.videoskipper.domain.repository

import kotlinx.coroutines.flow.Flow

interface MonitoringRepository {
    fun isTextDetectionEnabled(): Flow<Boolean>
    suspend fun setTextDetectionEnabled(enabled: Boolean)
    suspend fun getTextDetectionEnabledOnce(): Boolean

    fun isImageDetectionEnabled(): Flow<Boolean>
    suspend fun setImageDetectionEnabled(enabled: Boolean)
    suspend fun getImageDetectionEnabledOnce(): Boolean
}