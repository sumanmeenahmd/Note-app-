package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.NoteEntity
import com.example.data.NotesRepository
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import com.example.model.NoteCategory
import com.example.model.NoteSortOrder
import com.example.model.ThemePreference
import com.example.model.VaultFilter
import com.example.model.WorkspaceTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NotesFeedState {
    data object Loading : NotesFeedState
    data class Ready(
        val activeTab: WorkspaceTab = WorkspaceTab.NOTES,
        val vaultFilter: VaultFilter = VaultFilter.ARCHIVED,
        val searchQuery: String = "",
        val selectedCategory: NoteCategory? = null,
        val favoritesOnly: Boolean = false,
        val pinnedNotes: List<NoteEntity> = emptyList(),
        val otherNotes: List<NoteEntity> = emptyList(),
        val checklistNotes: List<NoteEntity> = emptyList(),
        val archivedNotes: List<NoteEntity> = emptyList(),
        val trashedNotes: List<NoteEntity> = emptyList(),
        val totalActiveCount: Int = 0,
        val totalPinnedCount: Int = 0,
        val selectedNoteIds: Set<Long> = emptySet(),
        val preferences: UserPreferences = UserPreferences()
    ) : NotesFeedState
}

sealed interface WorkspaceEvent {
    data class ShowUndoArchive(val noteIds: List<Long>) : WorkspaceEvent
    data class ShowUndoTrash(val noteIds: List<Long>) : WorkspaceEvent
    data class ShowMessage(val message: String) : WorkspaceEvent
}

private data class FilterState(
    val activeTab: WorkspaceTab = WorkspaceTab.NOTES,
    val vaultFilter: VaultFilter = VaultFilter.ARCHIVED,
    val searchQuery: String = "",
    val selectedCategory: NoteCategory? = null,
    val favoritesOnly: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet()
)

class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterState())
    private val _events = MutableSharedFlow<WorkspaceEvent>()
    val events: SharedFlow<WorkspaceEvent> = _events.asSharedFlow()

    val uiState: StateFlow<NotesFeedState> = combine(
        notesRepository.activeNotes,
        notesRepository.archivedNotes,
        notesRepository.trashedNotes,
        preferencesRepository.preferencesFlow,
        filterState
    ) { active, archived, trashed, prefs, filters ->
        val filteredActive = applyFiltersAndSort(
            notes = active,
            query = filters.searchQuery,
            category = filters.selectedCategory,
            favoritesOnly = filters.favoritesOnly,
            sortOrder = prefs.sortOrder
        )
        val pinned = filteredActive.filter { it.isPinned }
        val others = filteredActive.filterNot { it.isPinned }
        val checklists = filteredActive.filter { it.isChecklistMode || it.checklistItems.isNotEmpty() }

        NotesFeedState.Ready(
            activeTab = filters.activeTab,
            vaultFilter = filters.vaultFilter,
            searchQuery = filters.searchQuery,
            selectedCategory = filters.selectedCategory,
            favoritesOnly = filters.favoritesOnly,
            pinnedNotes = pinned,
            otherNotes = others,
            checklistNotes = checklists,
            archivedNotes = applyFiltersAndSort(
                notes = archived,
                query = filters.searchQuery,
                category = null,
                favoritesOnly = false,
                sortOrder = prefs.sortOrder
            ),
            trashedNotes = applyFiltersAndSort(
                notes = trashed,
                query = filters.searchQuery,
                category = null,
                favoritesOnly = false,
                sortOrder = prefs.sortOrder
            ),
            totalActiveCount = active.size,
            totalPinnedCount = active.count { it.isPinned },
            selectedNoteIds = filters.selectedNoteIds,
            preferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = NotesFeedState.Loading
    )

    private fun applyFiltersAndSort(
        notes: List<NoteEntity>,
        query: String,
        category: NoteCategory?,
        favoritesOnly: Boolean,
        sortOrder: NoteSortOrder
    ): List<NoteEntity> {
        val trimmedQuery = query.trim().lowercase()
        val filtered = notes.filter { note ->
            val matchesCategory = category == null || note.category == category
            val matchesFav = !favoritesOnly || note.isFavorite
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                val cleanTagQuery = trimmedQuery.removePrefix("#")
                note.title.lowercase().contains(trimmedQuery) ||
                    note.content.lowercase().contains(trimmedQuery) ||
                    note.category.displayName.lowercase().contains(trimmedQuery) ||
                    note.tags.any { it.lowercase().contains(cleanTagQuery) } ||
                    note.checklistItems.any { it.text.lowercase().contains(trimmedQuery) }
            }
            matchesCategory && matchesFav && matchesQuery
        }

        val comparator = when (sortOrder) {
            NoteSortOrder.UPDATED_DESC -> compareByDescending<NoteEntity> { it.updatedAt }
            NoteSortOrder.CREATED_DESC -> compareByDescending<NoteEntity> { it.createdAt }
            NoteSortOrder.TITLE_ASC -> compareBy<NoteEntity> {
                it.title.ifBlank { "\uFFFF" }.lowercase()
            }.thenByDescending { it.updatedAt }
            NoteSortOrder.COLOR -> compareBy<NoteEntity> { it.color.ordinal }
                .thenByDescending { it.updatedAt }
        }
        return filtered.sortedWith(comparator)
    }

    fun selectTab(tab: WorkspaceTab) {
        filterState.value = filterState.value.copy(
            activeTab = tab,
            selectedNoteIds = emptySet()
        )
    }

    fun selectVaultFilter(vaultFilter: VaultFilter) {
        filterState.value = filterState.value.copy(
            vaultFilter = vaultFilter,
            selectedNoteIds = emptySet()
        )
    }

    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(searchQuery = query)
    }

    fun selectCategory(category: NoteCategory?) {
        filterState.value = filterState.value.copy(
            selectedCategory = category,
            favoritesOnly = false
        )
    }

    fun toggleFavoritesFilter() {
        val next = !filterState.value.favoritesOnly
        filterState.value = filterState.value.copy(
            favoritesOnly = next,
            selectedCategory = if (next) null else filterState.value.selectedCategory
        )
    }

    fun clearFilters() {
        filterState.value = filterState.value.copy(
            searchQuery = "",
            selectedCategory = null,
            favoritesOnly = false
        )
    }

    fun toggleSelection(noteId: Long) {
        val current = filterState.value.selectedNoteIds
        val updated = if (noteId in current) current - noteId else current + noteId
        filterState.value = filterState.value.copy(selectedNoteIds = updated)
    }

    fun clearSelection() {
        filterState.value = filterState.value.copy(selectedNoteIds = emptySet())
    }

    fun toggleChecklistItemOnCard(note: NoteEntity, itemId: String) {
        viewModelScope.launch {
            val updatedItems = note.checklistItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
            }
            notesRepository.saveNote(note.copy(checklistItems = updatedItems))
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.setPinned(note.id, !note.isPinned)
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.setFavorite(note.id, !note.isFavorite)
        }
    }

    fun archiveNote(noteId: Long) {
        viewModelScope.launch {
            notesRepository.setArchived(noteId, true)
            _events.emit(WorkspaceEvent.ShowUndoArchive(listOf(noteId)))
        }
    }

    fun unarchiveNote(noteId: Long) {
        viewModelScope.launch {
            notesRepository.setArchived(noteId, false)
            _events.emit(WorkspaceEvent.ShowMessage("Note restored to workspace"))
        }
    }

    fun trashNote(noteId: Long) {
        viewModelScope.launch {
            notesRepository.setTrashed(noteId, true)
            _events.emit(WorkspaceEvent.ShowUndoTrash(listOf(noteId)))
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            notesRepository.setTrashed(noteId, false)
            _events.emit(WorkspaceEvent.ShowMessage("Note restored"))
        }
    }

    fun deleteForever(noteId: Long) {
        viewModelScope.launch {
            notesRepository.deleteForever(noteId)
            _events.emit(WorkspaceEvent.ShowMessage("Note permanently deleted"))
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            notesRepository.emptyTrash()
            _events.emit(WorkspaceEvent.ShowMessage("Trash emptied"))
        }
    }

    fun undoArchive(noteIds: List<Long>) {
        viewModelScope.launch {
            noteIds.forEach { notesRepository.setArchived(it, false) }
        }
    }

    fun undoTrash(noteIds: List<Long>) {
        viewModelScope.launch {
            noteIds.forEach { notesRepository.setTrashed(it, false) }
        }
    }

    fun batchPinSelected(notes: List<NoteEntity>) {
        val selected = filterState.value.selectedNoteIds
        if (selected.isEmpty()) return
        val targetNotes = notes.filter { it.id in selected }
        val shouldPin = targetNotes.any { !it.isPinned }
        viewModelScope.launch {
            selected.forEach { notesRepository.setPinned(it, shouldPin) }
            clearSelection()
        }
    }

    fun batchArchiveSelected() {
        val selected = filterState.value.selectedNoteIds.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { notesRepository.setArchived(it, true) }
            clearSelection()
            _events.emit(WorkspaceEvent.ShowUndoArchive(selected))
        }
    }

    fun batchTrashSelected() {
        val selected = filterState.value.selectedNoteIds.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { notesRepository.setTrashed(it, true) }
            clearSelection()
            _events.emit(WorkspaceEvent.ShowUndoTrash(selected))
        }
    }

    fun duplicateNote(note: NoteEntity) {
        viewModelScope.launch {
            notesRepository.duplicateNote(note)
            _events.emit(WorkspaceEvent.ShowMessage("Note duplicated"))
        }
    }

    fun toggleGridView() {
        val current = (uiState.value as? NotesFeedState.Ready)?.preferences?.isGridView ?: true
        viewModelScope.launch {
            preferencesRepository.setGridView(!current)
        }
    }

    fun setGridView(isGrid: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGridView(isGrid)
        }
    }

    fun setSortOrder(order: NoteSortOrder) {
        viewModelScope.launch {
            preferencesRepository.setSortOrder(order)
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            preferencesRepository.setThemePreference(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
    }

    class Factory(
        private val notesRepository: NotesRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(notesRepository, preferencesRepository) as T
        }
    }
}
