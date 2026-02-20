package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * An expected class that defines the contract for a platform-specific contact picker.
 *
 * This class facilitates the selection of contacts from the device's native address book.
 * Implementations are provided for different platforms (e.g., Android and iOS) to handle
 * the specific APIs and UI flows required by each.
 */
internal expect class ContactPicker() {

    /**
     * Registers the contact picker within the Compose composition.
     * 
     * This method must be called from a Composable function. It sets up any necessary
     * infrastructure (like activity launchers or delegates) required to receive the
     * result of a contact selection.
     *
     * @param onContactSelected Callback invoked with the selected [Contact] object, or `null` 
     * if the selection was cancelled or failed.
     */
    @Composable
    fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit)

    /**
     * Triggers the display of the platform-specific contact picker interface.
     *
     * Calling this method will navigate the user away from the current app to the system's 
     * contact selection UI.
     */
    fun launchContactPicker()
}
