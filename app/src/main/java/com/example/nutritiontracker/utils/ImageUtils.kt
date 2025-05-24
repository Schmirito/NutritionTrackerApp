package com.example.nutritiontracker.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    fun saveImageToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Erstelle Verzeichnis für Bilder
            val directory = File(context.filesDir, "images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Speichere Bitmap als JPEG
            val file = File(directory, "$fileName.jpg")
            val outputStream = FileOutputStream(file)

            // Komprimiere und skaliere das Bild
            val scaledBitmap = scaleBitmap(bitmap, 800, 800)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    fun deleteImage(imagePath: String?) {
        imagePath?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}