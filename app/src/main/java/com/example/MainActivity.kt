package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WizardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

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

    // Re-check permissions whenever user switches back from Android Settings screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle back button logically
    BackHandler(enabled = true) {
        if (uiState.currentScreen == AppScreen.WIZARD) {
            if (uiState.wizardStep > 0) {
                viewModel.prevWizardStep()
            } else if (uiState.config.wizardCompleted) {
                viewModel.navigateTo(AppScreen.DASHBOARD)
            }
        } else if (uiState.currentScreen == AppScreen.DASHBOARD) {
            // Already at home dashboard
        }
    }

    when (uiState.currentScreen) {
        AppScreen.WIZARD -> {
            WizardScreen(viewModel = viewModel)
        }
        AppScreen.DASHBOARD, AppScreen.LOGS_VIEWER -> {
            DashboardScreen(viewModel = viewModel)
        }
    }
}
