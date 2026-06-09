package com.viv3k.filehive.ui.screens.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.R
import com.viv3k.filehive.data.database.LockedFolderEntity
import com.viv3k.filehive.ui.components.FileThumbnail
import com.viv3k.filehive.ui.utils.formatTimestamp
import java.io.File

@Composable
fun VaultScreen(
    onNavigateBack: () -> Unit,
    viewModel: VaultViewModel = viewModel(factory = VaultViewModel.Factory)
) {
    val lockedFolders by viewModel.lockedFolders.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // Auto-lock when going to background
                onNavigateBack()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .statusBarsPadding()
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Locked Folders",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            IconButton(onClick = { /* Settings / Reset PIN? */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Protected Status Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFF1B2524),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shield), // or appropriate icon
                    contentDescription = "Protected",
                    tint = Color(0xFF13C296),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Protected",
                        color = Color(0xFF13C296),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${lockedFolders.size} folders secured",
                        color = Color(0xFF9AA0A6),
                        fontSize = 14.sp
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Locked",
                    tint = Color(0xFF13C296),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lockedFolders, key = { it.id }) { folder ->
                LockedFolderItem(
                    folder = folder,
                    onUnlockClick = { viewModel.unlockFolder(folder) }
                )
            }
        }
    }
}

@Composable
fun LockedFolderItem(
    folder: LockedFolderEntity,
    onUnlockClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF1A1C21)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder Icon + small lock badge
            Box(contentAlignment = Alignment.BottomEnd) {
                // We're mocking the thumbnail using the original file path
                FileThumbnail(
                    file = File(folder.originalPath),
                    modifier = Modifier.size(48.dp),
                    iconSize = 48.dp
                )
                // Small lock badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFF13C296), CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = null,
                        tint = Color(0xFF1A1C21),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.folderName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${folder.itemsCount} items • Locked ${formatTimestamp(folder.lockedTime)}",
                    color = Color(0xFF9AA0A6),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Unlock Button or Icon
            IconButton(
                onClick = onUnlockClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2B2D31), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shield), // Replace with unlock.xml when needed
                    contentDescription = "Unlock",
                    tint = Color(0xFF13C296),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
