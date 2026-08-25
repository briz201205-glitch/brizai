package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
  @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, updatedAt DESC")
  fun getAllSessions(): Flow<List<ChatSessionEntity>>

  @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
  fun getSessionById(sessionId: String): Flow<ChatSessionEntity?>

  @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
  suspend fun getSessionDirect(sessionId: String): ChatSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: ChatSessionEntity)

  @Update
  suspend fun updateSession(session: ChatSessionEntity)

  @Query("UPDATE chat_sessions SET updatedAt = :timestamp WHERE id = :sessionId")
  suspend fun updateSessionTimestamp(sessionId: String, timestamp: Long)

  @Query("UPDATE chat_sessions SET title = :title, updatedAt = :timestamp WHERE id = :sessionId")
  suspend fun updateSessionTitle(sessionId: String, title: String, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE chat_sessions SET isPinned = NOT isPinned WHERE id = :sessionId")
  suspend fun toggleSessionPin(sessionId: String)

  @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
  suspend fun deleteSession(sessionId: String)

  @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
  suspend fun deleteMessagesForSession(sessionId: String)

  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  suspend fun getMessagesForSessionDirect(sessionId: String): List<ChatMessageEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity): Long

  @Update
  suspend fun updateMessage(message: ChatMessageEntity)

  @Query("UPDATE chat_messages SET isPinned = NOT isPinned WHERE id = :messageId")
  suspend fun toggleMessagePin(messageId: Long)

  @Query("DELETE FROM chat_messages WHERE id = :messageId")
  suspend fun deleteMessageById(messageId: Long)

  @Query("SELECT * FROM chat_messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
  fun searchMessages(query: String): Flow<List<ChatMessageEntity>>

  @Query("DELETE FROM chat_sessions")
  suspend fun clearAllSessions()

  @Query("DELETE FROM chat_messages")
  suspend fun clearAllMessages()
}
