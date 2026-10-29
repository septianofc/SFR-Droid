package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import com.example.util.EncryptionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showKey by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentKey = uiState.config.encryptionKey.ifBlank { "KUNCI_RAHASIA_ANDA" }
    val snippetText = when (selectedTab) {
        0 -> EncryptionHelper.getPhpDecryptionSnippet(currentKey)
        1 -> EncryptionHelper.getNodeJsDecryptionSnippet(currentKey)
        else -> EncryptionHelper.getPythonDecryptionSnippet(currentKey)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Keamanan & Enkripsi Webhook", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("encryption_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enkripsi standar industri AES-256-CBC. Data dikirim dalam format aman Base64. Default aplikasi adalah No Enc (bebas terserah pengguna).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Toggle 1: Token Encryption
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enkripsi Token Autentikasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enkripsi header Authorization: Bearer <token> sebelum dikirim ke endpoint.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.config.encryptToken,
                        onCheckedChange = { viewModel.setEncryptToken(it) },
                        modifier = Modifier.testTag("toggle_screen_encrypt_token")
                    )
                }
            }

            // Toggle 2: Payload Encryption
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enkripsi Isi Payload Notifikasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enkripsi data JSON (judul, pesan, waktu) dengan AES-256-CBC.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.config.encryptPayload,
                        onCheckedChange = { viewModel.setEncryptPayload(it) },
                        modifier = Modifier.testTag("toggle_screen_encrypt_payload")
                    )
                }
            }

            // Secret Key Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kunci Enkripsi Rahasia (Secret Key)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { viewModel.generateNewEncryptionKey() }) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate Acak", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = uiState.config.encryptionKey,
                        onValueChange = { viewModel.setEncryptionKey(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("screen_enc_key_input"),
                        placeholder = { Text("Masukkan atau buat kunci acak...") },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Tampilkan Kunci"
                                    )
                                }
                                if (uiState.config.encryptionKey.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            copyText(context, "Kunci SFR Droid", uiState.config.encryptionKey)
                                            Toast.makeText(context, "Kunci enkripsi berhasil disalin!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin Kunci")
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Decryption Code at Server Side
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kode Dekripsi di Server Backend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("PHP") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Node.js") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Python") })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E222A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = snippetText,
                                color = Color(0xFF81D4FA),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    copyText(context, "Skrip Dekripsi", snippetText)
                                    Toast.makeText(context, "Skrip dekripsi berhasil disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Kode Backend", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    cm.setPrimaryClip(clip)
}
