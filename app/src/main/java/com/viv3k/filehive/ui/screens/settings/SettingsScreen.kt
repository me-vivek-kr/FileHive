package com.viv3k.filehive.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.utils.NeomorphicIcon
import com.viv3k.filehive.ui.utils.NeomorphicSwitch
import com.viv3k.filehive.ui.utils.soft
import com.viv3k.filehive.ui.utils.softClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onStorageAnalyzerClick: () -> Unit,
    onRecycleBinClick: () -> Unit,
    onVaultClick: () -> Unit
) {
    var darkMode by remember { mutableStateOf(false) }
    var gridViewDefault by remember { mutableStateOf(true) }
    var biometricAuth by remember { mutableStateOf(true) }

    val backgroundColor = Color(0xFFF4F6FA)
    val primaryTextColor = Color(0xFF5051D8)
    val secondaryTextColor = Color(0xFF6B7280)
    val headerTextColor = Color(0xFF006064) // Teal-ish as seen in headers like "File Management"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = primaryTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .softClickable(
                                onClick = onBackClick,
                                shape = CircleShape,
                                cornerRadius = 20.dp,
                                blurRadius = 8.dp,
                                offsetY = 4.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
        ) {
            // File Management Section
            item {
                SettingsSection(title = "File Management", textColor = headerTextColor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .soft(
                                shape = RoundedCornerShape(32.dp),
                                cornerRadius = 32.dp
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        SettingsItem(
                            icon = R.drawable.info,
                            title = "Storage Analyzer",
                            subtitle = "Analyze storage usage",
                            onClick = onStorageAnalyzerClick,
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor
                        )
                        SettingsItem(
                            icon = R.drawable.trash,
                            title = "Recycle Bin",
                            subtitle = "Manage deleted items",
                            onClick = onRecycleBinClick,
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor
                        )
                        SettingsItem(
                            icon = R.drawable.lock,
                            title = "Vault",
                            subtitle = "Secure private files",
                            onClick = onVaultClick,
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor,
                            isLast = true
                        )
                    }
                }
            }

            // Appearance Section
            item {
                SettingsSection(title = "Appearance", textColor = headerTextColor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .soft(
                                shape = RoundedCornerShape(32.dp),
                                cornerRadius = 32.dp
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        SettingsToggleItem(
                            icon = R.drawable.moon,
                            title = "Dark Mode",
                            subtitle = "Toggle dark theme",
                            checked = darkMode,
                            onCheckedChange = { darkMode = it },
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor
                        )
                        SettingsToggleItem(
                            icon = R.drawable.grid,
                            title = "Grid View Default",
                            subtitle = "Default to grid layout",
                            checked = gridViewDefault,
                            onCheckedChange = { gridViewDefault = it },
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor,
                            isLast = true
                        )
                    }
                }
            }

            // Security Section
            item {
                SettingsSection(title = "Security", textColor = headerTextColor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .soft(
                                shape = RoundedCornerShape(32.dp),
                                cornerRadius = 32.dp
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        SettingsToggleItem(
                            icon = R.drawable.shield,
                            title = "Biometric Auth",
                            subtitle = "Use fingerprint to unlock",
                            checked = biometricAuth,
                            onCheckedChange = { biometricAuth = it },
                            textColor = primaryTextColor,
                            subtitleColor = secondaryTextColor,
                            isLast = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color,
    subtitleColor: Color,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeomorphicIcon(
            icon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = subtitleColor
                )
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    subtitleColor: Color,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeomorphicIcon(
            icon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = subtitleColor
                )
            )
        }
        NeomorphicSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onBackClick = {},
        onStorageAnalyzerClick = {},
        onRecycleBinClick = {},
        onVaultClick = {}
    )
}
