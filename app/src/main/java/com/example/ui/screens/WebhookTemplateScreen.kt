package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppWebhookRule
import com.example.data.model.DispatchMode
import com.example.data.model.WebhookFormatMode
import com.example.ui.theme.SfrPrimary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WebhookTemplateScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.config
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Format Webhook & Shortcode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Kustomisasi Parameter (MacroDroid style)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SfrPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = SfrPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Format & Parameter", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Multi Webhook", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Fitur Lanjut", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 1: Format & Parameter Shortcode
                        FormatSettingsContent(
                            config = config,
                            viewModel = viewModel
                        )
                    }
                    1 -> {
                        // TAB 2: Multi Webhook & Routing
                        MultiWebhookContent(
                            config = config,
                            viewModel = viewModel,
                            onOpenAddDialog = { showAddRuleDialog = true }
                        )
                    }
                    2 -> {
                        // TAB 3: Fitur Lanjutan (Heartbeat, Floating Bubble, Kunci Aplikasi)
                        AdvancedFeaturesContent(
                            config = config,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AddRuleDialog(
            viewModel = viewModel,
            onDismiss = { showAddRuleDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormatSettingsContent(
    config: com.example.data.model.SfrConfig,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pilih Mode Format Pengiriman",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.setFormatMode(WebhookFormatMode.DEFAULT_JSON) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.formatMode == WebhookFormatMode.DEFAULT_JSON) SfrPrimary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "JSON Standar",
                        color = if (config.formatMode == WebhookFormatMode.DEFAULT_JSON) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.setFormatMode(WebhookFormatMode.CUSTOM_TEMPLATE) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.formatMode == WebhookFormatMode.CUSTOM_TEMPLATE) SfrPrimary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "Custom Template",
                        color = if (config.formatMode == WebhookFormatMode.CUSTOM_TEMPLATE) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (config.formatMode == WebhookFormatMode.DEFAULT_JSON)
                    "Format JSON terstruktur bawaan aplikasi dengan atribut standar."
                else
                    "Bebas sesuaikan nama atribut JSON atau query URL GET menggunakan shortcode (MacroDroid style).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (config.formatMode == WebhookFormatMode.CUSTOM_TEMPLATE) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Metode HTTP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = config.httpMethod == "POST",
                        onClick = { viewModel.setHttpMethod("POST") },
                        label = { Text("POST (Body)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SfrPrimary, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = config.httpMethod == "GET",
                        onClick = { viewModel.setHttpMethod("GET") },
                        label = { Text("GET (URL Query)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SfrPrimary, selectedLabelColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Daftar Shortcode Tersedia (Klik untuk Menempel):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val shortcodes = listOf("{app}", "{package}", "{title}", "{message}", "{time}", "{date}", "{id}", "{device}")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shortcodes.forEach { code ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                if (config.httpMethod == "GET") {
                                    val current = config.endpoint
                                    val separator = if (current.contains("?")) "&" else "?"
                                    viewModel.updateEndpoint("$current${separator}param=$code")
                                } else {
                                    val current = config.customBodyTemplate
                                    viewModel.setCustomBodyTemplate("$current\n$code")
                                }
                            }
                        ) {
                            Text(
                                text = code,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (config.httpMethod == "GET") {
                    Text(
                        text = "Contoh URL Template dengan Parameter Query:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "https://contoh.com/webhook.php?nama_app={app}&pesan={message}&jam={time}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = config.endpoint,
                        onValueChange = { viewModel.updateEndpoint(it) },
                        label = { Text("URL Endpoint GET Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Text(
                        text = "Template Body POST (JSON atau String):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.customBodyTemplate,
                        onValueChange = { viewModel.setCustomBodyTemplate(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nilai {app}, {message}, dll. akan otomatis di-escape dan diisi real-time saat notifikasi tertangkap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    } else {
        // Default JSON Field Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pilih Field JSON yang Disertakan:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val availableFields = listOf(
                    "app" to "Nama Aplikasi (app)",
                    "package" to "Package ID (package)",
                    "title" to "Judul Notifikasi (title)",
                    "message" to "Isi Pesan (message)",
                    "timestamp" to "Waktu Milidetik (timestamp)",
                    "key" to "Kunci Notifikasi (key)",
                    "id" to "ID Notifikasi (id)"
                )

                availableFields.forEach { (key, label) ->
                    val isChecked = config.selectedFields.contains(key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleField(key) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleField(key) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SfrPrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiWebhookContent(
    config: com.example.data.model.SfrConfig,
    viewModel: MainViewModel,
    onOpenAddDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mode Routing Webhook",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.setDispatchMode(DispatchMode.SINGLE_WEBHOOK) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.dispatchMode == DispatchMode.SINGLE_WEBHOOK) SfrPrimary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "Satu Webhook",
                        color = if (config.dispatchMode == DispatchMode.SINGLE_WEBHOOK) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.setDispatchMode(DispatchMode.MULTI_WEBHOOK) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.dispatchMode == DispatchMode.MULTI_WEBHOOK) SfrPrimary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "Multi Webhook",
                        color = if (config.dispatchMode == DispatchMode.MULTI_WEBHOOK) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (config.dispatchMode == DispatchMode.SINGLE_WEBHOOK)
                    "Semua aplikasi yang dipilih akan diteruskan ke URL endpoint utama."
                else
                    "Tiap aplikasi (misal WhatsApp, Shopee, BCA) dapat memiliki URL endpoint dan template respon masing-masing!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (config.dispatchMode == DispatchMode.MULTI_WEBHOOK) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aturan Khusus per Aplikasi (${config.appWebhookRules.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onOpenAddDialog,
                colors = ButtonDefaults.buttonColors(containerColor = SfrPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah Aturan", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (config.appWebhookRules.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Belum ada aturan aplikasi.", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aplikasi akan menggunakan endpoint default. Klik 'Tambah Aturan' untuk menambahkan URL tujuan khusus per aplikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            config.appWebhookRules.values.forEach { rule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rule.appName.ifBlank { rule.packageName },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = rule.packageName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.removeAppRule(rule.packageName) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "URL: ${rule.endpoint}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Metode: ${rule.method}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedFeaturesContent(
    config: com.example.data.model.SfrConfig,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    // Heartbeat Settings
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = SfrPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Interval Heartbeat Standby",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mengirim status uptime & ping ke webhook secara berkala saat Standby aktif.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            val intervals = listOf(
                1 to "1 Detik (Real-time)",
                5 to "5 Detik",
                10 to "10 Detik",
                30 to "30 Detik",
                60 to "1 Menit",
                0 to "Mati (Off)"
            )

            intervals.forEach { (sec, label) ->
                val isSelected = config.heartbeatIntervalSeconds == sec
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setHeartbeatIntervalSeconds(sec) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SfrPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Text("● Dipilih", color = SfrPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Floating Overlay Bubble Toggle
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PictureInPicture, contentDescription = null, tint = SfrPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bubble Mengambang (Overlay)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tampilkan waktu berjalan (uptime) di atas aplikasi lain",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.floatingBubbleEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } else {
                            viewModel.setFloatingBubble(enable)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SfrPrimary)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // App Lock Toggle (Biometric / PIN HP)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SfrPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kunci Aplikasi (Biometrik / PIN HP)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Wajibkan sidik jari, wajah, atau PIN HP saat membuka SFR Droid",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.appLockEnabled,
                    onCheckedChange = { enable ->
                        viewModel.setAppLock(enable)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SfrPrimary)
                )
            }
        }
    }
}

@Composable
private fun AddRuleDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPackage by remember { mutableStateOf("") }
    var selectedAppName by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("POST") }
    var customTemplate by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tambah Aturan Multi Webhook",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Pilih Aplikasi Sumber:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))

                // Picker for installed apps
                val apps = uiState.allApps.take(15)
                apps.forEach { appItem ->
                    val isSelected = selectedPackage == appItem.packageName
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                selectedPackage = appItem.packageName
                                selectedAppName = appItem.appName
                            }
                    ) {
                        Text(
                            text = appItem.appName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("URL Endpoint Webhook") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customTemplate,
                    onValueChange = { customTemplate = it },
                    label = { Text("Template Body / Query (Opsional)") },
                    placeholder = { Text("{\"pesan\": \"{message}\"}") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedPackage.isNotBlank() && endpoint.isNotBlank()) {
                                viewModel.saveAppRule(
                                    AppWebhookRule(
                                        packageName = selectedPackage,
                                        appName = selectedAppName,
                                        endpoint = endpoint,
                                        method = method,
                                        customTemplate = customTemplate
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SfrPrimary)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
