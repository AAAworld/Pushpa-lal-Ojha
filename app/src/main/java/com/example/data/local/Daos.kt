package com.example.data.local

import androidx.room.*
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ReadingEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE userEmail = :userEmail ORDER BY timestamp ASC")
    fun getMessagesForUser(userEmail: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userEmail = :userEmail")
    suspend fun clearChat(userEmail: String)
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getReadingsForUser(userEmail: String): Flow<List<ReadingEntity>>

    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun getAllReadingsFlow(): Flow<List<ReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ReadingEntity): Long

    @Update
    suspend fun updateReading(reading: ReadingEntity)

    @Delete
    suspend fun deleteReading(reading: ReadingEntity)
}
