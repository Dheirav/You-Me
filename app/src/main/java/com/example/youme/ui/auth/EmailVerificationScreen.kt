package com.example.youme.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    var isVerified by remember { mutableStateOf(user?.isEmailVerified == true) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        user?.reload()
        isVerified = user?.isEmailVerified == true
    }

    if (isVerified) {
        onVerified()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Please verify your email address to continue.", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isLoading = true
                user?.sendEmailVerification()?.addOnCompleteListener { task ->
                    isLoading = false
                    message = if (task.isSuccessful) {
                        "Verification email sent. Please check your inbox."
                    } else {
                        "Failed to send verification email."
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("Resend Verification Email")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                isLoading = true
                user?.reload()?.addOnCompleteListener {
                    isLoading = false
                    isVerified = user?.isEmailVerified == true
                    if (isVerified) {
                        message = "Email verified!"
                        onVerified()
                    } else {
                        message = "Email not verified yet."
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text("I've Verified My Email")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSignOut, enabled = !isLoading) {
            Text("Sign Out")
        }
        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
    }
}

