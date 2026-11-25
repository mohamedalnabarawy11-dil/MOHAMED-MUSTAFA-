package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object ImageEncodingUtil {
    fun createTempCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    suspend fun encodeUriToBase64DataUrl(context: Context, uri: Uri, maxDim: Int = 1024): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }
                val largest = max(boundsOpts.outWidth, boundsOpts.outHeight)
                var sampleSize = 1
                while (largest / sampleSize > maxDim * 2) {
                    sampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext null

                val scale = if (max(rawBitmap.width, rawBitmap.height) > maxDim) {
                    maxDim.toFloat() / max(rawBitmap.width, rawBitmap.height).toFloat()
                } else {
                    1f
                }
                val finalBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        rawBitmap,
                        (rawBitmap.width * scale).toInt().coerceAtLeast(1),
                        (rawBitmap.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else {
                    rawBitmap
                }

                val baos = ByteArrayOutputStream()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 76, baos)
                if (finalBitmap !== rawBitmap) finalBitmap.recycle()
                rawBitmap.recycle()
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                "data:image/jpeg;base64,$base64"
            }.getOrNull()
        }
}
