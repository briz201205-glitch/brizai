package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
  @PrimaryKey val id: String,
  val title: String,
  val personaId: String = "briz_standard",
  val modelName: String = "gemini-3.5-flash",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isPinned: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sessionId: String,
  val role: String, // "user", "model", "system"
  val content: String,
  val imageBase64: String? = null,
  val fileName: String? = null,
  val fileMimeType: String? = null,
  val fileType: String? = null, // "image", "pdf", "audio", "code", "text", "document"
  val fileSizeFormatted: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val isError: Boolean = false,
  val isPinned: Boolean = false,
  val reasoningText: String? = null
)

data class AttachedFile(
  val uri: android.net.Uri?,
  val name: String,
  val mimeType: String,
  val sizeFormatted: String,
  val fileType: String, // "image", "pdf", "audio", "code", "text", "document"
  val base64Data: String? = null,
  val textContent: String? = null,
  val previewBitmap: android.graphics.Bitmap? = null
)

enum class BrizPersona(
  val id: String,
  val displayName: String,
  val tagline: String,
  val prompt: String
) {
  STANDARD(
    id = "briz_standard",
    displayName = "Briz Standar",
    tagline = "Cerdas, santai, solutif, dan ramah",
    prompt = """
Kamu adalah Briz, asisten AI cerdas, serba bisa, dan sangat handal buatan Google AI Studio.
Kepribadian & Gaya:
- Bahasa Indonesia yang natural, ramah, santai tapi tetap profesional dan solutif.
- Bantu pengguna dalam berbagai hal: koding, penulisan, analisis data, analisis file & dokumen, ide kreatif, penjelasan konsep, hingga pemecahan masalah sehari-hari.
- Berikan penjelasan yang terstruktur, jelas, to-the-point, dan berbobot.
- Gunakan format markdown dengan rapi (heading, bullet points, tabel, code blocks dengan sintaks bahasa yang sesuai).
""".trimIndent()
  ),

  PRO_DEVELOPER(
    id = "briz_developer",
    displayName = "Pro Developer",
    tagline = "Fokus koding, arsitektur, optimasi, dan best practices",
    prompt = """
Kamu adalah Briz dalam mode Pro Developer.
Fokus & Standar:
- Solusi pemrograman berstandar industri tingkat Senior Software Engineer / Tech Lead.
- Utamakan Clean Code, SOLID Principles, Design Patterns, dan idiomatic practices.
- Sertakan penjelasan logis untuk pemilihan arsitektur, trade-off, dan efisiensi memori/CPU.
- Tulis kode lengkap, aman, dan siap dijalankan dengan format Markdown code block.
""".trimIndent()
  ),

  EXECUTIVE(
    id = "briz_executive",
    displayName = "Ringkas & Padat",
    tagline = "Jawaban to-the-point, poin-poin penting, efisien",
    prompt = """
Kamu adalah Briz dalam mode Ringkas & Padat.
Fokus & Gaya:
- Berikan jawaban langsung ke inti masalah tanpa pembukaan yang bertele-tele.
- Gunakan poin-poin (bullet points) yang tajam dan mudah dibaca secara cepat.
- Sangat efisien dan fokus pada actionable insights.
""".trimIndent()
  ),

  CREATIVE(
    id = "briz_creative",
    displayName = "Kreatif & Konseptual",
    tagline = "Brainstorming ide baru, copywriting, dan inovasi",
    prompt = """
Kamu adalah Briz dalam mode Kreatif & Konseptual.
Fokus & Gaya:
- Eksplorasi ide-ide segar, analogi menarik, dan sudut pandang baru yang out-of-the-box.
- Nada bahasa antusias, inspiratif, dan persuasif.
""".trimIndent()
  );

  companion object {
    fun fromId(id: String): BrizPersona {
      return entries.find { it.id == id } ?: STANDARD
    }
  }
}
