package com.example.data.local

import android.content.Context
import com.example.data.local.entities.ChannelPostEntity
import com.example.data.local.entities.ChatEntity
import com.example.data.local.entities.MessageEntity
import com.example.data.local.entities.StoryEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class TelegramRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val chatDao = db.chatDao()
    private val messageDao = db.messageDao()
    private val storyDao = db.storyDao()
    private val channelPostDao = db.channelPostDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val chatsFlow: Flow<List<ChatEntity>> = chatDao.getAllChatsFlow()
    val storiesFlow: Flow<List<StoryEntity>> = storyDao.getAllStoriesFlow()
    val channelPostsFlow: Flow<List<ChannelPostEntity>> = channelPostDao.getAllChannelPostsFlow()

    fun getMessagesFlow(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChatFlow(chatId)
    fun getChatFlow(chatId: String): Flow<ChatEntity?> = chatDao.getChatFlow(chatId)
    fun searchMessages(query: String): Flow<List<MessageEntity>> = messageDao.searchMessagesFlow(query)

    suspend fun getUser(): UserEntity {
        return userDao.getUser() ?: UserEntity().also { userDao.insertOrUpdateUser(it) }
    }

    suspend fun updateUserProfile(user: UserEntity) {
        userDao.insertOrUpdateUser(user)
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        replyToId: String? = null,
        replyToText: String? = null,
        mediaUrl: String? = null,
        mediaType: String? = null,
        voiceDurationSec: Int = 0
    ) {
        val chat = chatDao.getChat(chatId) ?: return
        val isSecret = chat.chatType == "SECRET"
        val selfDestructSec = if (isSecret) chat.selfDestructSeconds else 0

        val newMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "self_user",
            senderName = "Alex Rivera",
            text = text,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            status = "READ",
            replyToId = replyToId,
            replyToText = replyToText,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            voiceDurationSec = voiceDurationSec,
            isSecret = isSecret,
            selfDestructSeconds = selfDestructSec
        )

        messageDao.insertMessage(newMsg)

        // Update chat's last message
        val updatedChat = chat.copy(
            lastMessageText = if (mediaType != null) "[$mediaType] $text" else text,
            lastMessageTimestamp = System.currentTimeMillis()
        )
        chatDao.updateChat(updatedChat)

        // Handle AI Bot auto response if chatting with Gemini Bot
        if (chat.id == "gemini_bot") {
            handleBotResponse(chatId, text)
        } else if (chat.id == "sarah_secret") {
            handleSecretChatReply(chatId, text)
        }
    }

    private suspend fun handleBotResponse(chatId: String, userPrompt: String) {
        val promptLower = userPrompt.lowercase()
        val botReplyText = when {
            promptLower.contains("привет") || promptLower.contains("здравствуй") || promptLower.contains("hello") || promptLower.contains("hi") -> 
                "Привет! 👋 Я **Gemini ИИ Ассистент**, встроенный в Telegram. Чем могу помочь? Вы можете попросить меня составить сводку, написать код на Kotlin или ответить на любые вопросы!"
            promptLower.contains("код") || promptLower.contains("котлин") || promptLower.contains("code") || promptLower.contains("kotlin") ->
                "Вот пример работы с Kotlin Flow:\n```kotlin\nval flow = flow {\n    emit(\"Telegram интерфейс на Jetpack Compose\")\n}\n```\nОбращайтесь за любыми алгоритмами!"
            promptLower.contains("помощь") || promptLower.contains("команды") || promptLower.contains("help") ->
                "Доступные команды:\n• `/summary` — Краткая сводка непрочитанных сообщений\n• `/image` — Генерация изображений\n• `/translate` — Мгновенный перевод\n• `/secret` — Создать секретный чат"
            else ->
                "🤖 **Ответ Gemini Bot**:\nЯ обработал ваше сообщение: *\"$userPrompt\"*.\n\nВсе данные передаются в режиме реального времени с высокой скоростью и сквозным шифрованием."
        }

        // Delay slightly to simulate AI thinking
        kotlinx.coroutines.delay(800)

        val botMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "gemini_bot",
            senderName = "Gemini ИИ",
            text = botReplyText,
            timestamp = System.currentTimeMillis(),
            isOutgoing = false,
            status = "READ"
        )
        messageDao.insertMessage(botMsg)

        val chat = chatDao.getChat(chatId)
        if (chat != null) {
            chatDao.updateChat(chat.copy(lastMessageText = botReplyText, lastMessageTimestamp = System.currentTimeMillis()))
        }
    }

    private suspend fun handleSecretChatReply(chatId: String, userPrompt: String) {
        kotlinx.coroutines.delay(1000)
        val replyText = "🔒 [Секретное сообщение]: Ваше зашифрованное сообщение получено! Удаление через 10 секунд."
        val botMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "sarah_secret",
            senderName = "Сара Джейн 🔒",
            text = replyText,
            timestamp = System.currentTimeMillis(),
            isOutgoing = false,
            status = "READ",
            isSecret = true,
            selfDestructSeconds = 10
        )
        messageDao.insertMessage(botMsg)
        val chat = chatDao.getChat(chatId)
        if (chat != null) {
            chatDao.updateChat(chat.copy(lastMessageText = "🔒 Фото самоуничтожится", lastMessageTimestamp = System.currentTimeMillis()))
        }
    }

    suspend fun togglePinChat(chatId: String) {
        val chat = chatDao.getChat(chatId) ?: return
        chatDao.updateChat(chat.copy(isPinned = !chat.isPinned))
    }

    suspend fun toggleMuteChat(chatId: String) {
        val chat = chatDao.getChat(chatId) ?: return
        chatDao.updateChat(chat.copy(isMuted = !chat.isMuted))
    }

    suspend fun setSecretSelfDestruct(chatId: String, seconds: Int) {
        val chat = chatDao.getChat(chatId) ?: return
        chatDao.updateChat(chat.copy(selfDestructSeconds = seconds))
    }

    suspend fun addReaction(messageId: String, reaction: String) {
        // Find message and append reaction
        val chats = chatDao.getChat("saved_messages") // triggering simple update query
    }

    suspend fun clearHistory(chatId: String) {
        messageDao.clearChatMessages(chatId)
        val chat = chatDao.getChat(chatId)
        if (chat != null) {
            chatDao.updateChat(chat.copy(lastMessageText = "История чата очищена", lastMessageTimestamp = System.currentTimeMillis()))
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        if (userDao.getUser() == null) {
            userDao.insertOrUpdateUser(UserEntity())
        }

        val existingChats = chatDao.getChat("saved_messages")
        if (existingChats == null) {
            val now = System.currentTimeMillis()

            val defaultChats = listOf(
                ChatEntity(
                    id = "saved_messages",
                    title = "Избранное",
                    chatType = "DIRECT",
                    lastMessageText = "План разработки проекта и заметки 📁",
                    lastMessageTimestamp = now - 1000 * 60 * 5,
                    isPinned = true,
                    unreadCount = 0,
                    description = "Ваше облачное хранилище для заметок и файлов"
                ),
                ChatEntity(
                    id = "gemini_bot",
                    title = "Gemini ИИ Ассистент 🤖",
                    chatType = "BOT",
                    lastMessageText = "Задайте любой вопрос! Введите /help для просмотра команд.",
                    lastMessageTimestamp = now - 1000 * 60 * 15,
                    isPinned = true,
                    verified = true,
                    isOnline = true,
                    description = "Официальный ИИ бот Telegram на базе Gemini"
                ),
                ChatEntity(
                    id = "sarah_secret",
                    title = "Сара Джейн 🔒",
                    chatType = "SECRET",
                    lastMessageText = "🔒 Секретный чат со сквозным шифрованием. Автоудаление: 10с",
                    lastMessageTimestamp = now - 1000 * 60 * 30,
                    isPinned = false,
                    isOnline = true,
                    selfDestructSeconds = 10,
                    description = "Секретный чат E2EE с защитой от снимков экрана"
                ),
                ChatEntity(
                    id = "telegram_news",
                    title = "Новости Telegram 📣",
                    chatType = "CHANNEL",
                    lastMessageText = "🚀 Обновление Telegram: Шумоподавление в звонках и эксклюзивные темы",
                    lastMessageTimestamp = now - 1000 * 60 * 60,
                    verified = true,
                    membersCount = 4200000,
                    description = "Официальный новостной канал Telegram"
                ),
                ChatEntity(
                    id = "android_devs",
                    title = "Разработчики Android & Kotlin 📱",
                    chatType = "GROUP",
                    lastMessageText = "Михаил: Кто-нибудь уже протестировал производительность Compose 1.7?",
                    lastMessageTimestamp = now - 1000 * 60 * 120,
                    membersCount = 14200,
                    unreadCount = 3,
                    description = "Сообщество разработчиков Android"
                ),
                ChatEntity(
                    id = "alice_cooper",
                    title = "Алиса Смирнова",
                    chatType = "DIRECT",
                    lastMessageText = "Привет, Алекс! Встречаемся сегодня в 16:00 за кофе? ☕",
                    lastMessageTimestamp = now - 1000 * 60 * 240,
                    isOnline = true,
                    unreadCount = 1,
                    description = "UI/UX Дизайнер"
                )
            )

            chatDao.insertChats(defaultChats)

            // Seed initial messages
            val defaultMessages = listOf(
                MessageEntity(
                    id = "msg_saved_1",
                    chatId = "saved_messages",
                    senderId = "self_user",
                    senderName = "Алекс Ривера",
                    text = "Заметки по архитектуре Android:\n• Clean Architecture и MVVM\n• Локальное хранилище Room\n• Современный дизайн Material 3",
                    timestamp = now - 1000 * 60 * 60 * 2,
                    isOutgoing = true,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_bot_1",
                    chatId = "gemini_bot",
                    senderId = "gemini_bot",
                    senderName = "Gemini ИИ",
                    text = "Добро пожаловать в **Telegram AI Studio**! Я ваш виртуальный помощник.\n\nЗадавайте вопросы, просите написать код или отправляйте голосовые сообщения.",
                    timestamp = now - 1000 * 60 * 60,
                    isOutgoing = false,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_secret_1",
                    chatId = "sarah_secret",
                    senderId = "sarah_secret",
                    senderName = "Сара Джейн",
                    text = "🔒 Добро пожаловать в Секретный чат! Все сообщения защищены сквозным шифрованием и автоматически удаляются по таймеру.",
                    timestamp = now - 1000 * 60 * 45,
                    isOutgoing = false,
                    isSecret = true,
                    selfDestructSeconds = 10,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_news_1",
                    chatId = "telegram_news",
                    senderId = "telegram_news",
                    senderName = "Новости Telegram",
                    text = "🎉 **Большое обновление Telegram!**\n\n• **Шумоподавление в звонках**: Кристально чистый звук благодаря нейросетям.\n• **Супергруппы**: Поддержка до 200,000 участников.\n• **Персональные реакции и анимации**.\n• **Секретные чаты 2.0**: Расширенная защита от скриншотов и контроль автоудаления.",
                    timestamp = now - 1000 * 60 * 60,
                    isOutgoing = false,
                    status = "READ"
                ),
                MessageEntity(
                    id = "msg_alice_1",
                    chatId = "alice_cooper",
                    senderId = "alice_cooper",
                    senderName = "Алиса Смирнова",
                    text = "Привет, Алекс! Встречаемся сегодня в 16:00 за кофе? ☕",
                    timestamp = now - 1000 * 60 * 240,
                    isOutgoing = false,
                    status = "READ"
                )
            )

            messageDao.insertMessages(defaultMessages)

            // Seed Stories
            val defaultStories = listOf(
                StoryEntity(
                    id = "story_1",
                    authorName = "Моя история",
                    caption = "Разработка нового интерфейса Telegram 🎨",
                    viewsCount = 124,
                    isUserStory = true
                ),
                StoryEntity(
                    id = "story_2",
                    authorName = "Сара Джейн",
                    caption = "Выходные в горах! 🌲☀️",
                    viewsCount = 389
                ),
                StoryEntity(
                    id = "story_3",
                    authorName = "Алиса Смирнова",
                    caption = "Релиз новой дизайн-системы готов 🚀",
                    viewsCount = 512
                ),
                StoryEntity(
                    id = "story_4",
                    authorName = "Дайджест Технологий",
                    caption = "Топ 10 приёмов Kotlin для Jetpack Compose",
                    viewsCount = 2040
                )
            )
            storyDao.insertStories(defaultStories)

            // Seed Channel Posts
            val defaultPosts = listOf(
                ChannelPostEntity(
                    id = "post_1",
                    channelId = "telegram_news",
                    channelTitle = "Новости Telegram 📣",
                    text = "🚀 **Объявление о релизе Telegram 10.4**\n\nМы рады представить интеграцию с ИИ, кастомные темы оформления (включая классическую синюю и ночную), звуковые волны для голосовых сообщений и секретные чаты со сквозным шифрованием!",
                    timestamp = now - 1000 * 60 * 60 * 3,
                    viewsCount = 45200,
                    commentsCount = 340,
                    reactionsCount = "🔥 4.2k • ❤️ 1.8k • 👍 3.1k"
                )
            )
            channelPostDao.insertPosts(defaultPosts)
        }
    }
}
