package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import com.devtamuno.kmp.contactpicker.data.Contact

internal expect class ContactPicker() {
    @Composable
    fun registerContactPicker(onContactPicked: (Contact?) -> Unit)
    fun launchContactPicker()
}