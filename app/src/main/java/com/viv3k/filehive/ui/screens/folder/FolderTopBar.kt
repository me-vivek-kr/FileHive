package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.screens.utils.CustomDropdownMenu
import com.viv3k.filehive.ui.screens.utils.RoundedIconButton

data class MenuItemData(
    val text: String,
    val iconRes: Int,
    val iconColor: Color
)

@Composable
fun FolderTopBar(
    title: String,
    onBackClick: () -> Unit,
//    onSearchClick: () -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onNavigateTo: (String) -> Unit,
    onNewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RoundedIconButton(
            icon = R.drawable.arrow_left,
            onClick = onBackClick,
            backgroundColor = Color.White,
            iconTint = Color.Black,
            elevation = 6.dp
        )

        Spacer(Modifier.width(20.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = Color.Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

//        RoundedIconButton(
//            icon = R.drawable.search,
//            onClick = onSearchClick,
//            backgroundColor = Color.White,
//            iconTint = Color.Black,
//            elevation = 6.dp
//        )

        Spacer(Modifier.width(16.dp))

        // Anchor Box
        Box {
            val iconRes = if (expanded) R.drawable.x else R.drawable.more_vertical
            val bgColor = if (expanded) Color(0xFFEBEDF2) else Color.White

            RoundedIconButton(
                icon = iconRes,
                onClick = { onExpandChange(!expanded) },
                backgroundColor = bgColor,
                iconTint = Color.Black,
                elevation = 6.dp
            )

            CustomDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
                onNavigateTo = onNavigateTo,
                onNewClick = onNewClick
            )
        }
    }
}

@Composable
@Preview
fun previewTopBar(){
    FolderTopBar(
        title = "Internal Storage",
        onBackClick = {},
//        onSearchClick = {},
        expanded = false,
        onExpandChange = {},
        onNavigateTo = {},
        onNewClick = {}
    )
}