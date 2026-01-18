package com.viv3k.filehive.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Splash : Screen
    @Serializable data object Home : Screen
    @Serializable data object Local : Screen
    @Serializable data object Search : Screen
    @Serializable data object RecycleBin : Screen

    @Serializable
    data class Folder(val path: String) : Screen

    @Serializable
    data class ImageViewer(val filePath: String) : Screen
}