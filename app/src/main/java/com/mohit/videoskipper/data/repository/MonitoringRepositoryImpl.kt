package com.mohit.videoskipper.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mohit.videoskipper.domain.repository.MonitoringRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MonitoringRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : MonitoringRepository {

    private val textDetectionKey = booleanPreferencesKey("text_detection_enabled")
    private val imageDetectionKey = booleanPreferencesKey("image_detection_enabled")

    override fun isTextDetectionEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[textDetectionKey] ?: false }

    override suspend fun setTextDetectionEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[textDetectionKey] = enabled }
    }

    override suspend fun getTextDetectionEnabledOnce(): Boolean =
        isTextDetectionEnabled().first()

    override fun isImageDetectionEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[imageDetectionKey] ?: false }

    override suspend fun setImageDetectionEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[imageDetectionKey] = enabled }
    }

    override suspend fun getImageDetectionEnabledOnce(): Boolean =
        isImageDetectionEnabled().first()
}