package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainTab
import com.example.ui.TelegramViewModel
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TelegramTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TelegramViewModel) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val filteredChats by viewModel.filteredChats.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val channelPosts by viewModel.channelPosts.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val isDrawerOpen by viewModel.isDrawerOpen.collectAsStateWithLifecycle()
    val activeCallState by viewModel.activeCallState.collectAsStateWithLifecycle()
    val selectedStory by viewModel.selectedStory.collectAsStateWithLifecycle()
    val isPasscodeLocked by viewModel.isPasscodeLocked.collectAsStateWithLifecycle()
    val showSelfDestructDialog by viewModel.showSelfDestructDialog.collectAsStateWithLifecycle()

    var showNewChatModal by remember { mutableStateOf(false) }

    TelegramTheme(themeMode = user?.themeMode ?: "DARK") {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                // 1. Passcode Security Lock Screen
                isPasscodeLocked -> {
                    PasscodeLockScreen(viewModel = viewModel)
                }

                // 2. Active Call Screen (Audio / Video)
                activeCallState != null -> {
                    CallScreen(viewModel = viewModel, callState = activeCallState!!)
                }

                // 3. Story Player Modal
                selectedStory != null -> {
                    StoryPlayerModal(
                        story = selectedStory!!,
                        onClose = { viewModel.closeStory() }
                    )
                }

                // 4. Main App Interface
                else -> {
                    val drawerState = rememberDrawerState(initialValue = if (isDrawerOpen) DrawerValue.Open else DrawerValue.Closed)
                    LaunchedEffect(isDrawerOpen) {
                        if (isDrawerOpen) drawerState.open() else drawerState.close()
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            NavigationDrawerContent(
                                viewModel = viewModel,
                                user = user,
                                onCloseDrawer = { viewModel.closeDrawer() },
                                onSelectChat = { chatId ->
                                    viewModel.selectChat(chatId)
                                }
                            )
                        }
                    ) {
                        if (selectedChatId != null) {
                            val activeChat = filteredChats.find { it.id == selectedChatId }
                                ?: viewModel.rawChats.value.find { it.id == selectedChatId }

                            if (activeChat != null) {
                                ChatDetailScreen(
                                    viewModel = viewModel,
                                    chat = activeChat,
                                    messages = activeMessages,
                                    onBackClick = { viewModel.selectChat(null) }
                                )
                            }
                        } else {
                            Scaffold(
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.testTag("bottom_navigation")
                                    ) {
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.CHATS,
                                            onClick = { viewModel.setTab(MainTab.CHATS) },
                                            icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Чаты") },
                                            label = { Text("Чаты") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.STORIES,
                                            onClick = { viewModel.setTab(MainTab.STORIES) },
                                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Каналы") },
                                            label = { Text("Каналы") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.CALLS,
                                            onClick = { viewModel.setTab(MainTab.CALLS) },
                                            icon = { Icon(Icons.Outlined.Call, contentDescription = "Звонки") },
                                            label = { Text("Звонки") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.CONTACTS,
                                            onClick = { viewModel.setTab(MainTab.CONTACTS) },
                                            icon = { Icon(Icons.Outlined.People, contentDescription = "Контакты") },
                                            label = { Text("Контакты") }
                                        )
                                        NavigationBarItem(
                                            selected = currentTab == MainTab.SETTINGS,
                                            onClick = { viewModel.setTab(MainTab.SETTINGS) },
                                            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Настройки") },
                                            label = { Text("Настройки") }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                ) {
                                    Crossfade(targetState = currentTab, label = "tab_fade") { tab ->
                                        when (tab) {
                                            MainTab.CHATS -> {
                                                ChatListScreen(
                                                    viewModel = viewModel,
                                                    chats = filteredChats,
                                                    stories = stories,
                                                    selectedFilter = selectedFilter,
                                                    searchQuery = searchQuery,
                                                    onChatClick = { chatId -> viewModel.selectChat(chatId) },
                                                    onOpenDrawer = { viewModel.toggleDrawer() },
                                                    onStoryClick = { story -> viewModel.openStory(story) },
                                                    onNewChatClick = { showNewChatModal = true }
                                                )
                                            }

                                            MainTab.STORIES -> {
                                                ChannelsScreen(
                                                    posts = channelPosts,
                                                    onOpenDrawer = { viewModel.toggleDrawer() }
                                                )
                                            }

                                            MainTab.CALLS -> {
                                                CallsHistoryScreen(
                                                    chats = filteredChats,
                                                    onStartCall = { chat, isVideo -> viewModel.startCall(chat, isVideo) }
                                                )
                                            }

                                            MainTab.CONTACTS -> {
                                                ContactsScreen(
                                                    chats = filteredChats,
                                                    onSelectChat = { chatId -> viewModel.selectChat(chatId) }
                                                )
                                            }

                                            MainTab.SETTINGS -> {
                                                SettingsScreen(
                                                    viewModel = viewModel,
                                                    user = user
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Self-Destruct Seconds Selector Dialog
            if (showSelfDestructDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowSelfDestructDialog(false) },
                    title = { Text("Таймер автоудаления") },
                    text = {
                        Column {
                            Text("Выберите таймер автоудаления для секретного чата:")
                            Spacer(modifier = Modifier.height(12.dp))
                            listOf(5, 10, 30, 60, 3600).forEach { sec ->
                                TextButton(
                                    onClick = { viewModel.setSecretSelfDestruct(sec) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (sec == 3600) "1 час" else "$sec секунд")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.setShowSelfDestructDialog(false) }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            // New Chat Bottom Sheet
            if (showNewChatModal) {
                NewChatModalSheet(
                    onDismiss = { showNewChatModal = false },
                    onSelectChatType = { chatId ->
                        viewModel.selectChat(chatId)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsHistoryScreen(
    chats: List<com.example.data.local.entities.ChatEntity>,
    onStartCall: (com.example.data.local.entities.ChatEntity, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Недавние звонки", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(chats) { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(chat.title.take(1), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(chat.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CallReceived, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Входящий звонок • Сегодня 14:20", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = { onStartCall(chat, false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Позвонить", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onStartCall(chat, true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Видеозвонок", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    chats: List<com.example.data.local.entities.ChatEntity>,
    onSelectChat: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакты и люди рядом", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Люди рядом", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Поиск пользователей и чатов неподалёку", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
            }

            items(chats) { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectChat(chat.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(chat.title.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(chat.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(if (chat.isOnline) "в сети" else "был(а) недавно", fontSize = 12.sp, color = if (chat.isOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
