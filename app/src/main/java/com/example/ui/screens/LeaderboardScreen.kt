package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.LeaderboardEntry
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = if (isBm) listOf("Pelajar (Individu)", "Kelas", "Sekolah") else listOf("Students (Individual)", "Class", "School")

    val currentList: List<LeaderboardEntry> = when (selectedTab) {
        0 -> viewModel.repository.individualLeaderboard
        1 -> viewModel.repository.classLeaderboard
        else -> viewModel.repository.schoolLeaderboard
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("leaderboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBm) "Papan Pendahulu Lestari" else "Eco Leaderboard",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "🏆 Top 10",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isBm)
                        "Senarai kedudukan pelajar aktif mengikut jumlah Mata Ganjaran Eko dan lencana kelestarian yang diraih."
                    else
                        "Ranked standings of students based on their earned Eco Points and sustainability badges.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Tab Selector (Students / Class / School)
        item {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("leaderboard_tab_$index")
                    )
                }
            }
        }

        // Top 3 Podium Cards (when viewing individual or classes)
        if (currentList.size >= 3) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place
                    PodiumCard(
                        entry = currentList[1],
                        medalEmoji = "🥈",
                        rankText = "2",
                        color = Color(0xFF94A3B8),
                        height = 140.dp,
                        modifier = Modifier.weight(1f)
                    )

                    // 1st Place (Taller)
                    PodiumCard(
                        entry = currentList[0],
                        medalEmoji = "🥇",
                        rankText = "1",
                        color = Color(0xFFF59E0B),
                        height = 165.dp,
                        modifier = Modifier.weight(1.1f)
                    )

                    // 3rd Place
                    PodiumCard(
                        entry = currentList[2],
                        medalEmoji = "🥉",
                        rankText = "3",
                        color = Color(0xFFD97706),
                        height = 125.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Leaderboard Full Ranks List Header
        item {
            Text(
                text = if (isBm) "Senarai Kedudukan Lengkap Pelajar" else "Full Student Rankings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Ranked Student Cards List
        items(currentList) { entry ->
            val isUser = entry.name.contains("(You)")
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(if (isUser) 3.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_item_${entry.rank}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge with Medals for Top 3
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (entry.rank) {
                                    1 -> Color(0xFFFDE68A)
                                    2 -> Color(0xFFE2E8F0)
                                    3 -> Color(0xFFFED7AA)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (entry.rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "${entry.rank}"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (entry.rank <= 3) 16.sp else 14.sp,
                            color = if (entry.rank <= 3) Color(0xFF1E293B) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Student Details (Name, Class, and Badge)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            )
                            if (isUser) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EcoGreenPrimary
                                ) {
                                    Text(
                                        text = if (isBm) "ANDA" else "YOU",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        if (entry.schoolOrClass.isNotBlank()) {
                            Text(
                                text = entry.schoolOrClass,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Respective Badge Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (entry.rank) {
                                1 -> Color(0xFFFEF3C7)
                                2 -> Color(0xFFF1F5F9)
                                3 -> Color(0xFFFFF7ED)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = entry.badge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = when (entry.rank) {
                                    1 -> Color(0xFFB45309)
                                    2 -> Color(0xFF475569)
                                    3 -> Color(0xFFC2410C)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Eco Points & Items Recycled
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${entry.points}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Eco Points",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${entry.itemsRecycled} ${if (isBm) "item" else "items"}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumCard(
    entry: LeaderboardEntry,
    medalEmoji: String,
    rankText: String,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.height(height)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(medalEmoji, fontSize = 26.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.name.replace(" (You)", ""),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                fontSize = 12.sp
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = entry.badge.take(16),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Text(
                text = "${entry.points} pts",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 11.sp
                )
            )
        }
    }
}
