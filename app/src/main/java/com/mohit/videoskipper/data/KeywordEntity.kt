package com.mohit.videoskipper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords")
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,          // e.g. "pizza"
    val isActive: Boolean = true, // allow user to toggle keyword on/off
    val createdAt: Long = System.currentTimeMillis()
)