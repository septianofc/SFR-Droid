package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun DeveloperInfoDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Informasi Developer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Developer Avatar
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(96.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shadowElevation = 6.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dev_avatar),
                        contentDescription = "Foto Profil Developer Septian Fathur Rohman",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Septian Fathur Rohman",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Pengembang & Kreator SFR Droid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ContactRow(
                            label = "WhatsApp",
                            value = "082130392599",
                            iconColor = Color(0xFF25D366),
                            onClick = {
                                openUrl(context, "https://wa.me/6282130392599")
                            }
                        )

                        HorizontalDivider()

                        ContactRow(
                            label = "Telegram",
                            value = "@sfrdigital",
                            iconColor = Color(0xFF0088CC),
                            onClick = {
                                openUrl(context, "https://t.me/sfrdigital")
                            }
                        )

                        HorizontalDivider()

                        ContactRow(
                            label = "Instagram",
                            value = "@_septianfr",
                            iconColor = Color(0xFFE4405F),
                            onClick = {
                                openUrl(context, "https://instagram.com/_septianfr")
                            }
                        )

                        HorizontalDivider()

                        ContactRow(
                            label = "Website",
                            value = "septianfr.web.id",
                            iconColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                openUrl(context, "https://septianfr.web.id")
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun ContactRow(
    label: String,
    value: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
        }

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(32.dp)
        ) {
            Text("Buka", fontSize = 11.sp)
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
