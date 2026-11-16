package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.FolioDatabase
import com.example.data.NotesRepository
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesWorkspaceScreen
import com.example.ui.theme.FolioNotesTheme
import com.example.ui.viewmodel.NoteEditorViewModel
import com.example.ui.viewmodel.NotesViewModel

private const val ROUTE_WORKSPACE = "workspace"
private const val ROUTE_EDITOR_PATTERN = "editor?noteId={noteId}&isChecklist={isChecklist}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContext = applicationContext
        val database = FolioDatabase.getInstance(appContext)
        val notesRepository = NotesRepository(database.noteDao())
        val preferencesRepository = UserPreferencesRepository(appContext)

        setContent {
            val prefs by preferencesRepository.preferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences()
            )

            FolioNotesTheme(
                themePreference = prefs.themePreference,
                dynamicColor = prefs.dynamicColor
            ) {
                val navController = rememberNavController()
                val workspaceViewModel: NotesViewModel = viewModel(
                    factory = remember {
                        NotesViewModel.Factory(notesRepository, preferencesRepository)
                    }
                )

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_WORKSPACE
                ) {
                    composable(ROUTE_WORKSPACE) {
                        NotesWorkspaceScreen(
                            viewModel = workspaceViewModel,
                            onOpenNote = { noteId ->
                                navController.navigate("editor?noteId=$noteId&isChecklist=false")
                            },
                            onCreateNote = { isChecklist ->
                                navController.navigate("editor?noteId=0&isChecklist=$isChecklist")
                            }
                        )
                    }

                    composable(
                        route = ROUTE_EDITOR_PATTERN,
                        arguments = listOf(
                            navArgument("noteId") {
                                type = NavType.LongType
                                defaultValue = 0L
                            },
                            navArgument("isChecklist") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                        val isChecklist = backStackEntry.arguments?.getBoolean("isChecklist") ?: false
                        val editorViewModel: NoteEditorViewModel = viewModel(
                            key = "editor_${noteId}_$isChecklist",
                            factory = NoteEditorViewModel.Factory(
                                noteId = noteId,
                                startAsChecklist = isChecklist,
                                repository = notesRepository
                            )
                        )
                        NoteEditorScreen(
                            viewModel = editorViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
