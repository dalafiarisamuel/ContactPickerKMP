package com.tamuno.contactapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.devtamuno.kmp.contactpicker.data.Contact
import com.devtamuno.kmp.contactpicker.extension.toPlatformImageBitmap
import com.devtamuno.kmp.contactpicker.rememberContactPickerState
import com.devtamuno.kmp.contactpicker.rememberMultiContactPickerState
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun App() {
  MaterialTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
      val readContacts = rememberPermissionState(Permission.ReadContacts)
      var contactToShowDetails by remember { mutableStateOf<Contact?>(null) }

      val contactPicker = rememberContactPickerState { println("Single Selected: ${it?.name}") }

      val multiContactPicker = rememberMultiContactPickerState {
        println("Multi Selected ${it.size} contacts")
      }

      val contactSelected by contactPicker.value
      val multiContactsSelected by multiContactPicker.value

      Column(
          modifier =
              Modifier.fillMaxWidth()
                  .verticalScroll(rememberScrollState())
                  .safeContentPadding()
                  .padding(horizontal = 8.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
            text = "Contact Picker",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Selection Buttons
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                onClick = {
                  runWithPermission(readContacts) { contactPicker.launchContactPicker() }
                },
            ) {
              Text("Single Pick", style = MaterialTheme.typography.button)
            }

            Button(
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                onClick = {
                  runWithPermission(readContacts) { multiContactPicker.launchContactPicker() }
                },
            ) {
              Text("Multi Pick", style = MaterialTheme.typography.button)
            }
          }

          if (contactSelected != null || multiContactsSelected.isNotEmpty()) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                  contactPicker.clear()
                  multiContactPicker.clear()
                },
                shape = CircleShape,
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = Color.LightGray.copy(alpha = 0.2f)
                    ),
                elevation = ButtonDefaults.elevation(0.dp, 0.dp),
            ) {
              Text("Clear Selections", color = Color.DarkGray)
            }
          }
        }

        Divider(modifier = Modifier.alpha(0.3f))

        // Single Selection Display
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
          Text(
              text = "SINGLE SELECTION",
              style = MaterialTheme.typography.overline,
              color = Color.Gray,
              modifier = Modifier.padding(start = 12.dp),
          )
          Spacer(modifier = Modifier.height(4.dp))

          if (contactSelected != null) {
            ContactProfile(contact = contactSelected!!) { contactToShowDetails = contactSelected }
          } else {
            EmptyState("No contact selected")
          }
        }

        Divider(modifier = Modifier.alpha(0.3f))

        // Multi Selection Display
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
              text = "MULTI SELECTION (${multiContactsSelected.size})",
              style = MaterialTheme.typography.overline,
              color = Color.Gray,
              modifier = Modifier.padding(start = 12.dp),
          )
          Spacer(modifier = Modifier.height(4.dp))

          if (multiContactsSelected.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
              multiContactsSelected.forEachIndexed { index, contact ->
                ContactProfile(contact = contact) { contactToShowDetails = contact }
                if (index < multiContactsSelected.lastIndex) {
                  Divider(modifier = Modifier.padding(start = 64.dp).alpha(0.15f))
                }
              }
            }
          } else {
            EmptyState("No contacts selected")
          }
        }
      }

      if (contactToShowDetails != null) {
        ContactDetailsDialog(
            contact = contactToShowDetails!!,
            onDismiss = { contactToShowDetails = null },
        )
      }
    }
  }
}

@Composable
private fun EmptyState(message: String) {
  Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
    Text(
        message,
        color = Color.LightGray.copy(alpha = 0.8f),
        style = MaterialTheme.typography.caption,
    )
  }
}

@Composable
private fun ContactProfile(contact: Contact, onClick: () -> Unit) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.fillMaxWidth()
              .clickable(onClick = onClick)
              .heightIn(min = 56.dp)
              .padding(horizontal = 12.dp),
  ) {
    ContactAvatar(contact = contact, size = 40.dp)
    Spacer(modifier = Modifier.width(12.dp))
    Column(verticalArrangement = Arrangement.Center) {
      Text(
          text = contact.name,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colors.onSurface,
      )
      if (contact.phoneNumbers.isNotEmpty()) {
        Text(
            text = contact.phoneNumbers.first(),
            style = MaterialTheme.typography.caption,
            color = Color.Gray,
        )
      }
    }
  }
}

@Composable
private fun ContactDetailsDialog(contact: Contact, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colors.surface,
        elevation = 8.dp,
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Spacer(modifier = Modifier.height(8.dp))

          ContactAvatar(
              contact = contact,
              size = 120.dp,
              modifier =
                  Modifier.background(
                          color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                          shape = CircleShape,
                      )
                      .padding(4.dp),
          )

          Spacer(modifier = Modifier.height(20.dp))

          Text(
              text = contact.name,
              style = MaterialTheme.typography.h5,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colors.onSurface,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
              text = "ID: ${contact.id}",
              style = MaterialTheme.typography.caption,
              color = Color.Gray.copy(alpha = 0.6f),
              textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(24.dp))

          Divider(modifier = Modifier.alpha(0.1f))

          Spacer(modifier = Modifier.height(24.dp))

          Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            if (contact.phoneNumbers.isNotEmpty()) {
              ContactDetailSection(title = "Phone Numbers", items = contact.phoneNumbers)
            }

            if (contact.email.isNotEmpty()) {
              ContactDetailSection(title = "Emails", items = contact.email)
            }

            if (contact.phoneNumbers.isEmpty() && contact.email.isEmpty()) {
              Text(
                  text = "No contact details available.",
                  style = MaterialTheme.typography.body2,
                  color = Color.Gray,
                  modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                  textAlign = TextAlign.Center,
              )
            }
          }

          Spacer(modifier = Modifier.height(32.dp))

          Button(
              onClick = onDismiss,
              modifier = Modifier.fillMaxWidth(),
              shape = CircleShape,
              elevation = ButtonDefaults.elevation(0.dp, 0.dp),
              contentPadding = PaddingValues(vertical = 12.dp),
          ) {
            Text("DISMISS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun ContactDetailSection(title: String, items: List<String>) {
  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.overline,
        color = MaterialTheme.colors.primary,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.2.sp,
    )
    items.forEach { item ->
      Text(
          text = item,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colors.onSurface,
      )
    }
  }
}

@Composable
private fun ContactAvatar(
    contact: Contact,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
  val imageBitmap =
      remember(contact.contactAvatar) {
        contact.contactAvatar?.let { if (it.isNotEmpty()) it.toPlatformImageBitmap() else null }
      }

  if (imageBitmap != null) {
    Image(
        bitmap = imageBitmap,
        contentDescription = contact.name,
        modifier = modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
  } else {
    val initials =
        contact.name
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "?" }

    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
      Text(
          text = initials,
          color = MaterialTheme.colors.primary,
          fontSize = (size.value * 0.35f).sp,
          fontWeight = FontWeight.Bold,
      )
    }
  }
}
