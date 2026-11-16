package com.example.data

import kotlinx.coroutines.flow.Flow

class NotesRepository(private val noteDao: NoteDao) {
    val activeNotes: Flow<List<NoteEntity>> = noteDao.observeActiveNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.observeArchivedNotes()
    val trashedNotes: Flow<List<NoteEntity>> = noteDao.observeTrashedNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun observeNoteById(id: Long): Flow<NoteEntity?> = noteDao.observeNoteById(id)

    suspend fun saveNote(note: NoteEntity): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            noteDao.insertNote(note.copy(createdAt = now, updatedAt = now))
        } else {
            noteDao.updateNote(note.copy(updatedAt = now))
            note.id
        }
    }

    suspend fun duplicateNote(note: NoteEntity): Long {
        val now = System.currentTimeMillis()
        val copyTitle = if (note.title.isBlank()) "Copy" else "${note.title} (Copy)"
        return noteDao.insertNote(
            note.copy(
                id = 0L,
                title = copyTitle,
                isPinned = false,
                isArchived = false,
                isTrashed = false,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun setPinned(id: Long, pinned: Boolean) = noteDao.setPinned(id, pinned)

    suspend fun setFavorite(id: Long, favorite: Boolean) = noteDao.setFavorite(id, favorite)

    suspend fun setArchived(id: Long, archived: Boolean) = noteDao.setArchived(id, archived)

    suspend fun setTrashed(id: Long, trashed: Boolean) = noteDao.setTrashed(id, trashed)

    suspend fun deleteForever(id: Long) = noteDao.deleteForever(id)

    suspend fun emptyTrash() = noteDao.emptyTrash()
}
