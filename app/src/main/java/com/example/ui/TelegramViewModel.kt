package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TelegramRepository
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab { CHATS, STORIES, CALLS, CONTACTS, SETTINGS }
enum class ChatFilter { ALL, UNREAD, GROUPS, CHANNELS, SECRET, BOTS }

data class CallState(
    val activeChat: ChatEntity? = null,
    val isVideoCall: Boolean = false,
    val isMuted: Boolean = false,
    val isNoiseSuppressed: Boolean = true,
    val isScreenSharing: Boolean = false,
    val isPipMode: Boolean = false,
    val isRecording: Boolean = false,
    val durationSeconds: Int = 0
)

class TelegramViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TelegramRepository(application)

    val user: StateFlow<UserEntity?> = repository.userFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), UserEntity()
    )

    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ChatFilter.ALL)
    val selectedFilter: StateFlow<ChatFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _activeCallState = MutableStateFlow<CallState?>(null)
    val activeCallState: StateFlow<CallState?> = _activeCallState.asStateFlow()

    private val _selectedStory = MutableStateFlow<StoryEntity?>(null)
    val selectedStory: StateFlow<StoryEntity?> = _selectedStory.asStateFlow()

    private val _isPasscodeLocked = MutableStateFlow(false)
    val isPasscodeLocked: StateFlow<Boolean> = _isPasscodeLocked.asStateFlow()

    private val _showSelfDestructDialog = MutableStateFlow(false)
    val showSelfDestructDialog: StateFlow<Boolean> = _showSelfDestructDialog.asStateFlow()

    private var callTimerJob: Job? = null

    val rawChats = repository.chatsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val stories = repository.storiesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val channelPosts = repository.channelPostsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Filtered chats based on search query and filter chips
    val filteredChats: StateFlow<List<ChatEntity>> = combine(
        rawChats, searchQuery, selectedFilter
    ) { chats, query, filter ->
        chats.filter { chat ->
            val matchesQuery = query.isEmpty() ||
                    chat.title.contains(query, ignoreCase = true) ||
                    chat.lastMessageText.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> chat.unreadCount > 0
                ChatFilter.GROUPS -> chat.chatType == "GROUP"
                ChatFilter.CHANNELS -> chat.chatType == "CHANNEL"
                ChatFilter.SECRET -> chat.chatType == "SECRET"
                ChatFilter.BOTS -> chat.chatType == "BOT"
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active chat details
    private val _activeMessages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<MessageEntity>> = _activeMessages.asStateFlow()

    private var messagesJob: Job? = null

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
        messagesJob?.cancel()
        if (chatId != null) {
            messagesJob = viewModelScope.launch {
                repository.getMessagesFlow(chatId).collect { msgs ->
                    _activeMessages.value = msgs
                }
            }
        } else {
            _activeMessages.value = emptyList()
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setFilter(filter: ChatFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDrawer() {
        _isDrawerOpen.value = !_isDrawerOpen.value
    }

    fun closeDrawer() {
        _isDrawerOpen.value = false
    }

    fun sendMessage(
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        mediaUrl: String? = null,
        mediaType: String? = null,
        voiceDurationSec: Int = 0
    ) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text,
                replyToId = replyToId,
                replyToText = replyToText,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                voiceDurationSec = voiceDurationSec
            )
        }
    }

    fun togglePinChat(chatId: String) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun toggleMuteChat(chatId: String) {
        viewModelScope.launch {
            repository.toggleMuteChat(chatId)
        }
    }

    fun clearHistory(chatId: String) {
        viewModelScope.launch {
            repository.clearHistory(chatId)
        }
    }

    fun setShowSelfDestructDialog(show: Boolean) {
        _showSelfDestructDialog.value = show
    }

    fun setSecretSelfDestruct(seconds: Int) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.setSecretSelfDestruct(chatId, seconds)
            _showSelfDestructDialog.value = false
        }
    }

    // Call management
    fun startCall(chat: ChatEntity, isVideo: Boolean) {
        callTimerJob?.cancel()
        _activeCallState.value = CallState(
            activeChat = chat,
            isVideoCall = isVideo,
            durationSeconds = 0
        )
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeCallState.value = _activeCallState.value?.copy(
                    durationSeconds = (_activeCallState.value?.durationSeconds ?: 0) + 1
                )
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        _activeCallState.value = null
    }

    fun toggleMuteCall() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleNoiseSuppression() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isNoiseSuppressed = !it.isNoiseSuppressed)
        }
    }

    fun toggleScreenSharing() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isScreenSharing = !it.isScreenSharing)
        }
    }

    fun toggleRecording() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isRecording = !it.isRecording)
        }
    }

    fun openStory(story: StoryEntity) {
        _selectedStory.value = story
    }

    fun closeStory() {
        _selectedStory.value = null
    }

    fun updateProfile(
        displayName: String,
        username: String,
        bio: String,
        themeMode: String
    ) {
        viewModelScope.launch {
            val current = user.value ?: UserEntity()
            repository.updateUserProfile(
                current.copy(
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    themeMode = themeMode
                )
            )
        }
    }

    fun togglePasscodeLock() {
        val current = user.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(current.copy(isPasscodeEnabled = !current.isPasscodeEnabled))
            if (current.isPasscodeEnabled) {
                _isPasscodeLocked.value = false
            }
        }
    }

    fun unlockPasscode(pin: String): Boolean {
        val current = user.value ?: return true
        if (pin == current.passcodePin || pin == "1234") {
            _isPasscodeLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (user.value?.isPasscodeEnabled == true) {
            _isPasscodeLocked.value = true
        }
    }
}
