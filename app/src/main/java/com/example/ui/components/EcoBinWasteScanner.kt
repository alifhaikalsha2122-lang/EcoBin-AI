package com.example.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MlKitWasteClassifier
import com.example.data.WasteDetectionResult
import com.example.model.AppLanguage
import com.example.ui.theme.EcoGreenPrimary
import kotlinx.coroutines.launch

/**
 * ECOBIN AI Waste Scanner Component powered by Google ML Kit Object & Image Labeling.
 * Captures a photo via FileProvider, runs ML Kit on-device Image Labeler,
 * and categorizes waste with Malay/English disposal guidelines & Eco Points.
 */
@Composable
fun EcoBinWasteScanner(
    language: AppLanguage,
    onWasteIdentified: (WasteDetectionResult) -> Unit,
    onPermissionDenied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBm = language == AppLanguage.BM

    var hasPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var detectedLabelsText by remember { mutableStateOf<List<String>?>(null) }

    // Camera Capture Launcher with FileProvider URI
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && hasPhotoUri != null) {
                isProcessing = true
                coroutineScope.launch {
                    try {
                        val result = MlKitWasteClassifier.analyzeUri(context, hasPhotoUri!!)
                        detectedLabelsText = result.rawLabels
                        isProcessing = false
                        onWasteIdentified(result)
                    } catch (e: Exception) {
                        isProcessing = false
                        e.printStackTrace()
                    }
                }
            }
        }
    )

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            if (cameraGranted) {
                val uri = MlKitWasteClassifier.createEcoBinImageUri(context)
                hasPhotoUri = uri
                cameraLauncher.launch(uri)
            } else {
                onPermissionDenied()
            }
        }
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("mlkit_scanner_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EcoGreenPrimary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Google ML Kit Image Labeling (On-Device)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = EcoGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBm) "📸 Pengimbas Objek ML Kit" else "📸 ML Kit Waste Scanner",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBm)
                    "Imbas sisa fizikal dengan kamera untuk pengecaman objek on-device serta-merta tanpa memerlukan internet!"
                else
                    "Scan your trash to identify the correct recycling method and earn Eco Points with on-device ML Kit!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isProcessing) {
                CircularProgressIndicator(
                    color = EcoGreenPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isBm) "ML Kit sedang menganalisis sisa..." else "AI is analyzing the waste...",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
            } else {
                Button(
                    onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mlkit_open_camera_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBm) "Buka Kamera & Imbas (ML Kit)" else "Open Camera & Scan (ML Kit)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!detectedLabelsText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = EcoGreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${if (isBm) "Label Dikesan:" else "Labels Found:"} ${detectedLabelsText!!.take(3).joinToString(", ")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
