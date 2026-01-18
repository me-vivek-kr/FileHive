package com.viv3k.filehive.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

@Composable
fun BreadcrumbNavigation(
    folderPath: String,
    onPathClick: (String) -> Unit
){
    // Define the actual system paths
    val internalRootPath = "/storage/emulated/0"
    val storageBase = "/storage/"

    val pathSegments = remember(folderPath) {
        val segments = mutableListOf<Pair<String, String>>() // Name, FullPath

        when {
            // Case 1: Internal Storage
            folderPath.startsWith(internalRootPath) -> {
                segments.add("INTERNAL STORAGE" to internalRootPath)
                val subPath = folderPath.removePrefix(internalRootPath).trim('/')
                if (subPath.isNotEmpty()) {
                    val parts = subPath.split('/')
                    var currentPath = internalRootPath
                    parts.forEach { part ->
                        currentPath += "/$part"
                        segments.add(part.uppercase() to currentPath)
                    }
                }
            }

            // Case 2: External Storage (SD Card / USB)
            folderPath.startsWith(storageBase) -> {
                val relativeToStorage = folderPath.removePrefix(storageBase).trim('/')
                val parts = relativeToStorage.split('/')

                if (parts.isNotEmpty()) {
                    val volumeId = parts[0] // e.g., 89FA-1D01
                    val volumePath = "$storageBase$volumeId"
                    segments.add(volumeId.uppercase() to volumePath)

                    var currentPath = volumePath
                    for (i in 1 until parts.size) {
                        currentPath += "/${parts[i]}"
                        segments.add(parts[i].uppercase() to currentPath)
                    }
                }
            }

            // Case 3: Any other path (Fallback)
            else -> {
                val parts = folderPath.trim('/').split('/')
                var currentPath = ""
                parts.forEach { part ->
                    currentPath += "/$part"
                    segments.add(part.uppercase() to currentPath)
                }
            }
        }
        segments
    }

    val listState = rememberLazyListState()

    LaunchedEffect(pathSegments.size) {
        if(pathSegments.isNotEmpty()){
            listState.animateScrollToItem(pathSegments.size - 1)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(pathSegments) { index, segment ->
            val isLast = index == pathSegments.size - 1

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = segment.first,
                    fontSize = 12.sp, // Slightly smaller to match your image
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                    // Use your theme teal color for the active folder, gray for others
                    color = if (isLast) Color(0xFF3e94a2) else Color(0xFF9AA0A6),
                    modifier = Modifier.clickable {
                        if (!isLast) onPathClick(segment.second)
                    }
                )

                if (!isLast) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_right),
                        contentDescription = null,
                        tint = Color(0xFF45494F),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewBreadcrumbNavigation(){
    val path = "Internal/Download/New Folder/0"
    BreadcrumbNavigation(
        folderPath = path,
        onPathClick = {}
    )
}