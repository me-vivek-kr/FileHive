package com.viv3k.filehive.data.coil

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import java.io.File

//object ApkFileTypes {
//    private val extensions = setOf("apk")
//
//    fun contains(ext: String): Boolean = extensions.contains(ext.lowercase())
//}
//
//class ApkFileDecoder(
//    private val source: File,
//) : Decoder {
//    override suspend fun decode(): DecodeResult? {
//        val packageManager = globalClass.packageManager
//        packageManager.getPackageArchiveInfo(source.absolutePath, 0)?.let { apkInfo ->
//            apkInfo.applicationInfo?.apply {
//                sourceDir = source.absolutePath
//                publicSourceDir = source.absolutePath
//                loadIcon(packageManager).drawableToBitmap()?.let { bitmap ->
//                    return DecodeResult(bitmap.asImage(), false)
//                }
//            }
//        }
//        return null
//    }
//
//    class Factory : Decoder.Factory {
//        override fun create(
//            result: SourceFetchResult,
//            options: Options,
//            imageLoader: ImageLoader
//        ): Decoder? {
//            val file = result.source.file().toFile()
//            if (file.exists() && ApkFileTypes.contains(file.extension)) {
//                return ApkFileDecoder(file)
//            }
//            return null
//        }
//    }
//}

object ApkFileTypes {
    private val extensions = setOf("apk")

    fun contains(ext: String): Boolean = extensions.contains(ext.lowercase())
}

/** Safe helper to convert a Drawable to Bitmap */
private fun Drawable.toBitmapCompat(): Bitmap {
    if (this is BitmapDrawable && this.bitmap != null) return this.bitmap
    val width = intrinsicWidth.takeIf { it > 0 } ?: 1
    val height = intrinsicHeight.takeIf { it > 0 } ?: 1
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

class ApkFileDecoder(
    private val source: File,
    private val packageManager: PackageManager
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        return try {
            val pkgInfo = packageManager.getPackageArchiveInfo(source.absolutePath, 0)
            val appInfo = pkgInfo?.applicationInfo ?: return null

            // Point the ApplicationInfo to the apk file so loadIcon works
            appInfo.sourceDir = source.absolutePath
            appInfo.publicSourceDir = source.absolutePath

            val drawable = appInfo.loadIcon(packageManager)
            val bitmap = drawable.toBitmapCompat()
            DecodeResult(bitmap.asImage(), isSampled = false)
        } catch (e: Exception) {
            null
        }
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val file = result.source.file().toFile()
            val pm = options.context.packageManager
            return if (file.exists() && ApkFileTypes.contains(file.extension)) {
                ApkFileDecoder(file, pm)
            } else null
        }
    }
}