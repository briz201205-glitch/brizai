package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AttachedFile
import com.example.data.model.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiService {

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  companion object {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val AVAILABLE_MODELS = listOf(
      "gemini-3.5-flash" to ("Flash" to "Cepat, cerdas & seimbang untuk semua tugas harian & multimodal"),
      "gemini-3.1-pro-preview" to ("Pro" to "Penalaran mendalam, coding arsitektur & analisis dokumen kompleks"),
      "gemini-3.1-flash-lite-preview" to ("Lite" to "Ringan, super responsif & efisien"),
      "gemini-2.5-flash-image" to ("Visual" to "Analisis visual, gambar & multimodal tingkat lanjut")
    )

    fun getModelShortName(modelId: String): String {
      return when {
        modelId.contains("pro", ignoreCase = true) -> "Pro"
        modelId.contains("lite", ignoreCase = true) -> "Lite"
        modelId.contains("image", ignoreCase = true) -> "Visual"
        else -> "Flash"
      }
    }

    private val FALLBACK_MODELS = listOf(
      "gemini-3.1-flash-lite-preview",
      "gemini-3.5-flash",
      "gemini-3.1-pro-preview"
    )
  }

  fun getEffectiveApiKey(customKey: String?): String {
    return if (!customKey.isNullOrBlank()) {
      customKey.trim()
    } else {
      BuildConfig.GEMINI_API_KEY
    }
  }

  /**
   * Streams generation response in real-time using SSE (Server-Sent Events)
   * Supports universal multimodal inputs (Images, Audio, PDF, Code, Documents)
   */
  fun streamGenerateContent(
    history: List<ChatMessageEntity>,
    newPrompt: String,
    attachedFile: AttachedFile? = null,
    systemPrompt: String,
    modelName: String = "gemini-3.5-flash",
    temperature: Float = 0.7f,
    topP: Float = 0.95f,
    customApiKey: String? = null
  ): Flow<String> = flow {
    val apiKey = getEffectiveApiKey(customApiKey)

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      emit("Gak bisa, mentok! API Key Gemini belum terpasang di Secrets (.env) atau Settings. Masukkan API Key kamu untuk mulai menggunakan Briz.")
      return@flow
    }

    val requestJson = buildRequestBody(
      history = history,
      newPrompt = newPrompt,
      attachedFile = attachedFile,
      systemPrompt = systemPrompt,
      temperature = temperature,
      topP = topP
    )

    val requestedModel = modelName.ifBlank { "gemini-3.5-flash" }
    val candidateModels = buildList {
      add(requestedModel)
      for (fb in FALLBACK_MODELS) {
        if (fb != requestedModel) add(fb)
      }
    }

    var lastErrorMessage = ""
    var streamSucceeded = false

    for (targetModel in candidateModels) {
      val isFallback = targetModel != requestedModel
      val url = "$BASE_URL$targetModel:streamGenerateContent?key=$apiKey&alt=sse"

      val mediaType = "application/json; charset=utf-8".toMediaType()
      val body = requestJson.toString().toRequestBody(mediaType)

      val request = Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Content-Type", "application/json")
        .build()

      // Retry up to 2 times per model
      for (attempt in 1..2) {
        try {
          val response = okHttpClient.newCall(request).execute()
          if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            Log.w(TAG, "Model $targetModel attempt $attempt returned ${response.code}: $errorBody")
            lastErrorMessage = parseErrorMessage(response.code, errorBody)

            if (response.code == 503 || response.code == 429 || response.code == 500) {
              kotlinx.coroutines.delay(1000L * attempt)
              continue
            } else {
              emit(lastErrorMessage)
              return@flow
            }
          }

          val inputStream = response.body?.byteStream()
          if (inputStream == null) {
            continue
          }

          val reader = BufferedReader(InputStreamReader(inputStream))
          var line: String?
          var hasEmittedAnyChunk = false

          while (reader.readLine().also { line = it } != null) {
            val currentLine = line?.trim() ?: continue
            if (currentLine.startsWith("data:")) {
              val jsonStr = currentLine.removePrefix("data:").trim()
              if (jsonStr.isNotEmpty() && jsonStr != "[DONE]") {
                val chunkText = extractTextFromChunk(jsonStr)
                if (!chunkText.isNullOrEmpty()) {
                  hasEmittedAnyChunk = true
                  emit(chunkText)
                }
              }
            }
          }

          if (hasEmittedAnyChunk) {
            streamSucceeded = true
            return@flow
          }

        } catch (e: Exception) {
          Log.e(TAG, "Network attempt $attempt for $targetModel failed", e)
          lastErrorMessage = "Kendala koneksi: ${e.localizedMessage ?: "Koneksi timeout"}."
          kotlinx.coroutines.delay(800L * attempt)
        }
      }
    }

    if (!streamSucceeded) {
      if (lastErrorMessage.isNotBlank()) {
        emit(lastErrorMessage)
      } else {
        emit("Layanan server sedang mengalami lonjakan antrean. Silakan coba beberapa detik lagi.")
      }
    }
  }.flowOn(Dispatchers.IO)

  /**
   * One-shot generation for quick title generation
   */
  suspend fun generateSingle(
    prompt: String,
    systemPrompt: String = "Generate a short 3-5 words title in Indonesian summarizing the user prompt.",
    modelName: String = "gemini-3.5-flash",
    customApiKey: String? = null
  ): String = withContext(Dispatchers.IO) {
    val apiKey = getEffectiveApiKey(customApiKey)
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext "Percakapan Baru"
    }

    val requestedModel = modelName.ifBlank { "gemini-3.5-flash" }
    val candidateModels = listOf(requestedModel, "gemini-3.1-flash-lite-preview", "gemini-3.5-flash")

    for (targetModel in candidateModels) {
      try {
        val requestJson = JSONObject().apply {
          put("contents", JSONArray().apply {
            put(JSONObject().apply {
              put("role", "user")
              put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
              })
            })
          })
          put("systemInstruction", JSONObject().apply {
            put("parts", JSONArray().apply {
              put(JSONObject().apply { put("text", systemPrompt) })
            })
          })
          put("generationConfig", JSONObject().apply {
            put("temperature", 0.3)
            put("maxOutputTokens", 40)
          })
        }

        val url = "$BASE_URL$targetModel:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
          .url(url)
          .post(body)
          .build()

        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
          val respBody = response.body?.string() ?: continue
          val jsonObj = JSONObject(respBody)
          val candidates = jsonObj.optJSONArray("candidates")
          val content = candidates?.optJSONObject(0)?.optJSONObject("content")
          val parts = content?.optJSONArray("parts")
          val text = parts?.optJSONObject(0)?.optString("text")?.trim()
          val result = text?.replace("\"", "")?.replace("Title:", "")?.trim()
          if (!result.isNullOrBlank()) {
            return@withContext result
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "generateSingle on $targetModel failed", e)
      }
    }
    return@withContext "Percakapan Baru"
  }

  private fun buildRequestBody(
    history: List<ChatMessageEntity>,
    newPrompt: String,
    attachedFile: AttachedFile?,
    systemPrompt: String,
    temperature: Float,
    topP: Float
  ): JSONObject {
    val root = JSONObject()
    val contentsArray = JSONArray()

    // Add recent history for context
    val recentHistory = history.takeLast(16)
    for (msg in recentHistory) {
      if (msg.isError) continue
      val role = if (msg.role == "user") "user" else "model"
      val contentObj = JSONObject().apply {
        put("role", role)
        val partsArray = JSONArray()
        if (!msg.imageBase64.isNullOrEmpty()) {
          val mime = msg.fileMimeType ?: "image/jpeg"
          partsArray.put(JSONObject().apply {
            put("inlineData", JSONObject().apply {
              put("mimeType", mime)
              put("data", msg.imageBase64)
            })
          })
        }
        partsArray.put(JSONObject().apply {
          put("text", msg.content)
        })
        put("parts", partsArray)
      }
      contentsArray.put(contentObj)
    }

    // Add current user prompt + attached file
    val currentPromptObj = JSONObject().apply {
      put("role", "user")
      val partsArray = JSONArray()

      var promptText = newPrompt

      if (attachedFile != null) {
        if (!attachedFile.base64Data.isNullOrEmpty()) {
          // Binary multimodal (Image, Audio, PDF)
          partsArray.put(JSONObject().apply {
            put("inlineData", JSONObject().apply {
              put("mimeType", attachedFile.mimeType)
              put("data", attachedFile.base64Data)
            })
          })
        } else if (!attachedFile.textContent.isNullOrEmpty()) {
          // Text / Code file content
          val fileAttachmentBlock = "\n\n[File Terlampir: ${attachedFile.name}]\n```\n${attachedFile.textContent}\n```\n"
          promptText = if (promptText.isNotBlank()) "$promptText\n$fileAttachmentBlock" else "Berikut isi file '${attachedFile.name}':\n$fileAttachmentBlock"
        }
      }

      partsArray.put(JSONObject().apply {
        put("text", promptText.ifBlank { "Tolong analisis file ini." })
      })
      put("parts", partsArray)
    }
    contentsArray.put(currentPromptObj)
    root.put("contents", contentsArray)

    // System instruction
    root.put("systemInstruction", JSONObject().apply {
      put("parts", JSONArray().apply {
        put(JSONObject().apply { put("text", systemPrompt) })
      })
    })

    // Generation config
    root.put("generationConfig", JSONObject().apply {
      put("temperature", temperature)
      put("topP", topP)
    })

    return root
  }

  private fun extractTextFromChunk(jsonStr: String): String? {
    return try {
      val jsonObj = JSONObject(jsonStr)
      val candidates = jsonObj.optJSONArray("candidates") ?: return null
      val firstCandidate = candidates.optJSONObject(0) ?: return null
      val content = firstCandidate.optJSONObject("content") ?: return null
      val parts = content.optJSONArray("parts") ?: return null
      val firstPart = parts.optJSONObject(0) ?: return null
      if (firstPart.has("text")) firstPart.optString("text") else null
    } catch (e: Exception) {
      null
    }
  }

  private fun parseErrorMessage(code: Int, errorBody: String): String {
    return try {
      val jsonObj = JSONObject(errorBody)
      val errorObj = jsonObj.optJSONObject("error")
      val message = errorObj?.optString("message") ?: errorBody
      when (code) {
        400 -> "Permintaan tidak dapat diproses (400 Bad Request): $message"
        403 -> "Akses ditolak (403 Forbidden). Pastikan API key kamu valid dan kuota masih tersedia."
        404 -> "Model tidak ditemukan (404 Not Found): $message"
        429 -> "Sedang mencapai batas pemakaian (429 Rate Limit). Mohon tunggu beberapa detik dan coba lagi ya."
        500, 503 -> "Layanan server sedang mengalami lonjakan antrean ($code). Sistem otomatis mencoba kembali..."
        else -> "Terjadi kendala ($code): $message"
      }
    } catch (e: Exception) {
      "Terjadi kendala ($code): $errorBody"
    }
  }
}
