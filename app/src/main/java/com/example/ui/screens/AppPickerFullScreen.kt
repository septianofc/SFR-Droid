package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppFilter
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerFullScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val filteredApps = remember(uiState.allApps, uiState.appSearchQuery, uiState.appFilter) {
        uiState.allApps.filter { app ->
            val matchesFilter = when (uiState.appFilter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystemApp
                AppFilter.SYSTEM -> app.isSystemApp
            }
            val matchesSearch = uiState.appSearchQuery.isBlank() ||
                    app.appName.contains(uiState.appSearchQuery, ignoreCase = true) ||
                    app.packageName.contains(uiState.appSearchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pilih Aplikasi Sumber", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("app_picker_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInstalledApps() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = uiState.appSearchQuery,
                onValueChange = { viewModel.setAppSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fullscreen_app_search"),
                placeholder = { Text("Cari nama aplikasi atau package...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.appSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setAppSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.appFilter == AppFilter.ALL,
                    onClick = { viewModel.setAppFilter(AppFilter.ALL) },
                    label = { Text("Semua (${uiState.allApps.size})") }
                )
                FilterChip(
                    selected = uiState.appFilter == AppFilter.USER,
                    onClick = { viewModel.setAppFilter(AppFilter.USER) },
                    label = { Text("Pengguna (${uiState.allApps.count { !it.isSystemApp }})") }
                )
                FilterChip(
                    selected = uiState.appFilter == AppFilter.SYSTEM,
                    onClick = { viewModel.setAppFilter(AppFilter.SYSTEM) },
                    label = { Text("Sistem (${uiState.allApps.count { it.isSystemApp }})") }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.config.selectedApps.size} aplikasi dipilih",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.config.selectedApps.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Row {
                    TextButton(onClick = { viewModel.selectAllFiltered(filteredApps.map { it.packageName }) }) {
                        Text("Pilih Semua", fontSize = 12.sp)
                    }
                    TextButton(onClick = { viewModel.deselectAll() }) {
                        Text("Hapus Semua", fontSize = 12.sp)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            if (uiState.isLoadingApps) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Membaca aplikasi terpasang di Android...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (filteredApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tidak ada aplikasi yang cocok.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.loadInstalledApps() }) {
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
                        val isChecked = uiState.config.selectedApps.contains(app.packageName)
                        AppItemRow(
                            app = app,
                            isSelected = isChecked,
                            onToggle = { viewModel.toggleAppSelection(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}
