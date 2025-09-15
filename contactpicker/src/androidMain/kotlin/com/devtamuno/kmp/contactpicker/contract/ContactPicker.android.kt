package com.devtamuno.kmp.contactpicker.contract

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.devtamuno.kmp.contactpicker.data.Contact
import androidx.core.net.toUri

internal actual class ContactPicker {

    private lateinit var picker: ManagedActivityResultLauncher<Void?, Uri?>
    private val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.HAS_PHONE_NUMBER,
        ContactsContract.Contacts.PHOTO_URI
    )

    @Composable
    actual fun registerContactPicker(onContactSelected: (Contact?) -> Unit) {
        val context = LocalContext.current
        picker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            if (uri != null) {
                val contact = getContactFromUri(context, uri)
                onContactSelected(contact)
            } else {
                onContactSelected(null)
            }
        }
    }

    actual fun launchContactPicker() {
        picker.launch()
    }

    private fun getContactFromUri(context: Context, uri: Uri): Contact? {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex =
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex)
                val hasPhone = cursor.getInt(hasPhoneIndex) > 0
                val phoneNumber = if (hasPhone) {
                    getPhoneNumber(context, id)
                } else emptyList()

                val email: List<String> = getEmail(context, id)
                val contactAvatar = getContactAvatar(context, id.toLong())

                return Contact(id, name, phoneNumber, email, contactAvatar)
            }
        }

        return null
    }

    private fun getPhoneNumber(context: Context, contactId: String): List<String> {
        val phoneNumbers = mutableListOf<String>()
        val phoneCursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use {
            while (it.moveToNext()) {
                phoneNumbers.add(
                    it.getString(
                        it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                )
            }
        }

        return phoneNumbers
    }

    private fun getEmail(context: Context, contactId: String): List<String> {
        val emailAddresses = mutableListOf<String>()
        val emailCursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        emailCursor?.use {
            while (it.moveToNext()) {
                emailAddresses.add(
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                )
            }
        }

        return emailAddresses
    }

    private fun getContactAvatar(context: Context, contactId: Long): ByteArray? {
        val contactUri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId
        )

        val cursor = context.contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts.PHOTO_URI),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val photoUriString =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                if (!photoUriString.isNullOrEmpty()) {
                    // Try full-size first
                    context.contentResolver.openInputStream(
                        Uri.withAppendedPath(
                            contactUri,
                            ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                        )
                    )?.use { inputStream ->
                        return inputStream.readBytes()
                    }

                    // Fall back to thumbnail
                    context.contentResolver.openInputStream(photoUriString.toUri())
                        ?.use { inputStream ->
                            return inputStream.readBytes()
                        }
                }
            }
        }

        return null
    }
}