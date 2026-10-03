package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppThemeMode
import com.example.model.EcoBadge
import com.example.ui.EcoViewModel
import com.example.ui.components.UserProfileAvatar
import com.example.ui.theme.*
import com.example.util.EcoShareUtils

@Composable
fun ProfileScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM
    val userPoints by viewModel.repository.userPoints.collectAsState()
    val totalItems by viewModel.repository.totalItemsRecycled.collectAsState()
    val badges by viewModel.repository.badges.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val userOccupation by viewModel.userOccupation.collectAsState()
    val userIcNumber by viewModel.userIcNumber.collectAsState()
    val userPhoneNumber by viewModel.userPhoneNumber.collectAsState()
    val userProfileImage by viewModel.userProfileImage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAvatarPickerDialog by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileImage(uri.toString())
            Toast.makeText(
                context,
                if (isBm) "Gambar profil berjaya dikemaskini!" else "Profile photo updated successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .testTag("profile_avatar_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        UserProfileAvatar(
                            imageIdentifier = userProfileImage,
                            size = 80.dp,
                            onClick = { showAvatarPickerDialog = true }
                        )

                        // Camera Edit Badge
                        Surface(
                            shape = CircleShape,
                            color = EcoGreenPrimary,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { showAvatarPickerDialog = true }
                                .testTag("change_profile_photo_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Tukar Gambar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAvatarPickerDialog = true },
                        modifier = Modifier.testTag("change_avatar_text_button")
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = EcoGreenPrimary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isBm) "Tukar Gambar Profil" else "Change Profile Photo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EcoGreenPrimary
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = { showChangeUsernameDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("edit_username_button")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Username",
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "$userOccupation • $userLocation",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "🪪 IC: $userIcNumber",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "📞 $userPhoneNumber",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "📧 $userEmail",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    // Account & Authentication Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangeUsernameDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("change_username_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Change Username", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBm) "Tukar Nama" else "Edit Name", fontSize = 11.sp)
                        }

                        if (isLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logout()
                                    viewModel.navigateTo(com.example.model.Screen.LOGIN)
                                    Toast.makeText(
                                        context,
                                        if (isBm) "Log keluar berjaya. Sila masukkan nama untuk mula semula." else "Logged out successfully. Please enter a name to start.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("logout_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBm) "Log Keluar" else "Log Out", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { showLoginDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_login_dialog_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Login", modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBm) "Log Masuk" else "Log In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!isLoggedIn) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isBm)
                                        "Mod Tetamu: Sila log masuk untuk simpan rekod & lencana anda."
                                    else
                                        "Guest Mode: Log in to save records & school badges.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = viewModel.repository.getLevelName(language),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    // Quick Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$userPoints", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = EcoGreenPrimary)
                            Text("Eco Points", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalItems", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0284C7))
                            Text(if (isBm) "Item Dikitar" else "Items Recycled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val unlockedCount = badges.count { it.unlocked }
                            Text("$unlockedCount / ${badges.size}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFF59E0B))
                            Text(if (isBm) "Lencana" else "Badges", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Social Media Share Milestones Button
                    Button(
                        onClick = {
                            val carbonSaved = viewModel.repository.carbonImpactKg.value
                            val unlockedCount = badges.count { it.unlocked }
                            EcoShareUtils.shareRecyclingMilestones(
                                context = context,
                                userName = viewModel.repository.userName,
                                userSchool = viewModel.repository.userSchool,
                                userClass = viewModel.repository.userClass,
                                levelName = viewModel.repository.getLevelName(language),
                                points = userPoints,
                                totalItems = totalItems,
                                carbonSavedKg = carbonSaved,
                                unlockedBadgesCount = unlockedCount,
                                language = language
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_milestones_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "Kongsi Pencapaian Eko ke Media Sosial" else "Share Eco Milestones to Social Media",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Badges & Achievements Section (Section 13)
        item {
            Text(
                text = if (isBm) "Koleksi Lencana Kelestarian" else "Eco Badges & Achievements",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(badges) { badge ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (badge.unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(if (badge.unlocked) 2.dp else 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("badge_card_${badge.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (badge.unlocked) EcoGreenPrimary.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badge.unlocked) badge.iconEmoji else "🔒",
                            fontSize = 22.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBm) badge.titleBm else badge.titleEn,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (badge.unlocked) {
                                Spacer(Modifier.width(6.dp))
                                Text("✨", fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = if (isBm) badge.descBm else badge.descEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        val prog = (badge.progress.toFloat() / badge.max).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { prog },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (badge.unlocked) EcoGreenPrimary else Color.Gray,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${badge.progress}/${badge.max}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.unlocked) EcoGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (badge.unlocked) {
                            IconButton(
                                onClick = { EcoShareUtils.shareBadge(context, badge, language) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("share_badge_${badge.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Badge",
                                    tint = EcoGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings Section
        item {
            Column {
                Text(
                    text = if (isBm) "Tetapan Aplikasi" else "Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isBm)
                        "Urus kebenaran notifikasi dan bahasa pilihan antaramuka."
                    else
                        "Manage notification preferences and switch display language.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_settings_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. Notifications Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (notificationsEnabled) EcoGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (notificationsEnabled) EcoGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isBm) "Notifikasi Peringatan" else "Notifications",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (notificationsEnabled) EcoGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (notificationsEnabled) (if (isBm) "Aktif" else "ON") else (if (isBm) "Mati" else "OFF"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (notificationsEnabled) EcoGreenPrimary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isBm)
                                        "Peringatan harian cabaran, kiosk e-sisa & acara sekolah"
                                    else
                                        "Daily recycling reminders, challenge alerts & school events",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EcoGreenPrimary
                            ),
                            modifier = Modifier.testTag("notifications_toggle")
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(14.dp))

                    // 2. Theme Mode Switcher Row (System / Light / Dark)
                    val themeMode by viewModel.themeMode.collectAsState()
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (themeMode == AppThemeMode.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme Mode",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Tema Aplikasi (Light / Dark)" else "App Theme Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isBm)
                                        "Pilih Mod Cerah (Light), Mod Gelap (Dark), atau Ikut Sistem"
                                    else
                                        "Choose Light Mode, Dark Mode, or Follow System",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 3 Interactive Theme Switch Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Light Mode Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (themeMode == AppThemeMode.LIGHT) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setThemeMode(AppThemeMode.LIGHT) }
                                    .testTag("theme_light_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("☀️", fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Cerah" else "Light",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (themeMode == AppThemeMode.LIGHT) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Dark Mode Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (themeMode == AppThemeMode.DARK) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setThemeMode(AppThemeMode.DARK) }
                                    .testTag("theme_dark_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🌙", fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Gelap" else "Dark",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (themeMode == AppThemeMode.DARK) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // System Default Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (themeMode == AppThemeMode.SYSTEM) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                                    .testTag("theme_system_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("⚙️", fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Sistem" else "System",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (themeMode == AppThemeMode.SYSTEM) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(14.dp))

                    // 3. Language Switcher Row (English / Bahasa Melayu)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Pilihan Bahasa" else "Language Selection",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isBm)
                                        "Tukar antara Bahasa Melayu dan English"
                                    else
                                        "Switch between English and Bahasa Melayu",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Dual Language Switch Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bahasa Melayu Option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isBm) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setLanguage(AppLanguage.BM) }
                                    .testTag("settings_language_bm_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🇲🇾", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Bahasa Melayu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isBm) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // English Option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isBm) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setLanguage(AppLanguage.EN) }
                                    .testTag("settings_language_en_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🇬🇧", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "English",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (!isBm) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(10.dp))

                    // 3. Smart Reminders & About Rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReminderDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBm) "Lihat Jadual Peringatan Aktif" else "View Scheduled Reminders",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isBm) "2 Jadual 🔔" else "2 Active 🔔",
                            fontSize = 12.sp,
                            color = EcoGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isLoggedIn) {
                                    showChangeUsernameDialog = true
                                } else {
                                    showLoginDialog = true
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("account_settings_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Pengurusan Akaun & Profil" else "Account & Profile Settings",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            text = if (isLoggedIn) (if (isBm) "Aktif ✅" else "Active ✅") else (if (isBm) "Log Masuk ⚠️" else "Log In ⚠️"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoggedIn) EcoGreenPrimary else MaterialTheme.colorScheme.error
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "About", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Tentang ECOBIN AI" else "About ECOBIN AI",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text("v1.0 Pro", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    // Change Username & Profile Dialog
    if (showChangeUsernameDialog) {
        var newUsernameInput by remember { mutableStateOf(userName) }
        var newEmailInput by remember { mutableStateOf(userEmail) }
        var newLocationInput by remember { mutableStateOf(userLocation) }
        var newOccupationInput by remember { mutableStateOf(userOccupation) }
        var newIcInput by remember { mutableStateOf(userIcNumber) }
        var newPhoneInput by remember { mutableStateOf(userPhoneNumber) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            modifier = Modifier.testTag("edit_username_dialog"),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✏️", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBm) "Kemaskini Profil Pengguna" else "Update User Profile",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBm)
                            "Kemaskini nama paparan, emel, lokasi, pekerjaan, no. IC dan no. telefon anda."
                        else
                            "Update your display name, email, location, occupation, IC number and phone number.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newUsernameInput,
                        onValueChange = {
                            newUsernameInput = it
                            if (it.trim().length >= 3) errorMessage = null
                        },
                        label = { Text(if (isBm) "Nama Pengguna" else "Username") },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(if (isBm) "Sekurang-kurangnya 3 aksara" else "Minimum 3 characters")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { newEmailInput = it },
                        label = { Text(if (isBm) "Alamat Emel" else "Email Address") },
                        placeholder = { Text("alifhaikalsha21202@gmail.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    OutlinedTextField(
                        value = newLocationInput,
                        onValueChange = { newLocationInput = it },
                        label = { Text(if (isBm) "Lokasi Anda" else "Your Location") },
                        placeholder = { Text("Contoh: Petaling Jaya, Selangor") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_input")
                            .testTag("school_input")
                    )

                    OutlinedTextField(
                        value = newOccupationInput,
                        onValueChange = { newOccupationInput = it },
                        label = { Text(if (isBm) "Pekerjaan" else "Occupation") },
                        placeholder = { Text("Contoh: Eksekutif Kelestarian / Pelajar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("occupation_input")
                            .testTag("class_input")
                    )

                    OutlinedTextField(
                        value = newIcInput,
                        onValueChange = { newIcInput = it },
                        label = { Text(if (isBm) "No. Kad Pengenalan (IC)" else "IC Number") },
                        placeholder = { Text("Contoh: 020512-10-5432") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ic_input")
                    )

                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = { newPhoneInput = it },
                        label = { Text(if (isBm) "Nombor Telefon" else "Phone Number") },
                        placeholder = { Text("Contoh: +60 12-345 6789") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newUsernameInput.trim()
                        if (trimmed.length < 3) {
                            errorMessage = if (isBm) "Nama mestilah sekurang-kurangnya 3 aksara" else "Name must be at least 3 characters"
                        } else {
                            viewModel.updateProfile(
                                name = trimmed,
                                location = newLocationInput.trim(),
                                occupation = newOccupationInput.trim(),
                                icNumber = newIcInput.trim(),
                                phoneNumber = newPhoneInput.trim(),
                                email = newEmailInput.trim()
                            )
                            showChangeUsernameDialog = false
                            Toast.makeText(
                                context,
                                if (isBm) "Profil berjaya dikemaskini!" else "Profile updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    modifier = Modifier.testTag("save_username_button")
                ) {
                    Text(if (isBm) "Simpan" else "Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangeUsernameDialog = false },
                    modifier = Modifier.testTag("cancel_username_button")
                ) {
                    Text(if (isBm) "Batal" else "Cancel")
                }
            }
        )
    }

    // Login & Switch Account Dialog
    if (showLoginDialog) {
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var locationInput by remember { mutableStateOf("Petaling Jaya, Selangor") }
        var occupationInput by remember { mutableStateOf("Eksekutif Kelestarian") }
        var icInput by remember { mutableStateOf("020512-10-5432") }
        var phoneInput by remember { mutableStateOf("+60 12-345 6789") }
        var passwordVisible by remember { mutableStateOf(false) }
        var loginError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            modifier = Modifier.testTag("login_dialog"),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔐", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBm) "Log Masuk ECOBIN AI" else "Log In to ECOBIN AI",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBm)
                            "Masukkan Nama / Emel atau pilih profil contoh untuk log masuk."
                        else
                            "Enter Username / Email or pick a demo profile to log in.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Demo Accounts Chips
                    Text(
                        text = if (isBm) "Pilih Akaun Contoh:" else "Quick Demo Accounts:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                emailInput = "Aiman Hakim"
                                passwordInput = "eco12345"
                                locationInput = "Petaling Jaya, Selangor"
                                occupationInput = "Eksekutif Kelestarian"
                                icInput = "020512-10-5432"
                                phoneInput = "+60 12-345 6789"
                            },
                            label = { Text("Aiman (PJ)", fontSize = 10.sp) },
                            modifier = Modifier.testTag("demo_account_aiman")
                        )
                        SuggestionChip(
                            onClick = {
                                emailInput = "Sarah Qistina"
                                passwordInput = "eco12345"
                                locationInput = "Kuala Lumpur"
                                occupationInput = "Jurutera Alam Sekitar"
                                icInput = "010814-14-6542"
                                phoneInput = "+60 13-987 6543"
                            },
                            label = { Text("Sarah (KL)", fontSize = 10.sp) },
                            modifier = Modifier.testTag("demo_account_sarah")
                        )
                        SuggestionChip(
                            onClick = {
                                emailInput = "Amir Danish"
                                passwordInput = "eco12345"
                                locationInput = "Cyberjaya, Selangor"
                                occupationInput = "Pengurus Projek Hijau"
                                icInput = "990325-10-5123"
                                phoneInput = "+60 19-456 7890"
                            },
                            label = { Text("Amir (Cyber)", fontSize = 10.sp) },
                            modifier = Modifier.testTag("demo_account_nurul")
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            loginError = null
                        },
                        label = { Text(if (isBm) "Nama Pengguna / Emel" else "Username / Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(if (isBm) "Kata Laluan" else "Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Password"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { locationInput = it },
                        label = { Text(if (isBm) "Lokasi Anda" else "Your Location") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_location_input")
                            .testTag("login_school_input")
                    )

                    OutlinedTextField(
                        value = occupationInput,
                        onValueChange = { occupationInput = it },
                        label = { Text(if (isBm) "Pekerjaan" else "Occupation") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_occupation_input")
                    )

                    OutlinedTextField(
                        value = icInput,
                        onValueChange = { icInput = it },
                        label = { Text(if (isBm) "No. Kad Pengenalan (IC)" else "IC Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_ic_input")
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(if (isBm) "Nombor Telefon" else "Phone Number") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input")
                    )

                    if (loginError != null) {
                        Text(loginError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    // Google Email Sign-In Option
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithGoogle("alifhaikalsha21202@gmail.com", "Alif Haikal")
                            showLoginDialog = false
                            Toast.makeText(
                                context,
                                if (isBm) "Log masuk dengan Google Email (alifhaikalsha21202@gmail.com) berjaya!" else "Signed in with Google Email (alifhaikalsha21202@gmail.com)!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_login_button")
                    ) {
                        Text("🇬", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBm) "Log Masuk dengan Google Email" else "Sign In with Google Email", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            loginError = if (isBm) "Sila masukkan Nama atau Emel" else "Please enter Name or Email"
                        } else {
                            viewModel.login(
                                usernameOrEmail = emailInput.trim(),
                                location = locationInput.trim(),
                                occupation = occupationInput.trim(),
                                icNumber = icInput.trim(),
                                phoneNumber = phoneInput.trim()
                            )
                            showLoginDialog = false
                            Toast.makeText(
                                context,
                                if (isBm) "Log masuk berjaya! Selamat kembali." else "Login successful! Welcome back.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    modifier = Modifier.testTag("login_submit_button")
                ) {
                    Text(if (isBm) "Log Masuk" else "Log In")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginDialog = false },
                    modifier = Modifier.testTag("close_login_dialog_button")
                ) {
                    Text(if (isBm) "Batal" else "Cancel")
                }
            }
        )
    }

    // Reminder Dialog (Section 19)
    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            icon = { Text("🔔", fontSize = 28.sp) },
            title = {
                Text(if (isBm) "Peringatan Kitar Semula Pintar" else "Smart Recycling Reminders", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBm) "🔋 Hari Pengumpulan E-Sisa Sekolah" else "🔋 School E-Waste Drop-off Day",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isBm) "Esok jam 8:00 pagi di Dataran Sekolah." else "Tomorrow at 8:00 AM at the School Quad.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBm) "🎯 Cabaran Kitar Semula Harian" else "🎯 Daily Recycling Challenge",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isBm) "Tinggal 2 botol plastik untuk menamatkan cabaran minggu ini!" else "2 plastic bottles left to complete this week's challenge!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReminderDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text(if (isBm) "Faham" else "Got It")
                }
            }
        )
    }

    // About ECOBIN AI Dialog (Section 29)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Text("🌱", fontSize = 32.sp) },
            title = {
                Text("ECOBIN AI", fontWeight = FontWeight.ExtraBold)
            },
            text = {
                Column {
                    Text(
                        text = "“Scan. Recycle. Earn. Make an Impact.”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isBm)
                            "ECOBIN AI ialah platform pintar yang membantu pengguna mengenal pasti sisa, mendapatkan panduan pelupusan, mencari lokasi kitar semula dan menjejaki impak aktiviti mereka."
                        else
                            "An AI-powered recycling assistant that helps people identify, recycle and track their environmental impact.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "🏆 Dibina khas untuk Pertandingan Inovasi Sekolah & Kelestarian Komuniti.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text(if (isBm) "Tutup" else "Close")
                }
            }
        )
    }

    // Avatar Picker Dialog
    if (showAvatarPickerDialog) {
        val ecoAvatars = listOf(
            "avatar:robot" to (if (isBm) "Robot AI" else "AI Bot"),
            "avatar:scholar" to (if (isBm) "Pelajar" else "Scholar"),
            "avatar:nature" to (if (isBm) "Alam" else "Nature"),
            "avatar:farmer" to (if (isBm) "Pekebun" else "Gardener"),
            "avatar:fox" to (if (isBm) "Rubah" else "Eco Fox"),
            "avatar:panda" to (if (isBm) "Panda" else "Eco Panda"),
            "avatar:cyclist" to (if (isBm) "Basikal" else "Cyclist"),
            "avatar:earth" to (if (isBm) "Bumi" else "Earth"),
            "avatar:butterfly" to (if (isBm) "Rama²" else "Butterfly"),
            "avatar:superhero" to (if (isBm) "Adiwira" else "Green Hero"),
            "avatar:hero" to (if (isBm) "Pejuang" else "Eco Hero"),
            "logo" to (if (isBm) "Logo" else "Logo")
        )

        AlertDialog(
            onDismissRequest = { showAvatarPickerDialog = false },
            icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EcoGreenPrimary) },
            title = {
                Text(
                    text = if (isBm) "Tukar Gambar Profil" else "Change Profile Photo",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isBm)
                            "Pilih foto dari galeri telefon anda atau pilih salah satu avatar tema eko di bawah:"
                        else
                            "Pick a photo from your phone's gallery or choose an eco avatar below:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Button to launch Zero-Permission Photo Picker
                    OutlinedButton(
                        onClick = {
                            showAvatarPickerDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_gallery_photo_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = EcoGreenPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "📸 Pilih Foto Dari Galeri" else "📸 Choose Photo From Gallery",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (isBm) "Pilih Avatar Eko:" else "Choose Eco Avatar:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Grid of Avatar Presets
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                    ) {
                        items(ecoAvatars) { (id, label) ->
                            val isSelected = userProfileImage == id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) EcoGreenPrimary.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) EcoGreenPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.updateProfileImage(id)
                                        showAvatarPickerDialog = false
                                        Toast.makeText(
                                            context,
                                            if (isBm) "Avatar berjaya ditukar!" else "Avatar updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                                    .testTag("avatar_option_$id")
                            ) {
                                UserProfileAvatar(
                                    imageIdentifier = id,
                                    size = 40.dp
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAvatarPickerDialog = false }
                ) {
                    Text(if (isBm) "Tutup" else "Close")
                }
            }
        )
    }
}
