package com.example

import android.os.Bundle
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

        setContent {
            val userRoleStateProvider = remember { UserRoleStateProvider(repository) }

            NexGenLmsTheme {
                CompositionLocalProvider(LocalUserRoleState provides userRoleStateProvider) {
                    MainAppEntry(repository = repository)
                }
            }
        }
    }
}

@Composable
fun MainAppEntry(repository: AppRepository) {
    val currentUser by repository.currentUser.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by repository.hasCompletedOnboardingFlow.collectAsStateWithLifecycle()
    val globalLoadingState by repository.globalLoadingState.collectAsStateWithLifecycle()
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
                        onLogout = { repository.logout() },
                        modifier = modifier
                    )
                    UserRole.TUTOR -> TutorMainScreen(
                        repository = repository,
                        currentUser = user,
                        onLogout = { repository.logout() },
                        modifier = modifier
                    )
                    UserRole.SUPER_ADMIN -> AdminMainScreen(
                        repository = repository,
                        currentUser = user,
                        onLogout = { repository.logout() },
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
