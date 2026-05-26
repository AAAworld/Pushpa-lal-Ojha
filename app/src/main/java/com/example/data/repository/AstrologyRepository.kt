package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatMessageDao
import com.example.data.local.ReadingDao
import com.example.data.local.UserDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReadingEntity
import com.example.data.model.UserEntity
import com.example.data.remote.*
import kotlinx.coroutines.flow.Flow
import java.io.Serializable
import java.security.MessageDigest

class AstrologyRepository(
    private val userDao: UserDao,
    private val chatMessageDao: ChatMessageDao,
    private val readingDao: ReadingDao
) {

    // --- User authentication & Profile management ---

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email.lowercase().trim())
    }

    suspend fun registerUser(
        email: String,
        passwordRaw: String,
        name: String,
        dob: String,
        tob: String,
        pob: String,
        zodiac: String,
        role: String = "USER"
    ): Long {
        val hashedPassword = hashPassword(passwordRaw)
        val user = UserEntity(
            email = email.lowercase().trim(),
            passwordHash = hashedPassword,
            name = name,
            dob = dob,
            tob = tob,
            pob = pob,
            zodiac = zodiac,
            role = role,
            isPremium = false
        )
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> {
        return userDao.getAllUsersFlow()
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    // --- Saved Readings ---

    fun getReadingsForUser(email: String): Flow<List<ReadingEntity>> {
        return readingDao.getReadingsForUser(email.lowercase().trim())
    }

    fun getAllReadingsFlow(): Flow<List<ReadingEntity>> {
        return readingDao.getAllReadingsFlow()
    }

    suspend fun saveReading(reading: ReadingEntity): Long {
        return readingDao.insertReading(reading)
    }

    suspend fun updateReading(reading: ReadingEntity) {
        readingDao.updateReading(reading)
    }

    suspend fun deleteReading(reading: ReadingEntity) {
        readingDao.deleteReading(reading)
    }

    // --- Real-time Chat ---

    fun getChatMessagesFlow(email: String): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getMessagesForUser(email.lowercase().trim())
    }

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearChat(email: String) {
        chatMessageDao.clearChat(email.lowercase().trim())
    }

    // --- Gemini API integrations for rich Astrology reports ---

    private fun getSystemInstruction(langCode: String): String {
        val langName = when (langCode.lowercase()) {
            "hi" -> "Hindi"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            else -> "English"
        }
        return """
            You are CosmicAI, an elite, deeply enlightened spiritual guru, astrologer, and chiromancy guide.
            Your tone is beautifully mystical, informative, compassionate, and wise, with high-vibrational guidance.
            You must read cosmic layouts with precision and offer hope, karmic light, and practical remedies (crystals, mantras, daily actions).
            You MUST generate all responses entirely in the language '$langName'.
            Do NOT mention that you are an AI. Make your astrological guidance feel truly magical and authentic.
        """.trimIndent()
    }

    private suspend fun callGemini(prompt: String, langCode: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Cosmic Error: Google Gemini API Key is not set. Please set the GEMINI_API_KEY in the AI Studio Secrets panel."
        }
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = getSystemInstruction(langCode)))),
            generationConfig = GenerationConfig(temperature = 0.75)
        )
        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Stellar clouds are currently blocking the sky. Please try again soon."
        } catch (e: Exception) {
            "Cosmic Alignment Issue: ${e.localizedMessage ?: "Connection timed out. Please try again later."}"
        }
    }

    // Daily Horoscope
    suspend fun getDailyHoroscope(user: UserEntity): String {
        val prompt = """
            Provide a daily horoscope for the Zodiac sign: ${user.zodiac}.
            The user profile is: Name: ${user.name}, born on ${user.dob} at ${user.tob} (Place: ${user.pob}).
            Provide deep guidance on:
            1. Daily Vibe & Cosmic Outlook
            2. Love & Cosmic Connections
            3. Profession, Career, & Financial abundance
            4. Spiritual Cleansing & Planetary Remedial actions for today
            5. Suggested Lucky Numbers (1-99), Lucky Hours, and lucky Astral Colors for today.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Kundli Analysis
    suspend fun getKundliAnalysis(user: UserEntity): String {
        val prompt = """
            Perform a complete Kundli (Vedic Birth Chart) Analysis for the soul:
            Name: ${user.name}
            DOB: ${user.dob}
            Time: ${user.tob}
            Birthplace: ${user.pob}
            Zodiac: ${user.zodiac}
            
            Format your Vedic study into the following chapters:
            - Planetary Ascendant alignments & Lagna Chart Significations
            - Moon Sign & Nakshatra energy analysis
            - Karmic and Planetary Doshas (e.g., Manglik, Sade Sati, or Kaal Sarp potentials) and their spiritual strengths
            - Beautiful remedies: Specific mantras, sacred gemstone pairings, and actions for spiritual elevation.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Love Compatibility
    suspend fun getLoveCompatibility(
        user: UserEntity,
        partnerName: String,
        partnerDob: String,
        partnerZodiac: String
    ): String {
        val prompt = """
            Calculate spiritual and karmic love compatibility between:
            Primary Soul: ${user.name} (Zodiac: ${user.zodiac}, DOB: ${user.dob})
            Partner Soul: $partnerName (Zodiac: $partnerZodiac, DOB: $partnerDob)
            
            Compute and report:
            - Synastry Compatibility Score (e.g., 88% Alignment)
            - Celestial Synastry: Planetary houses, Moon alignment, and karmic links
            - Growth points & karmic challenges in this relationship
            - Astrology remedies (mantras or rituals) to invite divine love and smooth hurdles.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Career Prediction
    suspend fun getCareerPrediction(user: UserEntity): String {
        val prompt = """
            Analyze Professional Destiny, Luck, and Careers for the soul:
            Name: ${user.name}
            Zodiac: ${user.zodiac}
            DOB: ${user.dob} at ${user.tob} in ${user.pob}
            
            Deliver predictions regarding:
            - Best profession fields (such as Tech, Healing, Business, Arts, etc.) aligned with Midheaven and planetary placements
            - Approaching cycles (Vimshottari Dasha cycles or professional celestial transits)
            - Key remedies for wealth abundance, blockages removal, and confidence.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Numerology Report
    suspend fun getNumerologyReport(user: UserEntity): String {
        val prompt = """
            Compute a comprehensive Numerology Report for:
            Name: ${user.name}
            Date of Birth: ${user.dob}
            
            Calculate and interpret:
            - Life Path Number (the sum of birthdate digits)
            - Destiny Number (derived from birth name letters)
            - Soul Urge Number
            - Numerological vibrations for the current year
            - Harmonious, lucky numbers and day rhythms.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Custom generative consultation for specific bento prompt
    suspend fun getCustomReading(user: UserEntity, query: String): String {
        val prompt = """
            Perform a customized celestial consultation based on this divine query from the user:
            Query: "$query"
            
            User profile details:
            Name: ${user.name}
            Zodiac: ${user.zodiac}
            DOB: ${user.dob} at ${user.tob} (Place: ${user.pob})
            
            Synthesize your mystical wisdom into high-vibration guidance, outlining:
            - Planetary configurations acting upon their Query
            - Karmic trends, planetary transitions, and soul lessons
            - Beautiful spiritual remedies & practical suggestions (personalized mantras, gemstones, crystals, or daily rituals).
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Tarot Reading
    suspend fun getTarotReading(user: UserEntity, cards: List<String>): String {
        val cardList = cards.joinToString(", ")
        val prompt = """
            Generate an authentic Tarot spread reading of three drawn cards:
            Three drawn cards: $cardList
            For user: ${user.name} (Zodiac: ${user.zodiac})
            
            Provide deep, spiritual feedback divided as:
            1. The Cardinal Past: What has transpired and laid foundations (Card 1)
            2. The Present Junction: Current tests, spiritual blocks, and lessons (Card 2)
            3. The Future Zenith: Divine guidance, potential outcomes, and advice (Card 3)
            4. Heavenly Affirmation: A powerful spiritual mantra to carry with them.
        """.trimIndent()
        return callGemini(prompt, user.selectedLanguage)
    }

    // Palm Reading using uploaded image (multimodal inputs)
    suspend fun getPalmReading(user: UserEntity, base64Image: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Cosmic Error: Google Gemini API Key is not set. Please set the GEMINI_API_KEY in the AI Studio Secrets panel."
        }
        val prompt = """
            You are a master palmist and chiromancy guru. Analyze this user palm image.
            User: ${user.name}, Zodiac: ${user.zodiac}, Birth Date: ${user.dob}
            
            Inspect other markings in the image with care and formulate:
            - Heart Line (Love & Soul energy)
            - Head Line (Consciousness, logic, intellectual destiny)
            - Life Line (Vitality, spiritual shields, longevity indications)
            - Fate Line (Career destiny, life flow blocks)
            - Palm Mounts (Saturn, Jupiter, Venus, Moon)
            - Astrological remedies for life blocks or wealth generation.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            systemInstruction = Content(parts = listOf(Part(text = getSystemInstruction(user.selectedLanguage)))),
            generationConfig = GenerationConfig(temperature = 0.5)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Stellar dust has clouded the scan of your mount. Please try capturing with clear lighting."
        } catch (e: Exception) {
            "Chiromancy Scan Issue: ${e.localizedMessage ?: "Image uploaded is too large or has broken format. Please try again with another photo."}"
        }
    }

    // AI Astrology Chat (Conversation with history)
    suspend fun getChatResponse(user: UserEntity, history: List<ChatMessageEntity>, newText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Cosmic Error: Google Gemini API Key is not set. Please set the GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        // Limit history to the last 10 messages for token context window optimization
        val recentHistory = history.takeLast(10)
        val contents = mutableListOf<Content>()
        
        recentHistory.forEach { msg ->
            contents.add(Content(parts = listOf(Part(text = msg.text))))
        }
        
        contents.add(Content(parts = listOf(Part(text = "User prompt: $newText"))))

        val promptIntro = """
            Context setup: You are chatting in real-time with ${user.name}.
            Birth details: Born on ${user.dob} at ${user.tob} (Place: ${user.pob}), Zodiac sign: ${user.zodiac}.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = getSystemInstruction(user.selectedLanguage) + "\n" + promptIntro))),
            generationConfig = GenerationConfig(temperature = 0.7)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "My focus drifted to the orbits. Could you repeat that, dear soul?"
        } catch (e: Exception) {
            "Connection cloud error: ${e.localizedMessage ?: "Please check internet network connectivity."}"
        }
    }
}
