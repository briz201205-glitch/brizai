package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrizAccentGrey
import com.example.ui.theme.BrizBgLight
import com.example.ui.theme.BrizPillBorder
import com.example.ui.theme.BrizPillLight
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizSparkGrey
import com.example.ui.theme.BrizTextLight
import com.example.ui.theme.BrizTextPrimary
import com.example.ui.theme.BrizTextSecondary
import com.example.ui.theme.CodeBg
import com.example.ui.theme.CodeBorder
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeHeaderBg
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeString
import com.example.ui.theme.CodeText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
  data class Paragraph(val text: String) : MarkdownBlock()
  data class Heading(val level: Int, val text: String) : MarkdownBlock()
  data class BulletItem(val text: String) : MarkdownBlock()
  data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
  data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
}

@Composable
fun BrizMarkdownView(
  content: String,
  modifier: Modifier = Modifier,
  isUser: Boolean = false,
  onCodeAction: ((action: String, code: String) -> Unit)? = null
) {
  val blocks = remember(content) { parseMarkdownBlocks(content) }

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    blocks.forEachIndexed { index, block ->
      when (block) {
        is MarkdownBlock.Heading -> {
          val fontSize = when (block.level) {
            1 -> 20.sp
            2 -> 17.sp
            else -> 15.sp
          }
          val textColor = if (isUser) BrizTextPrimary else BrizTextPrimary
          Text(
            text = renderInlineMarkdown(block.text, isUser),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(top = if (index > 0) 6.dp else 0.dp)
          )
        }

        is MarkdownBlock.Paragraph -> {
          val textColor = if (isUser) BrizTextPrimary else BrizTextPrimary
          Text(
            text = renderInlineMarkdown(block.text, isUser),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            lineHeight = 24.sp
          )
        }

        is MarkdownBlock.BulletItem -> {
          val textColor = if (isUser) BrizTextPrimary else BrizTextPrimary
          Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "• ",
              fontWeight = FontWeight.Bold,
              color = BrizSparkGrey,
              fontSize = 16.sp
            )
            Text(
              text = renderInlineMarkdown(block.text, isUser),
              style = MaterialTheme.typography.bodyLarge,
              color = textColor,
              lineHeight = 24.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }

        is MarkdownBlock.NumberedItem -> {
          val textColor = if (isUser) BrizTextPrimary else BrizTextPrimary
          Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "${block.number}. ",
              fontWeight = FontWeight.Medium,
              color = BrizSparkGrey,
              fontSize = 14.sp
            )
            Text(
              text = renderInlineMarkdown(block.text, isUser),
              style = MaterialTheme.typography.bodyLarge,
              color = textColor,
              lineHeight = 24.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }

        is MarkdownBlock.CodeBlock -> {
          BrizCodeBlockCard(
            language = block.language,
            code = block.code,
            onCodeAction = onCodeAction
          )
        }
      }
    }
  }
}

@Composable
fun BrizCodeBlockCard(
  language: String,
  code: String,
  onCodeAction: ((action: String, code: String) -> Unit)? = null
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var copied by remember { mutableStateOf(false) }

  val displayLanguage = if (language.isNotBlank()) language.lowercase() else "code"

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CodeBg)
      .border(1.dp, CodeBorder, RoundedCornerShape(12.dp))
  ) {
    // Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(CodeHeaderBg)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = displayLanguage,
        style = MaterialTheme.typography.labelSmall,
        color = BrizTextLight,
        fontWeight = FontWeight.Medium
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Briz Code", code)
            clipboard.setPrimaryClip(clip)
            copied = true
            Toast.makeText(context, "Kode disalin!", Toast.LENGTH_SHORT).show()
            scope.launch {
              delay(2000)
              copied = false
            }
          },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = "Salin Kode",
            tint = if (copied) Color.White else BrizTextLight,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }

    // Code Content
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(14.dp)
    ) {
      Text(
        text = highlightSyntax(code, language),
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = CodeText
      )
    }
  }
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
  val blocks = mutableListOf<MarkdownBlock>()
  val lines = text.lines()
  var i = 0

  while (i < lines.size) {
    val line = lines[i]

    // Code Block ```lang
    if (line.trimStart().startsWith("```")) {
      val language = line.trimStart().removePrefix("```").trim()
      val codeBuilder = StringBuilder()
      i++
      while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
        codeBuilder.appendLine(lines[i])
        i++
      }
      if (i < lines.size) i++ // skip closing ```
      blocks.add(MarkdownBlock.CodeBlock(language, codeBuilder.toString().trimEnd()))
      continue
    }

    // Headings # ## ###
    if (line.startsWith("#")) {
      val level = line.takeWhile { it == '#' }.length
      val headingText = line.drop(level).trim()
      blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 3), headingText))
      i++
      continue
    }

    // Bullet Items * or -
    if (line.trimStart().startsWith("* ") || line.trimStart().startsWith("- ")) {
      val bulletText = line.trimStart().drop(2).trim()
      blocks.add(MarkdownBlock.BulletItem(bulletText))
      i++
      continue
    }

    // Numbered Items (1. 2. etc)
    val numberedMatch = Regex("""^\s*(\d+)\.\s+(.*)""").find(line)
    if (numberedMatch != null) {
      val num = numberedMatch.groupValues[1]
      val numText = numberedMatch.groupValues[2]
      blocks.add(MarkdownBlock.NumberedItem(num, numText))
      i++
      continue
    }

    // Regular Paragraph
    if (line.isNotBlank()) {
      val paragraphBuilder = StringBuilder(line)
      i++
      while (i < lines.size &&
        lines[i].isNotBlank() &&
        !lines[i].trimStart().startsWith("```") &&
        !lines[i].startsWith("#") &&
        !lines[i].trimStart().startsWith("* ") &&
        !lines[i].trimStart().startsWith("- ") &&
        !Regex("""^\s*(\d+)\.\s+""").containsMatchIn(lines[i])
      ) {
        paragraphBuilder.append(" ").append(lines[i].trim())
        i++
      }
      blocks.add(MarkdownBlock.Paragraph(paragraphBuilder.toString()))
    } else {
      i++
    }
  }

  return blocks
}

@Composable
private fun renderInlineMarkdown(text: String, isUser: Boolean): AnnotatedString {
  return buildAnnotatedString {
    var cursor = 0
    val inlineCodeColor = if (isUser) BrizBgLight else BrizPillLight
    val inlineCodeTextColor = if (isUser) BrizTextPrimary else BrizTextPrimary
    val baseTextColor = if (isUser) BrizTextPrimary else BrizTextPrimary

    val pattern = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
    val matches = pattern.findAll(text)

    for (match in matches) {
      if (match.range.first > cursor) {
        withStyle(SpanStyle(color = baseTextColor)) {
          append(text.substring(cursor, match.range.first))
        }
      }

      val matchedStr = match.value
      when {
        matchedStr.startsWith("**") && matchedStr.endsWith("**") && matchedStr.length >= 4 -> {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseTextColor)) {
            append(matchedStr.substring(2, matchedStr.length - 2))
          }
        }
        matchedStr.startsWith("*") && matchedStr.endsWith("*") && matchedStr.length >= 2 -> {
          withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseTextColor)) {
            append(matchedStr.substring(1, matchedStr.length - 1))
          }
        }
        matchedStr.startsWith("`") && matchedStr.endsWith("`") && matchedStr.length >= 2 -> {
          withStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              background = inlineCodeColor,
              color = inlineCodeTextColor,
              fontSize = 13.sp
            )
          ) {
            append(" ${matchedStr.substring(1, matchedStr.length - 1)} ")
          }
        }
        else -> {
          // Fallback if it doesn't match the expected lengths
          withStyle(SpanStyle(color = baseTextColor)) {
            append(matchedStr)
          }
        }
      }
      cursor = match.range.last + 1
    }

    if (cursor < text.length) {
      withStyle(SpanStyle(color = baseTextColor)) {
        append(text.substring(cursor))
      }
    }
  }
}

private fun highlightSyntax(code: String, language: String): AnnotatedString {
  return buildAnnotatedString {
    val keywords = setOf(
      "fun", "val", "var", "class", "interface", "object", "return", "if", "else",
      "when", "for", "while", "import", "package", "override", "public", "private",
      "protected", "sealed", "data", "enum", "suspend", "coroutine", "flow",
      "const", "let", "def", "async", "await", "SELECT", "FROM", "WHERE", "JOIN",
      "struct", "impl", "fn", "type", "void", "int", "boolean", "String"
    )

    val lines = code.lines()
    lines.forEachIndexed { lineIdx, line ->
      var i = 0
      while (i < line.length) {
        // Comments
        if (line.substring(i).startsWith("//") || line.substring(i).startsWith("#")) {
          withStyle(SpanStyle(color = CodeComment, fontStyle = FontStyle.Italic)) {
            append(line.substring(i))
          }
          break
        }

        // Strings
        if (line[i] == '"' || line[i] == '\'') {
          val quoteChar = line[i]
          val endQuote = line.indexOf(quoteChar, i + 1)
          if (endQuote != -1) {
            withStyle(SpanStyle(color = CodeString)) {
              append(line.substring(i, endQuote + 1))
            }
            i = endQuote + 1
            continue
          }
        }

        // Check word
        if (line[i].isLetter()) {
          val wordStart = i
          while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
            i++
          }
          val word = line.substring(wordStart, i)
          if (keywords.contains(word)) {
            withStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold)) {
              append(word)
            }
          } else {
            withStyle(SpanStyle(color = CodeText)) {
              append(word)
            }
          }
          continue
        }

        append(line[i])
        i++
      }

      if (lineIdx < lines.size - 1) {
        append("\n")
      }
    }
  }
}
