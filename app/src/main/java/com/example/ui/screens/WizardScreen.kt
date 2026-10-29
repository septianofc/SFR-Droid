package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.InstalledAppItem
import com.example.ui.components.AppIconImage
import com.example.ui.viewmodel.AppFilter
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PermissionHelper
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Periodically verify permissions whenever user views or returns to wizard
    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    val stepTitles = listOf(
        "Selamat Datang",
        "Akses & Izin Sistem",
        "Pilih Aplikasi Sumber",
        "Konfigurasi Endpoint",
        "Pilih Field Payload",
        "Ringkasan & Aktivasi"
    )

    val stepSubtitles = listOf(
        "Langkah awal menyiapkan penerusan notifikasi otomatis",
        "Aktifkan izin Android yang diperlukan agar sistem dapat berjalan",
        "Pilih aplikasi terpasang di Android untuk dipantau",
        "Atur alamat URL webhook dan interval pengiriman",
        "Tentukan data apa saja yang dikirim ke server",
        "Verifikasi konfigurasi dan aktifkan layanan standby"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PANDUAN LANGKAH ${uiState.wizardStep + 1} DARI 6",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stepTitles[uiState.wizardStep],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    if (uiState.wizardStep > 0) {
                        IconButton(
                            onClick = { viewModel.prevWizardStep() },
                            modifier = Modifier.testTag("wizard_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke langkah sebelumnya"
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.config.wizardCompleted) {
                        TextButton(
                            onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                            modifier = Modifier.testTag("wizard_to_dashboard")
                        ) {
                            Text("Ke Dashboard")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            WizardBottomNav(
                currentStep = uiState.wizardStep,
                canProceed = when (uiState.wizardStep) {
                    1 -> uiState.permissions.isNotificationAccessGranted
                    2 -> uiState.config.selectedApps.isNotEmpty()
                    3 -> uiState.config.endpoint.isNotBlank() && uiState.config.endpoint.startsWith("http")
                    4 -> uiState.config.selectedFields.isNotEmpty()
                    else -> true
                },
                onBack = { viewModel.prevWizardStep() },
                onNext = { viewModel.nextWizardStep() },
                onFinish = { viewModel.finishWizard() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Visual Progress Bar
            LinearProgressIndicator(
                progress = { (uiState.wizardStep + 1) / 6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (uiState.wizardStep) {
                    0 -> WizardStepIntro(
                        subtitle = stepSubtitles[0],
                        onStart = { viewModel.nextWizardStep() }
                    )
                    1 -> WizardStepPermissions(
                        subtitle = stepSubtitles[1],
                        permissions = uiState.permissions,
                        onCheckPermissions = { viewModel.refreshPermissions() },
                        onOpenNotificationSettings = { PermissionHelper.openNotificationListenerSettings(context) },
                        onOpenBatterySettings = { PermissionHelper.openBatteryOptimizationSettings(context) }
                    )
                    2 -> WizardStepAppPicker(
                        subtitle = stepSubtitles[2],
                        allApps = uiState.allApps,
                        isLoading = uiState.isLoadingApps,
                        selectedApps = uiState.config.selectedApps,
                        searchQuery = uiState.appSearchQuery,
                        appFilter = uiState.appFilter,
                        onSearchChange = { viewModel.setAppSearchQuery(it) },
                        onFilterChange = { viewModel.setAppFilter(it) },
                        onToggleApp = { viewModel.toggleAppSelection(it) },
                        onSelectAll = { visiblePkgs -> viewModel.selectAllFiltered(visiblePkgs) },
                        onDeselectAll = { viewModel.deselectAll() },
                        onReloadApps = { viewModel.loadInstalledApps() }
                    )
                    3 -> WizardStepEndpoint(
                        subtitle = stepSubtitles[3],
                        endpoint = uiState.config.endpoint,
                        token = uiState.config.token,
                        intervalMinutes = uiState.config.intervalMinutes,
                        isTesting = uiState.isTestingApi,
                        testResult = uiState.testResult,
                        onEndpointChange = { viewModel.updateEndpoint(it) },
                        onTokenChange = { viewModel.updateToken(it) },
                        onIntervalChange = { viewModel.updateInterval(it) },
                        onTestApi = { viewModel.testEndpoint() },
                        onClearTestResult = { viewModel.clearTestResult() }
                    )
                    4 -> WizardStepPayloadFields(
                        subtitle = stepSubtitles[4],
                        selectedFields = uiState.config.selectedFields,
                        onToggleField = { viewModel.toggleField(it) }
                    )
                    5 -> WizardStepSummary(
                        subtitle = stepSubtitles[5],
                        uiState = uiState,
                        onActivateStandby = { viewModel.finishWizard() }
                    )
                }
            }
        }
    }
}

@Composable
fun WizardBottomNav(
    currentStep: Int,
    canProceed: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kembali")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (currentStep < 5) {
                Button(
                    onClick = onNext,
                    enabled = canProceed,
                    modifier = Modifier.testTag("nav_next_button")
                ) {
                    Text(if (currentStep == 0) "Mulai Setup" else "Lanjutkan")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_finish_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Selesai & Aktifkan")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 0: Welcome & Overview
// ---------------------------------------------------------------------------
@Composable
fun WizardStepIntro(
    subtitle: String,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.size(120.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.sfr_logo),
                contentDescription = "SFR Droid Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SFR Droid",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Notification Webhook Bridge & Forwarder",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FeatureRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Tangkap Realtime",
                    desc = "Membaca notifikasi dari aplikasi tertentu begitu muncul."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.FilterList,
                    title = "Pilih Aplikasi Visual",
                    desc = "Pilih langsung dari daftar aplikasi terpasang di HP Anda."
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Send,
                    title = "Kirim ke API / Webhook",
                    desc = "POST JSON otomatis ke endpoint server Anda secara aman."
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("intro_start_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Mulai Konfigurasi Wizard", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 1: Permissions
// ---------------------------------------------------------------------------
@Composable
fun WizardStepPermissions(
    subtitle: String,
    permissions: com.example.data.model.PermissionStatus,
    onCheckPermissions: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val postNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onCheckPermissions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (permissions.isNotificationAccessGranted)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (permissions.isNotificationAccessGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (permissions.isNotificationAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (permissions.isNotificationAccessGranted)
                            "Akses Notifikasi Aktif! Sistem siap menangkap pesan."
                        else
                            "Akses Notifikasi Belum Aktif. Anda wajib mengaktifkannya di pengaturan Android untuk melanjutkan.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 1. Notification Listener Access (Mandatory)
        item {
            PermissionCard(
                title = "1. Akses Pendengar Notifikasi (Wajib)",
                description = "Mengizinkan SFR Droid menerima siaran notifikasi dari aplikasi yang Anda pilih. Tanpa izin ini, aplikasi tidak bisa bekerja.",
                isGranted = permissions.isNotificationAccessGranted,
                buttonText = "Buka Izin Akses Notifikasi Android",
                icon = Icons.Default.NotificationsActive,
                onButtonClick = onOpenNotificationSettings,
                instructions = "Cari 'SFR Droid' di daftar listener Android, lalu aktifkan saklar izin."
            )
        }

        // 2. Battery Optimization (Recommended)
        item {
            PermissionCard(
                title = "2. Pengecualian Hemat Baterai (Disarankan)",
                description = "Mencegah Android mematikan proses background saat HP standby atau layar mati.",
                isGranted = permissions.isBatteryOptimizationIgnored,
                buttonText = "Atur Pengecualian Baterai",
                icon = Icons.Default.BatteryChargingFull,
                onButtonClick = onOpenBatterySettings,
                instructions = "Pilih 'Jangan Optimalkan' atau 'Izinkan Aktivitas Latar Belakang'."
            )
        }

        // 3. Post Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                PermissionCard(
                    title = "3. Izin Notifikasi Sistem (Android 13+)",
                    description = "Diperlukan untuk menampilkan status standby di bilah notifikasi dan pengujian simulasi.",
                    isGranted = permissions.isPostNotificationGranted,
                    buttonText = "Minta Izin Notifikasi",
                    icon = Icons.Default.Notifications,
                    onButtonClick = {
                        postNotifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    },
                    instructions = "Tekan 'Izinkan' pada dialog konfirmasi Android."
                )
            }
        }

        // Refresh Status Button
        item {
            OutlinedButton(
                onClick = onCheckPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("refresh_permissions_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Periksa Ulang Status Izin")
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onButtonClick: () -> Unit,
    instructions: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (isGranted) "AKTIF" else "BELUM AKTIF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Petunjuk: $instructions",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(buttonText)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 2: Installed Apps Picker (Visual, Real App Icons, Search & Filter)
// ---------------------------------------------------------------------------
@Composable
fun WizardStepAppPicker(
    subtitle: String,
    allApps: List<InstalledAppItem>,
    isLoading: Boolean,
    selectedApps: Set<String>,
    searchQuery: String,
    appFilter: AppFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (AppFilter) -> Unit,
    onToggleApp: (String) -> Unit,
    onSelectAll: (List<String>) -> Unit,
    onDeselectAll: () -> Unit,
    onReloadApps: () -> Unit
) {
    val filteredApps = remember(allApps, searchQuery, appFilter) {
        allApps.filter { app ->
            val matchesFilter = when (appFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }
            val matchesSearch = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_input"),
            placeholder = { Text("Cari nama aplikasi atau package...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = appFilter == AppFilter.ALL,
                onClick = { onFilterChange(AppFilter.ALL) },
                label = { Text("Semua (${allApps.size})") }
            )
            FilterChip(
                selected = appFilter == AppFilter.USER,
                onClick = { onFilterChange(AppFilter.USER) },
                label = { Text("Pengguna (${allApps.count { !it.isSystemApp }})") }
            )
            FilterChip(
                selected = appFilter == AppFilter.SYSTEM,
                onClick = { onFilterChange(AppFilter.SYSTEM) },
                label = { Text("Sistem (${allApps.count { it.isSystemApp }})") }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Status and Batch Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedApps.size} aplikasi dipilih",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selectedApps.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Row {
                TextButton(
                    onClick = { onSelectAll(filteredApps.map { it.packageName }) },
                    modifier = Modifier.testTag("select_all_button")
                ) {
                    Text("Pilih Semua", fontSize = 12.sp)
                }
                TextButton(
                    onClick = onDeselectAll,
                    modifier = Modifier.testTag("deselect_all_button")
                ) {
                    Text("Hapus Semua", fontSize = 12.sp)
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Apps List
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Membaca aplikasi terpasang di Android...", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else if (filteredApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tidak ada aplikasi yang cocok.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onReloadApps) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Muat Ulang Aplikasi")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isChecked = selectedApps.contains(app.packageName)
                    AppItemRow(
                        app = app,
                        isSelected = isChecked,
                        onToggle = { onToggleApp(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: InstalledAppItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surface,
        label = "app_row_bg"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .testTag("app_item_${app.packageName}"),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real Installed App Icon
            AppIconImage(
                drawable = app.icon,
                contentDescription = app.appName,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Sistem",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("checkbox_${app.packageName}")
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 3: Endpoint & Real API Test
// ---------------------------------------------------------------------------
@Composable
fun WizardStepEndpoint(
    subtitle: String,
    endpoint: String,
    token: String,
    intervalMinutes: Int,
    isTesting: Boolean,
    testResult: com.example.data.model.ApiCallResult?,
    onEndpointChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onTestApi: () -> Unit,
    onClearTestResult: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Alamat Penerima (Webhook URL)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Masukkan URL server Anda yang menerima HTTP POST JSON.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = endpoint,
                onValueChange = onEndpointChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("endpoint_input"),
                placeholder = { Text("https://domainanda.com/api/notify.php") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(12.dp)
            )

            if (endpoint.isNotBlank() && !endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                Text(
                    text = "URL harus diawali https:// atau http://",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }

        item {
            Text(
                text = "Token Autentikasi (Opsional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Jika diisi, akan dikirimkan di header: Authorization: Bearer <token>",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("token_input"),
                placeholder = { Text("Contoh: sk_live_abcdef123456") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Encryption Settings Option
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enkripsi Data (AES-256)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Standar keamanan opsional. Default tidak dienkripsi (No Enc). Anda dapat mengaktifkannya jika server Anda ingin menerima ciphertext.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Interval Heartbeat Otomatis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kirim ping berkala saat standby aktif: $intervalMinutes menit (min: 15 menit)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = intervalMinutes.toFloat(),
                onValueChange = { onIntervalChange(it.toInt()) },
                valueRange = 15f..240f,
                steps = 14,
                modifier = Modifier.testTag("interval_slider")
            )
        }

        // Test Endpoint Section (REAL CALL)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uji Coba Endpoint Langsung", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tekan tombol di bawah untuk mengirim JSON pengujian nyata ke server dan melihat respons HTTP aslinya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onTestApi,
                        enabled = endpoint.isNotBlank() && !isTesting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_endpoint_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengirim permintaan...")
                        } else {
                            Text("Kirim Tes Koneksi Sekarang")
                        }
                    }

                    // REAL TEST RESULT
                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        RealTestResultCard(testResult = testResult, onDismiss = onClearTestResult)
                    }
                }
            }
        }
    }
}

@Composable
fun RealTestResultCard(
    testResult: com.example.data.model.ApiCallResult,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (testResult.isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (testResult.isSuccess) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (testResult.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (testResult.httpCode > 0) "Kode HTTP: ${testResult.httpCode}" else "Koneksi Gagal",
                        fontWeight = FontWeight.Bold,
                        color = if (testResult.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                Text(
                    text = "${testResult.latencyMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = testResult.message,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )

            if (testResult.responseBody.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Respons Server: ${testResult.responseBody}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 4: Payload Fields Customization & Live JSON Preview
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardStepPayloadFields(
    subtitle: String,
    selectedFields: Set<String>,
    onToggleField: (String) -> Unit
) {
    val fieldOptions = listOf(
        "app" to "Nama Aplikasi (cth: WhatsApp)",
        "package" to "Package ID (cth: com.whatsapp)",
        "title" to "Judul Notifikasi (cth: Budi)",
        "message" to "Isi Pesan / Teks Notifikasi",
        "timestamp" to "Waktu Postingan (Epoch ms)",
        "key" to "Notification Key Unik Android",
        "id" to "Notification ID Integer"
    )

    // Build Live JSON Preview
    val previewJson = remember(selectedFields) {
        val obj = JSONObject()
        if (selectedFields.contains("app")) obj.put("app", "WhatsApp")
        if (selectedFields.contains("package")) obj.put("package", "com.whatsapp")
        if (selectedFields.contains("title")) obj.put("title", "Ahmad Fauzi")
        if (selectedFields.contains("message")) obj.put("message", "Halo, apakah pesanan sudah dikirim?")
        if (selectedFields.contains("timestamp")) obj.put("timestamp", 1730160000000L)
        if (selectedFields.contains("key")) obj.put("key", "0|com.whatsapp|101|null|10001")
        if (selectedFields.contains("id")) obj.put("id", 101)
        obj.put("event", "notification")
        obj.toString(2)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Pilih Field yang Dikirim ke Server",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Centang data apa saja yang ingin Anda sertakan di dalam payload JSON.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(fieldOptions) { (key, label) ->
            val isChecked = selectedFields.contains(key)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleField(key) }
                    .border(
                        width = 1.dp,
                        color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(10.dp)
                    ),
                color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleField(key) }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(text = "Key: \"$key\"", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pratinjau Format JSON Payload:", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E222A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = previewJson,
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF81D4FA),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 5: Summary & Standby Activation
// ---------------------------------------------------------------------------
@Composable
fun WizardStepSummary(
    subtitle: String,
    uiState: com.example.ui.viewmodel.MainUiState,
    onActivateStandby: () -> Unit
) {
    val permOk = uiState.permissions.isNotificationAccessGranted
    val appsOk = uiState.config.selectedApps.isNotEmpty()
    val endpointOk = uiState.config.endpoint.isNotBlank() && uiState.config.endpoint.startsWith("http")
    val fieldsOk = uiState.config.selectedFields.isNotEmpty()

    val allReady = permOk && appsOk && endpointOk && fieldsOk

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Ringkasan Konfigurasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Periksa kembali semua parameter sebelum mengaktifkan mode Standby.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCheckItem(
                        title = "Izin Akses Notifikasi Android",
                        value = if (permOk) "AKTIF (Terverifikasi)" else "BELUM AKTIF",
                        isOk = permOk
                    )
                    HorizontalDivider()
                    SummaryCheckItem(
                        title = "Aplikasi Sumber Dipantau",
                        value = "${uiState.config.selectedApps.size} aplikasi dipilih",
                        isOk = appsOk
                    )
                    HorizontalDivider()
                    SummaryCheckItem(
                        title = "URL Endpoint Webhook",
                        value = if (endpointOk) uiState.config.endpoint else "Belum diisi dengan benar",
                        isOk = endpointOk
                    )
                    HorizontalDivider()
                    SummaryCheckItem(
                        title = "Field Data Payload",
                        value = "${uiState.config.selectedFields.size} field aktif",
                        isOk = fieldsOk
                    )
                    HorizontalDivider()
                    SummaryCheckItem(
                        title = "Heartbeat Otomatis",
                        value = "Tiap ${uiState.config.intervalMinutes} menit",
                        isOk = true
                    )
                }
            }
        }

        if (!allReady) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ada langkah yang belum lengkap. Harap lengkapi izin, pilih minimal 1 aplikasi, dan isi endpoint sebelum mengaktifkan.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onActivateStandby,
                enabled = allReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("activate_standby_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Aktifkan Standby & Masuk Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SummaryCheckItem(
    title: String,
    value: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
            modifier = Modifier.size(20.dp)
        )
    }
}
