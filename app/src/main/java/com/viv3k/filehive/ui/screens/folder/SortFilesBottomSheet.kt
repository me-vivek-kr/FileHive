package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.components.NeumorphicIconBadge
import com.viv3k.filehive.ui.utils.NeomorphicSwitch
import com.viv3k.filehive.ui.utils.soft

enum class FolderSortOption {
    Name,
    DateModified,
    Size
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilesBottomSheet(
    selectedSort: FolderSortOption,
    ascending: Boolean,
    foldersFirst: Boolean,
    onSortSelected: (FolderSortOption) -> Unit,
    onAscendingChange: (Boolean) -> Unit,
    onFoldersFirstChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = Color(0xFFF7F9FB),
        contentColor = Color(0xFF1F2937)
    ) {
        SortFilesSheetContent(
            selectedSort = selectedSort,
            ascending = ascending,
            foldersFirst = foldersFirst,
            onSortSelected = onSortSelected,
            onAscendingChange = onAscendingChange,
            onFoldersFirstChange = onFoldersFirstChange,
            onDismiss = onDismiss,
            onApply = onApply,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun SortFilesSheetContent(
    selectedSort: FolderSortOption,
    ascending: Boolean,
    foldersFirst: Boolean,
    onSortSelected: (FolderSortOption) -> Unit,
    onAscendingChange: (Boolean) -> Unit,
    onFoldersFirstChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .soft(
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
                cornerRadius = 34.dp,
                backgroundColor = Color(0xFFF7F9FB),
                blurRadius = 20.dp,
                offsetY = 0.dp
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(42.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFD5DAE3))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sort Files",
                color = Color(0xFF1F2937),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .soft(
                        shape = CircleShape,
                        cornerRadius = 19.dp,
                        backgroundColor = Color.White,
                        blurRadius = 10.dp,
                        offsetY = 4.dp
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.x),
                    contentDescription = "Close sort sheet",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        SortSectionLabel("SORT BY")
        SortChoiceRow(
            icon = R.drawable.ic_alphabet,
            title = "Name",
            selected = selectedSort == FolderSortOption.Name,
            onClick = { onSortSelected(FolderSortOption.Name) }
        )
        SortChoiceRow(
            icon = R.drawable.ic_modified,
            title = "Date Modified",
            selected = selectedSort == FolderSortOption.DateModified,
            onClick = { onSortSelected(FolderSortOption.DateModified) }
        )
        SortChoiceRow(
            icon = R.drawable.ic_size,
            title = "Size",
            selected = selectedSort == FolderSortOption.Size,
            onClick = { onSortSelected(FolderSortOption.Size) }
        )

        SortSectionLabel("ORDER")
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SortOrderCard(
                icon = R.drawable.ic_arrowup,
                title = "Ascending",
                selected = ascending,
                modifier = Modifier.weight(1f),
                onClick = { onAscendingChange(true) }
            )
            SortOrderCard(
                icon = R.drawable.ic_arrowdown,
                title = "Descending",
                selected = !ascending,
                modifier = Modifier.weight(1f),
                onClick = { onAscendingChange(false) }
            )
        }

        SortSectionLabel("PREFERENCES")
        SortPreferenceRow(
            icon = R.drawable.folder,
            title = "Folders First",
            checked = foldersFirst,
            onCheckedChange = onFoldersFirstChange
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF3B35D8))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onApply() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Apply Sort",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SortSectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF8B93A1),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
    )
}

@Composable
private fun SortChoiceRow(
    icon: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .soft(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 18.dp,
                backgroundColor = Color.White,
                blurRadius = 10.dp,
                offsetY = 4.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeumorphicIconBadge(
            icon = painterResource(id = icon),
            modifier = Modifier.size(30.dp),
            iconSize = 17.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color(0xFF1F2937),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        SelectionDot(selected = selected)
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun SortOrderCard(
    icon: Int,
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)

    Column(
        modifier = modifier
            .height(88.dp)
            .then(
                if (selected) {
                    Modifier
                        .clip(shape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFDDE1E7),
                                    Color(0xFFEBEDF2)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.9f)
                                )
                            ),
                            shape = shape
                        )
                } else {
                    Modifier.soft(
                        shape = shape,
                        cornerRadius = 20.dp,
                        backgroundColor = Color.White,
                        blurRadius = 12.dp,
                        offsetY = 5.dp
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeumorphicIconBadge(
            icon = painterResource(id = icon),
            modifier = Modifier.size(30.dp),
            iconSize = 17.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SortPreferenceRow(
    icon: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .soft(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 18.dp,
                backgroundColor = Color.White,
                blurRadius = 10.dp,
                offsetY = 4.dp
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeumorphicIconBadge(
            icon = painterResource(id = icon),
            modifier = Modifier.size(30.dp),
            iconSize = 17.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = Color(0xFF1F2937),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        NeomorphicSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .soft(
                shape = CircleShape,
                cornerRadius = 16.dp,
                backgroundColor = Color(0xFFF1F4F8),
                blurRadius = 12.dp,
                offsetY = 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF5C60F0),
                                Color(0xFF2D2BCB)
                            )
                        )
                    )
            )
        }
    }
}

@Preview
@Composable
fun PreviewSortFilesBottomSheet(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F9FB))
            .padding(top = 24.dp)
    ) {
        SortFilesSheetContent(
            selectedSort = FolderSortOption.Name,
            ascending = true,
            foldersFirst = true,
            onSortSelected = {},
            onAscendingChange = {},
            onFoldersFirstChange = {},
            onDismiss = {},
            onApply = {}
        )
    }
}