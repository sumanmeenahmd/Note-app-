package com.example.ui.util

import com.example.data.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object NoteFormatters {
    fun formatRelativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (now - timestamp).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun formatFullTimestamp(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatInsertableTimestamp(timestamp: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }

    fun wordCount(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(Regex("\\s+")).size
    }

    fun buildShareText(note: NoteEntity): String {
        val builder = StringBuilder()
        if (note.title.isNotBlank()) {
            builder.appendLine(note.title.trim())
            builder.appendLine()
        }
        if (note.content.isNotBlank()) {
            builder.appendLine(note.content.trim())
        }
        if (note.checklistItems.isNotEmpty()) {
            if (note.content.isNotBlank()) builder.appendLine()
            note.checklistItems.filter { it.text.isNotBlank() }.forEach { item ->
                val mark = if (item.isChecked) "[x]" else "[ ]"
                builder.appendLine("$mark ${item.text.trim()}")
            }
        }
        if (note.tags.isNotEmpty()) {
            builder.appendLine()
            builder.append(note.tags.joinToString(" ") { "#$it" })
        }
        return builder.toString().trim()
    }
}
