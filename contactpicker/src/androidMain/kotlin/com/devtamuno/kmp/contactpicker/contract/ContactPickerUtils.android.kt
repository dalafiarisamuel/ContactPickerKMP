package com.devtamuno.kmp.contactpicker.contract

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.devtamuno.kmp.contactpicker.data.Contact

/** Columns queried from the [ContactsContract.Contacts] table. */
internal val contactProjection =
    arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.HAS_PHONE_NUMBER,
        ContactsContract.Contacts.PHOTO_URI,
    )

/**
 * Extracts comprehensive contact data from the provided [Uri].
 */
internal fun getContactFromUri(context: Context, uri: Uri): Contact? {
    return getContactsByUris(context, listOf(uri)).firstOrNull()
}

/**
 * Extracts comprehensive contact data from the provided [Uri]s.
 */
internal fun getContactsByUris(context: Context, uris: List<Uri>): List<Contact> {
    val contactData = mutableListOf<Triple<String, String, Boolean>>()
    uris.forEach { uri ->
        context.contentResolver.query(uri, contactProjection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: "Unknown"
                val hasPhone = cursor.getInt(hasPhoneIndex) > 0
                contactData.add(Triple(id, name, hasPhone))
            }
        }
    }

    if (contactData.isEmpty()) return emptyList()

    val ids = contactData.map { it.first }
    val phoneMap = getPhoneNumbers(context, ids)
    val emailMap = getEmailAddresses(context, ids)

    return contactData.map { (id, name, _) ->
        Contact(
            id = id,
            name = name,
            phoneNumbers = phoneMap[id] ?: emptyList(),
            email = emailMap[id] ?: emptyList(),
            contactAvatar = getContactAvatar(context, id.toLong())
        )
    }
}

/**
 * Queries the [ContactsContract.CommonDataKinds.Phone] table for numbers.
 * If [contactIds] is null, it fetches all phone numbers.
 */
internal fun getPhoneNumbers(
    context: Context,
    contactIds: List<String>? = null
): Map<String, List<String>> {
    val phoneMap = mutableMapOf<String, MutableList<String>>()
    val selection = when {
        contactIds == null -> null
        contactIds.isEmpty() -> return emptyMap()
        contactIds.size == 1 -> "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        else -> "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN (${contactIds.joinToString(",") { "?" }})"
    }
    val selectionArgs = contactIds?.toTypedArray()

    val phoneCursor: Cursor? = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER),
        selection,
        selectionArgs,
        null
    )

    phoneCursor?.use {
        val idIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val numberIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val id = it.getString(idIndex)
            val number = it.getString(numberIndex)
            phoneMap.getOrPut(id) { mutableListOf() }.add(number)
        }
    }
    return phoneMap
}

/**
 * Queries the [ContactsContract.CommonDataKinds.Email] table for email addresses.
 * If [contactIds] is null, it fetches all email addresses.
 */
internal fun getEmailAddresses(
    context: Context,
    contactIds: List<String>? = null
): Map<String, List<String>> {
    val emailMap = mutableMapOf<String, MutableList<String>>()
    val selection = when {
        contactIds == null -> null
        contactIds.isEmpty() -> return emptyMap()
        contactIds.size == 1 -> "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        else -> "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} IN (${contactIds.joinToString(",") { "?" }})"
    }
    val selectionArgs = contactIds?.toTypedArray()

    val emailCursor: Cursor? = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Email.CONTACT_ID, ContactsContract.CommonDataKinds.Email.ADDRESS),
        selection,
        selectionArgs,
        null
    )

    emailCursor?.use {
        val idIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
        val addressIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
        while (it.moveToNext()) {
            val id = it.getString(idIndex)
            val address = it.getString(addressIndex)
            emailMap.getOrPut(id) { mutableListOf() }.add(address)
        }
    }
    return emailMap
}

/**
 * Resolves the contact's photo into a [ByteArray].
 */
internal fun getContactAvatar(context: Context, contactId: Long): ByteArray? {
    val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)

    val cursor =
        context.contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts.PHOTO_URI),
            null,
            null,
            null,
        )

    cursor?.use {
        if (it.moveToFirst()) {
            val photoUriString =
                it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

            if (!photoUriString.isNullOrEmpty()) {
                // High-resolution photo
                context.contentResolver
                    .openInputStream(
                        Uri.withAppendedPath(
                            contactUri,
                            ContactsContract.Contacts.Photo.CONTENT_DIRECTORY,
                        )
                    )
                    ?.use { inputStream -> return inputStream.readBytes() }

                // Thumbnail fallback
                context.contentResolver.openInputStream(photoUriString.toUri())?.use { inputStream ->
                    return inputStream.readBytes()
                }
            }
        }
    }
    return null
}
