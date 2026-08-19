package com.devtamuno.kmp.contactpicker.contract

import com.devtamuno.kmp.contactpicker.data.Contact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContactPickerStateTest {

    @Test
    fun testContactPickerState_clear_resetsValue() {
        var callbackCalled = false
        var callbackValue: Contact? = Contact("1", "initial", emptyList(), emptyList(), null)
        
        val state = ContactPickerStateImpl { contact ->
            callbackCalled = true
            callbackValue = contact
        }
        
        state.clear()

        assertNull(state.value.value)
        assertNull(callbackValue)
        assertEquals(true, callbackCalled)
    }

    @Test
    fun testMultiContactPickerState_clear_resetsValue() {
        var callbackCalled = false
        var callbackValue: List<Contact> = listOf(Contact("1", "initial", emptyList(), emptyList(), null))

        val state = MultiContactPickerStateImpl { contacts ->
            callbackCalled = true
            callbackValue = contacts
        }

        state.clear()

        assertEquals(0, state.value.value.size)
        assertEquals(0, callbackValue.size)
        assertEquals(true, callbackCalled)
    }
}
