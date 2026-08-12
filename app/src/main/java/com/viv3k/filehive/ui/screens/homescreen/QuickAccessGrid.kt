package com.viv3k.filehive.ui.screens.homescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.utils.NeomorphicIcon
import com.viv3k.filehive.ui.utils.soft

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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chunk items into rows of 2 (as seen in the image)
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    QuickAccessCard(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .height(136.dp)
                            .clickableNoRipple { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun QuickAccessCard(
    item: QuickAccessItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .soft(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                backgroundColor = Color.White
            )
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NeomorphicIcon(
                size = 56.dp,
                backgroundColor = item.backgroundColor.copy(alpha = 0.1f),
                icon = {
                    Image(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.label,
                modifier = Modifier.fillMaxWidth()
                    .basicMarquee(iterations = Int.MAX_VALUE),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
)

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun PreviewGrid() {
    val previewItems = listOf(
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

    Box(modifier = Modifier.padding(16.dp)) {
        QuickAccessGrid(
            items = previewItems,
            onItemClick = {}
        )
    }
}
