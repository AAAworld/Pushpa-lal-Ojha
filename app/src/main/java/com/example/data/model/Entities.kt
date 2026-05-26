package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String, // SHA-256 secure hash
    val name: String,
    val dob: String, // YYYY-MM-DD
    val tob: String, // HH:MM
    val pob: String, // Birthplace
    val zodiac: String,
    val role: String = "USER", // USER or ADMIN
    val isPremium: Boolean = false,
    val selectedLanguage: String = "en"
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val type: String, // DAILY_HOROSCOPE, TAROT, KUNDLI, LOVE_COMPATIBILITY, CAREER, PALM, NUMEROLOGY
    val title: String,
    val mainContent: String,
    val keyMetrics: String, // Stored as a JSON or simple list string (e.g. lucky number/color/affinity)
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
