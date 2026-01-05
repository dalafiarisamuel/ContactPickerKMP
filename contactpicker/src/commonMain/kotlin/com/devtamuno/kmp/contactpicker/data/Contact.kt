package com.devtamuno.kmp.contactpicker.data

import androidx.compose.runtime.Immutable

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
