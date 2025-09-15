package com.devtamuno.kmp.contactpicker.extension

import androidx.compose.ui.graphics.ImageBitmap

interface BitmapConverter {
    fun bitmapFromBytes(byteArray: ByteArray): ImageBitmap?
}

expect fun getBitmapConverter(): BitmapConverter