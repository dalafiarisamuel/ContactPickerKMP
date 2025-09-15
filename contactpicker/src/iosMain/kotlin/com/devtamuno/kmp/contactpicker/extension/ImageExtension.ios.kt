package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

private class IosImplementation : BitmapConverter {
    override fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap? {
        return try {
            Image.makeFromEncoded(byteArray).toComposeImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

actual fun getBitmapConverter(): BitmapConverter = IosImplementation()