package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channel_posts")
data class ChannelPostEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val channelTitle: String,
    val avatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val viewsCount: Int = 1200,
    val commentsCount: Int = 45,
    val mediaUrl: String? = null,
    val reactionsCount: String = "❤️ 234 • 🔥 89 • 👍 512"
)
