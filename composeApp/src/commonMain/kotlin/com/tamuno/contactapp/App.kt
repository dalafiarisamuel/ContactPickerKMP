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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
                    .safeContentPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "KMP Contact Picker",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )

                // Selection Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                runWithPermission(readContacts) {
                                    contactPicker.launchContactPicker()
                                }
                            }
                        ) {
                            Text("Single Pick")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                runWithPermission(readContacts) {
                                    multiContactPicker.launchContactPicker()
                                }
                            }
                        ) {
                            Text("Multi Pick")
                        }
                    }

                    if (contactSelected != null || multiContactsSelected.isNotEmpty()) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                contactPicker.clear()
                                multiContactPicker.clear()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
                            ),
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Text("Clear All Selections", color = Color.DarkGray)
                        }
                    }
                }

                Divider()

                // Single Selection Display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Single Selection",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (contactSelected != null) {
                        ContactProfile(contact = contactSelected!!)
                    } else {
                        Text("No contact selected", color = Color.LightGray)
                    }
                }

                Divider()

                // Multi Selection Display
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Multi Selection (${multiContactsSelected.size})",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (multiContactsSelected.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(multiContactsSelected) { contact ->
                                ContactAvatar(
                                    contact = contact,
                                    size = 56.dp
                                )
                            }
                        }
                    } else {
                        Text(
                            "No contacts selected",
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactProfile(contact: Contact) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ContactAvatar(contact = contact, size = 80.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Medium
            )
            if (contact.phoneNumbers.isNotEmpty()) {
                Text(
                    text = contact.phoneNumbers.first(),
                    style = MaterialTheme.typography.body2,
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
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
