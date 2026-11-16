package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.NoteEntity
import com.example.model.NoteCategory
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.surfaceTokens
import com.example.ui.util.NoteFormatters

enum class NoteCardMode {
    WORKSPACE,
    ARCHIVE,
    TRASH
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    isSelected: Boolean,
    isGrid: Boolean,
    mode: NoteCardMode = NoteCardMode.WORKSPACE,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onTrash: () -> Unit,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val tokens = note.color.surfaceTokens(isDark)
    var menuExpanded by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else tokens.border,
        animationSpec = spring(),
        label = "card_border"
    )
    val borderWidth = if (isSelected) 2.5.dp else 1.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(20.dp),
        color = tokens.container,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 10.dp,
                top = 14.dp,
                bottom = 14.dp
            )
        ) {
            // Top metadata row: Category pill + status icons + menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = note.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(20.dp)
                        )
                    } else {
                        if (note.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = stringResource(R.string.cd_favorite_note),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(16.dp)
                            )
                        }
                        if (note.isPinned && mode == NoteCardMode.WORKSPACE) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = stringResource(R.string.cd_pin_note),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(16.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(36.dp)
                                .testTag("note_card_menu_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            when (mode) {
                                NoteCardMode.WORKSPACE -> {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (note.isPinned) stringResource(R.string.cd_unpin_note)
                                                else stringResource(R.string.cd_pin_note)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onTogglePin()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (note.isFavorite) stringResource(R.string.cd_unfavorite_note)
                                                else stringResource(R.string.cd_favorite_note)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onToggleFavorite()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_duplicate)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDuplicate()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_archive)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Archive, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onArchive()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_move_to_trash)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onTrash()
                                        }
                                    )
                                }
                                NoteCardMode.ARCHIVE -> {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_unarchive)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Unarchive, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onUnarchive()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_move_to_trash)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onTrash()
                                        }
                                    )
                                }
                                NoteCardMode.TRASH -> {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_restore)) },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.RestoreFromTrash, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onRestore()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_delete_forever)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.DeleteForever,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteForever()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (note.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isGrid) 6 else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            if (note.checklistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val visibleItems = note.checklistItems.take(if (isGrid) 4 else 3)
                val completedCount = note.checklistItems.count { it.isChecked }
                val totalCount = note.checklistItems.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Column(
                    modifier = Modifier.padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    visibleItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = mode == NoteCardMode.WORKSPACE) {
                                    onToggleChecklistItem(item.id)
                                }
                                .padding(vertical = 3.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.isChecked) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (item.isChecked) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (totalCount > visibleItems.size) {
                        Text(
                            text = "+${totalCount - visibleItems.size} more items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "$completedCount/$totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.padding(end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (note.tags.size > 3) {
                        Text(
                            text = "+${note.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = NoteFormatters.formatRelativeTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                val words = NoteFormatters.wordCount(note.content)
                if (words > 0) {
                    Text(
                        text = "$words w",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
