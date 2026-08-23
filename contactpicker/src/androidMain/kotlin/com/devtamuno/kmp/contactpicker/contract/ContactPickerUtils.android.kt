package com.devtamuno.kmp.contactpicker.contract

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.devtamuno.kmp.contactpicker.data.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val contentResolver = context.contentResolver

    uris.forEach { uri ->
        contentResolver.query(uri, contactProjection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: "Unknown"
                val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
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
 * Processes the Session URI returned by the Android 17+ Contact Picker.
 */
internal suspend fun processContactPickerSessionUri(
    context: Context,
    sessionUri: Uri
): List<Contact> = withContext(Dispatchers.IO) {
    val projection = arrayOf(
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Data.MIMETYPE,
        ContactsContract.Data.DATA1,
        ContactsContract.Data.DATA15,
    )

    val contactsMap = mutableMapOf<String, Contact>()

    context.contentResolver.query(sessionUri, projection, null, null, null)?.use { cursor ->
        val lookupKeyIdx = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
        val contactIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val mimeTypeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
        val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val data1Idx = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        val data15Idx = cursor.getColumnIndex(ContactsContract.Data.DATA15)

        while (cursor.moveToNext()) {
            val lookupKey = cursor.getString(lookupKeyIdx)
            val contactId = cursor.getString(contactIdIdx)
            val mimeType = cursor.getString(mimeTypeIdx)
            val name = cursor.getString(nameIdx) ?: "Unknown"
            val data1 = cursor.getString(data1Idx) ?: ""

            val email = if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) data1 else null
            val phone = if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) data1 else null
            val photo = if (mimeType == ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE) cursor.getBlob(data15Idx) else null

            val existingContact = contactsMap[lookupKey]
            if (existingContact != null) {
                contactsMap[lookupKey] = existingContact.copy(
                    email = if (email != null && email !in existingContact.email) existingContact.email + email else existingContact.email,
                    phoneNumbers = if (phone != null && phone !in existingContact.phoneNumbers) existingContact.phoneNumbers + phone else existingContact.phoneNumbers,
                    contactAvatar = existingContact.contactAvatar ?: photo
                )
            } else {
                contactsMap[lookupKey] = Contact(
                    id = contactId ?: lookupKey,
                    name = name,
                    email = if (email != null) listOf(email) else emptyList(),
                    phoneNumbers = if (phone != null) listOf(phone) else emptyList(),
                    contactAvatar = photo
                )
            }
        }
    }
    contactsMap.values.toList()
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

internal suspend fun fetchAllContacts(context: Context): List<Contact> =
    withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val cursor =
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                contactProjection,
                null,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME} ASC",
            )

        val contactIds = mutableListOf<String>()
        val contactNames = mutableMapOf<String, String>()

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                contactIds.add(id)
                contactNames[id] = name
            }
        }

        if (contactIds.isEmpty()) return@withContext emptyList()

        val phoneMap = getPhoneNumbers(context, null)
        val emailMap = getEmailAddresses(context, null)

        for (id in contactIds) {
            contacts.add(
                Contact(
                    id = id,
                    name = contactNames[id] ?: "",
                    phoneNumbers = phoneMap[id] ?: emptyList(),
                    email = emailMap[id] ?: emptyList(),
                    contactAvatar = null, // Hydrate on selection for performance
                )
            )
        }

        contacts
    }