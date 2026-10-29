package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppDrawerContent
import com.example.ui.screens.AppPickerFullScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeveloperInfoScreen
import com.example.ui.screens.EncryptionScreen
import com.example.ui.screens.LegalType
import com.example.ui.screens.LegalWebScreen
import com.example.ui.screens.WizardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SfrDroidApp()
                }
            }
        }
    }
}

@Composable
fun SfrDroidApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Re-check permissions and background jobs on app resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                viewModel.checkDeveloperNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle back button logically
    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (uiState.currentScreen == AppScreen.WIZARD) {
            if (uiState.wizardStep > 0) {
                viewModel.prevWizardStep()
            } else if (uiState.config.wizardCompleted) {
                viewModel.navigateTo(AppScreen.DASHBOARD)
            }
        } else if (uiState.currentScreen != AppScreen.DASHBOARD) {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = uiState.currentScreen == AppScreen.DASHBOARD,
        drawerContent = {
            AppDrawerContent(
                currentScreen = uiState.currentScreen,
                onNavigate = { screen ->
                    viewModel.navigateTo(screen)
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        when (uiState.currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    }
                )
            }
            AppScreen.WIZARD -> {
                WizardScreen(viewModel = viewModel)
            }
            AppScreen.APP_PICKER -> {
                AppPickerFullScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.ENCRYPTION -> {
                EncryptionScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.DEVELOPER_INFO -> {
                DeveloperInfoScreen(
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.PRIVACY_POLICY -> {
                LegalWebScreen(
                    type = LegalType.PRIVACY_POLICY,
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.TERMS_OF_SERVICE -> {
                LegalWebScreen(
                    type = LegalType.TERMS_OF_SERVICE,
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
        }
    }
}
