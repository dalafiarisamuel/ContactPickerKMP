package com.tamuno.contactapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.devtamuno.kmp.contactpicker.extension.getBitmapConverter
import com.devtamuno.kmp.contactpicker.rememberContactPickerState
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {

        val readContacts = rememberPermissionState(
            Permission.ReadContacts
        )

        val contactPicker = rememberContactPickerState {
            println(it?.name)
        }

        val contactSelected by contactPicker.value

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    runWithPermission(readContacts) {
                        contactPicker.launchContactPicker()
                    }
                }
            ) {
                Text("Choose Contact")
            }

            Spacer(modifier = Modifier.padding(20.dp))

            Text("Selected Contact: ${contactSelected?.name}")

            contactSelected?.contactAvatar?.let {
                val bitmap = getBitmapConverter().bitmapFromBytes(it)
                if (bitmap != null) {
                    Image(
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                            .height(200.dp),
                        bitmap = bitmap,
                        contentDescription = null
                    )
                }
            }
        }
    }
}