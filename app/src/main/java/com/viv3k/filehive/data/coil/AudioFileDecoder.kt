package com.viv3k.filehive.data.coil

import android.graphics.BitmapFactory.decodeByteArray
import android.media.MediaMetadataRetriever
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import java.io.File

object AudioFileTypes {
    private val extensions = setOf(
        "mp3", "m4a", "flac", "wav", "ogg", "aac", "opus", "wma", "alac"
    )

    fun contains(ext: String): Boolean = extensions.contains(ext.lowercase())
}

class AudioFileDecoder(
    private val source: File,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        return try {
            val metadata = MediaMetadataRetriever()
            metadata.setDataSource(source.absolutePath)

            val albumArt = metadata.embeddedPicture
            metadata.release()

            if (albumArt != null) {
                val bitmap = decodeByteArray(albumArt, 0, albumArt.size)
                if (bitmap != null) {
                    DecodeResult(
                        image = bitmap.asImage(),
                        isSampled = false
                    )
                } else null
            } else null
        } catch (_: Exception) {
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
            if (file.exists() && AudioFileTypes.contains(file.extension)) {
                return AudioFileDecoder(file)
            }
            return null
        }
    }
}