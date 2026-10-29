package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Informasi Developer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dev_info_back")) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with App Platform Logo and Developer Avatar
            Box(
                modifier = Modifier.padding(top = 10.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Circular Developer Avatar
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(120.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dev_avatar),
                        contentDescription = "Septian Fathur Rohman",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Mini Platform Logo Badge
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .border(2.dp, Color.White, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sfr_logo),
                        contentDescription = "SFR Droid Platform Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Septian Fathur Rohman",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Founder & Lead Developer of SFR Droid",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Developer Mission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tentang SFR Droid",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SFR Droid dikembangkan untuk menjembatani notifikasi Android secara instan ke server webhook Anda dengan performa tinggi, keamanan enkripsi AES-256, dan kendali privasi penuh di tangan pengguna.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Hubungi Developer Resmi:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Channels
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FullContactItem(
                        title = "WhatsApp",
                        value = "082130392599",
                        actionLabel = "Kirim Pesan",
                        accentColor = Color(0xFF25D366),
                        onClick = { openUrl(context, "https://wa.me/6282130392599") }
                    )

                    HorizontalDivider()

                    FullContactItem(
                        title = "Telegram",
                        value = "@sfrdigital",
                        actionLabel = "Buka Chat",
                        accentColor = Color(0xFF0088CC),
                        onClick = { openUrl(context, "https://t.me/sfrdigital") }
                    )

                    HorizontalDivider()

                    FullContactItem(
                        title = "Instagram",
                        value = "@_septianfr",
                        actionLabel = "Kunjungi",
                        accentColor = Color(0xFFE4405F),
                        onClick = { openUrl(context, "https://instagram.com/_septianfr") }
                    )

                    HorizontalDivider()

                    FullContactItem(
                        title = "Website Portofolio",
                        value = "https://septianfr.web.id",
                        actionLabel = "Buka Web",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { openUrl(context, "https://septianfr.web.id") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FullContactItem(
    title: String,
    value: String,
    actionLabel: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = accentColor)
        }

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(34.dp)
        ) {
            Text(actionLabel, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
