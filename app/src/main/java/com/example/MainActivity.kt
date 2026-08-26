package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val appListViewModel: AppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AppNavHost(
                    activity = this,
                    settingsViewModel = settingsViewModel,
                    appListViewModel = appListViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavHost(
    activity: FragmentActivity,
    settingsViewModel: SettingsViewModel,
    appListViewModel: AppListViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "desktop") {
        composable("desktop") {
            DesktopScreen(
                activity = activity,
                viewModel = appListViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                settingsViewModel = settingsViewModel,
                appListViewModel = appListViewModel
            )
        }
    }
}
