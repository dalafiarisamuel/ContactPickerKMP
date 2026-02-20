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
import androidx.core.net.toUri
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Android-specific implementation of the [ContactPicker] contract.
 *
 * This implementation utilizes the Android [ContactsContract] provider and the Activity Result API
 * ([ActivityResultContracts.PickContact]) to enable secure and seamless contact selection.
 *
 * It manages the lifecycle of the activity launcher and performs background queries to the
 * ContentResolver to aggregate contact details like names, multiple phone numbers, emails, and
 * avatars.
 */
internal actual class ContactPicker {

  private lateinit var picker: ManagedActivityResultLauncher<Void?, Uri?>

  /** Columns queried from the [ContactsContract.Contacts] table. */
  private val projection =
      arrayOf(
          ContactsContract.Contacts._ID,
          ContactsContract.Contacts.DISPLAY_NAME,
          ContactsContract.Contacts.HAS_PHONE_NUMBER,
          ContactsContract.Contacts.PHOTO_URI,
      )

  /**
   * Registers the [ManagedActivityResultLauncher] within the Compose composition.
   *
   * This method must be called within a Composable context. It initializes the launcher once and
   * sets up the callback to process the resulting [Uri] into a [Contact] object.
   *
   * @param onContactSelected Callback invoked with the mapped [Contact] or `null` if the selection
   *   failed or was cancelled.
   */
  @Composable
  actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
    if (::picker.isInitialized) return
    val context = LocalContext.current
    picker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
          if (uri != null) {
            val contact = getContactFromUri(context, uri)
            onContactSelected(contact)
          } else {
            onContactSelected(null)
          }
        }
  }

  /**
   * Triggers the Android system contact picker UI.
   *
   * Requires [RegisterContactPicker] to have been called previously to initialize the launcher.
   */
  actual fun launchContactPicker() {
    picker.launch()
  }

  /**
   * Extracts comprehensive contact data from the provided [Uri].
   *
   * @param context The [Context] used to access [android.content.ContentResolver].
   * @param uri The URI returned by the contact picker activity.
   * @return A hydrated [Contact] instance or `null` if the record could not be found.
   */
  private fun getContactFromUri(context: Context, uri: Uri): Contact? {
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
        val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
        val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

        val id = cursor.getString(idIndex)
        val name = cursor.getString(nameIndex)
        val hasPhone = cursor.getInt(hasPhoneIndex) > 0
        val phoneNumber =
            if (hasPhone) {
              getPhoneNumber(context, id)
            } else emptyList()

        val email: List<String> = getEmail(context, id)
        val contactAvatar = getContactAvatar(context, id.toLong())

        return Contact(id, name, phoneNumber, email, contactAvatar)
      }
    }

    return null
  }

  /**
   * Queries the [ContactsContract.CommonDataKinds.Phone] table for all numbers associated with a
   * contact.
   *
   * @param context Application context.
   * @param contactId The raw ID of the contact.
   * @return List of phone number strings.
   */
  private fun getPhoneNumber(context: Context, contactId: String): List<String> {
    val phoneNumbers = mutableListOf<String>()
    val phoneCursor: Cursor? =
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null,
        )

    phoneCursor?.use {
      while (it.moveToNext()) {
        phoneNumbers.add(
            it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
        )
      }
    }

    return phoneNumbers
  }

  /**
   * Queries the [ContactsContract.CommonDataKinds.Email] table for all addresses associated with a
   * contact.
   *
   * @param context Application context.
   * @param contactId The raw ID of the contact.
   * @return List of email address strings.
   */
  private fun getEmail(context: Context, contactId: String): List<String> {
    val emailAddresses = mutableListOf<String>()
    val emailCursor: Cursor? =
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null,
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

  /**
   * Resolves the contact's photo into a [ByteArray].
   *
   * This method attempts to fetch the high-resolution photo from the
   * [ContactsContract.Contacts.Photo] directory. If high-res is unavailable, it falls back to the
   * system thumbnail.
   *
   * @param context Application context.
   * @param contactId The unique contact identifier.
   * @return Byte array of the image data or `null` if no photo exists.
   */
  private fun getContactAvatar(context: Context, contactId: Long): ByteArray? {
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
          // Attempt to load high-resolution photo
          context.contentResolver
              .openInputStream(
                  Uri.withAppendedPath(
                      contactUri,
                      ContactsContract.Contacts.Photo.CONTENT_DIRECTORY,
                  )
              )
              ?.use { inputStream ->
                return inputStream.readBytes()
              }

          // Fallback to thumbnail URI
          context.contentResolver.openInputStream(photoUriString.toUri())?.use { inputStream ->
            return inputStream.readBytes()
          }
        }
      }
    }

    return null
  }
}
