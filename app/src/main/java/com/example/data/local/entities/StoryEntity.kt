package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val avatarUrl: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val viewsCount: Int = 0,
    val isMuted: Boolean = false,
    val isUserStory: Boolean = false
)
