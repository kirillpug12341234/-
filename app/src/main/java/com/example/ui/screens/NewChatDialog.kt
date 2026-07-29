package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SecretChatLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatModalSheet(
    onDismiss: () -> Unit,
    onSelectChatType: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Новое сообщение",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            NewChatItem(
                icon = Icons.Default.Group,
                title = "Создать группу",
                subtitle = "До 200 000 участников",
                onClick = {
                    onSelectChatType("android_devs")
                    onDismiss()
                }
            )

            NewChatItem(
                icon = Icons.Outlined.Lock,
                title = "Создать секретный чат",
                subtitle = "Сквозное шифрование, автоудаление",
                iconTint = SecretChatLock,
                onClick = {
                    onSelectChatType("sarah_secret")
                    onDismiss()
                }
            )

            NewChatItem(
                icon = Icons.Default.Campaign,
                title = "Создать канал",
                subtitle = "Канал для вещания без ограничений",
                onClick = {
                    onSelectChatType("telegram_news")
                    onDismiss()
                }
            )

            NewChatItem(
                icon = Icons.Default.Person,
                title = "Чат с ИИ Ассистентом",
                subtitle = "ИИ Ассистент Gemini 🤖",
                onClick = {
                    onSelectChatType("gemini_bot")
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NewChatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
