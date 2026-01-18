package com.viv3k.filehive.ui.components

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import com.viv3k.filehive.ui.utils.PlayInsetIcon
import java.io.File


@Composable
fun FileThumbnail(
    file: File,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    displayName: String? = null
) {
    val context = LocalContext.current
    val nameToCheck = displayName ?: file.name
//    val extension = file.extension.lowercase()
    val extension = nameToCheck.substringAfterLast('.', "").lowercase()

    // Check if it's a visual media file we can load
    val isImage = extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "svg", "ico", "dng", "raw", "nef", "cr2")
    val isVideo = extension in listOf("mp4", "mkv", "mov", "avi", "wmv", "flv", "webm", "3gp", "mpeg", "mpg", "ts", "m4v")
    val isAudio = extension in listOf("mp3", "m4a", "aac", "flac", "ogg", "wav", "opus")
    val isApk = extension == "apk"

    if (isImage || isVideo) {
        Box(modifier = modifier) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .crossfade(true)
                    .apply {
                        if (isVideo && Build.VERSION.SDK_INT >= 23) {
                            decoderFactory(VideoFrameDecoder.Factory())
                        }
                    }
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = FileIcons.getIcon(file)),
                placeholder = painterResource(id = FileIcons.getIcon(file))
            )
            if (isVideo) {
                PlayInsetIcon(iconSize)
            }
        }
    }
    else if (isAudio) {
        val albumArt by produceState<Bitmap?>(initialValue = null, key1 = file.absolutePath) {
            value = withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(file.absolutePath)
                    retriever.embeddedPicture?.let { art ->
                        BitmapFactory.decodeByteArray(art, 0, art.size)
                    }
                } catch (e: Exception) {
                    null
                } finally {
                    retriever.release()
                }
            }
        }

        Box(modifier = modifier) {
            if (albumArt != null) {
                Image(
                    bitmap = albumArt!!.asImageBitmap(),
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = FileIcons.getIcon(file)),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
            PlayInsetIcon(iconSize)
        }
    }
    else if (isApk) {
        val apkIconState = produceState<Drawable?>(initialValue = null, key1 = file) {
            value = withContext(Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val info = pm.getPackageArchiveInfo(
                        file.absolutePath,
                        PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA
                    )

                    info?.applicationInfo?.apply {
                        sourceDir = file.absolutePath
                        publicSourceDir = file.absolutePath
                    }?.loadIcon(pm)
                } catch (e: Exception) {
                    null
                }
            }
        }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            apkIconState.value?.let { icon ->
                Image(
                    painter = rememberAsyncImagePainter(icon),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: Icon(
                painter = painterResource(id = FileIcons.getIcon(file)),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
    }
    else {
        // Fallback for non-media files (PDFs, Docs, etc.)
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = FileIcons.getIcon(file)),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
