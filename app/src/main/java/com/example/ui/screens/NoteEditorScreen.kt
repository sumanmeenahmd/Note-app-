package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.NoteCategory
import com.example.model.NoteColor
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.surfaceTokens
import com.example.ui.util.NoteFormatters
import com.example.ui.viewmodel.NoteEditorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = LocalIsDarkTheme.current
    val tokens = state.color.surfaceTokens(isDark)
    val context = LocalContext.current

    val backgroundColor by animateColorAsState(
        targetValue = tokens.container,
        animationSpec = spring(),
        label = "editor_bg"
    )

    var showColorPicker by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var pendingTagText by remember { mutableStateOf("") }

    BackHandler {
        viewModel.saveAndFinish(onNavigateBack)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.saveAndFinish(onNavigateBack) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = viewModel::togglePin,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (state.isPinned) {
                                stringResource(R.string.cd_unpin_note)
                            } else {
                                stringResource(R.string.cd_pin_note)
                            },
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = viewModel::toggleFavorite,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (state.isFavorite) {
                                stringResource(R.string.cd_unfavorite_note)
                            } else {
                                stringResource(R.string.cd_favorite_note)
                            },
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareText = NoteFormatters.buildShareText(state.toEntity())
                            if (shareText.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, state.title.ifBlank { "Folio Note" })
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, null))
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("editor_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.cd_share_note),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("editor_more_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_duplicate)) },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.duplicateNote(onNavigateBack)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_archive)) },
                                leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.archiveAndClose { onNavigateBack() }
                                }
                            )
                            if (state.noteId > 0L) {
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
                                        showOverflowMenu = false
                                        viewModel.trashAndClose { onNavigateBack() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = tokens.container,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(visible = showColorPicker) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NoteColor.entries.forEach { noteColor ->
                                val colorTokens = noteColor.surfaceTokens(isDark)
                                val selected = state.color == noteColor
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colorTokens.swatch)
                                        .border(
                                            width = if (selected) 2.5.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else colorTokens.border,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setColor(noteColor) }
                                        .testTag("color_swatch_${noteColor.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = noteColor.label,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showColorPicker = !showColorPicker },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_color_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = stringResource(R.string.cd_pick_color),
                                    tint = if (showColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = viewModel::toggleChecklistMode,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_checklist_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isChecklistMode) Icons.Filled.CheckBox else Icons.Outlined.CheckBox,
                                    contentDescription = stringResource(R.string.cd_toggle_checklist_mode),
                                    tint = if (state.isChecklistMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = viewModel::insertBulletAtEnd,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_bullet_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FormatListBulleted,
                                    contentDescription = stringResource(R.string.cd_insert_bullet),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = viewModel::insertTimestampAtEnd,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("editor_timestamp_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = stringResource(R.string.cd_insert_timestamp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val words = NoteFormatters.wordCount(state.content)
                        Text(
                            text = if (state.isChecklistMode) {
                                "${state.checklistItems.count { it.isChecked }}/${state.checklistItems.size} tasks"
                            } else {
                                "$words words"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (!state.isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Category Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NoteCategory.entries.forEach { category ->
                            FilterChip(
                                selected = state.category == category,
                                onClick = { viewModel.setCategory(category) },
                                label = { Text(category.displayName) },
                                shape = CircleShape,
                                modifier = Modifier.testTag("editor_category_${category.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (state.title.isEmpty()) {
                            Text(
                                text = if (state.isChecklistMode) {
                                    stringResource(R.string.editor_checklist_title_hint)
                                } else {
                                    stringResource(R.string.editor_title_hint)
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                            value = state.title,
                            onValueChange = viewModel::updateTitle,
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_title_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = NoteFormatters.formatFullTimestamp(state.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checklist Section
                    AnimatedVisibility(visible = state.isChecklistMode || state.checklistItems.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val total = state.checklistItems.size
                            val done = state.checklistItems.count { it.isChecked }
                            if (total > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { done.toFloat() / total },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Text(
                                        text = "$done/$total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            state.checklistItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { viewModel.toggleChecklistItem(item.id) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("checklist_checkbox_${item.id}")
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (item.text.isEmpty()) {
                                            Text(
                                                text = "List item…",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                            )
                                        }
                                        BasicTextField(
                                            value = item.text,
                                            onValueChange = { viewModel.updateChecklistItemText(item.id, it) },
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = if (item.isChecked) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                textDecoration = if (item.isChecked) {
                                                    TextDecoration.LineThrough
                                                } else {
                                                    TextDecoration.None
                                                }
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("checklist_text_${item.id}")
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeChecklistItem(item.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.cd_delete_checklist_item),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.addChecklistItem() },
                                    modifier = Modifier.testTag("add_checklist_item_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.editor_add_checklist_item))
                                }

                                if (done > 0) {
                                    TextButton(
                                        onClick = viewModel::clearCompletedChecklistItems,
                                        modifier = Modifier.testTag("clear_completed_button")
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_clear_completed, done),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 6.dp),
                                color = tokens.border
                            )
                        }
                    }

                    // Body Text Field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightInMinBody()
                    ) {
                        if (state.content.isEmpty()) {
                            Text(
                                text = stringResource(R.string.editor_body_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        BasicTextField(
                            value = state.content,
                            onValueChange = viewModel::updateContent,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightInMinBody()
                                .testTag("editor_content_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tags FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { viewModel.removeTag(tag) },
                                label = { Text("#$tag") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove $tag",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        InputChip(
                            selected = false,
                            onClick = {
                                pendingTagText = ""
                                showAddTagDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(stringResource(R.string.editor_add_tag)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("editor_add_tag_chip")
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text(stringResource(R.string.editor_add_tag)) },
            text = {
                OutlinedTextField(
                    value = pendingTagText,
                    onValueChange = { pendingTagText = it },
                    placeholder = { Text(stringResource(R.string.editor_tag_placeholder)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tag_dialog_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addTag(pendingTagText)
                        showAddTagDialog = false
                    },
                    modifier = Modifier.testTag("tag_dialog_confirm")
                ) {
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun Modifier.heightInMinBody(): Modifier = this.then(
    Modifier.padding(bottom = 12.dp)
)
