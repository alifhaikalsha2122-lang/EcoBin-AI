package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color as AndroidColor
import com.example.data.EcoStrings
import com.example.model.AppLanguage
import com.example.model.WasteCategory
import com.example.ui.EcoViewModel
import com.example.ui.theme.*
import com.example.util.EcoShareUtils

@Composable
fun ImpactScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.repository.language.collectAsState()
    val wasteDiverted by viewModel.repository.wasteDivertedKg.collectAsState()
    val carbonImpact by viewModel.repository.carbonImpactKg.collectAsState()
    val totalItems by viewModel.repository.totalItemsRecycled.collectAsState()

    val plasticCount by viewModel.repository.plasticCount.collectAsState()
    val paperCount by viewModel.repository.paperCount.collectAsState()
    val glassCount by viewModel.repository.glassCount.collectAsState()
    val metalCount by viewModel.repository.metalCount.collectAsState()
    val eWasteCount by viewModel.repository.eWasteCount.collectAsState()

    val calcCategory by viewModel.calculatorCategory.collectAsState()
    val calcUnits by viewModel.calculatorUnits.collectAsState()
    val isBm = language == AppLanguage.BM

    // Estimated calculation formulas (labeled clearly as illustrative estimates)
    val factorKgPerUnit = when (calcCategory) {
        WasteCategory.PLASTIC -> 0.025f
        WasteCategory.PAPER -> 0.12f
        WasteCategory.GLASS -> 0.25f
        WasteCategory.METAL -> 0.015f
        WasteCategory.E_WASTE -> 0.04f
        else -> 0.05f
    }
    val factorCo2PerUnit = when (calcCategory) {
        WasteCategory.PLASTIC -> 0.09f
        WasteCategory.PAPER -> 0.18f
        WasteCategory.GLASS -> 0.20f
        WasteCategory.METAL -> 0.12f
        WasteCategory.E_WASTE -> 0.15f
        else -> 0.08f
    }

    val estimatedDivertedKg = calcUnits * factorKgPerUnit
    val estimatedCo2Kg = calcUnits * factorCo2PerUnit
    val estimatedWaterLiters = estimatedCo2Kg * 6.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = if (isBm) "Impak Alam Sekitar Saya" else "My Eco Impact",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isBm)
                        "Pantau jumlah sisa yang diselamatkan daripada tapak pelupusan dan sumbangan anda kepada alam sekitar."
                    else
                        "Track waste diverted from landfills and your cumulative environmental contributions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Hero Metric Cards (Waste Diverted & Carbon Avoided)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isBm) "Anggaran Impak Kumulatif Anda" else "Your Estimated Cumulative Impact",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = EcoStrings.estimatedTag(language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("⚖️", fontSize = 24.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "%.1f kg".format(wasteDiverted),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EcoGreenDark
                                    )
                                )
                                Text(
                                    text = if (isBm) "Sisa Dielakkan" else "Waste Diverted",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("🌱", fontSize = 24.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "%.1f kg".format(carbonImpact),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EcoGreenPrimary
                                    )
                                )
                                Text(
                                    text = if (isBm) "CO₂ Dijimatkan" else "CO₂ Avoided",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💧 ~${(carbonImpact * 6.5f).toInt()} L ${if (isBm) "Air Disimpan" else "Water Saved"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "⚡ ~${(carbonImpact * 1.4f).toInt()} kWh ${if (isBm) "Tenaga Dijimat" else "Energy Preserved"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val badges = viewModel.repository.badges.value
                            val userPoints = viewModel.repository.userPoints.value
                            val unlockedCount = badges.count { it.unlocked }
                            EcoShareUtils.shareRecyclingMilestones(
                                context = context,
                                userName = viewModel.repository.userName,
                                userSchool = viewModel.repository.userLocation,
                                userClass = viewModel.repository.userOccupation,
                                levelName = viewModel.repository.getLevelName(language),
                                points = userPoints,
                                totalItems = totalItems,
                                carbonSavedKg = carbonImpact,
                                unlockedBadgesCount = unlockedCount,
                                language = language,
                                userLocation = viewModel.repository.userLocation,
                                userOccupation = viewModel.repository.userOccupation
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_impact_milestones_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "Kongsi Impak Eko ke Media Sosial" else "Share Impact to Social Media",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Weekly and Monthly Recycling Trends (MPAndroidChart Bar Chart)
        item {
            RecyclingTrendsChartSection(
                viewModel = viewModel,
                isBm = isBm
            )
        }

        // Waste Categories Breakdown
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBm) "Pecahan Kategori Sisa Dikitar ($totalItems Item)" else "Waste Segregation Breakdown ($totalItems Items)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(12.dp))

                    CategoryProgressRow(
                        name = WasteCategory.PLASTIC.getDisplayName(language),
                        emoji = "♻️",
                        count = plasticCount,
                        total = totalItems,
                        color = ColorPlastic
                    )
                    CategoryProgressRow(
                        name = WasteCategory.PAPER.getDisplayName(language),
                        emoji = "📄",
                        count = paperCount,
                        total = totalItems,
                        color = ColorPaper
                    )
                    CategoryProgressRow(
                        name = WasteCategory.GLASS.getDisplayName(language),
                        emoji = "🍾",
                        count = glassCount,
                        total = totalItems,
                        color = ColorGlass
                    )
                    CategoryProgressRow(
                        name = WasteCategory.METAL.getDisplayName(language),
                        emoji = "🥫",
                        count = metalCount,
                        total = totalItems,
                        color = ColorMetal
                    )
                    CategoryProgressRow(
                        name = WasteCategory.E_WASTE.getDisplayName(language),
                        emoji = "🔋",
                        count = eWasteCount,
                        total = totalItems,
                        color = ColorEWaste
                    )
                }
            }
        }

        // Interactive Carbon Impact Estimator (Section 16)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧮", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "Kalkulator Anggaran Impak Karbon" else "Carbon Impact Estimator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = if (isBm)
                            "Pilih jenis dan kuantiti sisa untuk mengira anggaran kesan pengurangan karbon."
                        else
                            "Choose material and quantity to calculate estimated carbon reduction.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Select Category Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(WasteCategory.PLASTIC, WasteCategory.PAPER, WasteCategory.GLASS, WasteCategory.METAL, WasteCategory.E_WASTE)) { cat ->
                            FilterChip(
                                selected = calcCategory == cat,
                                onClick = { viewModel.setCalculatorCategory(cat) },
                                label = { Text(cat.getDisplayName(language), fontSize = 11.sp) },
                                leadingIcon = { Text(cat.iconEmoji, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Quantity Counter Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBm) "Kuantiti Item:" else "Quantity of Items:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.setCalculatorUnits(calcUnits - 5) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "$calcUnits",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )

                            IconButton(
                                onClick = { viewModel.setCalculatorUnits(calcUnits + 5) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Output Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isBm) "Anggaran Sisa Dielakkan:" else "Estimated Waste Diverted:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "%.2f kg".format(estimatedDivertedKg),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = EcoGreenDark
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isBm) "CO₂ Dielakkan:" else "CO₂ Avoided:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "%.2f kg CO₂e".format(estimatedCo2Kg),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = EcoGreenPrimary
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "💧 ~${estimatedWaterLiters.toInt()} L air dijimatkan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Regulatory Note from Brief
                    Text(
                        text = if (isBm)
                            "“Anggaran adalah ilustrasi sahaja dan mungkin berbeza mengikut bahan, sistem kitar semula tempatan dan metodologi.”"
                        else
                            "“Estimates are illustrative and may vary depending on material, local recycling systems, and methodology.”",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        // SDG Section (Section 28)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "SDG 12",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = EcoStrings.sdgTitle(language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = EcoStrings.sdgDesc(language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SdgPill("SDG 11", if (isBm) "Bandar Lestari" else "Sustainable Cities")
                        SdgPill("SDG 13", if (isBm) "Tindakan Iklim" else "Climate Action")
                        SdgPill("SDG 4", if (isBm) "Pendidikan Berkualiti" else "Quality Education")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    name: String,
    emoji: String,
    count: Int,
    total: Int,
    color: Color
) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("$count item (${(pct * 100).toInt()}%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun SdgPill(code: String, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(code, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RecyclingTrendsChartSection(
    viewModel: EcoViewModel,
    isBm: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTrendMode by remember { mutableStateOf("WEEKLY") } // WEEKLY, MONTHLY
    val weeklyData = viewModel.repository.weeklyTrends
    val monthlyData = viewModel.repository.monthlyTrends

    val activeData = if (selectedTrendMode == "WEEKLY") weeklyData else monthlyData
    val totalCount = activeData.sumOf { it.count }
    val maxPoint = activeData.maxByOrNull { it.count }
    val avgCount = if (activeData.isNotEmpty()) totalCount.toFloat() / activeData.size else 0f

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isBm) "Trend Kitar Semula" else "Recycling Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isBm) "Graf Bar MPAndroidChart" else "MPAndroidChart Bar Trends",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Weekly / Monthly Toggle Buttons
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTrendMode == "WEEKLY") EcoGreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTrendMode = "WEEKLY" }
                                .testTag("trends_tab_weekly")
                        ) {
                            Text(
                                text = if (isBm) "Mingguan" else "Weekly",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedTrendMode == "WEEKLY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedTrendMode == "MONTHLY") EcoGreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTrendMode = "MONTHLY" }
                                .testTag("trends_tab_monthly")
                        ) {
                            Text(
                                text = if (isBm) "Bulanan" else "Monthly",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedTrendMode == "MONTHLY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // MPAndroidChart BarChart view integration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.02f))
                    .testTag("recycling_trends_barchart")
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        BarChart(ctx).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            setDrawBarShadow(false)
                            setDrawValueAboveBar(true)
                            setPinchZoom(false)
                            setScaleEnabled(false)
                            isDoubleTapToZoomEnabled = false

                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            xAxis.granularity = 1f
                            xAxis.textColor = AndroidColor.parseColor("#475569")
                            xAxis.textSize = 10f

                            axisLeft.setDrawGridLines(true)
                            axisLeft.gridColor = AndroidColor.parseColor("#E2E8F0")
                            axisLeft.textColor = AndroidColor.parseColor("#475569")
                            axisLeft.axisMinimum = 0f

                            axisRight.isEnabled = false
                            legend.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val labels = activeData.map { if (isBm) it.labelBm else it.label }
                        val entries = activeData.mapIndexed { index, point ->
                            BarEntry(index.toFloat(), point.count.toFloat())
                        }
                        val dataSet = BarDataSet(entries, "Items Recycled").apply {
                            color = AndroidColor.parseColor("#10B981")
                            valueTextColor = AndroidColor.parseColor("#0F172A")
                            valueTextSize = 10f
                        }
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        chart.data = BarData(dataSet).apply {
                            barWidth = 0.55f
                        }
                        chart.notifyDataSetChanged()
                        chart.invalidate()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Summary Stats Row (Total, Peak, Average)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EcoGreenPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBm) "Jumlah Dikitar" else "Total Recycled",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$totalCount item",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EcoGreenPrimary
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBm) "Puncak" else "Peak",
                            fontSize = 10.sp,
                            color = Color(0xFFB45309)
                        )
                        Text(
                            text = "${maxPoint?.let { if (isBm) it.labelBm else it.label } ?: "-"}: ${maxPoint?.count ?: 0}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBm) "Purata" else "Average",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1f /unit".format(avgCount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

