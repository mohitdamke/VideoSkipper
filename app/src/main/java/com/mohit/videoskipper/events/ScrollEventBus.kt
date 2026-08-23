package com.mohit.videoskipper.events

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class ScrollEventBus @Inject constructor() {

    // extraBufferCapacity + DROP_OLDEST so a burst of scroll ticks during a fling
    // doesn't queue up — we only care about "did a scroll just happen", not every tick.
    private val _events = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun emitScroll() {
        _events.tryEmit(Unit)
    }
}