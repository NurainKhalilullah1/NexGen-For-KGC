package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.repository.AppRepository
import com.example.ui.theme.NexGenIndigoPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    repository: AppRepository,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var adminPasscode by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showGoogleAuthModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // NexGen App Logo / Header Card
                Surface(
                    color = NexGenIndigoPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "NexGen LMS Logo",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NexGen By KGC",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Tech Skills LMS for Young Learners",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Toggle Login / Register
                TabRow(
                    selectedTabIndex = if (isSignUp) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = isSignUp,
                        onClick = { isSignUp = true; errorMessage = null },
                        text = { Text("Register", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("register_tab")
                    )
                    Tab(
                        selected = !isSignUp,
                        onClick = { isSignUp = false; errorMessage = null },
                        text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("signin_tab")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error Banner if any
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Form Fields
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSignUp) {
                            Text(
                                text = "Select Your Account Role",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Role selector chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == UserRole.STUDENT,
                                    onClick = { selectedRole = UserRole.STUDENT },
                                    label = { Text("Student") },
                                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f).testTag("role_student_chip")
                                )

                                FilterChip(
                                    selected = selectedRole == UserRole.TUTOR,
                                    onClick = { selectedRole = UserRole.TUTOR },
                                    label = { Text("Tutor") },
                                    leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f).testTag("role_tutor_chip")
                                )

                                FilterChip(
                                    selected = selectedRole == UserRole.SUPER_ADMIN,
                                    onClick = { selectedRole = UserRole.SUPER_ADMIN },
                                    label = { Text("Admin") },
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f).testTag("role_admin_chip")
                                )
                            }

                            if (selectedRole == UserRole.SUPER_ADMIN) {
                                OutlinedTextField(
                                    value = adminPasscode,
                                    onValueChange = { adminPasscode = it },
                                    label = { Text("Admin Authorization Passcode") },
                                    placeholder = { Text("Enter Admin Authorization Passcode") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("admin_passcode_input")
                                )
                            }

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Temitope Nurain") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("fullname_input")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. user@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password."
                                    return@Button
                                }
                                isLoading = true
                                coroutineScope.launch {
                                    val result = if (isSignUp) {
                                        if (fullName.isBlank()) {
                                            errorMessage = "Please enter your full name."
                                            isLoading = false
                                            return@launch
                                        }
                                        repository.registerUser(
                                            email = email,
                                            password = password,
                                            fullName = fullName,
                                            requestedRole = selectedRole,
                                            adminPasscode = adminPasscode
                                        )
                                    } else {
                                        repository.loginUser(
                                            email = email,
                                            password = password
                                        )
                                    }

                                    isLoading = false
                                    if (result.isSuccess) {
                                        onAuthSuccess()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed."
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = if (isSignUp) "Create NexGen Account" else "Sign In to NexGen",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "  OR  ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { showGoogleAuthModal = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_auth_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    color = Color(0xFF4285F4),
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Strict Role Partitioning Enforced (Student, Tutor, Super Admin)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showGoogleAuthModal) {
        GoogleAuthModalDialog(
            selectedRole = selectedRole,
            onDismiss = { showGoogleAuthModal = false },
            onConfirmGoogleAuth = { googleEmail, googleName ->
                showGoogleAuthModal = false
                isLoading = true
                coroutineScope.launch {
                    val result = repository.loginWithGoogle(
                        email = googleEmail,
                        fullName = googleName,
                        requestedRole = selectedRole
                    )
                    isLoading = false
                    if (result.isSuccess) {
                        onAuthSuccess()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign in failed."
                    }
                }
            }
        )
    }
}

@Composable
fun GoogleAuthModalDialog(
    selectedRole: UserRole,
    onDismiss: () -> Unit,
    onConfirmGoogleAuth: (email: String, fullName: String) -> Unit
) {
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var selectedAccountIndex by remember { mutableIntStateOf(0) }

    val presetAccounts = remember {
        listOf(
            "student.learner@gmail.com" to "Alex Johnson (Student)",
            "tutor.instructor@gmail.com" to "Dr. Sarah Admin (Tutor)",
            "custom" to "Use another Google Account..."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF4285F4),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sign in with Google", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose an account to continue to NexGen LMS (${selectedRole.name}):",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                presetAccounts.forEachIndexed { index, (email, label) ->
                    Card(
                        onClick = { selectedAccountIndex = index },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedAccountIndex == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAccountIndex == index,
                                onClick = { selectedAccountIndex = index }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (email != "custom") {
                                    Text(text = email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                if (selectedAccountIndex == 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val (email, name) = if (selectedAccountIndex == 2) {
                        (if (customEmail.isBlank()) "user.google@gmail.com" else customEmail) to (if (customName.isBlank()) "Google User" else customName)
                    } else {
                        val (accEmail, accLabel) = presetAccounts[selectedAccountIndex]
                        accEmail to accLabel.substringBefore(" (")
                    }
                    onConfirmGoogleAuth(email, name)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
