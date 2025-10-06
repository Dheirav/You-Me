package com.example.youme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.youme.ui.auth.AuthScreen
import com.example.youme.ui.auth.AuthViewModel
import com.example.youme.ui.couple.CoupleLinkScreen
import com.example.youme.ui.auth.EmailVerificationScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val context = this
            var isAuthenticated by remember {
                mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
            }
            val authViewModel: AuthViewModel by viewModels()
            // On app start, sign out if not remembered
            LaunchedEffect(Unit) {
                if (isAuthenticated && !authViewModel.checkRememberMe(context)) {
                    FirebaseAuth.getInstance().signOut()
                    isAuthenticated = false
                }
            }
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (!isAuthenticated) "auth" else "main"
                    ) {
                        composable("auth") {
                            AuthScreen(
                                onAuthSuccess = {
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user != null && !user.isEmailVerified) {
                                        navController.navigate("verify") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    } else {
                                        isAuthenticated = true
                                        navController.navigate("main") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                },
                                viewModel = authViewModel,
                                context = context
                            )
                        }
                        composable("verify") {
                            EmailVerificationScreen(
                                onVerified = {
                                    isAuthenticated = true
                                    navController.navigate("main") {
                                        popUpTo("verify") { inclusive = true }
                                    }
                                },
                                onSignOut = {
                                    FirebaseAuth.getInstance().signOut()
                                    isAuthenticated = false
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            MainContent(
                                onNavigateToCoupleLink = {
                                    navController.navigate("couple_link")
                                },
                                onSignOut = {
                                    authViewModel.clearRememberMe(context)
                                    FirebaseAuth.getInstance().signOut()
                                    isAuthenticated = false
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("couple_link") {
                            CoupleLinkScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
