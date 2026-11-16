package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.NoteEntity
import com.example.model.NoteCategory
import com.example.model.NoteSortOrder
import com.example.model.VaultFilter
import com.example.model.WorkspaceTab
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoteCard
import com.example.ui.components.NoteCardMode
import com.example.ui.components.SettingsSheet
import com.example.ui.viewmodel.NotesFeedState
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.WorkspaceEvent

@Composable
fun NotesWorkspaceScreen(
    viewModel: NotesViewModel,
    onOpenNote: (Long) -> Unit,
    onCreateNote: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WorkspaceEvent.ShowUndoArchive -> {
                    val label = if (event.noteIds.size > 1) {
                        context.getString(R.string.msg_notes_archived, event.noteIds.size)
                    } else {
                        context.getString(R.string.msg_note_archived)
                    }
                    val res = snackbarHostState.showSnackbar(
                        message = label,
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        viewModel.undoArchive(event.noteIds)
                    }
                }
                is WorkspaceEvent.ShowUndoTrash -> {
                    val label = if (event.noteIds.size > 1) {
                        context.getString(R.string.msg_notes_trashed, event.noteIds.size)
                    } else {
                        context.getString(R.string.msg_note_trashed)
                    }
                    val res = snackbarHostState.showSnackbar(
                        message = label,
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        viewModel.undoTrash(event.noteIds)
                    }
                }
                is WorkspaceEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val readyState = uiState as? NotesFeedState.Ready
    if (readyState != null && readyState.selectedNoteIds.isNotEmpty()) {
        BackHandler { viewModel.clearSelection() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp
        val gridColumns = when {
            maxWidth >= 900.dp -> 3
            maxWidth >= 600.dp -> 2
            else -> 2
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && readyState != null) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WorkspaceRailDestinations(
                        activeTab = readyState.activeTab,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }

            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.navigationBars,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && readyState != null) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 2.dp
                        ) {
                            WorkspaceBarDestinations(
                                activeTab = readyState.activeTab,
                                onSelectTab = viewModel::selectTab
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (readyState != null && readyState.activeTab != WorkspaceTab.VAULT) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FloatingActionButton(
                                onClick = { onCreateNote(true) },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("fab_new_checklist")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckBox,
                                    contentDescription = stringResource(R.string.cd_new_checklist)
                                )
                            }

                            ExtendedFloatingActionButton(
                                onClick = {
                                    onCreateNote(readyState.activeTab == WorkspaceTab.CHECKLISTS)
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.fab_new_note),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("fab_new_note")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (val state = uiState) {
                    NotesFeedState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    is NotesFeedState.Ready -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .statusBarsPadding()
                        ) {
                            // Top Bar: Contextual Selection OR Editorial Workspace Header
                            AnimatedContent(
                                targetState = state.selectedNoteIds.isNotEmpty(),
                                label = "header_transition"
                            ) { isSelectionMode ->
                                if (isSelectionMode) {
                                    SelectionTopBar(
                                        selectedCount = state.selectedNoteIds.size,
                                        onClose = viewModel::clearSelection,
                                        onBatchPin = {
                                            viewModel.batchPinSelected(state.pinnedNotes + state.otherNotes)
                                        },
                                        onBatchArchive = viewModel::batchArchiveSelected,
                                        onBatchTrash = viewModel::batchTrashSelected
                                    )
                                } else {
                                    EditorialHeader(
                                        totalActive = state.totalActiveCount,
                                        totalPinned = state.totalPinnedCount,
                                        isGridView = state.preferences.isGridView,
                                        sortOrder = state.preferences.sortOrder,
                                        onToggleLayout = viewModel::toggleGridView,
                                        onSelectSort = viewModel::setSortOrder,
                                        onOpenSettings = { showSettingsSheet = true }
                                    )
                                }
                            }

                            // Search bar
                            SearchBarPill(
                                query = state.searchQuery,
                                onQueryChange = viewModel::updateSearchQuery,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            when (state.activeTab) {
                                WorkspaceTab.NOTES -> {
                                    CategoryFilterStrip(
                                        selectedCategory = state.selectedCategory,
                                        favoritesOnly = state.favoritesOnly,
                                        onSelectCategory = viewModel::selectCategory,
                                        onToggleFavorites = viewModel::toggleFavoritesFilter
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    NotesFeedContent(
                                        pinnedNotes = state.pinnedNotes,
                                        otherNotes = state.otherNotes,
                                        isGrid = state.preferences.isGridView,
                                        gridColumns = gridColumns,
                                        selectedIds = state.selectedNoteIds,
                                        hasActiveFilter = state.searchQuery.isNotBlank() || state.selectedCategory != null || state.favoritesOnly,
                                        onClearFilters = viewModel::clearFilters,
                                        onCreateNote = { onCreateNote(false) },
                                        onCardClick = { note ->
                                            if (state.selectedNoteIds.isNotEmpty()) {
                                                viewModel.toggleSelection(note.id)
                                            } else {
                                                onOpenNote(note.id)
                                            }
                                        },
                                        onCardLongClick = { note -> viewModel.toggleSelection(note.id) },
                                        onToggleChecklistItem = viewModel::toggleChecklistItemOnCard,
                                        onTogglePin = viewModel::togglePin,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onDuplicate = viewModel::duplicateNote,
                                        onArchive = { viewModel.archiveNote(it.id) },
                                        onTrash = { viewModel.trashNote(it.id) }
                                    )
                                }

                                WorkspaceTab.CHECKLISTS -> {
                                    ChecklistsFeedContent(
                                        checklistNotes = state.checklistNotes,
                                        isGrid = state.preferences.isGridView,
                                        gridColumns = gridColumns,
                                        selectedIds = state.selectedNoteIds,
                                        hasSearch = state.searchQuery.isNotBlank(),
                                        onClearFilters = viewModel::clearFilters,
                                        onCreateChecklist = { onCreateNote(true) },
                                        onCardClick = { note ->
                                            if (state.selectedNoteIds.isNotEmpty()) {
                                                viewModel.toggleSelection(note.id)
                                            } else {
                                                onOpenNote(note.id)
                                            }
                                        },
                                        onCardLongClick = { note -> viewModel.toggleSelection(note.id) },
                                        onToggleChecklistItem = viewModel::toggleChecklistItemOnCard,
                                        onTogglePin = viewModel::togglePin,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onDuplicate = viewModel::duplicateNote,
                                        onArchive = { viewModel.archiveNote(it.id) },
                                        onTrash = { viewModel.trashNote(it.id) }
                                    )
                                }

                                WorkspaceTab.VAULT -> {
                                    VaultContent(
                                        vaultFilter = state.vaultFilter,
                                        archivedNotes = state.archivedNotes,
                                        trashedNotes = state.trashedNotes,
                                        isGrid = state.preferences.isGridView,
                                        gridColumns = gridColumns,
                                        onSelectVaultFilter = viewModel::selectVaultFilter,
                                        onOpenNote = onOpenNote,
                                        onUnarchive = { viewModel.unarchiveNote(it.id) },
                                        onTrash = { viewModel.trashNote(it.id) },
                                        onRestore = { viewModel.restoreFromTrash(it.id) },
                                        onDeleteForever = { viewModel.deleteForever(it.id) },
                                        onRequestEmptyTrash = { showEmptyTrashDialog = true }
                                    )
                                }
                            }
                        }

                        if (showSettingsSheet) {
                            SettingsSheet(
                                preferences = state.preferences,
                                totalNotes = state.totalActiveCount,
                                checklistCount = state.checklistNotes.size,
                                archivedCount = state.archivedNotes.size,
                                onThemeChange = viewModel::setThemePreference,
                                onDynamicColorChange = viewModel::setDynamicColor,
                                onLayoutChange = viewModel::setGridView,
                                onSortOrderChange = viewModel::setSortOrder,
                                onDismiss = { showSettingsSheet = false }
                            )
                        }

                        if (showEmptyTrashDialog) {
                            AlertDialog(
                                onDismissRequest = { showEmptyTrashDialog = false },
                                title = { Text(stringResource(R.string.confirm_empty_trash_title)) },
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.confirm_empty_trash_message,
                                            state.trashedNotes.size
                                        )
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showEmptyTrashDialog = false
                                            viewModel.emptyTrash()
                                        },
                                        modifier = Modifier.testTag("confirm_empty_trash_button")
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_delete_forever),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEmptyTrashDialog = false }) {
                                        Text(stringResource(R.string.action_cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceRailDestinations(
    activeTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit
) {
    val items = listOf(
        Triple(WorkspaceTab.NOTES, R.string.nav_notes, Pair(Icons.Filled.Description, Icons.Outlined.Description)),
        Triple(WorkspaceTab.CHECKLISTS, R.string.nav_checklists, Pair(Icons.Filled.CheckBox, Icons.Outlined.CheckBox)),
        Triple(WorkspaceTab.VAULT, R.string.nav_vault, Pair(Icons.Filled.Inventory2, Icons.Outlined.Inventory2))
    )
    items.forEach { (tab, labelRes, icons) ->
        val selected = activeTab == tab
        val icon = if (selected) icons.first else icons.second
        val label = stringResource(labelRes)
        NavigationRailItem(
            selected = selected,
            onClick = { onSelectTab(tab) },
            icon = { Icon(icon, contentDescription = label) },
            label = { Text(label) },
            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.WorkspaceBarDestinations(
    activeTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit
) {
    val items = listOf(
        Triple(WorkspaceTab.NOTES, R.string.nav_notes, Pair(Icons.Filled.Description, Icons.Outlined.Description)),
        Triple(WorkspaceTab.CHECKLISTS, R.string.nav_checklists, Pair(Icons.Filled.CheckBox, Icons.Outlined.CheckBox)),
        Triple(WorkspaceTab.VAULT, R.string.nav_vault, Pair(Icons.Filled.Inventory2, Icons.Outlined.Inventory2))
    )
    items.forEach { (tab, labelRes, icons) ->
        val selected = activeTab == tab
        val icon = if (selected) icons.first else icons.second
        val label = stringResource(labelRes)
        NavigationBarItem(
            selected = selected,
            onClick = { onSelectTab(tab) },
            icon = { Icon(icon, contentDescription = label) },
            label = { Text(label) },
            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
        )
    }
}

@Composable
private fun EditorialHeader(
    totalActive: Int,
    totalPinned: Int,
    isGridView: Boolean,
    sortOrder: NoteSortOrder,
    onToggleLayout: () -> Unit,
    onSelectSort: (NoteSortOrder) -> Unit,
    onOpenSettings: () -> Unit
) {
    var sortMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.workspace_subtitle_format, totalActive, totalPinned),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleLayout,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("toggle_layout_button")
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                    contentDescription = stringResource(R.string.cd_toggle_layout),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { sortMenuOpen = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("sort_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = stringResource(R.string.cd_sort_notes),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false }
                ) {
                    NoteSortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.label,
                                    fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal,
                                    color = if (order == sortOrder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                sortMenuOpen = false
                                onSelectSort(order)
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = stringResource(R.string.cd_settings_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onBatchPin: () -> Unit,
    onBatchArchive: () -> Unit,
    onBatchTrash: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_selection_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_selection),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = stringResource(R.string.selected_count_format, selectedCount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBatchPin,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("batch_pin_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = stringResource(R.string.cd_batch_pin),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    onClick = onBatchArchive,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("batch_archive_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = stringResource(R.string.cd_batch_archive),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    onClick = onBatchTrash,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("batch_trash_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_batch_trash),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarPill(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_notes_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_notes_input")
                )
            }
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("clear_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterStrip(
    selectedCategory: NoteCategory?,
    favoritesOnly: Boolean,
    onSelectCategory: (NoteCategory?) -> Unit,
    onToggleFavorites: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedCategory == null && !favoritesOnly,
            onClick = { onSelectCategory(null) },
            label = { Text(stringResource(R.string.filter_all)) },
            shape = CircleShape,
            modifier = Modifier.testTag("filter_chip_all")
        )

        FilterChip(
            selected = favoritesOnly,
            onClick = onToggleFavorites,
            label = { Text(stringResource(R.string.filter_favorites)) },
            leadingIcon = {
                Icon(
                    imageVector = if (favoritesOnly) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            modifier = Modifier.testTag("filter_chip_favorites")
        )

        NoteCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    if (selectedCategory == category) onSelectCategory(null)
                    else onSelectCategory(category)
                },
                label = { Text(category.displayName) },
                shape = CircleShape,
                modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun NotesFeedContent(
    pinnedNotes: List<NoteEntity>,
    otherNotes: List<NoteEntity>,
    isGrid: Boolean,
    gridColumns: Int,
    selectedIds: Set<Long>,
    hasActiveFilter: Boolean,
    onClearFilters: () -> Unit,
    onCreateNote: () -> Unit,
    onCardClick: (NoteEntity) -> Unit,
    onCardLongClick: (NoteEntity) -> Unit,
    onToggleChecklistItem: (NoteEntity, String) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onDuplicate: (NoteEntity) -> Unit,
    onArchive: (NoteEntity) -> Unit,
    onTrash: (NoteEntity) -> Unit
) {
    if (pinnedNotes.isEmpty() && otherNotes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (hasActiveFilter) {
                EmptyStateView(
                    imageRes = R.drawable.img_empty_notes,
                    imageDescription = stringResource(R.string.empty_search_title),
                    title = stringResource(R.string.empty_search_title),
                    subtitle = stringResource(R.string.empty_search_subtitle),
                    ctaLabel = stringResource(R.string.clear_filters),
                    isSecondaryCta = true,
                    onCtaClick = onClearFilters
                )
            } else {
                EmptyStateView(
                    imageRes = R.drawable.img_empty_notes,
                    imageDescription = stringResource(R.string.empty_notes_title),
                    title = stringResource(R.string.empty_notes_title),
                    subtitle = stringResource(R.string.empty_notes_subtitle),
                    ctaLabel = stringResource(R.string.empty_notes_cta),
                    onCtaClick = onCreateNote
                )
            }
        }
        return
    }

    val contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp)

    if (isGrid) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(gridColumns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            if (pinnedNotes.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SectionLabel(
                        text = stringResource(R.string.section_pinned),
                        showPinIcon = true
                    )
                }
                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = note.id in selectedIds,
                        isGrid = true,
                        onClick = { onCardClick(note) },
                        onLongClick = { onCardLongClick(note) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleFavorite = { onToggleFavorite(note) },
                        onDuplicate = { onDuplicate(note) },
                        onArchive = { onArchive(note) },
                        onUnarchive = {},
                        onTrash = { onTrash(note) },
                        onRestore = {},
                        onDeleteForever = {}
                    )
                }
            }

            if (otherNotes.isNotEmpty()) {
                if (pinnedNotes.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        SectionLabel(
                            text = stringResource(R.string.section_others),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                items(otherNotes, key = { "note_${it.id}" }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = note.id in selectedIds,
                        isGrid = true,
                        onClick = { onCardClick(note) },
                        onLongClick = { onCardLongClick(note) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleFavorite = { onToggleFavorite(note) },
                        onDuplicate = { onDuplicate(note) },
                        onArchive = { onArchive(note) },
                        onUnarchive = {},
                        onTrash = { onTrash(note) },
                        onRestore = {},
                        onDeleteForever = {}
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pinnedNotes.isNotEmpty()) {
                item(key = "header_pinned") {
                    SectionLabel(
                        text = stringResource(R.string.section_pinned),
                        showPinIcon = true
                    )
                }
                items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = note.id in selectedIds,
                        isGrid = false,
                        onClick = { onCardClick(note) },
                        onLongClick = { onCardLongClick(note) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleFavorite = { onToggleFavorite(note) },
                        onDuplicate = { onDuplicate(note) },
                        onArchive = { onArchive(note) },
                        onUnarchive = {},
                        onTrash = { onTrash(note) },
                        onRestore = {},
                        onDeleteForever = {},
                        modifier = Modifier.widthIn(max = 680.dp)
                    )
                }
            }
            if (otherNotes.isNotEmpty()) {
                if (pinnedNotes.isNotEmpty()) {
                    item(key = "header_others") {
                        SectionLabel(
                            text = stringResource(R.string.section_others),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                items(otherNotes, key = { "note_${it.id}" }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = note.id in selectedIds,
                        isGrid = false,
                        onClick = { onCardClick(note) },
                        onLongClick = { onCardLongClick(note) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                        onTogglePin = { onTogglePin(note) },
                        onToggleFavorite = { onToggleFavorite(note) },
                        onDuplicate = { onDuplicate(note) },
                        onArchive = { onArchive(note) },
                        onUnarchive = {},
                        onTrash = { onTrash(note) },
                        onRestore = {},
                        onDeleteForever = {},
                        modifier = Modifier.widthIn(max = 680.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistsFeedContent(
    checklistNotes: List<NoteEntity>,
    isGrid: Boolean,
    gridColumns: Int,
    selectedIds: Set<Long>,
    hasSearch: Boolean,
    onClearFilters: () -> Unit,
    onCreateChecklist: () -> Unit,
    onCardClick: (NoteEntity) -> Unit,
    onCardLongClick: (NoteEntity) -> Unit,
    onToggleChecklistItem: (NoteEntity, String) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onDuplicate: (NoteEntity) -> Unit,
    onArchive: (NoteEntity) -> Unit,
    onTrash: (NoteEntity) -> Unit
) {
    if (checklistNotes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (hasSearch) {
                EmptyStateView(
                    imageRes = R.drawable.img_empty_notes,
                    imageDescription = stringResource(R.string.empty_search_title),
                    title = stringResource(R.string.empty_search_title),
                    subtitle = stringResource(R.string.empty_search_subtitle),
                    ctaLabel = stringResource(R.string.clear_filters),
                    isSecondaryCta = true,
                    onCtaClick = onClearFilters
                )
            } else {
                EmptyStateView(
                    imageRes = R.drawable.img_empty_notes,
                    imageDescription = stringResource(R.string.empty_checklists_title),
                    title = stringResource(R.string.empty_checklists_title),
                    subtitle = stringResource(R.string.empty_checklists_subtitle),
                    ctaLabel = stringResource(R.string.empty_checklists_cta),
                    onCtaClick = onCreateChecklist
                )
            }
        }
        return
    }

    val contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp)
    if (isGrid) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(gridColumns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(checklistNotes, key = { "chk_${it.id}" }) { note ->
                NoteCard(
                    note = note,
                    isSelected = note.id in selectedIds,
                    isGrid = true,
                    onClick = { onCardClick(note) },
                    onLongClick = { onCardLongClick(note) },
                    onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                    onTogglePin = { onTogglePin(note) },
                    onToggleFavorite = { onToggleFavorite(note) },
                    onDuplicate = { onDuplicate(note) },
                    onArchive = { onArchive(note) },
                    onUnarchive = {},
                    onTrash = { onTrash(note) },
                    onRestore = {},
                    onDeleteForever = {}
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(checklistNotes, key = { "chk_${it.id}" }) { note ->
                NoteCard(
                    note = note,
                    isSelected = note.id in selectedIds,
                    isGrid = false,
                    onClick = { onCardClick(note) },
                    onLongClick = { onCardLongClick(note) },
                    onToggleChecklistItem = { itemId -> onToggleChecklistItem(note, itemId) },
                    onTogglePin = { onTogglePin(note) },
                    onToggleFavorite = { onToggleFavorite(note) },
                    onDuplicate = { onDuplicate(note) },
                    onArchive = { onArchive(note) },
                    onUnarchive = {},
                    onTrash = { onTrash(note) },
                    onRestore = {},
                    onDeleteForever = {},
                    modifier = Modifier.widthIn(max = 680.dp)
                )
            }
        }
    }
}

@Composable
private fun VaultContent(
    vaultFilter: VaultFilter,
    archivedNotes: List<NoteEntity>,
    trashedNotes: List<NoteEntity>,
    isGrid: Boolean,
    gridColumns: Int,
    onSelectVaultFilter: (VaultFilter) -> Unit,
    onOpenNote: (Long) -> Unit,
    onUnarchive: (NoteEntity) -> Unit,
    onTrash: (NoteEntity) -> Unit,
    onRestore: (NoteEntity) -> Unit,
    onDeleteForever: (NoteEntity) -> Unit,
    onRequestEmptyTrash: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = vaultFilter == VaultFilter.ARCHIVED,
                    onClick = { onSelectVaultFilter(VaultFilter.ARCHIVED) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("vault_tab_archived")
                ) {
                    Text(stringResource(R.string.vault_tab_archived, archivedNotes.size))
                }
                SegmentedButton(
                    selected = vaultFilter == VaultFilter.TRASH,
                    onClick = { onSelectVaultFilter(VaultFilter.TRASH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("vault_tab_trash")
                ) {
                    Text(stringResource(R.string.vault_tab_trash, trashedNotes.size))
                }
            }

            if (vaultFilter == VaultFilter.TRASH && trashedNotes.isNotEmpty()) {
                IconButton(
                    onClick = onRequestEmptyTrash,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("empty_trash_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.action_empty_trash),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val items = if (vaultFilter == VaultFilter.ARCHIVED) archivedNotes else trashedNotes
        val mode = if (vaultFilter == VaultFilter.ARCHIVED) NoteCardMode.ARCHIVE else NoteCardMode.TRASH

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    imageRes = R.drawable.img_empty_archive,
                    imageDescription = if (vaultFilter == VaultFilter.ARCHIVED) {
                        stringResource(R.string.empty_archive_title)
                    } else {
                        stringResource(R.string.empty_trash_title)
                    },
                    title = if (vaultFilter == VaultFilter.ARCHIVED) {
                        stringResource(R.string.empty_archive_title)
                    } else {
                        stringResource(R.string.empty_trash_title)
                    },
                    subtitle = if (vaultFilter == VaultFilter.ARCHIVED) {
                        stringResource(R.string.empty_archive_subtitle)
                    } else {
                        stringResource(R.string.empty_trash_subtitle)
                    }
                )
            }
        } else {
            val contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp)
            if (isGrid) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(gridColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(items, key = { "vault_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            isSelected = false,
                            isGrid = true,
                            mode = mode,
                            onClick = { if (mode == NoteCardMode.ARCHIVE) onOpenNote(note.id) },
                            onLongClick = {},
                            onToggleChecklistItem = {},
                            onTogglePin = {},
                            onToggleFavorite = {},
                            onDuplicate = {},
                            onArchive = {},
                            onUnarchive = { onUnarchive(note) },
                            onTrash = { onTrash(note) },
                            onRestore = { onRestore(note) },
                            onDeleteForever = { onDeleteForever(note) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { "vault_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            isSelected = false,
                            isGrid = false,
                            mode = mode,
                            onClick = { if (mode == NoteCardMode.ARCHIVE) onOpenNote(note.id) },
                            onLongClick = {},
                            onToggleChecklistItem = {},
                            onTogglePin = {},
                            onToggleFavorite = {},
                            onDuplicate = {},
                            onArchive = {},
                            onUnarchive = { onUnarchive(note) },
                            onTrash = { onTrash(note) },
                            onRestore = { onRestore(note) },
                            onDeleteForever = { onDeleteForever(note) },
                            modifier = Modifier.widthIn(max = 680.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    showPinIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showPinIcon) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
