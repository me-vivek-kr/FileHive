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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val image: Int ?= null,
    val title: String,
    val description: String
)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    onStartClick: () -> Unit = {}
) {

    val pages = listOf(
        OnboardingPage(
            title = "Manage Your\nFile Smartly",
            description = "Manage your digital file easily & smartly"
        ),
        OnboardingPage(
            title = "Organize\nEverything",
            description = "Keep your files organized in one place"
        ),
        OnboardingPage(
            title = "Secure Your\nData",
            description = "Lock and protect your private files"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()


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
                    // small page indicator dots
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val width = if (isSelected) 20.dp else 8.dp
                            val color = if (isSelected) Color(0xFF3e94a2) else Color(0xFF3E414B)

                            Box(
                                Modifier
                                    .padding(4.dp)
                                    .height(8.dp) // Height stays constant
                                    .width(width) // Width changes (Pill vs Circle)
                                    .clip(RoundedCornerShape(4.dp)) // Fully rounded corners
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalPager(state = pagerState) { page ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()){
                            Text(
                                text = pages[page].title,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 34.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = pages[page].description,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B6BD),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    //Button
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
                            .clickable {
                                if (pagerState.currentPage == pages.size - 1) {
                                    onStartClick()
                                } else {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val buttonText = if (pagerState.currentPage == pages.size - 1) "Start" else "Next"

                            Text(
                                text = buttonText,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
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
