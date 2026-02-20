package com.devtamuno.kmp.contactpicker.data

import androidx.compose.runtime.Immutable

/**
 * A platform-agnostic data model representing a contact's profile information.
 *
 * This class serves as the core data transfer object for the Contact Picker library, 
 * aggregating details retrieved from the native address books of different platforms.
 *
 * Annotated with [Immutable] to optimize recomposition performance in Jetpack Compose by 
 * signaling that all public properties are stable and will not change after construction.
 *
 * @property id A unique, platform-specific string identifier for the contact (e.g., RowID on Android, UUID on iOS).
 * @property name The full display name as recorded in the user's contacts.
 * @property phoneNumbers A list of all phone numbers associated with this contact.
 * @property email A list of all email addresses associated with this contact.
 * @property contactAvatar The raw binary data (byte array) of the contact's profile picture or thumbnail. 
 * Can be converted to an `ImageBitmap` using the provided platform extensions.
 */
@Immutable
data class Contact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
    val email: List<String>,
    val contactAvatar: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Contact

        if (id != other.id) return false
        if (name != other.name) return false
        if (phoneNumbers != other.phoneNumbers) return false
        if (email != other.email) return false
        if (!contactAvatar.contentEquals(other.contactAvatar)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + phoneNumbers.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (contactAvatar?.contentHashCode() ?: 0)
        return result
    }
}
