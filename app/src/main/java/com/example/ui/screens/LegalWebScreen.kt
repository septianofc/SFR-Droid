package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LegalType {
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalWebScreen(
    type: LegalType,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPrivacy = type == LegalType.PRIVACY_POLICY

    val title = if (isPrivacy) "Kebijakan Privasi" else "Persyaratan Penggunaan"
    val targetUrl = if (isPrivacy)
        "https://sfrdroid.septianfr.web.id/kebijakan-privasi"
    else
        "https://sfrdroid.septianfr.web.id/persyaratan-pegunaan"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("legal_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { openExternal(context, targetUrl) }) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Buka di Browser")
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
            // URL Link Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dokumen Resmi Online:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = targetUrl,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = { openExternal(context, targetUrl) },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Buka Web", fontSize = 11.sp)
                    }
                }
            }

            if (isPrivacy) {
                // Privacy Policy In-App Reader
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Komitmen Privasi SFR Droid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Text(
                            text = "Privasi Anda adalah prioritas utama kami. Aplikasi SFR Droid dirancang dengan prinsip nol pelacakan pihak ketiga dan transparansi penuh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        PolicySection(
                            title = "1. Pengumpulan & Pengiriman Data",
                            content = "SFR Droid hanya membaca notifikasi dari aplikasi yang Anda pilih secara manual di layar pemilih aplikasi. Data notifikasi diteruskan secara langsung ke alamat URL endpoint yang Anda konfigurasikan sendiri."
                        )

                        PolicySection(
                            title = "2. Enkripsi End-to-End Opsional",
                            content = "Aplikasi menyediakan opsi enkripsi AES-256-CBC untuk token autentikasi dan data notifikasi. Kunci rahasia disimpan secara lokal pada perangkat Anda dan tidak pernah dikirimkan ke server pengembang."
                        )

                        PolicySection(
                            title = "3. Tidak Ada Iklan & Tidak Ada Pelacak",
                            content = "SFR Droid tidak menyematkan pustaka analitik pihak ketiga, SDK pelacak, maupun jaringan iklan."
                        )

                        PolicySection(
                            title = "4. Hak Kendali Pengguna",
                            content = "Anda dapat kapan saja menonaktifkan mode Standby, mengubah daftar aplikasi sumber, menghapus token, atau mencabut izin Akses Notifikasi melalui menu Pengaturan Android."
                        )
                    }
                }
            } else {
                // Terms of Service In-App Reader
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Persyaratan Penggunaan SFR Droid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Text(
                            text = "Dengan mengunduh dan menggunakan aplikasi SFR Droid, Anda menyetujui ketentuan berikut ini:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        PolicySection(
                            title = "1. Penggunaan yang Sah",
                            content = "Anda bertanggung jawab penuh atas alamat URL Webhook dan server penerima yang Anda tentukan. Dilarang meneruskan data notifikasi ke endpoint berbahaya atau tanpa hak akses yang sah."
                        )

                        PolicySection(
                            title = "2. Batasan Tanggung Jawab",
                            content = "Aplikasi ini disediakan sebagaimana adanya ('as-is'). Keterlambatan pengiriman paket data akibat pemadaman jaringan internet, kebijakan pembatasan latar belakang Android (Doze Mode), atau downtime server tujuan berada di luar kendali pengembang."
                        )

                        PolicySection(
                            title = "3. Perubahan Ketentuan",
                            content = "Pengembang berhak memperbarui ketentuan ini dari waktu ke waktu. Informasi pembaruan resmi akan disampaikan melalui tautan resmi situs web sfrdroid.septianfr.web.id."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
    }
}

private fun openExternal(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
