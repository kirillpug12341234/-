package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.TelegramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TelegramViewModel,
    user: UserEntity?
) {
    val currentUser = user ?: UserEntity()
    var name by remember(currentUser) { mutableStateOf(currentUser.displayName) }
    var username by remember(currentUser) { mutableStateOf(currentUser.username) }
    var bio by remember(currentUser) { mutableStateOf(currentUser.bio) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать профиль")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = currentUser.phoneNumber,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "@${currentUser.username}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentUser.bio,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Themes & Customization
            SettingsSectionTitle("Оформление и темы")
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column {
                    val themes = listOf("DARK", "TELEGRAM_BLUE", "AMOLED", "SUNSET", "MATRIX")
                    themes.forEach { themeName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateProfile(currentUser.displayName, currentUser.username, currentUser.bio, themeName) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentUser.themeMode.equals(themeName, ignoreCase = true),
                                onClick = { viewModel.updateProfile(currentUser.displayName, currentUser.username, currentUser.bio, themeName) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (themeName) {
                                    "DARK" -> "Тёмная по умолчанию (Стиль Telegram)"
                                    "TELEGRAM_BLUE" -> "Классическая синяя Telegram"
                                    "AMOLED" -> "Глубокая чёрная (AMOLED)"
                                    "SUNSET" -> "Закат (Фиолетовый градиент)"
                                    "MATRIX" -> "Матрица (Киберпанк зелёный)"
                                    else -> themeName
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Privacy & Security
            SettingsSectionTitle("Конфиденциальность и безопасность")
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.Security,
                        title = "Двухэтапная аутентификация (2FA)",
                        subtitle = if (currentUser.isTwoFactorEnabled) "Включено" else "Выключено",
                        onClick = {}
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        title = "Код-пароль и биометрия",
                        subtitle = if (currentUser.isPasscodeEnabled) "ПИН активен (1234)" else "Выключено",
                        trailing = {
                            Switch(
                                checked = currentUser.isPasscodeEnabled,
                                onCheckedChange = { viewModel.togglePasscodeLock() }
                            )
                        },
                        onClick = { viewModel.togglePasscodeLock() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Data & Storage
            SettingsSectionTitle("Данные и память")
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Распределение памяти", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Storage Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(0.4f).fillMaxHeight().background(Color(0xFF2196F3))) // Media
                        Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(Color(0xFFFF9800))) // Docs
                        Box(modifier = Modifier.weight(0.2f).fillMaxHeight().background(Color(0xFF4CAF50))) // Voice
                        Box(modifier = Modifier.weight(0.1f).fillMaxHeight().background(Color.Gray)) // Cache
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Фото: 1.2 ГБ • Файлы: 850 МБ • Голосовые: 220 МБ • Кэш: 140 МБ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Storage, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Очистить кэш (140 МБ)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit Profile Modal
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактировать профиль") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Имя") }
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Имя пользователя (@)") }
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("О себе") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(name, username, bio, currentUser.themeMode)
                        showEditDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            trailing()
        }
    }
}
