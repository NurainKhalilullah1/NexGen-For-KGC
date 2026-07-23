package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.LocalUserRoleState
import com.example.data.auth.UserRoleStateProvider
import com.example.data.local.AppDatabase
import com.example.data.model.UserRole
import com.example.data.remote.InsforgeErrorHandler
import com.example.data.repository.AppRepository
import com.example.ui.admin.AdminMainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.components.GlobalLoadingOverlay
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.student.StudentMainScreen
import com.example.ui.theme.NexGenLmsTheme
import com.example.ui.tutor.TutorMainScreen

class MainActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        repository = AppRepository(db, applicationContext)

        com.example.notification.NotificationHelper.createNotificationChannels(applicationContext)

        // Handle OAuth redirect that arrives while the app is already running
        handleOAuthIntent(intent)

        setContent {
            val userRoleStateProvider = remember { UserRoleStateProvider(repository) }

            NexGenLmsTheme {
                CompositionLocalProvider(LocalUserRoleState provides userRoleStateProvider) {
                    MainAppEntry(repository = repository)
                }
            }
        }
    }

    /** Called when the app is already in the foreground and Chrome redirects back via deep-link */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme != "nexgen" || data.host != "auth") return

        // InsForge puts the token in the fragment (e.g. nexgen://auth/callback#access_token=xxx)
        // or as a query param depending on configuration — we check both.
        val fragment = data.fragment ?: ""
        val accessToken = fragment
            .split("&")
            .firstOrNull { it.startsWith("access_token=") }
            ?.removePrefix("access_token=")
            ?: data.getQueryParameter("access_token")
            ?: return

        // Dispatch asynchronously — MainAppEntry observes oauthResult StateFlow for the outcome
        kotlinx.coroutines.MainScope().launch {
            repository.handleGoogleOAuthCallback(accessToken)
        }
    }
}

@Composable
fun MainAppEntry(repository: AppRepository) {
    val currentUser by repository.currentUser.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by repository.hasCompletedOnboardingFlow.collectAsStateWithLifecycle()
    val globalLoadingState by repository.globalLoadingState.collectAsStateWithLifecycle()
    val oauthResult by repository.oauthResult.collectAsStateWithLifecycle()
    val roleStateProvider = LocalUserRoleState.current
    val isCheckingInsforgeRole by roleStateProvider?.isCheckingInsforge?.collectAsStateWithLifecycle() ?: remember { androidx.compose.runtime.mutableStateOf(false) }
    val insforgeMessage by roleStateProvider?.verificationMessage?.collectAsStateWithLifecycle() ?: remember { androidx.compose.runtime.mutableStateOf(null) }

    val isLoading = (globalLoadingState?.isLoading == true) || isCheckingInsforgeRole
    val displayMessage = globalLoadingState?.message ?: insforgeMessage ?: "Authenticating & Syncing Data..."

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        InsforgeErrorHandler.errorEvents.collect { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    // Show OAuth error as snackbar; success is handled by currentUser StateFlow changing
    LaunchedEffect(oauthResult) {
        val result = oauthResult ?: return@LaunchedEffect
        if (!result.success) {
            snackbarHostState.showSnackbar(result.errorMessage ?: "Google Sign-In failed.")
        }
        repository.clearOAuthResult()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val handleLogout = remember(context) {
        {
            repository.logout()
            android.widget.Toast.makeText(context, "Logged out successfully", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.testTag("global_snackbar_host")) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        Box(modifier = Modifier.fillMaxSize()) {
            val user = currentUser
            if (user == null) {
                if (!hasCompletedOnboarding) {
                    OnboardingScreen(
                        onFinishOnboarding = { repository.completeOnboarding() },
                        modifier = modifier
                    )
                } else {
                    AuthScreen(
                        repository = repository,
                        onAuthSuccess = { /* StateFlow updates automatically */ },
                        modifier = modifier
                    )
                }
            } else {
                when (user.role) {
                    UserRole.STUDENT -> StudentMainScreen(
                        repository = repository,
                        currentUser = user,
                        onLogout = handleLogout,
                        modifier = modifier
                    )
                    UserRole.TUTOR -> TutorMainScreen(
                        repository = repository,
                        currentUser = user,
                        onLogout = handleLogout,
                        modifier = modifier
                    )
                    UserRole.SUPER_ADMIN -> AdminMainScreen(
                        repository = repository,
                        currentUser = user,
                        onLogout = handleLogout,
                        modifier = modifier
                    )
                }
            }

            // Global Loading Indicator Overlay
            GlobalLoadingOverlay(
                isLoading = isLoading,
                message = displayMessage,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
