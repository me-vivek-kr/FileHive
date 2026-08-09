package com.viv3k.filehive.core.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.path
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.viv3k.filehive.ui.screens.LocalScreen
import com.viv3k.filehive.ui.screens.folder.FolderScreen
import com.viv3k.filehive.ui.screens.homescreen.HomeScreen
import com.viv3k.filehive.ui.screens.SplashScreen
import com.viv3k.filehive.ui.screens.recyclebin.RecycleBinScreen
import com.viv3k.filehive.ui.screens.search.SearchScreen
import com.viv3k.filehive.ui.screens.settings.SettingsScreen
import com.viv3k.filehive.ui.screens.vault.LockAuthScreen
import com.viv3k.filehive.ui.screens.vault.VaultScreen
import com.viv3k.filehive.ui.screens.viewer.ImageViewerScreen


@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {

        composable<Screen.Splash> {
            SplashScreen(onStartClick = { navController.navigate(Screen.Home) })
        }

        composable<Screen.Home> {
            HomeScreen(
                onOpenLocalStorage = { rootPath ->
                    navController.navigate(Screen.Folder(path = rootPath))
                },
                onFolderClick = { },
                onRecycleBinClick = { navController.navigate(Screen.RecycleBin) },
                onLockedClick = { navController.navigate(Screen.LockAuth) },
                onSearchClick = { navController.navigate(Screen.Search) },
                onSettingsClick = { navController.navigate(Screen.Settings) }
            )
        }

        composable<Screen.Local> {
            LocalScreen(
                onFolderClick = { path ->
                    navController.navigate(Screen.Folder(path = path))
                }
            )
        }

        composable<Screen.Folder> { backStackEntry ->

            val folderArgs = backStackEntry.toRoute<Screen.Folder>()

            FolderScreen(
                folderPath = folderArgs.path,
                onBackClick = { navController.popBackStack() },
                onFolderClick = { newPath ->
                    navController.navigate(Screen.Folder(path = newPath))
                },
                onImageClick = { path ->
                    navController.navigate(Screen.ImageViewer(filePath = path))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search)
                },
                onRecycleBinClick = {
                    navController.navigate(Screen.RecycleBin)
                },
                onHomeClick = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.Search> {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Home> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onFilesClick = {
                    navController.navigate(Screen.Folder(path = "/storage/emulated/0"))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.RecycleBin> {
            RecycleBinScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateTo = { route ->
                    if (route == "home") {
                        navController.navigate(Screen.Home) {
                            popUpTo<Screen.Home> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable<Screen.ImageViewer> { backStackEntry ->
            val viewerArgs = backStackEntry.toRoute<Screen.ImageViewer>()

            ImageViewerScreen(
                filePath = viewerArgs.filePath,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Screen.LockAuth>(
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn() },
            exitTransition = { fadeOut() },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut() }
        ) {
            LockAuthScreen(
                onNavigateBack = { navController.popBackStack() },
                onAuthSuccess = {
                    navController.navigate(Screen.Vault) {
                        popUpTo<Screen.LockAuth> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Vault>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            VaultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onStorageAnalyzerClick = { /* TODO */ },
                onRecycleBinClick = { navController.navigate(Screen.RecycleBin) },
                onVaultClick = { navController.navigate(Screen.LockAuth) }
            )
        }
    }
}
