package com.viv3k.filehive.ui.screens.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
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
import com.viv3k.filehive.ui.components.StorageChip

data class RecentFile(
    val name: String,
    val path: String,
    val size: String,
    val iconRes: Int
)

@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onOpenLocalStorage: (String) -> Unit,
    onRecycleBinClick: () -> Unit,
    onLockedClick: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val storageStats = remember {
        if (isPreview) {
            // Dummy data just for preview
            StorageStats(
                totalBytes = 128L * 1024 * 1024 * 1024,
                usedBytes = 100L * 1024 * 1024 * 1024,
                usedPercent = 50f,
                freeBytes = 64L * 1024 * 1024 * 1024
            )
        } else {
            readInternalStorageStats()
        }
    }

    // val storageStats = remember { readInternalStorageStats() }
    val usedText = storageStats.usedBytes.toGbString(2)              // 64.20 GB
    val totalText = storageStats.totalBytes.toGbString(0)            // 128 GB
    val percent = storageStats.usedPercent
    val progress = (percent / 100f).coerceIn(0f, 1f)

    val availableStorages = remember {
        if (isPreview) {
            listOf(
                DeviceStorage("internal", "Internal Storage", "/storage/emulated/0"),
                DeviceStorage("sd", "Memory Card", "/storage/XXXX-XXXX"),
//                DeviceStorage("USB", "USB Card", "/storage/XXXX-XXXX")
            )
        } else {
            getAvailableStorageVolumes(context)
                .filter { getStorageStatsForPath(it.path).totalBytes > 0L }
        }
    }

    val quickAccessItems = remember {
        listOf(
            QuickAccessItem("Documents", R.drawable.category_documents, Color(0xFF0F1115)),
            QuickAccessItem("Images", R.drawable.category_images, Color(0xFF0F1115)),
            QuickAccessItem("Audio", R.drawable.category_audio, Color(0xFF0F1115)),
            QuickAccessItem("Videos", R.drawable.category_videos, Color(0xFF0F1115)),
            QuickAccessItem("Archives", R.drawable.category_archives, Color(0xFF0F1115)),
            QuickAccessItem("Downloads", R.drawable.category_downloads, Color(0xFF0F1115)),
            QuickAccessItem("Locked", R.drawable.category_locked, Color(0xFF0F1115)),
            QuickAccessItem("Recycle", R.drawable.category_recycle, Color(0xFF0F1115))
        )
    }


    val recentFiles = remember {
        listOf(
            RecentFile("ProjectPlan.docx", "/Documents/Work", "1.2 MB", R.drawable.category_documents),
            RecentFile("Vacation.jpg", "/Pictures/Trips", "3.4 MB", R.drawable.category_images),
            RecentFile("Clip.mp4", "/Videos", "120 MB", R.drawable.category_videos),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070A))
            .statusBarsPadding()
            .padding(12.dp)
    ) {

        // Main storage card (big one)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0F15))
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth().
                    padding(8.dp)
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "File Hive 📂",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Image(
                    painter = painterResource(id = R.drawable.more_vertical),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular storage indicator
                Box(
                    modifier = Modifier
                        .size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressArc(
                        size = 220.dp,
                        progress = progress,
                        stroke = 8.dp
                    )

                    // inner circle with folder + text
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF12151C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                color = Color.White
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

                Spacer(modifier = Modifier.height(20.dp))

                // Internal & SD cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    availableStorages.forEachIndexed { index, device ->
                        val stats = remember(device.path) { getStorageStatsForPath(device.path) }
                        val used = stats.usedBytes.toGbString(2)
                        val total = stats.totalBytes.toGbString(0)

                        StorageChip(
                            modifier = Modifier.weight(1f),
                            title = device.label,
                            subtitle = "$used / $total",
                            iconRes = R.drawable.category_sdcard,
                            onClick = { onOpenLocalStorage(device.path) } // pass selected path
                        )
                    }
                }


                // Whole card clickable to open file browser
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Access Section
        Text(
            text = "Quick Access",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickAccessGrid(items = quickAccessItems, onItemClick = { item ->
                if (item.label == "Recycle") {
                    onRecycleBinClick()
                } else if (item.label == "Locked") {
                    onLockedClick()
                } else {
                    onFolderClick(item.label)
                }
            })

            Text(
                text = "Recent Files",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            recentFiles.forEach { file ->
                RecentFileRow(file)
            }
        }
    }
}




@Composable
private fun RecentFileRow(file: RecentFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoRipple { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0F15))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF151922)),
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = file.path,
                    fontSize = 12.sp,
                    color = Color(0xFF7F8691)
                )
            }

            Text(
                text = file.size,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB0B6BD)
            )
        }
    }
}


// Simple click wrapper without ripple (optional)
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
        onLockedClick = {}
    )
}
