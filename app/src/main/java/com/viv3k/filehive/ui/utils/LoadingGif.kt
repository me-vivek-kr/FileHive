//package com.viv3k.filehive.ui.utils
//
//import android.os.Build
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.foundation.Image
//import coil3.ImageLoader
//import coil3.compose.rememberAsyncImagePainter
////import coil3.decode.GifDecoder
////import coil3.decode.ImageDecoderDecoder
//import coil3.request.ImageRequest
//import com.viv3k.filehive.R
//
//@Composable
//fun LoadingGif(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//
//    // ImageLoader with GIF support
//    val imageLoader = remember {
//        ImageLoader.Builder(context)
//            .components {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    add(ImageDecoderDecoder.Factory())
//                } else {
//                    add(GifDecoder.Factory())
//                }
//            }
//            .build()
//    }
//
//    val painter = rememberAsyncImagePainter(
//        model = ImageRequest.Builder(context)
//            .data(R.drawable.loading)   // your GIF in res/drawable
//            .build(),
//        imageLoader = imageLoader
//    )
//
//    Image(
//        painter = painter,
//        contentDescription = "loading",
//        modifier = modifier
//    )
//}