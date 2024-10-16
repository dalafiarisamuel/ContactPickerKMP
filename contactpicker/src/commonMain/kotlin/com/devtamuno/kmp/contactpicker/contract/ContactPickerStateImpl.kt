package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.devtamuno.kmp.contactpicker.data.Contact

internal class ContactPickerStateImpl(
    private val contactPicked: (Contact?) -> Unit,
) : ContactPickerState {

    private var _value: MutableState<Contact?> = mutableStateOf(null)

    override val value: State<Contact?> get() = _value

    private val contactPicker = ContactPicker()

    @Composable
    override fun initContactPicker() {
        contactPicker.registerContactPicker {
            _value.value = it
            contactPicked(it)
        }
    }

    override fun launchContactPicker() {
        contactPicker.launchContactPicker()
    }
}