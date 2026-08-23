package com.mohit.videoskipper.data.repository

import com.mohit.videoskipper.domain.repository.ScrollEventRepository
import com.mohit.videoskipper.events.ScrollEventBus
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ScrollEventRepositoryImpl @Inject constructor(
    private val bus: ScrollEventBus
) : ScrollEventRepository {

    override fun observeScrollEvents(): Flow<Unit> = bus.events

    override fun notifyScrollDetected() {
        bus.emitScroll()
    }
}