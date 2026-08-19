package com.devtamuno.kmp.contactpicker.contract

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.devtamuno.kmp.contactpicker.data.Contact

/**
 * Android-specific implementation of the [ContactPicker] contract.
 */
internal actual class ContactPicker {

  private lateinit var picker: ManagedActivityResultLauncher<Void?, Uri?>
  private lateinit var multiPicker: ManagedActivityResultLauncher<Intent, ActivityResult>
  private val showCustomPicker = mutableStateOf(false)
  private var onContactsSelected: ((List<Contact>) -> Unit)? = null

  @Composable
  actual fun RegisterContactPicker(onContactSelected: (Contact?) -> Unit) {
    if (::picker.isInitialized) return
    val context = LocalContext.current
    picker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
          onContactSelected(uri?.let { getContactFromUri(context, it) })
        }
  }

  @Composable
  actual fun RegisterMultiContactPicker(onContactsSelected: (List<Contact>) -> Unit) {
    this.onContactsSelected = onContactsSelected
    val context = LocalContext.current

    multiPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
          if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uris = mutableListOf<Uri>()
            result.data?.data?.let { uris.add(it) }
            result.data?.clipData?.let { clipData ->
              for (i in 0 until clipData.itemCount) {
                uris.add(clipData.getItemAt(i).uri)
              }
            }
            val contacts = uris.mapNotNull { getContactFromUri(context, it) }
            onContactsSelected(contacts)
          } else {
            onContactsSelected(emptyList())
          }
        }

    if (showCustomPicker.value) {
      val contactsState = remember { mutableStateOf<List<Contact>>(emptyList()) }
      val isLoading = remember { mutableStateOf(true) }

      LaunchedEffect(Unit) {
        contactsState.value = fetchAllContacts(context)
        isLoading.value = false
      }

      CustomMultiContactPicker(
          contacts = contactsState.value,
          isLoading = isLoading.value,
          onDismiss = { showCustomPicker.value = false },
          onDone = { selected ->
            showCustomPicker.value = false
            val enrichedContacts = selected.map { contact ->
              contact.copy(contactAvatar = getContactAvatar(context, contact.id.toLong()))
            }
            onContactsSelected(enrichedContacts)
          }
      )
    }
  }

  actual fun launchContactPicker() {
    picker.launch(null)
  }

  actual fun launchMultiContactPicker() {
    if (Build.VERSION.SDK_INT >= 37) {
      val intent = Intent("android.intent.action.PICK_CONTACTS").apply {
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
      }
      multiPicker.launch(intent)
    } else {
      showCustomPicker.value = true
    }
  }
}
