package com.example.telegramclone.models

data class User(
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var full_name: String = "",
    var state: String = "",
    var phone: String = "",
    var photoUrl: String = "empty"
)