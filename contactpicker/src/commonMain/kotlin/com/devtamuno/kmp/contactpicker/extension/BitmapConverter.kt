package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap

interface BitmapConverter {
    fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap?
}

internal expect fun getBitmapConverter(): BitmapConverter

fun ByteArray.toComposeImageBitmap(): ImageBitmap? {
    return getBitmapConverter().bitmapFromBytes(this)
}