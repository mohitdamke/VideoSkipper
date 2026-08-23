package com.mohit.videoskipper.domain.repository

import kotlinx.coroutines.flow.Flow

interface ScrollEventRepository {
    fun observeScrollEvents(): Flow<Unit>
    fun notifyScrollDetected()
}