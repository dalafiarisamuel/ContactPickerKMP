package com.devtamuno.kmp.contactpicker.contract

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlin.math.absoluteValue

private val AvatarColors =
    listOf(
        Color(0xFFEF5350),
        Color(0xFFEC407A),
        Color(0xFFAB47BC),
        Color(0xFF7E57C2),
        Color(0xFF5C6BC0),
        Color(0xFF42A5F5),
        Color(0xFF29B6F6),
        Color(0xFF26C6DA),
        Color(0xFF26A69A),
        Color(0xFF66BB6A),
        Color(0xFF9CCC65),
        Color(0xFFD4E157),
        Color(0xFFFFEE58),
        Color(0xFFFFCA28),
        Color(0xFFFFA726),
        Color(0xFFFF7043),
    )

private fun getAvatarColor(name: String): Color {
  val firstChar = name.firstOrNull() ?: return Color.Gray
  val index = firstChar.code.absoluteValue % AvatarColors.size
  return AvatarColors[index]
}

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
      remember(searchQuery, contacts) {
        if (searchQuery.isEmpty()) {
          contacts
        } else {
          contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
      }

  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.surface,
        topBar = {
          TopAppBar(
              title = { Text("Select contacts") },
              navigationIcon = {
                IconButton(onClick = onDismiss) {
                  Icon(Icons.Default.Close, contentDescription = "Close")
                }
              },
              actions = {
                OutlinedButton(
                    onClick = {
                      val selected = contacts.filter { it.id in selectedIds }
                      onDone(selected)
                    },
                    enabled = selectedIds.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    border =
                        BorderStroke(
                            width = 1.dp,
                            color =
                                if (selectedIds.isNotEmpty()) {
                                  MaterialTheme.colors.primary.copy(alpha = 0.3f)
                                } else {
                                  MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                },
                        ),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colors.primary,
                            backgroundColor = Color.Transparent,
                            disabledContentColor =
                                MaterialTheme.colors.onSurface.copy(alpha = 0.38f),
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier =
                        Modifier.padding(end = 8.dp).height(32.dp).align(Alignment.CenterVertically),
                ) {
                  Text(
                      text = if (selectedIds.isEmpty()) "Done" else "Done (${selectedIds.size})",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                  )
                }
              },
              backgroundColor = MaterialTheme.colors.surface,
              elevation = 0.dp,
          )
        },
    ) { padding ->
      Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        // Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f),
        ) {
          TextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = { Text("Search contacts") },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
              trailingIcon =
                  if (searchQuery.isNotEmpty()) {
                    {
                      IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                      }
                    }
                  } else null,
              singleLine = true,
              colors =
                  TextFieldDefaults.textFieldColors(
                      backgroundColor = Color.Transparent,
                      focusedIndicatorColor = Color.Transparent,
                      unfocusedIndicatorColor = Color.Transparent,
                      disabledIndicatorColor = Color.Transparent,
                  ),
              modifier = Modifier.fillMaxWidth(),
          )
        }

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
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f)
                  else Color.Transparent
              )
              .clickable(onClick = onToggle)
              .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    val avatarColor = remember(contact.name) { getAvatarColor(contact.name) }
    Box(
        modifier = Modifier.size(48.dp).clip(CircleShape).background(avatarColor),
        contentAlignment = Alignment.Center,
    ) {
      Text(
          text = contact.name.firstOrNull()?.uppercase() ?: "?",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
      )
    }

    Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
      Text(
          text = contact.name,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colors.onSurface,
      )
      if (contact.phoneNumbers.isNotEmpty()) {
        Text(
            text = contact.phoneNumbers.first(),
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
        )
      }
    }

    Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggle() },
        colors =
            CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colors.primary,
            ),
    )
  }
}
