package com.viv3k.filehive.ui.screens.recyclebin

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.R

enum class FileType { IMAGE, ARCHIVE, DOCUMENT, VIDEO, AUDIO }

@Composable
fun RecycleBinScreen(
    onBackClick: () -> Unit = {},
    onNavigateTo: (String) -> Unit = {},
    viewModel: RecycleBinViewModel = viewModel()

) {
    val binItems by viewModel.binItems.collectAsState()

    // Reload data when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadBinItems()
    }

    var showEmptyDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    var itemToDeleteForever by remember { mutableStateOf<RealBinItem?>(null) }
    var itemToRestore by remember { mutableStateOf<RealBinItem?>(null) }

    val isDialogOpen = showEmptyDialog || showRestoreDialog || itemToDeleteForever != null || itemToRestore != null

    var menuExpanded by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1115))
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    if (isDialogOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val blur = 10f
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(
                                blur,
                                blur,
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                }
        ) {
            //Top Bar
            RecycleBinTopBar(
                onBackClick = onBackClick,
                expanded = menuExpanded,
                onExpandChange = { menuExpanded = it },
                onNavigateTo = onNavigateTo
            )

            Spacer(modifier = Modifier.height(24.dp))

            //Info Banner (Auto-delete)
            AutoDeleteBanner()

            Spacer(modifier = Modifier.height(24.dp))

            //Action Buttons (Restore / Empty)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Restore All Button
                RecycleActionButton(
                    text = "Restore All",
                    icon = R.drawable.clock,
                    textColor = Color.White,
                    backgroundColor = Color(0xFF1A1C21),
                    modifier = Modifier.weight(1f),
                    onClick = { if(binItems.isNotEmpty()) showRestoreDialog = true }

                )

                //Empty Bin Button
                RecycleActionButton(
                    text = "Empty Bin",
                    icon = R.drawable.trash,
                    textColor = Color(0xFFEF5350), // Red Text
                    backgroundColor = Color(0xFF2B1C1C), // Dark Red/Brown background tint
                    modifier = Modifier.weight(1f),
                    onClick = { if(binItems.isNotEmpty()) showEmptyDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            //List of Deleted Files
            if (binItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Recycle bin is empty", color = Color(0xFF9AA0A6))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(binItems) { item ->
                        RecycleBinItem(
                            item = item,
                            onRestoreClick = {
                                viewModel.restoreFile(item)
                            },
                            onDeleteForeverClick = {
                                itemToDeleteForever = item // Triggers the Confirmation Dialog
                            },
                            onPropertiesClick = {
                                /* Implement Properties Dialog if needed */
                            }
                        )
                    }
                    // Add spacer at the end for better scrolling visibility
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showEmptyDialog) {
        RecycleConfirmationDialog(
            title = "Empty Recycle Bin?",
            subtitle = "This action cannot be undone",
            message = "All ${binItems.size} files will be permanently deleted.",
            confirmText = "Delete All",
            confirmColor = Color(0xFFD32F2F), // Red for delete
            iconRes = R.drawable.alert_triangle, // Or standard Warning icon
            isWarning = true,
            onDismiss = { showEmptyDialog = false },
            onConfirm = {
                viewModel.emptyBin()
            }
        )
    }

    if (showRestoreDialog) {
        RecycleConfirmationDialog(
            title = "Restore All Files?",
            subtitle = "Files will return to their original location",
            message = "Are you sure you want to restore ${binItems.size} files?",
            confirmText = "Restore",
            confirmColor = Color(0xFF3e94a2), // Teal for restore
            iconRes = R.drawable.clock, // Use appropriate restore icon
            isWarning = false,
            onDismiss = { showRestoreDialog = false },
            onConfirm = {
                viewModel.restoreAll()
                showRestoreDialog = false
            }
        )
    }

    // Single Item Delete Forever Confirmation
    if (itemToDeleteForever != null) {
        RecycleConfirmationDialog(
            title = "Delete Permanently?",
            subtitle = "This action cannot be undone",
            message = "Are you sure you want to delete \"${itemToDeleteForever?.displayName}\"?",
            confirmText = "Delete",
            confirmColor = Color(0xFFD32F2F),
            iconRes = R.drawable.alert_triangle,
            isWarning = true,
            onDismiss = { itemToDeleteForever = null },
            onConfirm = {
                itemToDeleteForever?.let { viewModel.deleteFilePermanently(it) }
                itemToDeleteForever = null
            }
        )
    }
}


@Composable
fun AutoDeleteBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF16181D)) // Dark Card Background
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Trash Icon Circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF2B1C1C)), // Dark Red Background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.trash),
                contentDescription = null,
                tint = Color(0xFFEF5350), // Red Icon
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Auto-delete enabled",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Files in recycle bin will be permanently deleted after 30 days",
                color = Color(0xFF9AA0A6), // Grey subtitle
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RecycleActionButton(
    text: String,
    icon: Int,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
fun PreviewRecycleBin() {
    RecycleBinScreen()
}