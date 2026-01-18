package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.screens.folder.MenuItemData

@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onNewClick: () -> Unit
){
    val menuItems = listOf(
        MenuItemData("Home", R.drawable.home, Color(0xFF3e94a2)),         // Teal
        MenuItemData("New", R.drawable.plus, Color(0xFFFFA000)), // Orange/Gold
        MenuItemData("Upload", R.drawable.upload, Color(0xFF4CAF50)),     // Green
        MenuItemData("Locked", R.drawable.lock, Color(0xFF9C27B0)),       // Purple
        MenuItemData("Recycle Bin", R.drawable.trash, Color(0xFFF44336)), // Red
        MenuItemData("Settings", R.drawable.settings, Color(0xFF9E9E9E))  // Grey
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 8.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF16181D),
        modifier = Modifier
            .width(180.dp)
            .padding(vertical = 4.dp)
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        tint = item.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    onDismissRequest()
                    // Handle specific item clicks here if needed
                    when(item.text) {
                        "New" -> onNewClick()
                        "Recycle Bin" -> onNavigateTo("recycle_bin")
                        "Home" -> onNavigateTo("home")
                        // Add others as needed
                    }
                }
            )
        }
    }
}