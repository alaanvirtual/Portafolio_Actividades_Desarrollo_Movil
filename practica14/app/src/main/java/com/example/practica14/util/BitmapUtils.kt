package com.example.practica14.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat

object BitmapUtils {
    /**
     * Convierte un recurso vectorial (Vector Asset) a Bitmap
     * requerido por OSMdroid (setPersonIcon, setDirectionArrow, Marker icon).
     */
    fun vectorToBitmap(context: Context, drawableId: Int, widthPx: Int = 96, heightPx: Int = 96): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: throw IllegalArgumentException("Drawable no encontrado")

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else widthPx
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else heightPx

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
