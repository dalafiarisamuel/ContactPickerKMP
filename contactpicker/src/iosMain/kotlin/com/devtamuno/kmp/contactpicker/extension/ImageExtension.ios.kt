package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

/**
 * iOS-specific implementation of [BitmapConverter] that leverages Skia (via Compose Multiplatform)
 * to decode raw byte arrays into [ImageBitmap].
 */
private class IosImplementation : BitmapConverter {

    /**
     * Decodes a byte array into an [ImageBitmap] using Skia's image decoding capabilities.
     *
     * @param byteArray The source byte array representing the encoded image data (e.g., PNG, JPEG).
     * @return The decoded [ImageBitmap], or `null` if the byte array is invalid or decoding fails.
     */
    override fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap? {
        return try {
            Image.makeFromEncoded(byteArray).toComposeImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Provides the iOS-specific [BitmapConverter] instance.
 */
actual fun getBitmapConverter(): BitmapConverter = IosImplementation()
