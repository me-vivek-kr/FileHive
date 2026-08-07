package com.viv3k.filehive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import java.util.Locale

@Composable
fun BreadcrumbNavigation(
    folderPath: String,
    onPathClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    if (part.isNotEmpty()) {
                        currentPath += "/$part"
                        segments.add(part.uppercase() to currentPath)
                    }
                }
            }
        }
        segments
    }

    val listState = rememberLazyListState()

    LaunchedEffect(pathSegments.size) {
        if (pathSegments.isNotEmpty()) {
            listState.animateScrollToItem(pathSegments.size)
        }
    }

    LazyRow(
        state = listState,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Home Button
        item {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                onClick = onHomeClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Home",
                        tint = Color(0xFF45494F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (pathSegments.isNotEmpty()) {
            item {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_right),
                    contentDescription = null,
                    tint = Color(0xFFBCC1C8),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        itemsIndexed(pathSegments) { index, segment ->
            val isLast = index == pathSegments.size - 1
            val name = segment.first.lowercase(Locale.ROOT)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLast) {
                    // Active segment (last): Recessed pill with blue text
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFDDE1E7),
                                        Color(0xFFEBEDF2)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5051D8),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    // Inactive segments: Elevated white pill
                    Surface(
                        onClick = { onPathClick(segment.second) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.height(42.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF45494F),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (!isLast) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_right),
                        contentDescription = null,
                        tint = Color(0xFFBCC1C8),
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(12.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F2F5)
@Composable
fun PreviewBreadcrumbNavigation() {
    BreadcrumbNavigation(
        folderPath = "/storage/emulated/0/Download",
        onPathClick = {},
        onHomeClick = {}
    )
}