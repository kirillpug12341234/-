package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val status: String = "READ", // SENT, DELIVERED, READ
    val replyToId: String? = null,
    val replyToText: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // PHOTO, VIDEO, VOICE, FILE, STICKER, GIF
    val voiceDurationSec: Int = 0,
    val isSecret: Boolean = false,
    val selfDestructSeconds: Int = 0,
    val reactions: String = "", // e.g., "👍,🔥"
    val isPinned: Boolean = false,
    val isEdited: Boolean = false
)
