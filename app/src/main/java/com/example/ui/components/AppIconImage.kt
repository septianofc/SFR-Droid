package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppIconImage(
    drawable: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val imageBitmap = remember(drawable) {
        drawable?.let { d ->
            try {
                if (d is BitmapDrawable && d.bitmap != null) {
                    d.bitmap.asImageBitmap()
                } else {
                    val w = d.intrinsicWidth.takeIf { it > 0 } ?: 96
                    val h = d.intrinsicHeight.takeIf { it > 0 } ?: 96
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                    bitmap.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
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
