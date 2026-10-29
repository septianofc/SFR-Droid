package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.EventLogItem
import com.example.data.model.LogEventType
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PermissionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.eventLogs.collectAsState()
    val context = LocalContext.current

    var showTestDialog by remember { mutableStateOf(false) }

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
                                text = if (uiState.config.isStandby) "Layanan Berjalan Aktif" else "Layanan Standby Mati",
                                style = MaterialTheme.typography.labelSmall,
                                color = standbyColor
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshPermissions() },
                        modifier = Modifier.testTag("dashboard_refresh")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Segarkan")
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
            // Main Standby Controller Card
            item {
                StandbyControlCard(
                    isStandby = uiState.config.isStandby,
                    onToggleStandby = { viewModel.setStandby(it) },
                    selectedAppsCount = uiState.config.selectedApps.size,
                    endpoint = uiState.config.endpoint
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
                    onOpenApps = {
                        viewModel.goToWizardStep(2)
                        viewModel.navigateTo(AppScreen.WIZARD)
                    },
                    onOpenWizard = {
                        viewModel.startWizard()
                    }
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
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Aktivitas Realtime (${logs.size})",
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
                                text = "Belum ada log aktivitas. Notifikasi yang tertangkap akan muncul di sini secara realtime.",
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
                    Icon(Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
    onToggleStandby: (Boolean) -> Unit,
    selectedAppsCount: Int,
    endpoint: String
) {
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
                        text = if (isStandby) "STANDBY AKTIF" else "STANDBY NONAKTIF",
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isStandby)
                    "Notifikasi dari $selectedAppsCount aplikasi terpilih akan langsung dikirim ke webhook secara instan."
                else
                    "Layanan sedang dijeda. Aktifkan saklar untuk mulai meneruskan notifikasi.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isStandby) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endpoint.ifBlank { "Endpoint belum dikonfigurasi" },
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
                text = "Aksi Cepat & Pengujian",
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
                    Text("Tes Notif", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
        LogEventType.SYSTEM_EVENT -> Icons.Default.Info
    }

    val iconColor = if (item.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

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
