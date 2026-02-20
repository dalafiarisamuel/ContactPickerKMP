package com.devtamuno.kmp.contactpicker.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Concrete implementation of [ContactPickerState] responsible for bridging the common
 * state management with platform-specific contact picking logic.
 *
 * This class maintains the reactive state of the currently selected contact and
 * orchestrates the interaction with the underlying [ContactPicker] implementation.
 *
 * @param contactPicked A callback that is triggered every time a contact is 
 * successfully picked or the selection is cleared.
 */
internal class ContactPickerStateImpl(
    private val contactPicked: (Contact?) -> Unit,
) : ContactPickerState {

    private val _value: MutableState<Contact?> = mutableStateOf(null)

    /**
     * Exposes the currently selected [Contact] as a read-only [State].
     * 
     * Observe this property in Composable functions to reactively update the UI
     * when a contact is selected.
     */
    override val value: State<Contact?> get() = _value

    private val contactPicker = ContactPicker()

    /**
     * Initializes the underlying contact picker mechanism.
     *
     * This method must be called within a [Composable] context to ensure the 
     * platform-specific registration (e.g., ActivityResultRegistry on Android) 
     * is correctly tied to the composition lifecycle.
     *
     * When a contact selection is completed, it updates the internal [_value] 
     * and propagates the result via the [contactPicked] callback.
     */
    @Composable
    override fun InitContactPicker() {
        contactPicker.RegisterContactPicker { selectedContact ->
            _value.value = selectedContact
            contactPicked(selectedContact)
        }
    }

    /**
     * Requests the platform to display its native contact picker interface.
     *
     * This call is delegated directly to the expected [ContactPicker.launchContactPicker] 
     * implementation. Ensure [InitContactPicker] has been called previously in the 
     * composition to correctly receive the result.
     */
    override fun launchContactPicker() {
        contactPicker.launchContactPicker()
    }
}
