package com.viv3k.filehive.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onSearchDone: () -> Unit,
    onBackClick: () -> Unit
){
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 16.sp
        ),
        cursorBrush = SolidColor(Color(0xFF3e94a2)),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchDone() }),

        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1C21))
                    .border(1.dp, Color(0xFF2B2D31), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    tint = Color(0xFF9AA0A6),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)){
                    if(query.isEmpty()){
                        Text(
                            text= "Search files and folders...",
                            color = Color(0xFF5F6368),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }

                // Close/Clear Button
                if(query.isNotEmpty()){
                    IconButton(
                        onClick = onClearClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.x),
                            contentDescription = "Clear",
                            tint = Color(0xFF9AA0A6)
                        )
                    }
                }
            }
        }
    )
}