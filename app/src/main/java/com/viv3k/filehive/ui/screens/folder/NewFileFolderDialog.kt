package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.viv3k.filehive.R

@Composable
fun NewFileFolderDialog(
    onDismissRequest: () -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit
){

    Dialog(onDismissRequest = onDismissRequest){
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ){
                Text(
                    "New",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                //New File Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onNewFileClick()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(painter = painterResource(id = R.drawable.file_add), contentDescription = null, tint = Color(0xFF9AA0A6), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("File", color = Color.Black, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onNewFolderClick()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painter = painterResource(id = R.drawable.folder_plus), contentDescription = null, tint = Color(0xFF5051D8), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Folder", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewNewFileFolderDialog(){
    NewFileFolderDialog(
        onDismissRequest = {},
        onNewFileClick = {},
        onNewFolderClick = {}
    )
}