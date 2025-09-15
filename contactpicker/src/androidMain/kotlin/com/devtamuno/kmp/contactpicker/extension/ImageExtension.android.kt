package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private class AndroidImplementation : BitmapConverter {
    override fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap? {
        return try {
            android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                .asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

actual fun getBitmapConverter(): BitmapConverter = AndroidImplementation()