package com.viv3k.filehive.ui.navigation

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viv3k.filehive.ui.screens.LocalScreen
import com.viv3k.filehive.ui.screens.folder.FolderScreen
import com.viv3k.filehive.ui.screens.homescreen.HomeScreen
import com.viv3k.filehive.ui.screens.SplashScreen
import com.viv3k.filehive.ui.screens.recyclebin.RecycleBinScreen
import com.viv3k.filehive.ui.screens.search.SearchScreen
import com.viv3k.filehive.ui.screens.viewer.ImageViewerScreen

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(onStartClick = { navController.navigate("home") })
        }

        composable("home") {
            HomeScreen(
                onOpenLocalStorage = { rootPath ->
                    val encoded = Uri.encode(rootPath)
                    navController.navigate("folderPath/$encoded")
                },
                onFolderClick = { },
                onRecycleBinClick = { navController.navigate("recycle_bin") }
            )
        }

        // Local root listing
        composable("local") {
            LocalScreen(
                onFolderClick = { path ->
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
                },
                onImageClick = { path ->
                    val encodedPath = Uri.encode(path)
                    navController.navigate("imageViewer/$encodedPath")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                onRecycleBinClick = {
                    navController.navigate("recycle_bin")
                },
                onHomeClick = {
                    navController.navigate("home") {

                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("recycle_bin") {
            RecycleBinScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateTo = { route ->
                    if (route == "home") {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }

            )
        }

        composable(
            route = "imageViewer/{filePath}",
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments
                ?.getString("filePath")
                ?.let { Uri.decode(it) } ?: ""

            ImageViewerScreen(
                filePath = filePath,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
