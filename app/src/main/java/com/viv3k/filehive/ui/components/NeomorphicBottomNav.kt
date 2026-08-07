package com.viv3k.filehive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.viv3k.filehive.R

@Composable
fun NeomorphicBottomNav(
    activeTab: String = "home",
    onTabClick: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFFF0F2F5),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "home" to (R.drawable.home to "Home"),
                "files" to (R.drawable.file to "Files"),
                "search" to (R.drawable.search to "Search"),
                "settings" to (R.drawable.settings to "Settings")
            )

            tabs.forEach { (id, data) ->
                val (iconRes, label) = data
                val isActive = activeTab == id

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabClick(id) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF5051D8) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                            tint = if (isActive) Color.White else Color(0xFF9BA3AF),
                            modifier = Modifier.size(26.dp)
                        )
                    }

//                    Text(
//                        text = label,
//                        color = if (isActive) Color(0xFF5051D8) else Color(0xFF9BA3AF),
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewNeomorphicBottomNav() {
    NeomorphicBottomNav()
}