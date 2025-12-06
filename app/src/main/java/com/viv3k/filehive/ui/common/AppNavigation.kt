package com.viv3k.filehive.ui.common

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viv3k.filehive.ui.local.LocalScreen
import com.viv3k.filehive.ui.screens.FolderScreen
import com.viv3k.filehive.ui.screens.HomeScreen
import com.viv3k.filehive.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onStartClick = { navController.navigate("home") })
        }

        composable("home") {
            HomeScreen(
                onOpenLocalStorage = {
                    val rootPath = "/storage/emulated/0"
                    val encoded = Uri.encode(rootPath)
                    navController.navigate("folderPath/$encoded")
                },
                onFolderClick = { /* your other usage if needed */ }
            )
        }

        // Local root listing
        composable("local") {
            LocalScreen(
                onFolderClick = { path ->
                    // ✅ ENCODE before putting into route
                    val encoded = Uri.encode(path)
                    navController.navigate("folderPath/$encoded")
                }
            )
        }

        // Real filesystem folder route
        composable(
            route = "folderPath/{folderPath}",
            arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
        ) { backStackEntry ->
            // ✅ DECODE before using as File path
            val folderPath = backStackEntry.arguments
                ?.getString("folderPath")
                ?.let { Uri.decode(it) }
                ?: ""

            FolderScreen(
                folderPath = folderPath,
                onBackClick = { navController.popBackStack() },
                onFolderClick = { newPath ->
                    val enc = Uri.encode(newPath)
                    navController.navigate("folderPath/$enc")
                }
            )
        }
    }
}
