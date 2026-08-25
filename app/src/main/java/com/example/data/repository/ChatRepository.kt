package com.example.data.repository

import com.example.data.api.GeminiService
import com.example.data.local.ChatDao
import com.example.data.model.AttachedFile
import com.example.data.model.BrizPersona
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
  private val chatDao: ChatDao,
  private val geminiService: GeminiService
) {

  val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

  fun getSession(sessionId: String): Flow<ChatSessionEntity?> {
    return chatDao.getSessionById(sessionId)
  }

  fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
    return chatDao.getMessagesForSession(sessionId)
  }

  suspend fun createNewSession(
    title: String = "Chat Baru",
    personaId: String = BrizPersona.STANDARD.id,
    modelName: String = "gemini-3.5-flash"
  ): String {
    val sessionId = UUID.randomUUID().toString()
    val session = ChatSessionEntity(
      id = sessionId,
      title = title,
      personaId = personaId,
      modelName = modelName,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
    chatDao.insertSession(session)
    return sessionId
  }

  suspend fun updateSessionTitle(sessionId: String, newTitle: String) {
    chatDao.updateSessionTitle(sessionId, newTitle)
  }

  suspend fun updateSessionSettings(sessionId: String, personaId: String, modelName: String) {
    val existing = chatDao.getSessionDirect(sessionId)
    if (existing != null) {
      chatDao.updateSession(
        existing.copy(
          personaId = personaId,
          modelName = modelName,
          updatedAt = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun togglePinSession(sessionId: String) {
    chatDao.toggleSessionPin(sessionId)
  }

  suspend fun togglePinMessage(messageId: Long) {
    chatDao.toggleMessagePin(messageId)
  }

  suspend fun deleteSession(sessionId: String) {
    chatDao.deleteMessagesForSession(sessionId)
    chatDao.deleteSession(sessionId)
  }

  suspend fun clearAllHistory() {
    chatDao.clearAllMessages()
    chatDao.clearAllSessions()
  }

  suspend fun deleteMessage(messageId: Long) {
    chatDao.deleteMessageById(messageId)
  }

  /**
   * Sends user message, generates streaming response, and updates DB in real-time
   */
  fun sendMessageStreaming(
    sessionId: String,
    prompt: String,
    attachedFile: AttachedFile?,
    persona: BrizPersona,
    modelName: String,
    temperature: Float,
    topP: Float,
    customApiKey: String?,
    scope: CoroutineScope,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ): Job {
    return scope.launch(Dispatchers.IO) {
      val now = System.currentTimeMillis()
      // 1. Insert user message with attached file metadata
      val userMsg = ChatMessageEntity(
        sessionId = sessionId,
        role = "user",
        content = prompt,
        imageBase64 = attachedFile?.base64Data,
        fileName = attachedFile?.name,
        fileMimeType = attachedFile?.mimeType,
        fileType = attachedFile?.fileType,
        fileSizeFormatted = attachedFile?.sizeFormatted,
        timestamp = now
      )
      chatDao.insertMessage(userMsg)
      chatDao.updateSessionTimestamp(sessionId, now)

      // Fetch history for multi-turn context
      val history = chatDao.getMessagesForSessionDirect(sessionId)

      // Auto-generate title if still default
      val session = chatDao.getSessionDirect(sessionId)
      if (session != null && (session.title == "Chat Baru" || session.title.isBlank())) {
        scope.launch(Dispatchers.IO) {
          val autoTitle = geminiService.generateSingle(
            prompt = if (prompt.isNotBlank()) prompt else (attachedFile?.name ?: "Analisis File"),
            modelName = modelName,
            customApiKey = customApiKey
          )
          if (autoTitle.isNotBlank() && autoTitle != "Percakapan Baru") {
            chatDao.updateSessionTitle(sessionId, autoTitle)
          }
        }
      }

      val fullResponse = StringBuilder()

      try {
        geminiService.streamGenerateContent(
          history = history,
          newPrompt = prompt,
          attachedFile = attachedFile,
          systemPrompt = persona.prompt,
          modelName = modelName,
          temperature = temperature,
          topP = topP,
          customApiKey = customApiKey
        ).collect { chunk ->
          fullResponse.append(chunk)
          onChunk(fullResponse.toString())
        }

        val finalAiText = fullResponse.toString().ifBlank {
          "Tidak ada respon yang dapat dihasilkan. Silakan tanyakan hal lain."
        }

        // Save AI response to DB
        val aiMsg = ChatMessageEntity(
          sessionId = sessionId,
          role = "model",
          content = finalAiText,
          timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(aiMsg)
        chatDao.updateSessionTimestamp(sessionId, System.currentTimeMillis())

        onComplete()
      } catch (e: Exception) {
        val errorText = "Terjadi kendala: ${e.localizedMessage ?: "Koneksi bermasalah"}"
        val errorMsg = ChatMessageEntity(
          sessionId = sessionId,
          role = "model",
          content = errorText,
          timestamp = System.currentTimeMillis(),
          isError = true
        )
        chatDao.insertMessage(errorMsg)
        onError(errorText)
      }
    }
  }

  /**
   * Regenerate AI response for the latest or selected prompt
   */
  fun regenerateResponse(
    sessionId: String,
    persona: BrizPersona,
    modelName: String,
    temperature: Float,
    topP: Float,
    customApiKey: String?,
    scope: CoroutineScope,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ): Job {
    return scope.launch(Dispatchers.IO) {
      val messages = chatDao.getMessagesForSessionDirect(sessionId)
      val lastUserMsg = messages.lastOrNull { it.role == "user" }
      if (lastUserMsg == null) {
        onError("Tidak ada pesan pengguna sebelumnya untuk di-generate ulang.")
        return@launch
      }

      // If the very last message is from AI/error, remove it before generating new one
      val lastMsg = messages.lastOrNull()
      if (lastMsg != null && lastMsg.role == "model") {
        chatDao.deleteMessageById(lastMsg.id)
      }

      val history = chatDao.getMessagesForSessionDirect(sessionId)
      val fullResponse = StringBuilder()

      try {
        geminiService.streamGenerateContent(
          history = history,
          newPrompt = lastUserMsg.content,
          attachedFile = if (!lastUserMsg.imageBase64.isNullOrEmpty()) {
            AttachedFile(
              uri = android.net.Uri.EMPTY,
              name = lastUserMsg.fileName ?: "file",
              mimeType = lastUserMsg.fileMimeType ?: "image/jpeg",
              sizeFormatted = lastUserMsg.fileSizeFormatted ?: "",
              fileType = lastUserMsg.fileType ?: "image",
              base64Data = lastUserMsg.imageBase64
            )
          } else null,
          systemPrompt = persona.prompt,
          modelName = modelName,
          temperature = temperature + 0.1f, // Slight variation
          topP = topP,
          customApiKey = customApiKey
        ).collect { chunk ->
          fullResponse.append(chunk)
          onChunk(fullResponse.toString())
        }

        val finalAiText = fullResponse.toString().ifBlank {
          "Tidak ada respon yang dihasilkan."
        }

        val aiMsg = ChatMessageEntity(
          sessionId = sessionId,
          role = "model",
          content = finalAiText,
          timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(aiMsg)
        chatDao.updateSessionTimestamp(sessionId, System.currentTimeMillis())

        onComplete()
      } catch (e: Exception) {
        val errorText = "Terjadi kendala saat generate ulang: ${e.localizedMessage ?: "Koneksi bermasalah"}"
        val errorMsg = ChatMessageEntity(
          sessionId = sessionId,
          role = "model",
          content = errorText,
          timestamp = System.currentTimeMillis(),
          isError = true
        )
        chatDao.insertMessage(errorMsg)
        onError(errorText)
      }
    }
  }

  suspend fun exportSessionAsMarkdown(sessionId: String): String {
    val session = chatDao.getSessionDirect(sessionId) ?: return ""
    val messages = chatDao.getMessagesForSessionDirect(sessionId)

    val sb = StringBuilder()
    sb.append("# ${session.title}\n")
    sb.append("*Ekspor Percakapan Briz (${session.modelName})*\n\n---\n\n")

    for (msg in messages) {
      if (msg.role == "user") {
        val fileInfo = if (msg.fileName != null) " *(Lampiran: ${msg.fileName})*" else ""
        sb.append("### 👤 Pengguna$fileInfo:\n${msg.content}\n\n")
      } else {
        sb.append("### ✨ Briz:\n${msg.content}\n\n")
      }
      sb.append("---\n\n")
    }

    return sb.toString()
  }
}
