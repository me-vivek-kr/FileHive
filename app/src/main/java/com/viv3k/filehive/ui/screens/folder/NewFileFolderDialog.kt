package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.utils.soft

@Composable
fun NewFileFolderDialog(
    onDismissRequest: () -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit
){

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ){
        CreateNewDialogContent(
            onDismissRequest = onDismissRequest,
            onNewFileClick = onNewFileClick,
            onNewFolderClick = onNewFolderClick
        )
    }
}

@Composable
private fun CreateNewDialogContent(
    onDismissRequest: () -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(340.dp)
            .shadow(
                elevation = 34.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = Color(0xFF2F2ACF).copy(alpha = 0.24f),
                spotColor = Color(0xFF2F2ACF).copy(alpha = 0.34f)
            )
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White)
            .padding(horizontal = 28.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create New",
            color = Color(0xFF111827),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select an item to add",
            color = Color(0xFF6B7280),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreateNewOptionCard(
                title = "New Folder",
                icon = R.drawable.folder_plus,
                modifier = Modifier.weight(1f),
                onClick = onNewFolderClick
            )
            CreateNewOptionCard(
                title = "New File",
                icon = R.drawable.file_add,
                modifier = Modifier.weight(1f),
                onClick = onNewFileClick
            )
        }

        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = "Cancel",
            color = Color(0xFF4B35D3),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismissRequest() }
        )
    }
}

@Composable
private fun CreateNewOptionCard(
    title: String,
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(140.dp)
            .soft(
                shape = RoundedCornerShape(18.dp),
                cornerRadius = 18.dp,
                backgroundColor = Color.White,
                blurRadius = 18.dp,
                offsetY = 8.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFDCE8FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFF3B35D8),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            color = Color(0xFF111827),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun PreviewNewFileFolderDialog(){
    Box(
        modifier = Modifier
            .background(Color(0xFFF7F9FB))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CreateNewDialogContent(
            onDismissRequest = {},
            onNewFileClick = {},
            onNewFolderClick = {}
        )
    }
}