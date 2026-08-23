package com.mohit.videoskipper.events

sealed interface AutoScrollDecision {
    data class Skip(val matchedKeyword: String) : AutoScrollDecision
    data object Stay : AutoScrollDecision
    data object DetectionDisabled : AutoScrollDecision
}