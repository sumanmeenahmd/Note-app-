package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ChecklistItem
import com.example.model.NoteCategory
import com.example.model.NoteColor

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val checklistItems: List<ChecklistItem> = emptyList(),
    val tags: List<String> = emptyList(),
    val category: NoteCategory = NoteCategory.PERSONAL,
    val color: NoteColor = NoteColor.DEFAULT,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val isChecklistMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isEffectivelyEmpty: Boolean
        get() = title.isBlank() && content.isBlank() && checklistItems.all { it.text.isBlank() }
}
