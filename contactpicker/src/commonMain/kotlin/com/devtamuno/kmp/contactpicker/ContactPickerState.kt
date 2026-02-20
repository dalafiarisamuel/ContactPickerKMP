package com.devtamuno.kmp.contactpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.devtamuno.kmp.contactpicker.contract.ContactPickerState
import com.devtamuno.kmp.contactpicker.contract.ContactPickerStateImpl
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Creates and remembers a [ContactPickerState] instance across recompositions.
 *
 * This function is the primary entry point for using the contact picker in a Composable. 
 * It automatically initializes the underlying platform-specific picker infrastructure 
 * within the composition lifecycle.
 *
 * @param contactPicked An optional callback triggered whenever a contact is selected 
 * or the selection process is terminated.
 * @return A stable [ContactPickerState] to manage and trigger contact picking.
 */
@Composable
fun rememberContactPickerState(
    contactPicked: (Contact?) -> Unit = {},
): ContactPickerState {
    return rememberMutableContactPickerState(contactPicked).also {
        it.InitContactPicker()
    }
}

/**
 * Internal factory function to create and remember the state implementation.
 *
 * Separation of the memory allocation from the initialization logic ensures that
 * the state object is preserved correctly while allowing [InitContactPicker] to be
 * called safely within the composition flow.
 *
 * @param contactPicked Callback for selection events.
 * @return A remembered [ContactPickerStateImpl] instance.
 */
@Composable
private fun rememberMutableContactPickerState(contactPicked: (Contact?) -> Unit): ContactPickerState {
    return remember { ContactPickerStateImpl(contactPicked) }
}
