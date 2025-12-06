package com.viv3k.filehive.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

@Composable
fun FolderItem(name: String, onFolderClick: () -> Unit) {
    Card(
        onClick = onFolderClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17181C)),
        modifier = Modifier
            .height(140.dp)
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "Folder",
                tint = Color.Unspecified,
                modifier = Modifier.size(35.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "1,342", fontSize = 12.sp, color = Color(0xFF9AA0A6))
            Spacer(modifier = Modifier.weight(1f))
            LinearProgressIndicator(progress = 0.6f, modifier = Modifier.fillMaxWidth())
        }
    }
}