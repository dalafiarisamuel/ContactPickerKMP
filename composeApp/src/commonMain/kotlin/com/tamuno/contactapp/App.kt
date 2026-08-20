package com.tamuno.contactapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            val contactPicker = rememberContactPickerState {
                println("Single Selected: ${it?.name}")
            }

            val multiContactPicker = rememberMultiContactPickerState {
                println("Multi Selected ${it.size} contacts")
            }

            val contactSelected by contactPicker.value
            val multiContactsSelected by multiContactPicker.value

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .safeContentPadding()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Contact Picker",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Selection Buttons
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            onClick = {
                                runWithPermission(readContacts) {
                                    contactPicker.launchContactPicker()
                                }
                            }
                        ) {
                            Text("Single Pick", style = MaterialTheme.typography.button)
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            onClick = {
                                runWithPermission(readContacts) {
                                    multiContactPicker.launchContactPicker()
                                }
                            }
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
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.LightGray.copy(alpha = 0.2f)
                            ),
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Text("Clear Selections", color = Color.DarkGray)
                        }
                    }
                }

                Divider(modifier = Modifier.alpha(0.3f))

                // Single Selection Display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "SINGLE SELECTION",
                        style = MaterialTheme.typography.overline,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (contactSelected != null) {
                        ContactProfile(contact = contactSelected!!)
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
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (multiContactsSelected.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            multiContactsSelected.forEachIndexed { index, contact ->
                                ContactProfile(contact = contact)
                                if (index < multiContactsSelected.lastIndex) {
                                    Divider(
                                        modifier = Modifier
                                            .padding(start = 64.dp)
                                            .alpha(0.15f)
                                    )
                                }
                            }
                        }
                    } else {
                        EmptyState("No contacts selected")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Color.LightGray.copy(alpha = 0.8f), style = MaterialTheme.typography.caption)
    }
}

@Composable
private fun ContactProfile(contact: Contact) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp)
    ) {
        ContactAvatar(contact = contact, size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.onSurface
            )
            if (contact.phoneNumbers.isNotEmpty()) {
                Text(
                    text = contact.phoneNumbers.first(),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ContactAvatar(
    contact: Contact,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val imageBitmap = contact.contactAvatar?.toPlatformImageBitmap()
    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contact.name,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = contact.name.take(1).uppercase()
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFF6200EE).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials.ifEmpty { "?" },
                color = Color(0xFF6200EE),
                fontSize = (size.value * 0.35f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
