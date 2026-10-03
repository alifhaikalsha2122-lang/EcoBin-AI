package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.EcoStrings
import com.example.model.AppLanguage
import com.example.model.EcoChallenge
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.components.ChallengeCelebrationDialog
import com.example.ui.components.UserProfileAvatar
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userProfileImage by viewModel.userProfileImage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userPoints by viewModel.repository.userPoints.collectAsState()
    val totalItems by viewModel.repository.totalItemsRecycled.collectAsState()
    val wasteDiverted by viewModel.repository.wasteDivertedKg.collectAsState()
    val carbonImpact by viewModel.repository.carbonImpactKg.collectAsState()
    val recentActivities by viewModel.repository.recentActivities.collectAsState()
    val challenges by viewModel.repository.challenges.collectAsState()
    val isBm = language == AppLanguage.BM

    var completedCelebrationChallenge by remember { mutableStateOf<EcoChallenge?>(null) }

    val activeChallenge = challenges.firstOrNull { !it.isCompleted } ?: challenges.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // 1. Greeting Section
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_greeting_section")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserProfileAvatar(
                        imageIdentifier = userProfileImage,
                        size = 52.dp,
                        onClick = { viewModel.navigateTo(Screen.PROFILE) },
                        modifier = Modifier.testTag("home_user_avatar")
                    )

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = EcoStrings.welcomeGreeting(language),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = if (isBm) "Mari kitar semula demi masa depan hijau 🌿" else "Let's recycle for a greener tomorrow 🌿",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "ECOBIN AI Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, EcoGreenPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("home_header_logo_image")
                    )
                }
            }

            if (!isLoggedIn) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable { viewModel.navigateTo(Screen.PROFILE) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👋", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Anda Dalam Mod Tetamu" else "You Are in Guest Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isBm) "Log masuk untuk kumpul lencana & mata sekolah." else "Log in to earn badges & school points.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (isBm) "Log Masuk ➔" else "Log In ➔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. User Eco Points Display Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_eco_points_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBm) "MATA GANJARAN ANDA" else "YOUR ECO POINTS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$userPoints",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Eco Points",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isBm) "5 Hari Berterusan" else "5-Day Streak",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Level and Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 ${viewModel.repository.getLevelName(language)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (isBm) "${500 - userPoints} mata ke Tahap Seterusnya" else "${500 - userPoints} pts to Next Rank",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    val progress = (userPoints.toFloat() / 500f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EcoGreenPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Special Presentation Competition Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EcoGreenDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.PRESENTATION_DEMO) }
                    .testTag("home_presentation_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F3A22)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 22.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ECOBIN AI DEMO",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE047)
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x33FDE047)
                            ) {
                                Text(
                                    text = if (isBm) "MOD PERTANDINGAN" else "INNOVATION MODE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE047),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isBm)
                                "Alur penuh: Imbas → Panduan → Peta → Kitar → Impak"
                            else
                                "Full judge flow: Scan → Guide → Map → Recycle → Impact",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Demo",
                        tint = Color(0xFFFDE047)
                    )
                }
            }
        }

        // 3. Grid of Four 'Quick Actions' (Scan, Map, AI Chat, Challenge)
        item {
            Text(
                text = if (isBm) "Tindakan Pantas" else "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            // Row 1: Scan & Map
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = if (isBm) "Imbas (Scan)" else "Scan",
                    subtitle = if (isBm) "Kamera / AI Vision" else "AI Waste Scanner",
                    iconEmoji = "📸",
                    gradient = listOf(EcoGreenPrimary, EcoGreenLight),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_scan",
                    onClick = { viewModel.navigateTo(Screen.SCANNER) }
                )
                QuickActionCard(
                    title = if (isBm) "Peta (Map)" else "Map",
                    subtitle = if (isBm) "Pusat Kitar Semula" else "Recycling Centers",
                    iconEmoji = "📍",
                    gradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_map",
                    onClick = { viewModel.navigateTo(Screen.MAP) }
                )
            }

            Spacer(Modifier.height(10.dp))

            // Row 2: AI Chat & Challenge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = if (isBm) "AI Chat" else "AI Chat",
                    subtitle = if (isBm) "Tanya Pelupusan Sisa" else "Smart Disposal Guide",
                    iconEmoji = "🧠",
                    gradient = listOf(Color(0xFF7C3AED), Color(0xFFA78BFA)),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_ai_chat",
                    onClick = { viewModel.navigateTo(Screen.DISPOSAL_CHAT) }
                )
                QuickActionCard(
                    title = if (isBm) "Cabaran (Challenge)" else "Challenge",
                    subtitle = if (isBm) "Misi Harian & Mata" else "Daily Missions & Pts",
                    iconEmoji = "🎯",
                    gradient = listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_challenge",
                    onClick = { viewModel.navigateTo(Screen.CHALLENGES) }
                )
            }
        }

        // Secondary School & Education Shortcuts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SchoolShortcutChip(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = if (isBm) "Panduan 3R" else "3R Guide",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.RECYCLING_GUIDE) }
                )
                SchoolShortcutChip(
                    icon = Icons.Default.School,
                    label = if (isBm) "Sekolah" else "School",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.SCHOOL_DASHBOARD) }
                )
                SchoolShortcutChip(
                    icon = Icons.Default.AddPhotoAlternate,
                    label = if (isBm) "Bukti Kitar" else "Proof",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.PROOF_UPLOAD) }
                )
                SchoolShortcutChip(
                    icon = Icons.Default.Groups,
                    label = if (isBm) "Komuniti" else "Feed",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.COMMUNITY) }
                )
            }
        }

        // 4. 'Eco Summary' Section (Recycling Statistics)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_eco_summary_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBm) "Ringkasan Eko (Eco Summary)" else "Eco Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isBm) "Kitar Semula Anda" else "Your Recycling Stats",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            emoji = "♻️",
                            value = "$totalItems",
                            unit = if (isBm) "item" else "items",
                            label = EcoStrings.totalItemsRecycled(language),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            emoji = "🪙",
                            value = "$userPoints",
                            unit = "pts",
                            label = EcoStrings.ecoPoints(language),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            emoji = "⚖️",
                            value = "%.1f".format(wasteDiverted),
                            unit = "kg",
                            label = EcoStrings.wasteDiverted(language),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            emoji = "🌱",
                            value = "%.1f".format(carbonImpact),
                            unit = "kg CO₂e",
                            label = EcoStrings.carbonAvoided(language),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Active Daily Challenge Section with Engaging UI
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_challenge_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val catIcon = when {
                                activeChallenge.id == "ch-1" || activeChallenge.titleEn.contains("Plastic") -> "🍶"
                                activeChallenge.id == "ch-2" || activeChallenge.titleEn.contains("E-Waste") || activeChallenge.titleEn.contains("Battery") -> "⚡"
                                activeChallenge.id == "ch-3" || activeChallenge.titleEn.contains("Paper") -> "📦"
                                activeChallenge.id == "ch-4" || activeChallenge.titleEn.contains("Glass") -> "🫙"
                                activeChallenge.id == "ch-5" || activeChallenge.titleEn.contains("Scan") -> "📸"
                                else -> "🎯"
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EcoGreenPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(catIcon, fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Cabaran Harian Aktif" else "Daily Active Mission",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "🔥 5-Day Streak",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "+${activeChallenge.pointsReward} pts",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isBm) activeChallenge.titleBm else activeChallenge.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = if (isBm) activeChallenge.descBm else activeChallenge.descEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    val progress = (activeChallenge.current.toFloat() / activeChallenge.target).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${activeChallenge.current} / ${activeChallenge.target} ${if (isBm) "selesai" else "completed"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenPrimary
                            )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EcoGreenPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!activeChallenge.isCompleted) {
                            Button(
                                onClick = {
                                    viewModel.repository.completeChallenge(activeChallenge.id)
                                    completedCelebrationChallenge = activeChallenge
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_claim_challenge_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Claim", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isBm) "Tuntut +${activeChallenge.pointsReward} Pts" else "Claim +${activeChallenge.pointsReward} Pts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.SCANNER) },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_scan_now_challenge_btn")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Scan", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBm) "Imbas Sekarang" else "Scan Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.CHALLENGES) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("continue_challenge_button")
                        ) {
                            Text(
                                text = if (isBm) "Semua Cabaran ➔" else "All Missions ➔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = EcoStrings.recentActivity(language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${recentActivities.size} records",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        items(recentActivities.take(4)) { activity ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(activity.category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activity.category.iconEmoji, fontSize = 18.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.itemName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${activity.category.getDisplayName(language)} • ${activity.timestamp}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EcoGreenPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "+${activity.points} pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EcoGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Celebratory Animation Effect & Confetti Dialog
    if (completedCelebrationChallenge != null) {
        ChallengeCelebrationDialog(
            challenge = completedCelebrationChallenge!!,
            language = language,
            onDismiss = { completedCelebrationChallenge = null },
            onScanMore = {
                completedCelebrationChallenge = null
                viewModel.navigateTo(Screen.SCANNER)
            }
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp)
        ) {
            Column {
                Text(iconEmoji, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SchoolShortcutChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatTile(
    emoji: String,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}
