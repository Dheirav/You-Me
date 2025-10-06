package com.example.youme.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
    context: android.content.Context? = null
) {
    var isSignUp by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showCoupleCodeDialog by remember { mutableStateOf(false) }
    var showLinkCodeDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var coupleCode by remember { mutableStateOf("") }
    var coupleCodeMessage by remember { mutableStateOf("") }
    var linkCode by remember { mutableStateOf("") }
    var linkCodeMessage by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var phoneNumberInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var showAccountSettings by remember { mutableStateOf(false) }
    var showPasswordVisibility by remember { mutableStateOf(false) }  // Add this line
    var showDeletePasswordVisibility by remember { mutableStateOf(false) }
    var showCurrentPasswordVisibility by remember { mutableStateOf(false) }
    var showNewPasswordVisibility by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.authState) {
        if (viewModel.authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    // Sync input fields with viewModel when screen changes
    LaunchedEffect(isSignUp, showResetPassword) {
        emailInput = viewModel.email
        passwordInput = viewModel.password
        displayNameInput = viewModel.displayName
        phoneNumberInput = viewModel.phoneNumber
        newPasswordInput = viewModel.newPassword
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = {
                Column {
                    Text("This action cannot be undone. Please enter your password to confirm.")
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = if (showDeletePasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        trailingIcon = {
                            IconButton(onClick = { showDeletePasswordVisibility = !showDeletePasswordVisibility }) {
                                Icon(
                                    imageVector = if (showDeletePasswordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showDeletePasswordVisibility) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reauthenticateUser(currentPassword) {
                            viewModel.deleteAccount()
                            showDeleteConfirmation = false
                            currentPassword = ""
                        }
                    }
                ) {
                    Text("Confirm Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    currentPassword = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasswordChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordChangeDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = if (showCurrentPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPasswordVisibility = !showCurrentPasswordVisibility }) {
                                Icon(
                                    imageVector = if (showCurrentPasswordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showCurrentPasswordVisibility) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it; viewModel.updateNewPassword(it) },
                        label = { Text("New Password") },
                        visualTransformation = if (showNewPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPasswordVisibility = !showNewPasswordVisibility }) {
                                Icon(
                                    imageVector = if (showNewPasswordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showNewPasswordVisibility) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.changePassword(
                                currentPassword,
                                newPasswordInput
                            ) { _, _ ->  // Rename parameters to _ since they're unused
                                // Optionally show a message to the user
                                showPasswordChangeDialog = false
                                currentPassword = ""
                                viewModel.updateNewPassword("")
                            }
                        }
                    }
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordChangeDialog = false
                    currentPassword = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = !showResetPassword,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = if (isSignUp) "Create Account" else "Sign In",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        AnimatedVisibility(
            visible = showResetPassword,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                viewModel.updateEmail(it)
            },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = !showResetPassword) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        viewModel.updatePassword(it)
                    },
                    label = { Text("Password") },
                    visualTransformation = if (showPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showPasswordVisibility = !showPasswordVisibility }) {
                            Icon(
                                imageVector = if (showPasswordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                // Add Remember Me below password, only for sign-in
                if (!isSignUp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text("Remember Me", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                AnimatedVisibility(visible = isSignUp) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = displayNameInput,
                            onValueChange = {
                                displayNameInput = it
                                viewModel.updateDisplayName(it)
                            },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneNumberInput,
                            onValueChange = {
                                phoneNumberInput = it
                                viewModel.updatePhoneNumber(it)
                            },
                            label = { Text("Phone Number (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when {
            showResetPassword -> {
                Button(
                    onClick = { viewModel.resetPassword() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Reset Link")
                }
            }
            else -> {
                Button(
                    onClick = { if (isSignUp) viewModel.signUp() else viewModel.signIn(rememberMe, context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSignUp) "Sign Up" else "Sign In")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    if (showResetPassword) {
                        showResetPassword = false
                    } else {
                        isSignUp = !isSignUp
                    }
                }
            ) {
                Text(
                    if (showResetPassword) "Back to Sign In"
                    else if (isSignUp) "Already have an account?"
                    else "Need an account?"
                )
            }

            AnimatedVisibility(visible = !isSignUp && !showResetPassword) {
                TextButton(onClick = { showResetPassword = true }) {
                    Text("Forgot Password?")
                }
            }
        }

        AnimatedVisibility(
            visible = viewModel.authState is AuthState.Success,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Text(
                    "Account Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedButton(
                    onClick = { showPasswordChangeDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Password")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        coupleCodeMessage = ""
                        coupleCode = ""
                        showCoupleCodeDialog = true
                        coroutineScope.launch {
                            viewModel.generateCoupleCode { success, codeOrMsg ->
                                if (success) {
                                    coupleCode = codeOrMsg
                                } else {
                                    coupleCodeMessage = codeOrMsg
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Couple Code")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        linkCode = ""
                        linkCodeMessage = ""
                        showLinkCodeDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Link with Partner's Code")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Account")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Sign Out")
                }
            }
        }

        // Add a button to toggle account settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = { showAccountSettings = !showAccountSettings }) {
                Text(if (showAccountSettings) "Hide Account Settings" else "Show Account Settings")
            }
        }
        AnimatedVisibility(visible = showAccountSettings) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Account Settings", style = MaterialTheme.typography.titleMedium)
                // You can add more account settings UI here
            }
        }

        if (viewModel.authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }

        AnimatedVisibility(
            visible = viewModel.authState is AuthState.Error ||
                     viewModel.authState is AuthState.Success,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val message = when (val state = viewModel.authState) {
                is AuthState.Error -> state.message
                is AuthState.Success -> state.message
                else -> ""
            }
            val isError = viewModel.authState is AuthState.Error

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = if (isError)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    if (showCoupleCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCoupleCodeDialog = false },
            title = { Text("Your Couple Code") },
            text = {
                if (coupleCode.isNotEmpty()) {
                    Column {
                        Text("Share this code with your partner:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(coupleCode, style = MaterialTheme.typography.headlineLarge)
                    }
                } else {
                    Text(coupleCodeMessage.ifEmpty { "Generating code..." })
                }
            },
            confirmButton = {
                TextButton(onClick = { showCoupleCodeDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLinkCodeDialog) {
        AlertDialog(
            onDismissRequest = { showLinkCodeDialog = false },
            title = { Text("Link with Partner") },
            text = {
                Column {
                    OutlinedTextField(
                        value = linkCode,
                        onValueChange = { linkCode = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("Enter 6-digit Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (linkCodeMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(linkCodeMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (linkCode.length == 6) {
                            coroutineScope.launch {
                                viewModel.linkWithCoupleCode(linkCode) { success, msg ->
                                    linkCodeMessage = msg
                                    if (success) {
                                        showLinkCodeDialog = false
                                    }
                                }
                            }
                        } else {
                            linkCodeMessage = "Please enter a valid 6-digit code."
                        }
                    }
                ) {
                    Text("Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkCodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
