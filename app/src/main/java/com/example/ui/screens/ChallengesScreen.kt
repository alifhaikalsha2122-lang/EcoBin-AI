package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.EcoChallenge
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.components.ChallengeCelebrationDialog
import com.example.ui.theme.*

@Composable
fun ChallengesScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val challenges by viewModel.repository.challenges.collectAsState()
    val userPoints by viewModel.repository.userPoints.collectAsState()
    val pointsThisWeek by viewModel.repository.pointsThisWeek.collectAsState()
    val isBm = language == AppLanguage.BM

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, COMPLETED
    var completedCelebrationChallenge by remember { mutableStateOf<EcoChallenge?>(null) }

    val activeCount = challenges.count { !it.isCompleted }
    val completedCount = challenges.count { it.isCompleted }

    val filteredChallenges = when (selectedFilter) {
        "ACTIVE" -> challenges.filter { !it.isCompleted }
        "COMPLETED" -> challenges.filter { it.isCompleted }
        else -> challenges
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("daily_challenges_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Screen Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isBm) "Cabaran Harian" else "Daily Challenges",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EcoGreenPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$activeCount ${if (isBm) "Aktif" else "Active"}",
                                    color = EcoGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isBm)
                                "Selesaikan misi harian kitar semula untuk menjana mata ganjaran Eco Points."
                            else
                                "Complete daily recycling missions to simulate and earn valuable Eco Points.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }

        // Daily Streak & Reset Countdown Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBm) "5 Hari Berterusan" else "5-Day Streak",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = if (isBm) "Pengguna Aktif Eko" else "Active Eco Recycler",
                                fontSize = 10.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱️", fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBm) "Misi Harian Baru" else "Daily Reset In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "06 jam 45 min",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Eco Points & Level Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBm) "JUMLAH MATA TERKUMPUL" else "TOTAL EARNED POINTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$userPoints",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Eco Points",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = "🏆 ${viewModel.repository.getLevelName(language)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚡", fontSize = 18.sp)
                            Text(
                                text = "+$pointsThisWeek pts",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = if (isBm) "Minggu Ini" else "This Week",
                                fontSize = 9.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips (All, Active, Completed)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("${if (isBm) "Semua" else "All"} (${challenges.size})", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "ACTIVE",
                        onClick = { selectedFilter = "ACTIVE" },
                        label = { Text("${if (isBm) "Aktif" else "Active"} ($activeCount)", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == "COMPLETED",
                        onClick = { selectedFilter = "COMPLETED" },
                        label = { Text("${if (isBm) "Selesai" else "Completed"} ($completedCount)", fontSize = 12.sp) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = if (isBm) "Senarai Cabaran & Misi" else "Active Challenges List",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Active Challenges List
        items(filteredChallenges) { challenge ->
            val isDone = challenge.isCompleted
            val progressPercent = ((challenge.current.toFloat() / challenge.target) * 100).toInt().coerceIn(0, 100)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(if (isDone) 1.dp else 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("challenge_card_${challenge.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title and Reward Badge Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val catEmoji = when {
                                challenge.id == "ch-1" || challenge.titleEn.contains("Plastic") -> "🍶"
                                challenge.id == "ch-2" || challenge.titleEn.contains("E-Waste") || challenge.titleEn.contains("Battery") -> "⚡"
                                challenge.id == "ch-3" || challenge.titleEn.contains("Paper") -> "📦"
                                challenge.id == "ch-4" || challenge.titleEn.contains("Glass") -> "🫙"
                                challenge.id == "ch-5" || challenge.titleEn.contains("Scan") -> "📸"
                                challenge.id == "ch-6" || challenge.titleEn.contains("Aluminum") || challenge.titleEn.contains("Metal") -> "🥫"
                                else -> "🎯"
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) EcoGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isDone) "✅" else catEmoji, fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (isBm) challenge.titleBm else challenge.titleEn,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDone) EcoGreenPrimary.copy(alpha = 0.15f) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (isDone) (if (isBm) "Selesai" else "Completed") else "+${challenge.pointsReward} pts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isDone) EcoGreenPrimary else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Task Description
                    Text(
                        text = if (isBm) challenge.descBm else challenge.descEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Progress Bar & Percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${challenge.current} / ${challenge.target} ${if (isBm) "selesai" else "done"}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$progressPercent%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isDone) EcoGreenPrimary else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    val prog = (challenge.current.toFloat() / challenge.target).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { prog },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isDone) EcoGreenPrimary else Color(0xFF0284C7),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Deadline & Complete Challenge Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏰ ${challenge.deadline}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )

                        if (!isDone) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(Screen.SCANNER) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(if (isBm) "Imbas" else "Scan", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.repository.completeChallenge(challenge.id)
                                        completedCelebrationChallenge = challenge
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .testTag("complete_challenge_button")
                                        .testTag("complete_challenge_${challenge.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Complete",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Tuntut" else "Claim",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EcoGreenPrimary.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Done",
                                        tint = EcoGreenPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Mata Dituntut" else "Claimed",
                                        color = EcoGreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Celebratory Animation Dialog & Confetti Explosion Effect
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
