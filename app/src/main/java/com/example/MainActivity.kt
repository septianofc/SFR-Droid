package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppDrawerContent
import com.example.ui.screens.AppPickerFullScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeveloperInfoScreen
import com.example.ui.screens.EncryptionScreen
import com.example.ui.screens.FcmSetupScreen
import com.example.ui.screens.LegalType
import com.example.ui.screens.LegalWebScreen
import com.example.ui.screens.WebhookTemplateScreen
import com.example.ui.screens.WizardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SfrPrimary
import com.example.util.AppLockHelper
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
                    SfrDroidApp(intentData = intent?.data)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun SfrDroidApp(
    viewModel: MainViewModel = viewModel(),
    intentData: Uri? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Biometric / Device Lock Launcher
    val lockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.unlockApp()
        }
    }

    // Handle Deep Link
    LaunchedEffect(intentData) {
        if (intentData != null) {
            val scheme = intentData.scheme
            val host = intentData.host
            val path = intentData.path ?: ""

            when {
                scheme == "sfrdroid" -> {
                    when (host?.lowercase()) {
                        "apps", "picker" -> viewModel.navigateTo(AppScreen.APP_PICKER)
                        "encryption", "security", "lock" -> viewModel.navigateTo(AppScreen.ENCRYPTION)
                        "webhook", "template", "routing" -> viewModel.navigateTo(AppScreen.WEBHOOK_TEMPLATE)
                        "owner", "dev", "fcm", "backend" -> viewModel.navigateTo(AppScreen.FCM_SETUP)
                        "wizard", "setup" -> viewModel.navigateTo(AppScreen.WIZARD)
                        "developer", "devinfo", "author" -> viewModel.navigateTo(AppScreen.DEVELOPER_INFO)
                        "privacy", "kebijakan" -> viewModel.navigateTo(AppScreen.PRIVACY_POLICY)
                        "terms", "persyaratan" -> viewModel.navigateTo(AppScreen.TERMS_OF_SERVICE)
                        "overlay", "bubble" -> {
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                            viewModel.setFloatingBubble(true)
                        }
                        else -> viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                }
                host == "sfrdroid.septianfr.web.id" -> {
                    when {
                        path.contains("kebijakan-privasi") -> viewModel.navigateTo(AppScreen.PRIVACY_POLICY)
                        path.contains("persyaratan-pegunaan") -> viewModel.navigateTo(AppScreen.TERMS_OF_SERVICE)
                        else -> viewModel.navigateTo(AppScreen.DASHBOARD)
                    }
                }
            }
        }
    }

    // Re-check permissions and background jobs on app resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
                viewModel.checkDeveloperNotifications()
                com.example.service.SFRNotificationListenerService.ensureRebind(context)

                // If locked, trigger unlock prompt
                if (uiState.isAppLocked) {
                    val lockIntent = com.example.util.AppLockHelper.createConfirmDeviceCredentialIntent(context)
                    if (lockIntent != null) {
                        try {
                            lockLauncher.launch(lockIntent)
                        } catch (_: Exception) {}
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // App Lock Shield Overlay
    if (uiState.isAppLocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = SfrPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(88.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = SfrPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "SFR Droid Dilindungi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aplikasi ini terkunci untuk menjaga keamanan notifikasi dan webhook Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val lockIntent = com.example.util.AppLockHelper.createConfirmDeviceCredentialIntent(context)
                        if (lockIntent != null) {
                            try {
                                lockLauncher.launch(lockIntent)
                            } catch (_: Exception) {
                                viewModel.unlockApp()
                            }
                        } else {
                            viewModel.unlockApp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SfrPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Buka Kunci Aplikasi (Biometrik / PIN)", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
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
            AppScreen.WEBHOOK_TEMPLATE -> {
                WebhookTemplateScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.FCM_SETUP -> {
                FcmSetupScreen(
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
