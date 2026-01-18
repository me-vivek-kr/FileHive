package com.viv3k.filehive.ui.screens.viewer

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.screens.utils.DeleteConfirmationDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    filePath: String,
    onBackClick: () -> Unit
) {
    val initialFile = remember { File(filePath) }
    val context = LocalContext.current

    // State for the list of images
    var imageFiles by remember { mutableStateOf(listOf(initialFile)) }
    var isLoading by remember { mutableStateOf(true) }

    // Controls visibility
    var controlsVisible by remember { mutableStateOf(true) }
    var isZoomed by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }


    // Load sibling images from the parent folder
    LaunchedEffect(initialFile) {
        withContext(Dispatchers.IO) {
            val parent = initialFile.parentFile
            if (parent != null && parent.exists()) {
                val extensions = listOf("jpg", "jpeg", "png", "webp", "bmp", "heic", "gif")
                val files = parent.listFiles()?.filter {
                    it.isFile && it.extension.lowercase() in extensions
                }?.sortedBy { it.name.lowercase() }

                if (files != null && files.isNotEmpty()) {
                    imageFiles = files
                }
            }
        }
        isLoading = false
    }

    val pagerState = rememberPagerState(pageCount = { imageFiles.size })

    // FIX 1: Scroll to the correct page once loading finishes
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            val index = imageFiles.indexOfFirst { it.absolutePath == initialFile.absolutePath }
            if (index >= 0) {
                pagerState.scrollToPage(index)
            }
        }
    }

    // Get the current file safely based on the pager state
    val currentFile = if (imageFiles.isNotEmpty()) {
        imageFiles[pagerState.currentPage.coerceIn(imageFiles.indices)]
    } else {
        initialFile
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            // Sliding Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isZoomed, // Allow swipe only when not zoomed
                key = { index -> imageFiles[index].absolutePath }
            ) { pageIndex ->
                val file = imageFiles[pageIndex]
                val isCurrentPage = pageIndex == pagerState.currentPage

                ZoomableImage(
                    file = file,
                    isCurrentPage = isCurrentPage,
                    onTap = { controlsVisible = !controlsVisible },
                    onZoomChange = { zoomed ->
                        if (isCurrentPage) isZoomed = zoomed
                    }
                )
            }
        }

        // Top Bar Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .statusBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Title / Counter
                if (imageFiles.size > 1) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${imageFiles.size}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { /* TODO: Menu Options */ },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Bar Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share
                ViewerActionItem(
                    icon = R.drawable.share,
                    label = "Share",
                    onClick = {
                        shareImage(context, currentFile)
                    }
                )

                // Save
                ViewerActionItem(
                    icon = R.drawable.download,
                    label = "Save",
                    onClick = { /* TODO */ }
                )

                // Info
                ViewerActionItem(
                    icon = R.drawable.info,
                    label = "Info",
                    onClick = { /* TODO: Show Details Dialog */ }
                )

                // Delete
                ViewerActionItem(
                    icon = R.drawable.trash,
                    label = "Delete",
                    color = Color(0xFFEF5350),
                    onClick = {
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && currentFile != null) {
        DeleteConfirmationDialog(
            fileName = currentFile.name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { isPermanent ->
                // Perform Deletion Logic
                val fileToDelete = currentFile
                val success = if (isPermanent) {
                    fileToDelete.delete()
                } else {
                    moveToRecycleBin(fileToDelete)
                }

                if (success) {
                    val updatedList = imageFiles.toMutableList()
                    updatedList.remove(fileToDelete)
                    imageFiles = updatedList

                    if (imageFiles.isEmpty()) {
                        onBackClick()
                    }
                    Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }

                showDeleteDialog = false
            }
        )
    }
}

fun moveToRecycleBin(file: File): Boolean {
    val recyclePath = File(Environment.getExternalStorageDirectory(), ".FileHiveBin")
    if (!recyclePath.exists()) recyclePath.mkdirs()

    val timestamp = System.currentTimeMillis()
    val newName = "${file.name}_$timestamp"
    val destFile = File(recyclePath, newName)
    val originalPath = file.absolutePath

    return try {
        if (file.renameTo(destFile)) {
            val metaFile = File(recyclePath, "$newName.repo")
            metaFile.writeText(originalPath)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

@Composable
fun ZoomableImage(
    file: File,
    isCurrentPage: Boolean,
    onTap: () -> Unit,
    onZoomChange: (Boolean) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Reset zoom if page is changed
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    // Notify parent about zoom state (to disable/enable Pager swipe)
    LaunchedEffect(scale) {
        onZoomChange(scale > 1.0f)
    }

    //Zoom Logic (Multi-touch)
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
    }

    //Pan Logic (One finger) - Only enabled when zoomed in
    val panModifier = if (scale > 1f) {
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val maxOffsetX = (size.width * (scale - 1)) / 2
                val maxOffsetY = (size.height * (scale - 1)) / 2

                val newOffset = offset + dragAmount
                offset = Offset(
                    x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                    y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                )
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
            // Apply zoom listener (2 fingers) always
            .transformable(state = transformableState)
            // Apply pan listener (1 finger) only when zoomed
            .then(panModifier)
    ) {
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

@Composable
fun ViewerActionItem(
    icon: Int,
    label: String,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = color, fontSize = 12.sp)
    }
}

fun shareImage(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val extension = file.extension.lowercase()
        val mimeType = android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension) ?: "image/*"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Image")

        if (context !is android.app.Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooser)

    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(
            context,
            "Error sharing file: ${e.message}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}