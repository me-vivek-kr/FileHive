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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

data class QuickAccessItem(
    val label: String,
    val iconRes: Int,
    val backgroundColor: Color
)

@Composable
fun QuickAccessGrid(
    items: List<QuickAccessItem>,
    onItemClick: (QuickAccessItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chunk items into rows of 4
        items.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Render existing items
                rowItems.forEach { item ->
                    QuickAccessCard(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .clickableNoRipple { onItemClick(item) }
                    )
                }

                // Fill empty space if the last row has fewer than 4 items
                val emptySlots = 4 - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(item.backgroundColor) // ✅ USE IT
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.label,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(38.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8A8A8E),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}


// Helper for clickable without ripple (optional, keeps UI clean)
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewGrid() {
    val previewItems = listOf(
        QuickAccessItem("Documents", R.drawable.category_documents, Color(0xFF0F1115)),
        QuickAccessItem("Images", R.drawable.category_images, Color(0xFF0F1115)),
        QuickAccessItem("Audio", R.drawable.category_audio, Color(0xFF0F1115)),
        QuickAccessItem("Videos", R.drawable.category_videos, Color(0xFF0F1115)),
        QuickAccessItem("Archives", R.drawable.category_archives, Color(0xFF0F1115)),
        QuickAccessItem("Downloads", R.drawable.category_downloads, Color(0xFF0F1115)),
        QuickAccessItem("Locked", R.drawable.category_locked, Color(0xFF0F1115)),
        QuickAccessItem("Recycle", R.drawable.category_recycle, Color(0xFF0F1115))
    )

    Box(modifier = Modifier.padding(16.dp)) {
        QuickAccessGrid(
            items = previewItems,
            onItemClick = {}
        )
    }
}

