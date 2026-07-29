package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String = "",
    val chatType: String = "DIRECT", // DIRECT, GROUP, CHANNEL, SECRET, BOT
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isOnline: Boolean = false,
    val verified: Boolean = false,
    val selfDestructSeconds: Int = 0, // 0 = off
    val membersCount: Int = 1,
    val description: String = ""
)
