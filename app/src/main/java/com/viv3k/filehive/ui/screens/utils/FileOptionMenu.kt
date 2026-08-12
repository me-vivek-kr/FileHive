package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.viv3k.filehive.ui.utils.NeomorphicSwitch

@Composable
fun FileOptionsDropdownMenu (
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onCutClick: ()-> Unit,
    onCopyClick: ()-> Unit,
    onDeleteClick: ()-> Unit,
    onRenameClick: () -> Unit,
    onLockClick: () -> Unit = {}
){
    var lockEnabled by remember { mutableStateOf(false) }

    if (!expanded) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() }
        ) {
            FileOptionsMenuContent(
                lockEnabled = lockEnabled,
                onLockChange = { checked ->
                    lockEnabled = checked
                    onLockClick()
                },
                onDismissRequest = onDismissRequest,
                onCutClick = onCutClick,
                onCopyClick = onCopyClick,
                onDeleteClick = onDeleteClick,
                onRenameClick = onRenameClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp)
            )
        }
    }
}

@Composable
private fun FileOptionsMenuContent(
    lockEnabled: Boolean,
    onLockChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onCutClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(250.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.28f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            FileOptionItem("Open", R.drawable.arrow_up_right, onDismissRequest)
            FileOptionItem("Share", R.drawable.share, onDismissRequest)
            MenuDivider()

            FileOptionItem("Cut", R.drawable.scissors, onDismissRequest, onCutClick)
            FileOptionItem("Copy", R.drawable.copy, onDismissRequest, onCopyClick)
            FileOptionItem("Move", R.drawable.move, onDismissRequest)
            FileOptionItem("Rename", R.drawable.rename, onDismissRequest, onRenameClick)
            MenuDivider()

            FileOptionItem("Pin to Quick Access", R.drawable.shortcut, onDismissRequest)
            FileOptionItem("Archive", R.drawable.archive, onDismissRequest)
            MenuDivider()

            LockFolderOption(
                checked = lockEnabled,
                onCheckedChange = onLockChange
            )
            MenuDivider()

            FileOptionItem(
                text = "Delete",
                iconRes = R.drawable.trash,
                onDismissRequest = onDismissRequest,
                onClick = onDeleteClick,
                contentColor = Color(0xFFE53935)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFF6F7FA))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "×",
                    color = Color(0xFF6B7280),
                    fontSize = 22.sp
                )
                Text(
                    text = "  CLOSE",
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FileOptionItem(
    text: String,
    iconRes: Int,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit = {},
    contentColor: Color = Color(0xFF1F2937)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
                onDismissRequest()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun LockFolderOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.lock),
            contentDescription = null,
            tint = Color(0xFF374151),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Lock Folder",
            color = Color(0xFF1F2937),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 9.dp)
                .weight(1f)
        )
        NeomorphicSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = Color(0xFFE8EBF0)
    )
}

@Preview
@Composable
fun PreviewFileOption(){
    Box(
        modifier = Modifier
            .background(Color(0xFFF7F9FB))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        FileOptionsMenuContent(
            lockEnabled = false,
            onLockChange = {},
            onDismissRequest = {},
            onCutClick = {},
            onCopyClick = {},
            onDeleteClick = {},
            onRenameClick = {}
        )
    }
}