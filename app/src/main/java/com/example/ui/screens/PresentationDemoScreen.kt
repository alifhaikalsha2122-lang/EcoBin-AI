package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

data class DemoStepInfo(
    val stepIndex: Int,
    val titleEn: String,
    val titleBm: String,
    val subtitleEn: String,
    val subtitleBm: String,
    val iconEmoji: String,
    val targetScreen: Screen,
    val keyHighlightEn: String,
    val keyHighlightBm: String
)

@Composable
fun PresentationDemoScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM
    val currentStep by viewModel.demoStep.collectAsState()

    val demoSteps = remember {
        listOf(
            DemoStepInfo(
                stepIndex = 0,
                titleEn = "1. SCAN WASTE",
                titleBm = "1. IMBAS SISA",
                subtitleEn = "Capture an image of recyclable waste using camera or test sample",
                subtitleBm = "Ambil gambar sisa kitar semula menggunakan kamera atau sampel ujian",
                iconEmoji = "📸",
                targetScreen = Screen.SCANNER,
                keyHighlightEn = "Gemini Vision Multimodal AI input",
                keyHighlightBm = "Input AI Multimodal Gemini Vision"
            ),
            DemoStepInfo(
                stepIndex = 1,
                titleEn = "2. AI IDENTIFIES",
                titleBm = "2. AI MENGENAL PASTI",
                subtitleEn = "AI detects polymer type (PET #1), recyclability & hazards",
                subtitleBm = "AI mengenal pasti jenis bahan (PET #1), kebolehan kitar & bahaya",
                iconEmoji = "🧠",
                targetScreen = Screen.SCANNER,
                keyHighlightEn = "Structured classification & confidence score",
                keyHighlightBm = "Klasifikasi berstruktur & skor keyakinan"
            ),
            DemoStepInfo(
                stepIndex = 2,
                titleEn = "3. RECYCLING GUIDE",
                titleBm = "3. PANDUAN PELUPUSAN",
                subtitleEn = "Provides exact preparation steps, target bin & dos/don'ts",
                subtitleBm = "Menyediakan langkah penyediaan, tong sasaran & perkara perlu/elak",
                iconEmoji = "📖",
                targetScreen = Screen.RECYCLING_GUIDE,
                keyHighlightEn = "Zero-contamination educational guidelines",
                keyHighlightBm = "Panduan pendidikan cegah pencemaran sisa"
            ),
            DemoStepInfo(
                stepIndex = 3,
                titleEn = "4. FIND RECYCLING BIN",
                titleBm = "4. CARI TONG KITAR",
                subtitleEn = "Locates closest school collection point & community smart bins",
                subtitleBm = "Mencari pusat pengumpulan sekolah & tong pintar terdekat",
                iconEmoji = "📍",
                targetScreen = Screen.MAP,
                keyHighlightEn = "Distance mapping & accepted material filter",
                keyHighlightBm = "Peta jarak & penapis bahan yang diterima"
            ),
            DemoStepInfo(
                stepIndex = 4,
                titleEn = "5. RECYCLE & EARN",
                titleBm = "5. KITAR & DAPAT MATA",
                subtitleEn = "Deposit item, record activity & instantly gain Eco Points",
                subtitleBm = "Lupus item, rekod aktiviti & terima Eco Points serta-merta",
                iconEmoji = "🪙",
                targetScreen = Screen.PROOF_UPLOAD,
                keyHighlightEn = "Gamified motivation for youth & students",
                keyHighlightBm = "Gamifikasi motivasi untuk belia & pelajar"
            ),
            DemoStepInfo(
                stepIndex = 5,
                titleEn = "6. COMPETE ON LEADERBOARD",
                titleBm = "6. PAPAN PENDAHULU",
                subtitleEn = "Encourages positive inter-class and inter-school rivalry",
                subtitleBm = "Menggalakkan persaingan sihat antara kelas & sekolah",
                iconEmoji = "🏆",
                targetScreen = Screen.LEADERBOARD,
                keyHighlightEn = "Individual, Class & School rankings",
                keyHighlightBm = "Kedudukan Individu, Kelas & Sekolah"
            ),
            DemoStepInfo(
                stepIndex = 6,
                titleEn = "7. MY ECO IMPACT",
                titleBm = "7. IMPAK ALAM SEKITAR",
                subtitleEn = "Quantifies waste diverted, CO2 avoided and SDG 12 impact",
                subtitleBm = "Mengira sisa dielakkan, pengurangan CO2 & sumbangan SDG 12",
                iconEmoji = "🌱",
                targetScreen = Screen.IMPACT,
                keyHighlightEn = "Tangible environmental carbon calculator",
                keyHighlightBm = "Kalkulator pengurangan karbon nyata"
            )
        )
    }

    val activeStepInfo = demoSteps.getOrElse(currentStep) { demoSteps.first() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Presentation Mode Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = EcoGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFDE047)
                        ) {
                            Text(
                                text = "🏆 MOD PEMBENTANGAN JURI",
                                fontWeight = FontWeight.ExtraBold,
                                color = EcoGreenDark,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${currentStep + 1} / ${demoSteps.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "ECOBIN AI Innovation Pitch",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (isBm)
                            "Ikuti alur lengkap inovasi dari imbasan sisa hingga impak kelestarian untuk pembentangan pertandingan."
                        else
                            "Follow the end-to-end innovation journey from scanning waste to environmental impact metrics.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF6EE7B7),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Active Step Spotlight Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EcoGreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activeStepInfo.iconEmoji, fontSize = 24.sp)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isBm) activeStepInfo.titleBm else activeStepInfo.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isBm) activeStepInfo.keyHighlightBm else activeStepInfo.keyHighlightEn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = if (isBm) activeStepInfo.subtitleBm else activeStepInfo.subtitleEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Buttons: Test This Step & Next Step
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(activeStepInfo.targetScreen) },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("demo_open_screen_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Open")
                            Spacer(Modifier.width(6.dp))
                            Text(if (isBm) "Buka Halaman Ini" else "Launch Screen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (currentStep < demoSteps.size - 1) {
                            FilledTonalButton(
                                onClick = { viewModel.setDemoStep(currentStep + 1) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("demo_next_step_button")
                            ) {
                                Text(if (isBm) "Langkah Seterusnya" else "Next Step", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.setDemoStep(0) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBm) "Mula Semula" else "Restart", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Stepper Visual Flow List
        item {
            Text(
                text = if (isBm) "Langkah Demonstrasi Alur Penuh" else "Complete Presentation Workflow",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(demoSteps) { step ->
            val isCurrent = step.stepIndex == currentStep
            val isDone = step.stepIndex < currentStep

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                        isDone -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setDemoStep(step.stepIndex) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> EcoGreenPrimary
                                    isCurrent -> EcoGreenDark
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = "${step.stepIndex + 1}",
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBm) step.titleBm else step.titleEn,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isBm) step.keyHighlightBm else step.keyHighlightEn,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Text(step.iconEmoji, fontSize = 20.sp)
                }
            }
        }
    }
}
