package com.devtamuno.kmp.contactpicker.contract

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.provider.ContactsContract
import com.devtamuno.kmp.contactpicker.data.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ACTION_PICK_CONTACTS = "android.provider.action.PICK_CONTACTS"
private const val EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS =
    "android.provider.extra.PICK_CONTACTS_REQUESTED_DATA_FIELDS"
private const val EXTRA_USE_SYSTEM_CONTACTS_PICKER =
    "android.provider.extra.USE_SYSTEM_CONTACTS_PICKER"
private const val EXTRA_PICK_CONTACTS_SELECTION_LIMIT =
    "android.provider.extra.PICK_CONTACTS_SELECTION_LIMIT"
private const val EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS =
    "android.provider.extra.PICK_CONTACTS_MATCH_ALL_DATA_FIELDS"
private const val SYSTEM_CONTACTS_PICKER_MIN_SDK = 37

/**
 * Android-specific implementation of the [ContactPicker] contract.
 */
internal actual class ContactPicker {

  private lateinit var picker: ManagedActivityResultLauncher<Void?, Uri?>
  private lateinit var multiPicker: ManagedActivityResultLauncher<Intent, ActivityResult>
  private val showCustomPicker = mutableStateOf(false)

  @Composable
  internal actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
    val callback by rememberUpdatedState(onContactSelected)
    if (::picker.isInitialized) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    picker =
      rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
          coroutineScope.launch(Dispatchers.IO) {
            val contact = getContactFromUri(context, it)
            callback(contact)
          }
        }
      }
  }

  @Composable
  internal actual fun RegisterMultiContactPicker(onContactsSelected: (List<Contact>) -> Unit) {
    val callback by rememberUpdatedState(onContactsSelected)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (showCustomPicker.value) {
      val contactsState = remember { mutableStateOf<List<Contact>>(emptyList()) }
      val isLoading = remember { mutableStateOf(true) }

      LaunchedEffect(Unit) {
        isLoading.value = true
        contactsState.value = fetchAllContacts(context)
        isLoading.value = false
      }

      CustomMultiContactPicker(
        contacts = contactsState.value,
        isLoading = isLoading.value,
        onDismiss = { showCustomPicker.value = false },
        onDone = { selected ->
          showCustomPicker.value = false
          coroutineScope.launch(Dispatchers.IO) {
            val enrichedContacts = selected.map { contact ->
              contact.copy(contactAvatar = getContactAvatar(context, contact.id.toLong()))
            }
            callback(enrichedContacts)
          }
        }
      )
    }

    if (::multiPicker.isInitialized) return

    multiPicker =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleMultiContactPickerResult(result, context, coroutineScope, callback)
      }
  }

  internal actual fun launchContactPicker() {
    picker.launch(null)
  }

  internal actual fun launchMultiContactPicker() {
    if (Build.VERSION.SDK_INT >= SYSTEM_CONTACTS_PICKER_MIN_SDK) {
      val intent = Intent(ACTION_PICK_CONTACTS).apply {
        putExtra(EXTRA_USE_SYSTEM_CONTACTS_PICKER, true)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // Optional: putExtra(EXTRA_PICK_CONTACTS_SELECTION_LIMIT, 20)
        putExtra(EXTRA_PICK_CONTACTS_MATCH_ALL_DATA_FIELDS, false)
        putStringArrayListExtra(
          EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS,
          arrayListOf(
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
          )
        )
      }
      multiPicker.launch(intent)
    } else {
      showCustomPicker.value = true
    }
  }

  private fun handleMultiContactPickerResult(
    result: ActivityResult,
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    callback: (List<Contact>) -> Unit
  ) {
    if (result.resultCode != Activity.RESULT_OK) return

    val data = result.data

    if (Build.VERSION.SDK_INT >= SYSTEM_CONTACTS_PICKER_MIN_SDK) {
      data?.data?.let { uri ->
        coroutineScope.launch(Dispatchers.IO) {
          val contacts = processContactPickerSessionUri(context, uri)
          callback(contacts)
        }
      }
    } else {
      val uris = buildList {
        data?.data?.let(::add)
        data?.clipData?.let { clipData ->
          for (i in 0 until clipData.itemCount) {
            add(clipData.getItemAt(i).uri)
          }
        }
      }.distinct()

      coroutineScope.launch(Dispatchers.IO) {
        val contacts = getContactsByUris(context, uris)
        callback(contacts)
      }
    }
  }
}
