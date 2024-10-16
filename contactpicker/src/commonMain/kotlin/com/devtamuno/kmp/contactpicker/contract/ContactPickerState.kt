package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.devtamuno.kmp.contactpicker.data.Contact

interface ContactPickerState {
    val value: State<Contact?>

    @Composable
    fun initContactPicker()

    fun launchContactPicker()
}