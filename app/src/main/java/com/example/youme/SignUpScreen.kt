package com.example.youme

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignUpScreen(onSwitch: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    if (success) {
        Snackbar { Text("Sign up successful!") }
        // Navigate to main app screen here
    }

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") }
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") }
    )
    Button(onClick = {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) success = true else error = task.exception?.message
            }
    }) {
        Text("Sign Up")
    }
    Button(onClick = onSwitch) { Text("Already have an account? Login") }
    error?.let { Snackbar { Text(it) } }
}

