package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.Screen
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

@Composable
fun ProofScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val isVerifying by viewModel.isVerifyingProof.collectAsState()
    val awardedPoints by viewModel.proofAwardedPoints.collectAsState()
    val isBm = language == AppLanguage.BM

    var proofBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            proofBitmap = bmp
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = if (isBm) "Hantar Bukti Aktiviti Kitar Semula" else "Submit Recycling Activity Proof",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isBm)
                        "Muat naik gambar anda memasukkan sisa ke dalam tong kitar semula untuk disahkan oleh sistem AI dan tebus +15 Eco Points."
                    else
                        "Upload a photo of your recycling action to be verified by the AI system and earn +15 Eco Points.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Photo Upload / Camera Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (proofBitmap != null) {
                        Image(
                            bitmap = proofBitmap!!.asImageBitmap(),
                            contentDescription = "Proof Snapshot",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (isBm) "Ambil Gambar Bukti Kitar Semula" else "Capture Recycling Photo Proof",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isBm) "cth: Botol di dalam tong biru" else "e.g., Bottles entering the blue bin",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                modifier = Modifier.testTag("proof_camera_button")
                            ) {
                                Text(if (isBm) "Buka Kamera" else "Open Camera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Verification & Submission Button
        item {
            if (isVerifying) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = EcoGreenPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (isBm) "Sistem AI Sedang Memeriksa Bukti..." else "AI System Verifying Proof...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isBm) "Mengesahkan kesahihan sisa & tong kitar semula" else "Validating waste & proper receptacle",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (awardedPoints != null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGreenPrimary.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isBm) "Bukti Berjaya Disahkan! 🎉" else "Proof Successfully Verified! 🎉",
                            fontWeight = FontWeight.ExtraBold,
                            color = EcoGreenPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+$awardedPoints Eco Points Ditambah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.IMPACT) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                        ) {
                            Text(if (isBm) "Lihat Impak Saya" else "View My Impact")
                        }
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.submitProofUpload() },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_proof_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Submit")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBm) "Hantar & Sahkan Aktiviti (+15 pts)" else "Submit & Verify Activity (+15 pts)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Disclaimer requirement from brief (Section 14)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBm)
                        "ℹ️ Nota Kejujuran: Untuk versi demonstrasi inovasi ini, pengesahan foto disimulasikan oleh enjin ECOBIN AI. Kami menggalakkan integriti dalam setiap amalan kitar semula."
                    else
                        "ℹ️ Integrity Note: In this demonstration prototype, photo verification is simulated by the ECOBIN AI engine. We advocate honesty in personal recycling habits.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
