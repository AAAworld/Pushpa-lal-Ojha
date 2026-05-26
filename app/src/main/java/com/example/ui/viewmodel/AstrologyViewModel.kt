package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReadingEntity
import com.example.data.model.UserEntity
import com.example.data.repository.AstrologyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ReadingUiState {
    object Idle : ReadingUiState
    object Loading : ReadingUiState
    data class Success(val reading: ReadingEntity) : ReadingUiState
    data class Error(val message: String) : ReadingUiState
}

class AstrologyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AstrologyRepository(
        userDao = db.userDao(),
        chatMessageDao = db.chatMessageDao(),
        readingDao = db.readingDao()
    )

    // Current Session States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Active AI Reading States
    private val _readingState = MutableStateFlow<ReadingUiState>(ReadingUiState.Idle)
    val readingState: StateFlow<ReadingUiState> = _readingState.asStateFlow()

    // Global settings and theme (stored in state layers)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _notificationsLog = MutableStateFlow<List<String>>(
        listOf("Welcome to CosmicAI! Star system is in alignment.", "Mercury retrograde simulation ending soon.")
    )
    val notificationsLog: StateFlow<List<String>> = _notificationsLog.asStateFlow()

    // Flowed Data from DB based on current logged in user
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getChatMessagesFlow(user.email)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedReadings: StateFlow<List<ReadingEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getReadingsForUser(user.email)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Dashboard flow listing all users
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Dashboard all saved readings flow
    val allGlobalReadings: StateFlow<List<ReadingEntity>> = repository.getAllReadingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-simulate default admin user if database is empty for testing convenience
        viewModelScope.launch {
            val existingAdmin = repository.getUserByEmail("admin@cosmic.ai")
            if (existingAdmin == null) {
                repository.registerUser(
                    email = "admin@cosmic.ai",
                    passwordRaw = "admin123",
                    name = "Divine Astrologer (Admin)",
                    dob = "1990-01-01",
                    tob = "12:00",
                    pob = "Varanasi, India",
                    zodiac = "Leo",
                    role = "ADMIN"
                )
                // Add default non-admin user
                val existingUser = repository.getUserByEmail("user@cosmic.ai")
                if (existingUser == null) {
                    repository.registerUser(
                        email = "user@cosmic.ai",
                        passwordRaw = "user123",
                        name = "Siddharth Sharma",
                        dob = "1995-10-15",
                        tob = "08:15",
                        pob = "Mumbai, India",
                        zodiac = "Libra"
                    )
                }
            }
        }
    }

    // --- Authentication Actions ---

    fun login(email: String, passwordRaw: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null) {
                val hashed = repository.hashPassword(passwordRaw)
                if (user.passwordHash == hashed) {
                    _currentUser.value = user
                    _selectedLanguage.value = user.selectedLanguage
                    onSuccess()
                } else {
                    _authError.value = "Invalid spiritual password. Match your energy!"
                }
            } else {
                _authError.value = "Profile not found under this email. Create one!"
            }
        }
    }

    fun signUp(
        email: String,
        passwordRaw: String,
        name: String,
        dob: String,
        tob: String,
        pob: String,
        zodiac: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || passwordRaw.isBlank() || name.isBlank()) {
                _authError.value = "All cosmic fields are required!"
                return@launch
            }
            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                _authError.value = "This constellation / email is already mapped!"
                return@launch
            }
            repository.registerUser(
                email = email,
                passwordRaw = passwordRaw,
                name = name,
                dob = dob,
                tob = tob,
                pob = pob,
                zodiac = zodiac
            )
            // Log in immediately
            login(email, passwordRaw, onSuccess)
        }
    }

    fun logout(onComplete: () -> Unit) {
        _currentUser.value = null
        onComplete()
    }

    fun updateZodiac(zodiac: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(zodiac = zodiac)
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun updateLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(selectedLanguage = langCode)
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun togglePushNotifications() {
        _pushNotificationsEnabled.value = !_pushNotificationsEnabled.value
    }

    fun simulateAlertTrigger(message: String) {
        val currentList = _notificationsLog.value.toMutableList()
        currentList.add(0, "🔔 [ALERT] $message")
        _notificationsLog.value = currentList
    }

    // --- Readings Flow Generation ---

    private fun startReadingProcess() {
        _readingState.value = ReadingUiState.Loading
    }

    fun clearReadingState() {
        _readingState.value = ReadingUiState.Idle
    }

    // Daily Horoscope Generative Call
    fun generateDailyHoroscope() {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getDailyHoroscope(user)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "DAILY_HOROSCOPE",
                title = "Daily Star Path (${user.zodiac})",
                mainContent = response,
                keyMetrics = "Lucky No: ${(1..99).random()}, Color: ${getRandomAeroColor()}"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Kundli Chart Call
    fun generateKundliAnalysis() {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getKundliAnalysis(user)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "KUNDLI",
                title = "Divine Rashi & Kundli Chart",
                mainContent = response,
                keyMetrics = "Lagna: ${user.zodiac}, Guna: 32/36 Align"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Love Compatibility Call
    fun generateLoveCompatibility(partnerName: String, partnerDob: String, partnerZodiac: String) {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getLoveCompatibility(user, partnerName, partnerDob, partnerZodiac)
            val affinity = (75..99).random()
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "LOVE_COMPATIBILITY",
                title = "Love Alignment with $partnerName",
                mainContent = response,
                keyMetrics = "Affinity: $affinity%, Harmony: High"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Career Destiny Call
    fun generateCareerPrediction() {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getCareerPrediction(user)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "CAREER",
                title = "10th House Career Destiny",
                mainContent = response,
                keyMetrics = "Vibe: Abundance, Focus: Planetary Midheaven"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Numerology Call
    fun generateNumerologyReport() {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getNumerologyReport(user)
            val lifePath = calculateLifePath(user.dob)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "NUMEROLOGY",
                title = "Cosmic Vibrations Numerological Study",
                mainContent = response,
                keyMetrics = "Life Path: $lifePath, Soul Urge: ${(1..9).random()}"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Tarot Spreading
    fun generateTarotSpread(chosenCards: List<String>) {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getTarotReading(user, chosenCards)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "TAROT",
                title = "Karmic 3-Card Tarot Study",
                mainContent = response,
                keyMetrics = "Layout: Past-Present-Future, Energy: Arcana"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Palm Image Capture Reading
    fun generatePalmReading(base64Image: String) {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getPalmReading(user, base64Image)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "PALM",
                title = "Divine Palmistry Mapping",
                mainContent = response,
                keyMetrics = "Hands Model: Chiromancy Mounts, Detail: High"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Custom bento user prompt consultation reading
    fun generateCustomPromptHoroscope(promptText: String) {
        val user = _currentUser.value ?: return
        startReadingProcess()
        viewModelScope.launch {
            val response = repository.getCustomReading(user, promptText)
            val reading = ReadingEntity(
                userEmail = user.email,
                type = "CUSTOM_READING",
                title = "Gemini AI Oracle Report",
                mainContent = response,
                keyMetrics = "Prompt: ${if(promptText.length > 20) promptText.take(17) + "..." else promptText}"
            )
            val id = repository.saveReading(reading)
            _readingState.value = ReadingUiState.Success(reading.copy(id = id.toInt()))
        }
    }

    // Toggle Saved Bookmark
    fun toggleFavoriteReading(reading: ReadingEntity) {
        viewModelScope.launch {
            repository.updateReading(reading.copy(isFavorite = !reading.isFavorite))
        }
    }

    // Delete reading of history
    fun deleteSavedReading(reading: ReadingEntity) {
        viewModelScope.launch {
            repository.deleteReading(reading)
        }
    }

    // --- Interactive Chat Actions ---

    fun sendChatMessage(text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Write user text message
            val userMsg = ChatMessageEntity(userEmail = user.email, text = text, isUser = true)
            repository.insertChatMessage(userMsg)

            // Feed history with new message and invoke AI
            val response = repository.getChatResponse(user, chatMessages.value, text)
            val aiMsg = ChatMessageEntity(userEmail = user.email, text = response, isUser = false)
            repository.insertChatMessage(aiMsg)
        }
    }

    fun clearChatHistory() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearChat(user.email)
        }
    }

    // --- Helper Math Algorithms ---

    private fun calculateLifePath(dob: String): Int {
        // dob format: e.g. "1995-10-15"
        val cleanDigits = dob.filter { it.isDigit() }
        var sum = cleanDigits.sumOf { it.toString().toInt() }
        while (sum > 9 && sum != 11 && sum != 22 && sum != 33) {
            sum = sum.toString().sumOf { it.toString().toInt() }
        }
        return sum
    }

    private fun getRandomAeroColor(): String {
        val colors = listOf("Aura Violet", "Nirvana Gold", "Spiritual Indigo", "Prana Turquoise", "Shakti Coral", "Celestial Silver")
        return colors.random()
    }

    // --- Admin Dashboard updates ---

    fun adminToggleUserPremium(user: UserEntity) {
        viewModelScope.launch {
            val updated = user.copy(isPremium = !user.isPremium)
            repository.updateUser(updated)
            // If the current logged in user was modified, update their session state!
            if (updated.email == _currentUser.value?.email) {
                _currentUser.value = updated
            }
        }
    }

    fun adminDeleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}
