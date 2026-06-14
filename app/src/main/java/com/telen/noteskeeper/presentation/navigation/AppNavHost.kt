package com.telen.noteskeeper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.telen.noteskeeper.presentation.notes.NotesScreen
import com.telen.noteskeeper.presentation.options.OptionsScreen
import com.telen.noteskeeper.presentation.subnotedetail.SubNoteDetailScreen
import com.telen.noteskeeper.presentation.subnotes.SubNotesScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = NotesRoute,
    ) {
        composable<NotesRoute> {
            NotesScreen(
                onNavigateToSubNotes = { noteId ->
                    navController.navigate(SubNotesRoute(noteId))
                },
                onNavigateToOptions = {
                    navController.navigate(OptionsRoute)
                },
            )
        }
        composable<SubNotesRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SubNotesRoute>()
            SubNotesScreen(
                noteId = route.noteId,
                onNavigateToSubNoteDetail = { subNoteId ->
                    navController.navigate(SubNoteDetailRoute(subNoteId))
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<SubNoteDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SubNoteDetailRoute>()
            SubNoteDetailScreen(
                subNoteId = route.subNoteId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<OptionsRoute> {
            OptionsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
