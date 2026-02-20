package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Defines the contract for managing the state and interactions of a contact picker.
 *
 * This interface provides a reactive way to observe the selected contact and methods
 * to initialize and trigger the platform-specific contact selection UI.
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
     * Initializes the contact picker's internal infrastructure.
     *
     * This method **must** be invoked within a [Composable] context, typically via 
     * `rememberContactPickerState`. It ensures that the necessary platform hooks (such as 
     * activity result launchers on Android or delegates on iOS) are correctly registered 
     * within the composition lifecycle.
     */
    @Composable
    fun InitContactPicker()

    /**
     * Requests the display of the system's native contact selection interface.
     *
     * When invoked, the user will be presented with the platform's contact book. 
     * The result of this action will be reflected in the [value] property and 
     * delivered via the optional callback provided during state creation.
     */
    fun launchContactPicker()
}
