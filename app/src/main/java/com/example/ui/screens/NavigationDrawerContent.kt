package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.MainTab
import com.example.ui.TelegramViewModel

@Composable
fun NavigationDrawerContent(
    viewModel: TelegramViewModel,
    user: UserEntity?,
    onCloseDrawer: () -> Unit,
    onSelectChat: (String) -> Unit
) {
    val currentUser = user ?: UserEntity()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("navigation_drawer")
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.displayName.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Outlined.QrCode,
                                contentDescription = "QR-код",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentUser.phoneNumber,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Drawer List Options
            Column(modifier = Modifier.weight(1f)) {
                DrawerItem(
                    icon = Icons.Default.Bookmark,
                    label = "Избранное",
                    onClick = {
                        onCloseDrawer()
                        onSelectChat("saved_messages")
                    }
                )

                DrawerItem(
                    icon = Icons.Outlined.Lock,
                    label = "Создать секретный чат",
                    onClick = {
                        onCloseDrawer()
                        onSelectChat("sarah_secret")
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Group,
                    label = "Создать группу",
                    onClick = {
                        onCloseDrawer()
                        onSelectChat("android_devs")
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Campaign,
                    label = "Создать канал",
                    onClick = {
                        onCloseDrawer()
                        onSelectChat("telegram_news")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DrawerItem(
                    icon = Icons.Default.Call,
                    label = "Звонки",
                    onClick = {
                        onCloseDrawer()
                        viewModel.setTab(MainTab.CALLS)
                    }
                )

                DrawerItem(
                    icon = Icons.Outlined.PersonAdd,
                    label = "Контакты и люди рядом",
                    onClick = {
                        onCloseDrawer()
                        viewModel.setTab(MainTab.CONTACTS)
                    }
                )

                DrawerItem(
                    icon = Icons.Default.Settings,
                    label = "Настройки",
                    onClick = {
                        onCloseDrawer()
                        viewModel.setTab(MainTab.SETTINGS)
                    }
                )
            }

            // Bottom Night Mode Quick Switch
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Nightlight, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Ночной режим", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = currentUser.themeMode != "TELEGRAM_BLUE",
                        onCheckedChange = { isDark ->
                            val newTheme = if (isDark) "DARK" else "TELEGRAM_BLUE"
                            viewModel.updateProfile(currentUser.displayName, currentUser.username, currentUser.bio, newTheme)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
