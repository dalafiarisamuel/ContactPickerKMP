package com.devtamuno.kmp.contactpicker.contract

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

internal actual class ContactPicker {

    private lateinit var picker: ManagedActivityResultLauncher<Void?, Uri?>
    private val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
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

                val email: String = getEmail(context, id)

                return Contact(id, name, phoneNumber, email)
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

    private fun getEmail(context: Context, contactId: String): String {
        val emailCursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        emailCursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
            }
        }

        return ""
    }
}