package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String = "self_user",
    val phoneNumber: String = "+7 (999) 019-28-34",
    val displayName: String = "Алекс Ривера",
    val username: String = "alex_rivera",
    val bio: String = "Создаю будущее на современном стеке 🚀 | Открыт к общению",
    val avatarUrl: String = "",
    val isOnline: Boolean = true,
    val lastSeenText: String = "в сети",
    val isTwoFactorEnabled: Boolean = true,
    val passcodePin: String = "1234",
    val isPasscodeEnabled: Boolean = false,
    val themeMode: String = "DARK", // TELEGRAM_BLUE, DARK, AMOLED, SUNSET, MATRIX
    val fontSizeSp: Int = 16,
    val autoDownloadMedia: Boolean = true,
    val lastSeenPrivacy: String = "EVERYBODY", // EVERYBODY, CONTACTS, NOBODY
    val phonePrivacy: String = "MY_CONTACTS"
)
