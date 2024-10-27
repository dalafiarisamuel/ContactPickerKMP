package com.devtamuno.kmp.contactpicker.data

data class Contact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>,
    val email: String
)
