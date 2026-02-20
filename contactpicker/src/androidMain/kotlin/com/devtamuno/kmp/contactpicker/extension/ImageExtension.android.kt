package com.devtamuno.kmp.contactpicker.extension

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * An Android-specific implementation of the [BitmapConverter] interface.
 *
 * This implementation leverages the native [BitmapFactory] to efficiently decode raw compressed
 * image data (such as JPEG, PNG, or WEBP) into a platform-native `Bitmap`, which is then wrapped
 * into a Compose-compatible [ImageBitmap].
 *
 * It is marked as `internal` to encapsulate the platform-specific decoding logic while allowing the
 * [getBitmapConverter] actual function to instantiate it.
 */
private class AndroidImplementation : BitmapConverter {

  /**
   * Decodes a byte array into an [ImageBitmap] using the Android platform's
   * [BitmapFactory.decodeByteArray].
   *
   * This method provides a safe decoding wrapper that catches and handles potential exceptions
   * (e.g., [IllegalArgumentException] for malformed data or [OutOfMemoryError]) by returning `null`
   * instead of crashing the application.
   *
   * @param byteArray The raw, encoded byte array representing the image data.
   * @return A [ImageBitmap] representation of the data, or `null` if the data is invalid or cannot
   *   be decoded.
   */
  override fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap? {
    return try {
      BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)?.asImageBitmap()
    } catch (_: Throwable) {
      null
    }
  }
}

/**
 * Returns the Android-specific [BitmapConverter] implementation.
 *
 * This fulfills the `expect` declaration in the common module, enabling cross-platform image
 * processing while utilizing Android's optimized native APIs.
 */
actual fun getBitmapConverter(): BitmapConverter = AndroidImplementation()
