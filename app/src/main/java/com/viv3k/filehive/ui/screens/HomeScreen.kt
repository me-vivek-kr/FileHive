package com.viv3k.filehive.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.viv3k.filehive.ui.components.CircularProgressArc
import com.viv3k.filehive.ui.components.FolderItem
import com.viv3k.filehive.ui.utils.readStorageStats
import com.viv3k.filehive.ui.utils.toGbString

@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    onOpenLocalStorage: () -> Unit
) {

    val storageStats = remember { readStorageStats() }

    val totalText = storageStats.totalBytes.toGbString()
    val usedText = storageStats.usedBytes.toGbString()
    val percent = storageStats.usedPercent
    val percentText = "${percent.toInt()}%"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp)
            .statusBarsPadding()
    ) {


    // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "File Hive 📂",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                //Text(text = "Good morning", fontSize = 14.sp, color = Color(0xFF9AA0A6))
            }

            // circular profile picture
            Image(
                painter = painterResource(id = R.drawable.more_vert),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(30.dp)
            )
        }


        Spacer(modifier = Modifier.height(24.dp))


        // Storage Card
        Card(
            onClick = onOpenLocalStorage,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17181C))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Space", fontSize = 14.sp, color = Color(0xFFB0B6BD))
                    Text(
                        text = totalText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Used Space", fontSize = 14.sp, color = Color(0xFFB0B6BD))
                    Text(
                        text = usedText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }


                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    val percent = storageStats.usedPercent.toFloat()
                    val progress = (percent / 100f).coerceIn(0f, 1f)

                    CircularProgressArc(
                        size = 110.dp,
                        progress = progress,
                        stroke = 12.dp
                    )
                    Text(
                        text = percentText,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Folder grid (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                items(listOf("Photos", "Videos", "Audios", "Documents")) { name ->
                    FolderItem(name = name, onFolderClick = { onFolderClick(name) })
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onFolderClick = {},
        onOpenLocalStorage = {}
    )
}
