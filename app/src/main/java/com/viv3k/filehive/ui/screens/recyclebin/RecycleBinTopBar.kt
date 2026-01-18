package com.viv3k.filehive.ui.screens.recyclebin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.screens.utils.CustomDropdownMenu
import com.viv3k.filehive.ui.screens.utils.RoundedIconButton

@Composable
fun RecycleBinTopBar(
    onBackClick: () -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onNavigateTo: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Recycle Bin",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        // Using your existing RoundedIconButton style for consistency
//        RoundedIconButton(
//            icon = R.drawable.more_vertical,
//            onClick = { /* Menu options */ }
//        )
        Box {
            val iconRes = if (expanded) R.drawable.x else R.drawable.more_vertical
            val bgColor = if (expanded) Color(0xFF3e94a2) else Color(0xFF1A1C21)

            RoundedIconButton(
                icon = iconRes,
                onClick = { onExpandChange(!expanded) },
                backgroundColor = bgColor
            )

            CustomDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
                onNavigateTo = onNavigateTo,
                onNewClick = {}
            )
        }
    }
}