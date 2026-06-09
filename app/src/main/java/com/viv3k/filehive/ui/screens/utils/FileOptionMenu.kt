package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

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
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset((-12).dp, 0.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF16181D),
        modifier = Modifier.width(220.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Cut", color = Color.White, fontWeight = FontWeight.Normal) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.scissors),
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = {
                onCutClick()
                onDismissRequest()
            }
            // modifier = Modifier.background(...) <--- REMOVED THIS
        )

        // 2. Copy
        DropdownMenuItem(
            text = { Text("Copy", color = Color.White) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = null,
                    tint = Color.White
                )
            },
            onClick = {
                onCopyClick()
                onDismissRequest()
            }
        )

        // 3. Delete (Red Text)
        DropdownMenuItem(
            text = { Text("Delete", color = Color(0xFFD32F2F)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.trash),
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            onClick = {
                onDeleteClick()
                onDismissRequest()
            }
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        // 4. Other Standard Items
        FileOptionItem("Share", R.drawable.share, onDismissRequest)
        FileOptionItem(
            "Rename",
            R.drawable.rename,
            onDismissRequest,
            onClick = onRenameClick
        )
        FileOptionItem("Archive", R.drawable.archive, onDismissRequest)

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        FileOptionItem("Hide", R.drawable.eye_off, onDismissRequest)
        FileOptionItem(
            "Lock / Vault", 
            R.drawable.lock, 
            onDismissRequest,
            onClick = {
                onLockClick()
            }
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        FileOptionItem("Open Directory", R.drawable.folder_open, onDismissRequest)
        FileOptionItem("Transfer", R.drawable.file_up, onDismissRequest)
        FileOptionItem("Add to Favourite", R.drawable.heart, onDismissRequest)
        FileOptionItem("Create Shortcut", R.drawable.arrow_right, onDismissRequest)

        HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

        FileOptionItem("Properties", R.drawable.info, onDismissRequest)
    }
}

@Composable
fun FileOptionItem(
    text: String,
    iconRes: Int,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit = {}
) {
    DropdownMenuItem(
        text = { Text(text, color = Color.White, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White
            )
        },
        onClick = {
            onClick()
            onDismissRequest()
        }
    )
}

@Preview
@Composable
fun PreviewFileOption(){
    FileOptionsDropdownMenu(
        expanded = true,
        onDismissRequest = {},
        onCutClick = {},
        onCopyClick = {},
        onDeleteClick = {},
        onRenameClick = {},
        onLockClick = {}
    )
}