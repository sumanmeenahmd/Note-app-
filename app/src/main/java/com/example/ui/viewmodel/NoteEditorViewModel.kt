package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NoteEntity
import com.example.data.NotesRepository
import com.example.model.ChecklistItem
import com.example.model.NoteCategory
import com.example.model.NoteColor
import com.example.ui.util.NoteFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val isLoaded: Boolean = false,
    val noteId: Long = 0L,
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
    val updatedAt: Long = System.currentTimeMillis(),
    val hasChanges: Boolean = false
) {
    fun toEntity(): NoteEntity = NoteEntity(
        id = noteId,
        title = title,
        content = content,
        checklistItems = checklistItems.filter { it.text.isNotBlank() || checklistItems.size == 1 },
        tags = tags,
        category = category,
        color = color,
        isPinned = isPinned,
        isFavorite = isFavorite,
        isArchived = isArchived,
        isTrashed = isTrashed,
        isChecklistMode = isChecklistMode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

class NoteEditorViewModel(
    private val noteIdArg: Long,
    private val startAsChecklist: Boolean,
    private val repository: NotesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorUiState())
    val state: StateFlow<NoteEditorUiState> = _state.asStateFlow()

    init {
        loadInitialNote()
    }

    private fun loadInitialNote() {
        viewModelScope.launch {
            if (noteIdArg > 0L) {
                val existing = repository.getNoteById(noteIdArg)
                if (existing != null) {
                    _state.value = NoteEditorUiState(
                        isLoaded = true,
                        noteId = existing.id,
                        title = existing.title,
                        content = existing.content,
                        checklistItems = existing.checklistItems,
                        tags = existing.tags,
                        category = existing.category,
                        color = existing.color,
                        isPinned = existing.isPinned,
                        isFavorite = existing.isFavorite,
                        isArchived = existing.isArchived,
                        isTrashed = existing.isTrashed,
                        isChecklistMode = existing.isChecklistMode,
                        createdAt = existing.createdAt,
                        updatedAt = existing.updatedAt,
                        hasChanges = false
                    )
                    return@launch
                }
            }
            val now = System.currentTimeMillis()
            _state.value = NoteEditorUiState(
                isLoaded = true,
                category = if (startAsChecklist) NoteCategory.TASKS else NoteCategory.PERSONAL,
                isChecklistMode = startAsChecklist,
                checklistItems = if (startAsChecklist) listOf(ChecklistItem(text = "")) else emptyList(),
                createdAt = now,
                updatedAt = now,
                hasChanges = false
            )
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title, hasChanges = true) }
    }

    fun updateContent(content: String) {
        _state.update { it.copy(content = content, hasChanges = true) }
    }

    fun insertBulletAtEnd() {
        _state.update { current ->
            val prefix = if (current.content.isEmpty() || current.content.endsWith("\n")) "" else "\n"
            current.copy(content = "${current.content}${prefix}• ", hasChanges = true)
        }
    }

    fun insertTimestampAtEnd() {
        val stamp = NoteFormatters.formatInsertableTimestamp()
        _state.update { current ->
            val prefix = if (current.content.isEmpty() || current.content.endsWith("\n") || current.content.endsWith(" ")) "" else " "
            current.copy(content = "${current.content}${prefix}[$stamp] ", hasChanges = true)
        }
    }

    fun toggleChecklistMode() {
        _state.update { current ->
            val nextMode = !current.isChecklistMode
            val nextItems = if (nextMode && current.checklistItems.isEmpty()) {
                if (current.content.isNotBlank()) {
                    current.content.lines()
                        .map { it.trim().removePrefix("• ").trim() }
                        .filter { it.isNotEmpty() }
                        .map { ChecklistItem(text = it) }
                        .ifEmpty { listOf(ChecklistItem(text = "")) }
                } else {
                    listOf(ChecklistItem(text = ""))
                }
            } else {
                current.checklistItems
            }
            val nextContent = if (nextMode && current.checklistItems.isEmpty() && current.content.isNotBlank()) {
                ""
            } else {
                current.content
            }
            current.copy(
                isChecklistMode = nextMode,
                checklistItems = nextItems,
                content = nextContent,
                hasChanges = true
            )
        }
    }

    fun addChecklistItem(text: String = "") {
        _state.update { current ->
            current.copy(
                isChecklistMode = true,
                checklistItems = current.checklistItems + ChecklistItem(text = text),
                hasChanges = true
            )
        }
    }

    fun updateChecklistItemText(itemId: String, text: String) {
        _state.update { current ->
            current.copy(
                checklistItems = current.checklistItems.map { item ->
                    if (item.id == itemId) item.copy(text = text) else item
                },
                hasChanges = true
            )
        }
    }

    fun toggleChecklistItem(itemId: String) {
        _state.update { current ->
            current.copy(
                checklistItems = current.checklistItems.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                },
                hasChanges = true
            )
        }
    }

    fun removeChecklistItem(itemId: String) {
        _state.update { current ->
            current.copy(
                checklistItems = current.checklistItems.filterNot { it.id == itemId },
                hasChanges = true
            )
        }
    }

    fun clearCompletedChecklistItems() {
        _state.update { current ->
            current.copy(
                checklistItems = current.checklistItems.filterNot { it.isChecked },
                hasChanges = true
            )
        }
    }

    fun setCategory(category: NoteCategory) {
        _state.update { it.copy(category = category, hasChanges = true) }
    }

    fun setColor(color: NoteColor) {
        _state.update { it.copy(color = color, hasChanges = true) }
    }

    fun togglePin() {
        _state.update { it.copy(isPinned = !it.isPinned, hasChanges = true) }
    }

    fun toggleFavorite() {
        _state.update { it.copy(isFavorite = !it.isFavorite, hasChanges = true) }
    }

    fun addTag(rawTag: String) {
        val cleaned = rawTag.trim().removePrefix("#").lowercase()
        if (cleaned.isEmpty()) return
        _state.update { current ->
            if (cleaned in current.tags) current
            else current.copy(tags = current.tags + cleaned, hasChanges = true)
        }
    }

    fun removeTag(tag: String) {
        _state.update { current ->
            current.copy(tags = current.tags - tag, hasChanges = true)
        }
    }

    fun saveAndFinish(onDone: () -> Unit) {
        val snapshot = _state.value
        val entity = snapshot.toEntity().let {
            it.copy(checklistItems = it.checklistItems.filter { item -> item.text.isNotBlank() })
        }
        viewModelScope.launch {
            if (!entity.isEffectivelyEmpty) {
                if (snapshot.hasChanges || snapshot.noteId == 0L) {
                    repository.saveNote(entity)
                }
            } else if (entity.id > 0L) {
                repository.deleteForever(entity.id)
            }
            onDone()
        }
    }

    fun archiveAndClose(onDone: (Long) -> Unit) {
        val snapshot = _state.value
        viewModelScope.launch {
            val entity = snapshot.toEntity().copy(
                checklistItems = snapshot.checklistItems.filter { it.text.isNotBlank() },
                isArchived = true,
                isPinned = false
            )
            if (!entity.isEffectivelyEmpty) {
                val savedId = repository.saveNote(entity)
                onDone(savedId)
            } else {
                onDone(0L)
            }
        }
    }

    fun trashAndClose(onDone: (Long) -> Unit) {
        val snapshot = _state.value
        viewModelScope.launch {
            if (snapshot.noteId > 0L) {
                repository.setTrashed(snapshot.noteId, true)
                onDone(snapshot.noteId)
            } else {
                onDone(0L)
            }
        }
    }

    fun duplicateNote(onDone: () -> Unit) {
        val snapshot = _state.value
        val entity = snapshot.toEntity().copy(
            checklistItems = snapshot.checklistItems.filter { it.text.isNotBlank() }
        )
        if (entity.isEffectivelyEmpty) return
        viewModelScope.launch {
            if (snapshot.hasChanges || snapshot.noteId == 0L) {
                repository.saveNote(entity)
            }
            repository.duplicateNote(entity)
            onDone()
        }
    }

    class Factory(
        private val noteId: Long,
        private val startAsChecklist: Boolean,
        private val repository: NotesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteEditorViewModel(noteId, startAsChecklist, repository) as T
        }
    }
}
