package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.ui.TelegramViewModel
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecretChatLock
import com.example.ui.theme.TelegramDarkBubbleIncoming
import com.example.ui.theme.TelegramDarkBubbleOutgoing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: TelegramViewModel,
    chat: ChatEntity,
    messages: List<MessageEntity>,
    onBackClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var replyToMsg by remember { mutableStateOf<MessageEntity?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isSecret = chat.chatType == "SECRET"

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        if (isSecret) SecretChatLock.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.title.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isSecret) SecretChatLock else MaterialTheme.colorScheme.primary
                                )
                            }
                            if (chat.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSecret) {
                                    Icon(
                                        Icons.Outlined.Lock,
                                        contentDescription = "Secret",
                                        tint = SecretChatLock,
                                        modifier = Modifier.size(14.dp).padding(end = 4.dp)
                                    )
                                }
                                Text(
                                    text = chat.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = when {
                                    chat.isOnline -> "в сети"
                                    chat.chatType == "BOT" -> "бот"
                                    chat.chatType == "CHANNEL" -> "${chat.membersCount} подписчиков"
                                    chat.chatType == "GROUP" -> "${chat.membersCount} участников"
                                    else -> "был(а) недавно"
                                },
                                fontSize = 12.sp,
                                color = if (chat.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startCall(chat, isVideo = false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Аудиозвонок")
                    }
                    IconButton(onClick = { viewModel.startCall(chat, isVideo = true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Видеозвонок")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isSecret) {
                                DropdownMenuItem(
                                    text = { Text("Таймер автоудаления (${chat.selfDestructSeconds}с)") },
                                    leadingIcon = { Icon(Icons.Outlined.Timer, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setShowSelfDestructDialog(true)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(if (chat.isMuted) "Включить уведомления" else "Выключить уведомления") },
                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleMuteChat(chat.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Очистить историю") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.clearHistory(chat.id)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Secret Chat Info Banner
            if (isSecret) {
                Surface(
                    color = SecretChatLock.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = SecretChatLock,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Секретный чат: Сквозное шифрование • Автоудаление ${chat.selfDestructSeconds}с • Запрет пересылки",
                            fontSize = 12.sp,
                            color = SecretChatLock,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onReplyClick = { replyToMsg = msg },
                        onReactionClick = { emoji -> viewModel.sendMessage("Reacted $emoji") }
                    )
                }
            }

            // Reply Preview Header
            replyToMsg?.let { replyMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = replyMsg.senderName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = replyMsg.text,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { replyToMsg = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply")
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachmentSheet = true }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Прикрепить файл")
                    }

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Написать сообщение...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.sendMessage(
                                    text = "🎤 Голосовое сообщение (0:05)",
                                    mediaType = "VOICE",
                                    voiceDurationSec = 5
                                )
                            }
                        ) {
                            Icon(Icons.Outlined.Mic, contentDescription = "Записать голосовое", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.sendMessage(
                                    text = inputText,
                                    replyToId = replyToMsg?.id,
                                    replyToText = replyToMsg?.text
                                )
                                inputText = ""
                                replyToMsg = null
                            },
                            modifier = Modifier.testTag("send_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Отправить", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Attachment Modal Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Поделиться",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Галерея",
                        color = Color(0xFF2196F3),
                        onClick = {
                            showAttachmentSheet = false
                            viewModel.sendMessage("🖼️ Фото прикреплено", mediaType = "PHOTO")
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.InsertDriveFile,
                        label = "Файл",
                        color = Color(0xFFFF9800),
                        onClick = {
                            showAttachmentSheet = false
                            viewModel.sendMessage("📄 Отчёт_v2.pdf (4.2 МБ)", mediaType = "FILE")
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Геолокация",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            showAttachmentSheet = false
                            viewModel.sendMessage("📍 Трансляция геолокации", mediaType = "LOCATION")
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Poll,
                        label = "Опрос",
                        color = Color(0xFF9C27B0),
                        onClick = {
                            showAttachmentSheet = false
                            viewModel.sendMessage("📊 Опрос: Какую функцию добавить следующей?", mediaType = "POLL")
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onReplyClick: () -> Unit,
    onReactionClick: (String) -> Unit
) {
    val isOutgoing = message.isOutgoing
    val formattedTime = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isOutgoing) 16.dp else 4.dp,
                    bottomEnd = if (isOutgoing) 4.dp else 16.dp
                ),
                color = if (isOutgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Sender Name in Groups
                    if (!isOutgoing && message.senderName.isNotEmpty()) {
                        Text(
                            text = message.senderName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Reply Header
                    message.replyToText?.let { replyText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.1f))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = replyText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Media Waveform for Voice Notes
                    if (message.mediaType == "VOICE") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play voice note")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("||||\u00A6|\u00A6||||\u00A6|\u00A6|||", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("0:05", fontSize = 11.sp)
                        }
                    }

                    // Main Text Content
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timestamp & Status Ticks
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (message.isSecret) {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = "Secret Timer",
                                modifier = Modifier.size(12.dp),
                                tint = if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Read",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
