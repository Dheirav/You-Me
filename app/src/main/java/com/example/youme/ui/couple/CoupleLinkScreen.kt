package com.example.youme.ui.couple

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.youme.ui.auth.AuthViewModel

@Composable
fun CoupleLinkScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    var coupleCode by remember { mutableStateOf("") }
    var coupleCodeMessage by remember { mutableStateOf("") }
    var linkCode by remember { mutableStateOf("") }
    var linkCodeMessage by remember { mutableStateOf("") }
    var isLinked by remember { mutableStateOf(false) }
    var partnerId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var linkLoading by remember { mutableStateOf(false) }
    var codeLoading by remember { mutableStateOf(false) }
    var linkSuccess by remember { mutableStateOf(false) }

    // Check if already linked
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLinked = authViewModel.isUserLinked()
        }
    }

    // Fetch partner info if linked
    LaunchedEffect(isLinked) {
        if (isLinked) {
            coroutineScope.launch {
                authViewModel.fetchPartnerInfo()
                // Set partnerId from partnerInfo if available
                partnerId = authViewModel.partnerInfo?.displayName ?: ""
            }
        } else {
            partnerId = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Couple Linking", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        if (isLinked) {
            val partnerName = authViewModel.partnerInfo?.displayName
            Text("You are linked!", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            if (!partnerName.isNullOrBlank()) {
                Text("Partner: $partnerName", style = MaterialTheme.typography.bodyLarge)
                // Show partnerId for demonstration (can be removed if not needed)
                Text("PartnerId: $partnerId", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Partner info not available", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                coroutineScope.launch {
                    codeLoading = true
                    authViewModel.unlinkPartner { result, msg ->
                        codeLoading = false
                        isLinked = result
                        coupleCodeMessage = msg
                    }
                }
            }, enabled = !codeLoading) {
                Text("Unlink Couple")
            }
            if (coupleCodeMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(coupleCodeMessage, color = MaterialTheme.colorScheme.error)
            }
        } else {
            // Show the generated code as text, not in an input field
            if (coupleCode.isNotEmpty()) {
                Text("Your Couple Code: $coupleCode", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(onClick = {
                coroutineScope.launch {
                    codeLoading = true
                    authViewModel.generateCoupleCode { success, codeOrMsg ->
                        coupleCode = if (success) codeOrMsg else ""
                        coupleCodeMessage = if (success) "Share this code with your partner." else codeOrMsg
                        codeLoading = false
                    }
                }
            }, enabled = !codeLoading) {
                Text("Generate Couple Code")
            }
            if (coupleCodeMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(coupleCodeMessage, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = linkCode,
                onValueChange = { linkCode = it },
                label = { Text("Enter Partner's Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                coroutineScope.launch {
                    linkLoading = true
                    authViewModel.linkWithCoupleCode(linkCode) { success, msg ->
                        linkSuccess = success
                        isLinked = success
                        linkCodeMessage = msg
                        linkLoading = false
                    }
                }
            }, enabled = !linkLoading) {
                Text("Link Couple")
            }
            if (linkCodeMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(linkCodeMessage, color = if (linkSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
