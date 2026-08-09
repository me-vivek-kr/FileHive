package com.viv3k.filehive.ui.screens.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.model.DeviceStorage
import com.viv3k.filehive.data.storage.StorageStats
import com.viv3k.filehive.data.storage.getAvailableStorageVolumes
import com.viv3k.filehive.data.storage.getStorageStatsForPath
import com.viv3k.filehive.data.storage.readInternalStorageStats
import com.viv3k.filehive.data.storage.toGbString
import com.viv3k.filehive.ui.components.CircularProgressArc
import com.viv3k.filehive.ui.components.NeomorphicBottomNav
import com.viv3k.filehive.ui.components.StorageChip
import com.viv3k.filehive.ui.utils.soft

data class RecentFile(
    val name: String,
    val path: String,
    val size: String,
    val modified: String,
    val iconRes: Int
)

@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onOpenLocalStorage: (String) -> Unit,
    onRecycleBinClick: () -> Unit,
    onLockedClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val storageStats = remember {
        if (isPreview) {
            StorageStats(
                totalBytes = 512L * 1024 * 1024 * 1024,
                usedBytes = 358L * 1024 * 1024 * 1024,
                usedPercent = 70f,
                freeBytes = 154L * 1024 * 1024 * 1024
            )
        } else {
            readInternalStorageStats()
        }
    }

    val usedText = storageStats.usedBytes.toGbString(2)
    val totalText = storageStats.totalBytes.toGbString(0)
    val percent = storageStats.usedPercent
    val progress = (percent / 100f).coerceIn(0f, 1f)

    val availableStorages = remember {
        if (isPreview) {
            listOf(
                DeviceStorage("internal", "Internal Storage", "/storage/emulated/0"),
                DeviceStorage("sd", "SD Card", "/storage/XXXX-XXXX"),
            )
        } else {
            getAvailableStorageVolumes(context)
                .filter { getStorageStatsForPath(it.path).totalBytes > 0L }
        }
    }

    val quickAccessItems = remember {
        listOf(
            QuickAccessItem("Documents", R.drawable.category_documents, Color(0xFF6366F1)),
            QuickAccessItem("Images", R.drawable.category_images, Color(0xFF10B981)),
            QuickAccessItem("Videos", R.drawable.category_videos, Color(0xFFE74C1A)),
            QuickAccessItem("Archives", R.drawable.category_archives, Color(0xFF791EA0)) ,
            QuickAccessItem("Downloads", R.drawable.category_downloads, Color(0xFF00AABE)),
            QuickAccessItem("Locked", R.drawable.category_locked, Color(0xFF485C67)),
            QuickAccessItem("Recycle Bin", R.drawable.category_recycle, Color(0xFF6D8794)),
            QuickAccessItem("Favorites", R.drawable.category_favorites, Color(0xFFEF4444)),
            QuickAccessItem("Applications", R.drawable.category_apps, Color(0xFFFDB64C))
        )
    }

    val recentFiles = remember {
        listOf(
            RecentFile("Q4_Financial_Report.pdf", "/Documents", "2.4 MB", "2 hours ago", R.drawable.pdf_file),
            RecentFile("Design_Assets_v2.png", "/Pictures", "5.1 MB", "yesterday", R.drawable.image_file),
            RecentFile("Project_Nebula_Backup", "/Archives", "Folder", "Oct 24", R.drawable.folder_new),
        )
    }

    var isExpanded by remember { mutableStateOf(false) }
    val itemsToShow = if (isExpanded) quickAccessItems else quickAccessItems.take(6)

    val backgroundColor = Color(0xFFF4F6FA)

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Space for bottom nav
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                Box(
//                    modifier = Modifier
//                        .size(40.dp)
//                        .soft(shape = CircleShape, cornerRadius = 20.dp, backgroundColor = Color.White),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.cloud),
//                        contentDescription = "Cloud",
//                        modifier = Modifier.size(20.dp)
//                    )
//                }

                Text(
                    text = "File Hive",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3282)
                )

//                Spacer(modifier = Modifier.size(40.dp)) // To keep title centered
            }

            // Main Storage Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .soft(
                        shape = RoundedCornerShape(40.dp),
                        cornerRadius = 40.dp,
                        backgroundColor = Color.White
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressArc(
                            size = 220.dp,
                            progress = progress,
                            stroke = 8.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .background(color = Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.folder_new),
                                    contentDescription = "Folder",
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Used Storage",
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B6BD)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = usedText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "of $totalText",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757B84)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        availableStorages.forEach { device ->
                            val stats = remember(device.path) { getStorageStatsForPath(device.path) }
                            val used = stats.usedBytes.toGbString(0)
                            val total = stats.totalBytes.toGbString(0)

                            StorageChip(
                                title = if (device.label.contains("Internal")) "Internal Storage" else "SD Card",
                                subtitle = "$used GB / $total GB",
                                iconRes = if (device.label.contains("Internal")) R.drawable.list else R.drawable.file_text,
                                onClick = { onOpenLocalStorage(device.path) }
                            )
                        }
                    }
                }
            }

            // Quick Access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Access",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = if (isExpanded) "Show Less" else "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                QuickAccessGrid(items = itemsToShow, onItemClick = { item ->
                    onFolderClick(item.label)
                })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Activity
            Text(
                text = "Recent Activity",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recentFiles.forEach { file ->
                    RecentActivityRow(file)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            NeomorphicBottomNav(
                activeTab = "home",
                onTabClick = { tab ->
                    when (tab) {
                        "settings" -> onSettingsClick()
                        "search" -> onSearchClick()
                        "home" -> {}
                        "files" -> onOpenLocalStorage("/storage/emulated/0")
                    }
                }
            )
        }
    }
}

@Composable
private fun RecentActivityRow(file: RecentFile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .soft(
                shape = RoundedCornerShape(50.dp),
                cornerRadius = 50.dp,
                backgroundColor = Color.White
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EBF0))
                    .border(
                        width = 1.dp,
                        brush = verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = file.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Modified ${file.modified} • ${file.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF9BA3AF)
                )
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = Color(0xFF9BA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onFolderClick = {},
        onOpenLocalStorage = {},
        onRecycleBinClick = {},
        onLockedClick = {},
        onSearchClick = {},
        onSettingsClick = {}
    )
}
