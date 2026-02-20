package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap

/**
 * An interface for converting a byte array into a platform-specific [ImageBitmap].
 */
interface BitmapConverter {
    /**
     * Converts a raw byte array into an [ImageBitmap].
     *
     * @param byteArray The byte array to convert.
     * @return The resulting [ImageBitmap], or `null` if conversion fails.
     */
    fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap?
}

/**
 * An expected function that should return a platform-specific implementation of [BitmapConverter].
 */
internal expect fun getBitmapConverter(): BitmapConverter

/**
 * Extension function to convert a [ByteArray] to an [ImageBitmap] using the platform-specific converter.
 *
 * @return The converted [ImageBitmap], or `null` if the conversion fails.
 */
fun ByteArray.toPlatformImageBitmap(): ImageBitmap? {
    return getBitmapConverter().bitmapFromBytes(this)
}
