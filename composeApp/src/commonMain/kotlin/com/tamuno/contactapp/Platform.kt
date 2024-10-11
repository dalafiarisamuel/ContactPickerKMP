package com.tamuno.contactapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform