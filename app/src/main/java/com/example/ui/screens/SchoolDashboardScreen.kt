package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

@Composable
fun SchoolDashboardScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM
    val stats = viewModel.repository.schoolStats
    val classList = viewModel.repository.classLeaderboard

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // School Header Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EcoGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F3A22)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏫", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SMK BINTANG HIJAU",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (isBm) "Inisiatif Sekolah Hijau & Kelestarian" else "Green School & Sustainability Initiative",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = if (isBm)
                            "Kempen Kitar Semula Peringkat Sekolah: Sasaran 100kg Mingguan"
                        else
                            "School-wide Recycling Drive: 100kg Weekly Target",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    val prog = (stats.weeklyCurrentKg / stats.weeklyTargetKg).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { prog },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFFDE047),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stats.weeklyCurrentKg} kg / ${stats.weeklyTargetKg} kg",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                        )
                        Text(
                            text = "${(prog * 100).toInt()}% dicapai",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        )
                    }
                }
            }
        }

        // Stats Matrix 4 Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    emoji = "👥",
                    value = "${stats.totalStudents}",
                    unit = if (isBm) "pelajar" else "students",
                    label = if (isBm) "Pelajar Menyertai" else "Participating",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    emoji = "🪙",
                    value = "${stats.totalPoints}",
                    unit = "pts",
                    label = if (isBm) "Jumlah Mata Sekolah" else "Total School Points",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    emoji = "📦",
                    value = "${stats.totalItems}",
                    unit = "item",
                    label = if (isBm) "Jumlah Item Dikitar" else "Items Recycled",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    emoji = "⚖️",
                    value = "%.1f".format(stats.wasteDivertedKg),
                    unit = "kg",
                    label = if (isBm) "Sisa Dielakkan" else "Waste Diverted",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Highlight Cards: Top Class & Top Student
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🥇 ${if (isBm) "Kelas Terbaik" else "Top Class"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(stats.topClass, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🌟 ${if (isBm) "Pelajar Terbaik" else "Top Student"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(stats.topStudent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Inter-Class Recycling Standings
        item {
            Text(
                text = if (isBm) "Kedudukan Antara Kelas" else "Inter-Class Standings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(classList) { cl ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${cl.rank}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cl.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${cl.itemsRecycled} item dikumpul • ${cl.badge}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${cl.points} pts",
                        fontWeight = FontWeight.ExtraBold,
                        color = EcoGreenPrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
