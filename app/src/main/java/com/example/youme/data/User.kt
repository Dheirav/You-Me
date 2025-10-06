package com.example.youme.data

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val isEmailVerified: Boolean = false,
    val profileCreatedAt: Long = System.currentTimeMillis()
)
