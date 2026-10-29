package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.ui.theme.SfrPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PermissionHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.eventLogs.collectAsState()
    val context = LocalContext.current

    var showTestDialog by remember { mutableStateOf(false) }

    // Live Uptime Ticker
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(uiState.config.isStandby) {
        while (uiState.config.isStandby) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    val standbyColor by animateColorAsState(
        targetValue = if (uiState.config.isStandby) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
        label = "standby_indicator"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sfr_logo),
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SFR Droid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.config.isStandby) "Standby Mode Aktif" else "Standby Mode Nonaktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = standbyColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("dashboard_hamburger_menu")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu Navigasi Hamburger")
                    }
                },
                actions = {
                    // Developer Avatar shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DEVELOPER_INFO) },
                        modifier = Modifier.testTag("dashboard_dev_profile")
                    ) {
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(30.dp)
                                .border(1.5.dp, SfrPrimary, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.dev_avatar),
                                contentDescription = "Profil Developer",
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.startWizard() },
                        modifier = Modifier.testTag("dashboard_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Buka Wizard Pengaturan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Standby Controller Card with Live Stats
            item {
                StandbyControlCard(
                    isStandby = uiState.config.isStandby,
                    startTime = uiState.config.standbyStartTime,
                    currentTimeMs = currentTimeMs,
                    sentCount = uiState.config.sentNotificationsCount,
                    selectedAppsCount = uiState.config.selectedApps.size,
                    endpoint = uiState.config.endpoint,
                    isEncrypted = uiState.config.encryptPayload || uiState.config.encryptToken,
                    onToggleStandby = { viewModel.setStandby(it) },
                    onResetCount = { viewModel.resetSentCounter() }
                )
            }

            // Real System Permission Health Status
            item {
                PermissionStatusCard(
                    isNotificationGranted = uiState.permissions.isNotificationAccessGranted,
                    isBatteryIgnored = uiState.permissions.isBatteryOptimizationIgnored,
                    onOpenNotificationSettings = { PermissionHelper.openNotificationListenerSettings(context) },
                    onOpenBatterySettings = { PermissionHelper.openBatteryOptimizationSettings(context) }
                )
            }

            // Quick Actions Hub
            item {
                QuickActionsCard(
                    onTestApi = {
                        showTestDialog = true
                        viewModel.testEndpoint()
                    },
                    onSendSimulatedNotification = {
                        viewModel.triggerSimulatedNotification()
                    },
                    onOpenEncryption = {
                        viewModel.navigateTo(AppScreen.ENCRYPTION)
                    },
                    onOpenDeveloperInfo = {
                        viewModel.navigateTo(AppScreen.DEVELOPER_INFO)
                    },
                    onOpenApps = {
                        viewModel.navigateTo(AppScreen.APP_PICKER)
                    },
                    onOpenWizard = {
                        viewModel.startWizard()
                    }
                )
            }

            // Developer Banner Card
            item {
                DeveloperBannerCard(
                    onClick = { viewModel.navigateTo(AppScreen.DEVELOPER_INFO) }
                )
            }

            // Live Activity & Webhook Logs Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = SfrPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Transmisi Webhook (${logs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (logs.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier.testTag("clear_logs_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bersihkan", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Logs items
            if (logs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada notifikasi tertangkap. Notifikasi dari aplikasi terpilih akan otomatis diproses dan dikirim ke webhook.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { logItem ->
                    LogItemCard(item = logItem)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Real API Test Dialog
    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = SfrPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pengujian Endpoint Webhook")
                }
            },
            text = {
                Column {
                    if (uiState.isTestingApi) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mengirim paket HTTP POST nyata ke server...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (uiState.testResult != null) {
                        val res = uiState.testResult!!
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (res.isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (res.httpCode > 0) "Kode HTTP: ${res.httpCode}" else "Gagal Menghubungi Server",
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = res.message, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Latency: ${res.latencyMs} ms", style = MaterialTheme.typography.labelSmall)

                                if (res.responseBody.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Respons: ${res.responseBody}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTestDialog = false },
                    modifier = Modifier.testTag("dialog_close")
                ) {
                    Text("Tutup")
                }
            },
            dismissButton = {
                if (!uiState.isTestingApi) {
                    TextButton(onClick = { viewModel.testEndpoint() }) {
                        Text("Tes Lagi")
                    }
                }
            }
        )
    }
}

@Composable
fun StandbyControlCard(
    isStandby: Boolean,
    startTime: Long,
    currentTimeMs: Long,
    sentCount: Int,
    selectedAppsCount: Int,
    endpoint: String,
    isEncrypted: Boolean,
    onToggleStandby: (Boolean) -> Unit,
    onResetCount: () -> Unit
) {
    val elapsedMs = if (isStandby && startTime > 0) (currentTimeMs - startTime).coerceAtLeast(0L) else 0L
    val seconds = (elapsedMs / 1000) % 60
    val minutes = (elapsedMs / (1000 * 60)) % 60
    val hours = elapsedMs / (1000 * 60 * 60)
    val uptimeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStandby) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isStandby) Color(0xFF2E7D32) else Color(0xFFC62828))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isStandby) "STANDBY MODE AKTIF" else "STANDBY MODE NONAKTIF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isStandby) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isStandby,
                    onCheckedChange = onToggleStandby,
                    modifier = Modifier.testTag("standby_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2E7D32)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sent to webhook counter
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Terkirim Webhook",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sentCount > 0) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Hitungan",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable(onClick = onResetCount),
                                    tint = SfrPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$sentCount notifikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SfrPrimary
                        )
                    }
                }

                // Active Duration / Uptime
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Lama Waktu Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isStandby) uptimeFormatted else "00:00:00",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isStandby) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Endpoint & Encryption Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SfrPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endpoint.ifBlank { "Endpoint belum dikonfigurasi" },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isEncrypted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "AES-256",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (isStandby) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notifikasi status bar aktif secara real-time dengan tombol Hentikan (Stop).",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperBannerCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(2.dp, SfrPrimary, CircleShape),
                shadowElevation = 3.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.dev_avatar),
                    contentDescription = "Septian Fathur Rohman",
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Septian Fathur Rohman",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SfrPrimary
                )
                Text(
                    text = "Developer SFR Droid • WhatsApp, Telegram, Web",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.height(32.dp)
            ) {
                Text("Profil", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PermissionStatusCard(
    isNotificationGranted: Boolean,
    isBatteryIgnored: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Status Izin Sistem Android",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isNotificationGranted) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isNotificationGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Akses Notifikasi:", style = MaterialTheme.typography.bodySmall)
                }

                if (isNotificationGranted) {
                    Text("AKTIF", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                } else {
                    TextButton(onClick = onOpenNotificationSettings) {
                        Text("Aktifkan di Pengaturan", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBatteryIgnored) Icons.Default.Check else Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (isBatteryIgnored) Color(0xFF2E7D32) else Color(0xFFF57C00),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hemat Baterai:", style = MaterialTheme.typography.bodySmall)
                }

                if (isBatteryIgnored) {
                    Text("DIKECUALIKAN", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                } else {
                    TextButton(onClick = onOpenBatterySettings) {
                        Text("Kecualikan Pembatasan", fontSize = 11.sp, color = Color(0xFFF57C00), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onTestApi: () -> Unit,
    onSendSimulatedNotification: () -> Unit,
    onOpenEncryption: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenWizard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Aksi Cepat & Navigasi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onTestApi,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_test_api")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tes API", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onSendSimulatedNotification,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_sim_notif")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tes Suara", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenEncryption,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_encryption")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enkripsi (AES)", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenApps,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_edit_apps")
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ubah Aplikasi", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenDeveloperInfo,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_dev_info")
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Info Developer", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenWizard,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_open_wizard")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wizard Ulang", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LogItemCard(item: EventLogItem) {
    val timeStr = remember(item.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))
    }

    val icon = when (item.type) {
        LogEventType.NOTIFICATION_CAPTURED -> Icons.Default.NotificationsActive
        LogEventType.WEBHOOK_SENT -> Icons.Default.Check
        LogEventType.WEBHOOK_FAILED -> Icons.Default.Warning
        LogEventType.HEARTBEAT_PING -> Icons.Default.Sync
        LogEventType.TEST_PING -> Icons.Default.PlayArrow
        LogEventType.DEV_UPDATE_RECEIVED -> Icons.Default.Info
        LogEventType.SYSTEM_EVENT -> Icons.Default.Info
    }

    val iconColor = if (item.success) SfrPrimary else MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
