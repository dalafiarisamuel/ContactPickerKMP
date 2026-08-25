package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Defines the contract for managing the state and interactions of a contact picker.
 *
 * This interface provides a reactive way to observe the selected contact and methods
 * to trigger the platform-specific contact selection UI.
 */
interface ContactPickerState {

    /**
     * A reactive [State] containing the currently selected [Contact].
     *
     * This value will be updated automatically once a user selects a contact from the 
     * platform's native picker. It is initialized to `null`.
     */
    val value: State<Contact?>

    /**
     * Requests the display of the system's native contact selection interface.
     *
     * When invoked, the user will be presented with the platform's contact book. 
     * The result of this action will be reflected in the [value] property and 
     * delivered via the optional callback provided during state creation.
     */
    fun launchContactPicker()

    /**
     * Clears the currently selected contact.
     */
    fun clear()
}

/**
 * Defines the contract for managing the state and interactions of a multi-contact picker.
 *
 * This interface provides a reactive way to observe the selected contacts and methods
 * to trigger the platform-specific contact selection UI.
 */
interface MultiContactPickerState {

    /**
     * A reactive [State] containing the list of currently selected [Contact]s.
     *
     * This value will be updated automatically once a user selects contacts from the
     * platform's native picker. It is initialized to an empty list.
     */
    val value: State<List<Contact>>

    /**
     * Requests the display of the system's native multi-contact selection interface.
     *
     * When invoked, the user will be presented with the platform's contact book.
     * The result of this action will be reflected in the [value] property and
     * delivered via the optional callback provided during state creation.
     */
    fun launchContactPicker()

    /**
     * Clears all currently selected contacts.
     */
    fun clear()
}

internal interface InternalContactPickerState : ContactPickerState {
    @Composable
    fun InitContactPicker()
}

internal interface InternalMultiContactPickerState : MultiContactPickerState {
    @Composable
    fun InitContactPicker()
}
