package com.devtamuno.kmp.contactpicker.contract

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devtamuno.kmp.contactpicker.data.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun CustomMultiContactPicker(
    contacts: List<Contact>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onDone: (List<Contact>) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    val filteredContacts =
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                TopAppBar(
                    title = { Text("Select Contacts") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val selected = contacts.filter { it.id in selectedIds }
                                onDone(selected)
                            }
                        ) {
                            Text(
                                "DONE (${selectedIds.size})",
                                color = MaterialTheme.colors.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                )

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    placeholder = { Text("Search contacts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors =
                    TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredContacts, key = { it.id }) { contact ->
                            ContactItem(
                                contact = contact,
                                isSelected = contact.id in selectedIds,
                                onToggle = {
                                    if (contact.id in selectedIds) {
                                        selectedIds.remove(contact.id)
                                    } else {
                                        selectedIds.add(contact.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(contact: Contact, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Placeholder for Avatar (since we don't fetch all for the list)
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }

        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(text = contact.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (contact.phoneNumbers.isNotEmpty()) {
                Text(text = contact.phoneNumbers.first(), fontSize = 14.sp, color = Color.Gray)
            }
        }

        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
    }
}

internal suspend fun fetchAllContacts(context: Context): List<Contact> =
    withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        // Projection for basic contact info
        val contactProjection =
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
            )

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

        // Fetch Phone numbers in one query
        val phoneMap = mutableMapOf<String, MutableList<String>>()
        val phoneCursor =
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                null,
                null,
                null,
            )
        phoneCursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val number = it.getString(numberIndex)
                phoneMap.getOrPut(id) { mutableListOf() }.add(number)
            }
        }

        // Fetch Emails in one query
        val emailMap = mutableMapOf<String, MutableList<String>>()
        val emailCursor =
            contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                ),
                null,
                null,
                null,
            )
        emailCursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val addressIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val address = it.getString(addressIndex)
                emailMap.getOrPut(id) { mutableListOf() }.add(address)
            }
        }

        for (id in contactIds) {
            contacts.add(
                Contact(
                    id = id,
                    name = contactNames[id] ?: "",
                    phoneNumbers = phoneMap[id] ?: emptyList(),
                    email = emailMap[id] ?: emptyList(),
                    contactAvatar = null // Hydrate on selection for performance
                )
            )
        }

        contacts
    }
