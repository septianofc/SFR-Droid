package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private object AppIconCache {
    private val cache = LruCache<String, ImageBitmap>(40)

    fun get(key: String): ImageBitmap? = synchronized(this) { cache.get(key) }
    fun put(key: String, bitmap: ImageBitmap) { synchronized(this) { cache.put(key, bitmap) } }
}

@Composable
fun AppIconImage(
    drawable: Drawable? = null,
    packageName: String? = null,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val context = LocalContext.current

    val imageBitmap = remember(drawable, packageName) {
        val cacheKey = packageName ?: drawable?.hashCode()?.toString()
        if (cacheKey != null) {
            val cached = AppIconCache.get(cacheKey)
            if (cached != null) return@remember cached
        }

        val d: Drawable? = drawable ?: if (!packageName.isNullOrBlank()) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (_: Exception) {
                null
            }
        } else null

        val resultBitmap = d?.let { iconDrawable ->
            try {
                if (iconDrawable is BitmapDrawable && iconDrawable.bitmap != null && !iconDrawable.bitmap.isRecycled) {
                    iconDrawable.bitmap.asImageBitmap()
                } else {
                    val w = iconDrawable.intrinsicWidth.takeIf { it in 1..256 } ?: 72
                    val h = iconDrawable.intrinsicHeight.takeIf { it in 1..256 } ?: 72
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bitmap.asImageBitmap()
                }
            } catch (_: Throwable) {
                null
            }
        }

        if (resultBitmap != null && cacheKey != null) {
            AppIconCache.put(cacheKey, resultBitmap)
        }
        resultBitmap
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
