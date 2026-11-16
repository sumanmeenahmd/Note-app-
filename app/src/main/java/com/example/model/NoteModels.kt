package com.example.model

import java.util.UUID

enum class NoteCategory(val displayName: String) {
    PERSONAL("Personal"),
    WORK("Work"),
    IDEAS("Ideas"),
    TASKS("Tasks"),
    STUDY("Study")
}

enum class NoteColor(val label: String) {
    DEFAULT("Paper"),
    SAND("Warm Sand"),
    SAGE("Sage"),
    SKY("Slate Sky"),
    ROSE("Rose Clay"),
    LAVENDER("Lavender"),
    AMBER("Buttercream")
}

enum class NoteSortOrder(val label: String) {
    UPDATED_DESC("Recently edited"),
    CREATED_DESC("Newest created"),
    TITLE_ASC("Title A–Z"),
    COLOR("Paper color")
}

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class WorkspaceTab {
    NOTES,
    CHECKLISTS,
    VAULT
}

enum class VaultFilter {
    ARCHIVED,
    TRASH
}

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)
