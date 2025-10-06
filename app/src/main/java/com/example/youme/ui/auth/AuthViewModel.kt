package com.example.youme.ui.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private var firestoreListener: ListenerRegistration? = null

    // Add state for connection status
    private var _isConnected by mutableStateOf(false)
    val isConnected: Boolean get() = _isConnected

    var authState by mutableStateOf<AuthState>(AuthState.Initial)
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var displayName by mutableStateOf("")
        private set

    var phoneNumber by mutableStateOf("")
        private set

    var newPassword by mutableStateOf("")
        private set

    // --- Partner Info ---
    var partnerInfo by mutableStateOf<PartnerInfo?>(null)
        private set

    init {
        checkFirebaseConnection()
        // Check if user is already signed in
        auth.currentUser?.let { user ->
            if (user.isEmailVerified) {
                authState = AuthState.Success("Welcome back!")
            } else {
                authState = AuthState.Error("Please verify your email to continue")
            }
        }
    }

    private fun checkFirebaseConnection() {
        // Remove existing listener if any
        firestoreListener?.remove()

        // Check if user is authenticated
        val user = auth.currentUser
        if (user != null) {
            // If user is authenticated, check connection using their own document
            firestoreListener = firestore.collection("users")
                .document(user.uid)
                .addSnapshotListener { _, error ->
                    if (error != null) {
                        _isConnected = false
                        if (error.message?.contains("PERMISSION_DENIED") == true) {
                            // This is normal if user is not authenticated
                            _isConnected = true
                        } else {
                            authState = AuthState.Error("Network error: ${error.message}")
                        }
                        return@addSnapshotListener
                    }
                    _isConnected = true
                }
        } else {
            // If no user is authenticated, assume we're connected if Firebase Auth is accessible
            _isConnected = true

            // Add auth state listener to update connection status
            auth.addAuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    checkFirebaseConnection() // Recheck connection with authenticated user
                }
            }
        }
    }

    // Add this method to manually check connection
    fun refreshConnectionStatus() {
        checkFirebaseConnection()
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }

    data class PartnerInfo(
        val displayName: String?,
        val email: String?,
        val phoneNumber: String?
    )

    fun updateEmail(newEmail: String) {
        email = newEmail
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun updateDisplayName(name: String) {
        displayName = name
    }

    fun updatePhoneNumber(phone: String) {
        phoneNumber = phone
    }

    fun updateNewPassword(password: String) {
        newPassword = password
    }

    fun signUp() {
        if (!isValidInput()) return

        authState = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    // Send email verification
                    user.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                createUserProfile(user)
                            } else {
                                authState = AuthState.Error(task.exception?.message ?: "Failed to send verification email")
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                authState = AuthState.Error(formatErrorMessage(e.message ?: "Sign up failed"))
            }
    }

    private fun createUserProfile(user: FirebaseUser) {
        val userData = hashMapOf<String, Any>(
            "id" to user.uid,
            "email" to user.email.orEmpty(),
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "isEmailVerified" to user.isEmailVerified,
            "profileCreatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                authState = AuthState.Success(
                    message = "Account created! Please check your email for verification."
                )
            }
            .addOnFailureListener { e ->
                authState = AuthState.Error(formatErrorMessage(e.message ?: "Failed to create user profile"))
            }
    }

    // Call this from your Activity/Application to check if user should be signed out on app start
    fun checkRememberMe(context: Context): Boolean {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("remember_me", false)
    }

    // Save rememberMe preference on sign in
    fun saveRememberMe(context: Context, rememberMe: Boolean) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("remember_me", rememberMe).apply()
    }

    // Update signIn to accept rememberMe and save it
    fun signIn(rememberMe: Boolean = false, context: Context? = null) {
        if (!isValidInput()) return
        authState = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    if (!user.isEmailVerified) {
                        authState = AuthState.Error("Please verify your email before signing in")
                        return@let
                    }
                    context?.let { saveRememberMe(it, rememberMe) }
                    authState = AuthState.Success("Welcome back!")
                }
            }
            .addOnFailureListener { e ->
                authState = AuthState.Error(formatErrorMessage(e.message ?: "Sign in failed"))
            }
    }

    // Call this on sign out to clear the preference
    fun clearRememberMe(context: Context) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("remember_me").apply()
    }

    fun resetPassword() {
        if (email.isBlank()) {
            authState = AuthState.Error("Please enter your email address")
            return
        }

        authState = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                authState = AuthState.Success(
                    message = "Password reset email sent. Please check your inbox."
                )
            }
            .addOnFailureListener { e ->
                authState = AuthState.Error(formatErrorMessage(e.message ?: "Failed to send reset email"))
            }
    }

    fun resendVerificationEmail() {
        auth.currentUser?.let { user ->
            authState = AuthState.Loading
            user.sendEmailVerification()
                .addOnSuccessListener {
                    authState = AuthState.Success(
                        message = "Verification email sent. Please check your inbox."
                    )
                }
                .addOnFailureListener { e ->
                    authState = AuthState.Error(formatErrorMessage(e.message ?: "Failed to send verification email"))
                }
        }
    }

    fun signOut() {
        auth.signOut()
        email = ""
        password = ""
        displayName = ""
        phoneNumber = ""
        authState = AuthState.Initial
    }

    // Function to change password
    suspend fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            try {
                // Re-authenticate user
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                onResult(true, "Password updated successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Password update failed.")
            }
        } else {
            onResult(false, "No authenticated user.")
        }
    }

    // Function to update profile (display name)
    suspend fun updateProfile(newDisplayName: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            try {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build()
                user.updateProfile(profileUpdates).await()
                // Optionally update Firestore user profile as well
                firestore.collection("users").document(user.uid)
                    .update("displayName", newDisplayName).await()
                onResult(true, "Profile updated successfully.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Profile update failed.")
            }
        } else {
            onResult(false, "No authenticated user.")
        }
    }

    fun deleteAccount() {
        authState = AuthState.Loading
        val user = auth.currentUser
        if (user != null) {
            // First delete user data from Firestore
            firestore.collection("users")
                .document(user.uid)
                .delete()
                .addOnSuccessListener {
                    // Then delete the user account
                    user.delete()
                        .addOnSuccessListener {
                            authState = AuthState.Success("Account deleted successfully")
                            signOut() // Clear local state
                        }
                        .addOnFailureListener { e ->
                            authState = AuthState.Error(formatErrorMessage(e.message ?: "Failed to delete account"))
                        }
                }
                .addOnFailureListener { e ->
                    authState = AuthState.Error(formatErrorMessage(e.message ?: "Failed to delete user data"))
                }
        } else {
            authState = AuthState.Error("Please sign in again to delete your account")
        }
    }

    fun reauthenticateUser(password: String, onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(user.email ?: "", password)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    authState = AuthState.Error(formatErrorMessage(e.message ?: "Authentication failed"))
                }
        }
    }

    // --- Couple Linking Validation & Error Handling ---
    // Check if user is already linked
    suspend fun isUserLinked(): Boolean {
        val user = auth.currentUser ?: return false
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        return userDoc.getString("partnerId") != null
    }

    // Check if a code is valid and not expired (optionally, add expiration logic)
    suspend fun isCodeValid(code: String): Boolean {
        val codeDoc = firestore.collection("couple_codes").document(code).get().await()
        return codeDoc.exists()
    }

    // Optionally, add code expiration (e.g., 10 minutes)
    suspend fun cleanUpExpiredCodes(expirationMillis: Long = 10 * 60 * 1000) {
        val now = System.currentTimeMillis()
        val codes = firestore.collection("couple_codes").get().await()
        for (doc in codes.documents) {
            val createdAt = doc.getLong("createdAt") ?: continue
            if (now - createdAt > expirationMillis) {
                doc.reference.delete()
            }
        }
    }

    // Generate a unique 6-digit code and store it in Firestore for the current user
    suspend fun generateCoupleCode(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "No authenticated user.")
        // Delete any existing code for this user before generating a new one
        val codes = firestore.collection("couple_codes").whereEqualTo("userId", user.uid).get().await()
        for (doc in codes.documents) {
            doc.reference.delete()
        }
        val code = (100000..999999).random().toString()
        val codeRef = firestore.collection("couple_codes").document(code)
        try {
            val snapshot = codeRef.get().await()
            if (snapshot.exists()) {
                return onResult(false, "Code collision, try again.")
            }
            val coupleCode = hashMapOf(
                "code" to code,
                "userId" to user.uid,
                "createdAt" to System.currentTimeMillis()
            )
            codeRef.set(coupleCode).await()
            onResult(true, code)
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Failed to generate code.")
        }
    }

    // Link accounts using a 6-digit code
    suspend fun linkWithCoupleCode(inputCode: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "No authenticated user.")
        val userRef = firestore.collection("users").document(user.uid)
        val userDoc = userRef.get().await()
        if (userDoc.getString("partnerId") != null) {
            onResult(false, "You are already linked to a partner.")
            return
        }
        val codeRef = firestore.collection("couple_codes").document(inputCode)
        try {
            val snapshot = codeRef.get().await()
            if (!snapshot.exists()) {
                return onResult(false, "Invalid or expired code.")
            }
            val coupleCode = snapshot.data
            val partnerId = coupleCode?.get("userId") as? String
            if (partnerId == null || partnerId == user.uid) {
                return onResult(false, "Invalid or self code.")
            }
            val partnerRef = firestore.collection("users").document(partnerId)
            val partnerDoc = partnerRef.get().await()
            if (partnerDoc.getString("partnerId") != null) {
                return onResult(false, "This code's owner is already linked.")
            }
            val batch = firestore.batch()
            batch.update(userRef, "partnerId", partnerId)
            batch.update(partnerRef, "partnerId", user.uid)
            batch.delete(codeRef)
            batch.commit().await()
            onResult(true, "Accounts linked successfully.")
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Failed to link accounts.")
        }
    }

    // --- Partner Info ---
    suspend fun fetchPartnerInfo() {
        val user = auth.currentUser ?: return
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val partnerId = userDoc.getString("partnerId") ?: return
        val partnerDoc = firestore.collection("users").document(partnerId).get().await()
        partnerInfo = PartnerInfo(
            displayName = partnerDoc.getString("displayName"),
            email = partnerDoc.getString("email"),
            phoneNumber = partnerDoc.getString("phoneNumber")
        )
    }

    suspend fun unlinkPartner(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "No authenticated user.")
        val userRef = firestore.collection("users").document(user.uid)
        val userDoc = userRef.get().await()
        val partnerId = userDoc.getString("partnerId") ?: return onResult(false, "No partner to unlink.")
        val partnerRef = firestore.collection("users").document(partnerId)
        try {
            val batch = firestore.batch()
            batch.update(userRef, "partnerId", null)
            batch.update(partnerRef, "partnerId", null)
            batch.commit().await()
            partnerInfo = null
            onResult(true, "Unlinked successfully.")
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Failed to unlink.")
        }
    }

    private fun isValidInput(): Boolean {
        return when {
            email.isBlank() -> {
                authState = AuthState.Error("Email cannot be empty")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                authState = AuthState.Error("Please enter a valid email address")
                false
            }
            password.isBlank() -> {
                authState = AuthState.Error("Password cannot be empty")
                false
            }
            password.length < 6 -> {
                authState = AuthState.Error("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    private fun formatErrorMessage(message: String): String {
        return when {
            message.contains("email already in use", ignoreCase = true) ->
                "This email is already registered. Please sign in or use a different email."
            message.contains("weak password", ignoreCase = true) ->
                "Please choose a stronger password with at least 6 characters."
            message.contains("invalid email", ignoreCase = true) ->
                "Please enter a valid email address."
            message.contains("network error", ignoreCase = true) ->
                "Network error. Please check your internet connection."
            else -> message
        }
    }
}

// Data class for couple code
data class CoupleCode(val code: String = "", val userId: String = "")

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
}
