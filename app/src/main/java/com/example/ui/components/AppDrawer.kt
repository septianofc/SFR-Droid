package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SfrPrimary
import com.example.ui.viewmodel.AppScreen

@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Gradient and Platform Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SfrPrimary,
                                Color(0xFF1E88E5)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.size(52.dp),
                            shadowElevation = 4.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.sfr_logo),
                                contentDescription = "SFR Droid Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "SFR Droid",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "v1.0.0 • Pro Edition",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Android Notification Forwarder & Webhook Bridge",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            DrawerNavItem(
                label = "Dashboard Utama",
                icon = Icons.Default.Dashboard,
                isSelected = currentScreen == AppScreen.DASHBOARD,
                testTag = "drawer_item_dashboard",
                onClick = {
                    onNavigate(AppScreen.DASHBOARD)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                label = "Ubah Aplikasi (Full Layar)",
                icon = Icons.Default.Apps,
                isSelected = currentScreen == AppScreen.APP_PICKER,
                testTag = "drawer_item_apps",
                onClick = {
                    onNavigate(AppScreen.APP_PICKER)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                label = "Halaman Enkripsi (Full Layar)",
                icon = Icons.Default.Lock,
                isSelected = currentScreen == AppScreen.ENCRYPTION,
                testTag = "drawer_item_encryption",
                onClick = {
                    onNavigate(AppScreen.ENCRYPTION)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                label = "Wizard Ulang (Full Layar)",
                icon = Icons.Default.Tune,
                isSelected = currentScreen == AppScreen.WIZARD,
                testTag = "drawer_item_wizard",
                onClick = {
                    onNavigate(AppScreen.WIZARD)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            DrawerNavItem(
                label = "Info Developer (Full Layar)",
                icon = Icons.Default.Person,
                isSelected = currentScreen == AppScreen.DEVELOPER_INFO,
                testTag = "drawer_item_developer",
                onClick = {
                    onNavigate(AppScreen.DEVELOPER_INFO)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                label = "Kebijakan Privasi (Full Layar)",
                icon = Icons.Default.Shield,
                isSelected = currentScreen == AppScreen.PRIVACY_POLICY,
                testTag = "drawer_item_privacy",
                onClick = {
                    onNavigate(AppScreen.PRIVACY_POLICY)
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                label = "Persyaratan Penggunaan (Full Layar)",
                icon = Icons.Default.Gavel,
                isSelected = currentScreen == AppScreen.TERMS_OF_SERVICE,
                testTag = "drawer_item_terms",
                onClick = {
                    onNavigate(AppScreen.TERMS_OF_SERVICE)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Footer with Developer Mini Profile
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onNavigate(AppScreen.DEVELOPER_INFO)
                        onCloseDrawer()
                    },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.dev_avatar),
                            contentDescription = "Septian Fathur Rohman",
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Septian Fathur Rohman",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Lead Developer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun DrawerNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            selectedTextColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
