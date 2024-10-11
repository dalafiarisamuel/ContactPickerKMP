package com.devtamuno.kmp.contactpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.devtamuno.kmp.contactpicker.contract.ContactPickerState
import com.devtamuno.kmp.contactpicker.contract.ContactPickerStateImpl
import com.devtamuno.kmp.contactpicker.data.Contact


@Composable
fun rememberContactPickerState(
    contactPicked: (Contact?) -> Unit,
): ContactPickerState {
    return rememberMutableContactPickerState(contactPicked).also {
       it.registerContactPicker()
    }
}


@Composable
private fun rememberMutableContactPickerState(contactPicked: (Contact?) -> Unit): ContactPickerState {
    return remember { ContactPickerStateImpl(contactPicked) }
}