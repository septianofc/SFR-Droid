package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.SfrConfig
import com.example.util.EncryptionHelper

@Composable
fun EncryptionDialog(
    config: SfrConfig,
    onEncryptTokenChange: (Boolean) -> Unit,
    onEncryptPayloadChange: (Boolean) -> Unit,
    onKeyChange: (String) -> Unit,
    onGenerateKey: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showKey by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: PHP, 1: Node.js, 2: Python

    val currentKey = config.encryptionKey.ifBlank { "KUNCI_RAHASIA_ANDA" }
    val snippetText = when (selectedTab) {
        0 -> EncryptionHelper.getPhpDecryptionSnippet(currentKey)
        1 -> EncryptionHelper.getNodeJsDecryptionSnippet(currentKey)
        else -> EncryptionHelper.getPythonDecryptionSnippet(currentKey)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keamanan & Enkripsi Webhook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Enkripsi menggunakan algoritma standar AES-256-CBC. Data dikirim dalam format aman Base64 yang dapat didekripsi di server Anda dengan kode pembuka di bawah.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Toggle Encrypt Token
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enkripsi Token Autentikasi", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Enkripsi header Authorization Bearer sebelum dikirim", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.encryptToken,
                            onCheckedChange = onEncryptTokenChange,
                            modifier = Modifier.testTag("toggle_encrypt_token")
                        )
                    }
                }

                // Toggle Encrypt Payload
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enkripsi Isi Payload Notifikasi", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Isi data judul, pesan, dan waktu dienkripsi AES-256", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.encryptPayload,
                            onCheckedChange = onEncryptPayloadChange,
                            modifier = Modifier.testTag("toggle_encrypt_payload")
                        )
                    }
                }

                // Encryption Secret Key
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kunci Enkripsi Rahasia (Secret Key)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = onGenerateKey,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buat Acak", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = config.encryptionKey,
                        onValueChange = onKeyChange,
                        modifier = Modifier.fillMaxWidth(),
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
                                if (config.encryptionKey.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            copyToClipboard(context, "Kunci SFR Droid", config.encryptionKey)
                                            Toast.makeText(context, "Kunci enkripsi disalin ke clipboard", Toast.LENGTH_SHORT).show()
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

                HorizontalDivider()

                // Server Decryption Snippets
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skrip Pembuka / Dekripsi di Server:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("PHP") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Node.js") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Python") })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E222A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = snippetText,
                                color = Color(0xFF81D4FA),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, "Skrip Dekripsi", snippetText)
                                    Toast.makeText(context, "Skrip dekripsi berhasil disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Kode", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Simpan & Tutup")
            }
        }
    )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    cm.setPrimaryClip(clip)
}
