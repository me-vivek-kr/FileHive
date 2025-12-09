package com.viv3k.filehive.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    onStartClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            // main dark background
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1115), Color(0xFF131218))
                )
            )
    ) {
        // Top decorative bubbles (stacked)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .height(360.dp)
        ) {
            // big central bubble
            Image(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "Big bubble",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 24.dp)
            )

            // small left bubble
            Image(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "Small bubble left",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 40.dp, y = 44.dp)
            )

            // small right emoji bubble
            Image(
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "Small bubble right",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(84.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-36).dp, y = 8.dp)
            )
        }

        // Bottom card with rounded corners (frosted look)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card container
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(28.dp),
                color = Color(0x1A1A1C) // slightly lighter translucent card color
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // small page indicator dots (centered)
                    Row(
                        modifier = Modifier
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3e94a2))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD6D6E0))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD6D6E0))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Manage Your\nFile Smartly",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Manage your digital file easily & smartly",
                        fontSize = 14.sp,
                        color = Color(0xFFB0B6BD)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Start button (pill) with subtle shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF315F6B), Color(0xFF3e94a2))
                                )
                            )
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
                            .clickable { onStartClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Start", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(10.dp))
                            Image(
                                painter = painterResource(id = R.drawable.arrow_right),
                                contentDescription = "arrow",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Decorative shadow/edge behind card to better match screenshot
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onStartClick = {})
}
