package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.EcoStrings
import com.example.data.MlKitWasteClassifier
import com.example.data.db.ScannedItemEntity
import com.example.model.AppLanguage
import com.example.model.Screen
import com.example.model.WasteCategory
import com.example.ui.EcoViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScannerScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.repository.language.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val currentBitmap by viewModel.currentScannedBitmap.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val isBm = language == AppLanguage.BM

    val coroutineScope = rememberCoroutineScope()
    var scannerTab by remember { mutableStateOf("SCAN") } // "SCAN" or "HISTORY"
    var activeCategoryFilter by remember { mutableStateOf("ALL") } // ALL, E_WASTE, PLASTIC, GLASS, PAPER, METAL, ORGANIC
    var selectedPresetName by remember { mutableStateOf<String?>("Plastic Mineral Water Bottle (PET #1)") }
    var manualWasteQuery by remember { mutableStateOf("") }
    var expandedHistoryId by remember { mutableStateOf<Long?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isLocalProcessing by remember { mutableStateOf(false) }

    // FUNGSI UNTUK MEMPROSES GAMBAR DENGAN ML KIT SECARA AUTOMATIK
    fun processImageWithMLKit(uri: Uri) {
        imageUri = uri
        selectedPresetName = null
        viewModel.clearScan()
        isLocalProcessing = true

        coroutineScope.launch {
            try {
                // Decode bitmap for staging and preview
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (bitmap != null) {
                    viewModel.setStagedImage(bitmap)
                }

                // LANGKAH 2: AI mengenal pasti kategori sisa secara on-device (ML Kit)
                val detection = MlKitWasteClassifier.analyzeUri(context, uri)
                viewModel.handleMlKitDetectionResult(detection)

                Toast.makeText(
                    context,
                    if (isBm) "⚡ ML Kit: ${detection.category} dikesan (+${detection.scanResult.estimatedPoints} mata)"
                    else "⚡ ML Kit: ${detection.category} identified (+${detection.scanResult.estimatedPoints} pts)",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLocalProcessing = false
            }
        }
    }

    // 1. LAUNCHER KAMERA (Memproses automatik selepas foto ditangkap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                processImageWithMLKit(tempCameraUri!!)
            }
        }
    )

    // 2. LAUNCHER GALERI (Memproses automatik selepas gambar dipilih)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                processImageWithMLKit(uri)
            }
        }
    )

    // 3. LAUNCHER KEBENARAN KAMERA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = MlKitWasteClassifier.createEcoBinImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(
                    context,
                    if (isBm) "Kebenaran kamera diperlukan untuk mengambil foto sisa." else "Camera permission is required to capture waste photos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    val categoryPresetsMap = mapOf(
        "ALL" to listOf(
            // Plastics (1-7)
            "Plastic Mineral Water Bottle (PET #1)" to "🍶",
            "Shampoo / Detergent Bottle (HDPE #2)" to "🧴",
            "PVC Plastic Pipe (PVC #3)" to "🚿",
            "Plastic Shopping Bag (LDPE #4)" to "🛍️",
            "Food Container / Cap (PP #5)" to "🍱",
            "Polystyrene Foam / Cutlery (PS #6)" to "🥡",
            "Multi-Layer Snack Pack (OTHER #7)" to "🍿",
            // E-Waste
            "Used Lithium Battery" to "🔋",
            "USB Cable / Charger Adapter" to "🔌",
            "Old Smartphone / Tablet" to "📱",
            "Computer Motherboard (PCB)" to "💻",
            "Fluorescent Bulb / LED Light" to "💡",
            "Electronic Vape Device" to "⚡",
            "Computer Mouse / Keyboard" to "⌨️",
            "Small Electrical Fan / Iron" to "🔌",
            // Paper
            "Cardboard Delivery Box" to "📦",
            "Exam Paper / A4 Office Sheets" to "📄",
            "Old Newspaper / Magazine" to "📰",
            "Tetra Pak Milk / Juice Carton" to "🧃",
            // Glass
            "Glass Food & Jam Jar" to "🫙",
            "Glass Beverage / Sauce Bottle" to "🍾",
            "Amber Glass Medicine Bottle" to "🍶",
            // Metal
            "Aluminum Soda Drink Can" to "🥫",
            "Steel Food & Sardine Tin" to "🥫",
            "Biscuit / Milk Powder Tin" to "🛢️",
            "Clean Aluminum Foil Tray" to "🍽️",
            // Organic
            "Organic Apple Core & Fruit Peel" to "🍎",
            "Coffee Grounds & Tea Leaves" to "☕",
            "Dry Garden Leaves & Compost" to "🌿",
            // Hazardous & Textile
            "Aerosol Spray Can" to "💨",
            "Expired Medicine & Pills" to "💊",
            "Used Fabric / Old Clothing" to "👕"
        ),
        "PLASTIC_RESINS" to listOf(
            "Plastic Mineral Water Bottle (PET #1)" to "♳",
            "Shampoo / Detergent Bottle (HDPE #2)" to "♴",
            "PVC Plastic Pipe (PVC #3)" to "♵",
            "Plastic Shopping Bag (LDPE #4)" to "♶",
            "Food Container / Cap (PP #5)" to "♷",
            "Polystyrene Foam / Cutlery (PS #6)" to "♸",
            "Multi-Layer Snack Pack (OTHER #7)" to "♹"
        ),
        "E_WASTE" to listOf(
            "Used Lithium Battery" to "🔋",
            "USB Cable / Charger Adapter" to "🔌",
            "Old Smartphone / Tablet" to "📱",
            "Computer Motherboard (PCB)" to "💻",
            "Fluorescent Bulb / LED Light" to "💡",
            "Electronic Vape Device" to "⚡",
            "Computer Mouse / Keyboard" to "⌨️",
            "Earphones / Bluetooth Speaker" to "🎧",
            "Small Electrical Fan / Iron" to "🔌",
            "Printer Ink Cartridge" to "🖨️"
        ),
        "PLASTIC" to listOf(
            "Plastic Mineral Water Bottle (PET #1)" to "♳",
            "Fresh Milk Jug (HDPE #2)" to "♴",
            "Shampoo / Soap Bottle (HDPE #2)" to "♴",
            "PVC Pipe Conduit (PVC #3)" to "♵",
            "Plastic Shopping Bag (LDPE #4)" to "♶",
            "Bubble Wrap Packaging (LDPE #4)" to "♶",
            "Food Container / Tapau (PP #5)" to "♷",
            "Plastic Bottle Cap (PP #5)" to "♷",
            "Polystyrene Foam Box (PS #6)" to "♸",
            "Disposable Plastic Cutlery (PS #6)" to "♸",
            "Sports Water Bottle (OTHER #7)" to "♹",
            "Multi-Layer Snack Pack (OTHER #7)" to "♹"
        ),
        "GLASS" to listOf(
            "Glass Food & Jam Jar" to "🫙",
            "Glass Beverage / Sauce Bottle" to "🍾",
            "Amber Glass Medicine Bottle" to "🍶",
            "Green Olive Oil Glass Bottle" to "🫒",
            "Broken Glass / Window Pane" to "🪟"
        ),
        "PAPER" to listOf(
            "Cardboard Delivery Box" to "📦",
            "Exam Paper / A4 Office Sheets" to "📄",
            "Old Newspaper / Magazine" to "📰",
            "Tetra Pak Milk / Juice Carton" to "🧃",
            "Paper Egg Tray Carton" to "🍳",
            "Kraft Paper Bag" to "🛍️"
        ),
        "METAL" to listOf(
            "Aluminum Soda Drink Can" to "🥫",
            "Steel Food & Sardine Tin" to "🥫",
            "Biscuit / Milk Powder Tin" to "🛢️",
            "Clean Aluminum Foil Tray" to "🍽️",
            "Copper Wire / Metal Scrap" to "🔧"
        ),
        "ORGANIC" to listOf(
            "Organic Apple Core & Fruit Peel" to "🍎",
            "Banana Peel Compost" to "🍌",
            "Coffee Grounds & Tea Leaves" to "☕",
            "Dry Garden Leaves & Twigs" to "🌿",
            "Cooked Leftover Food Scraps" to "🍲"
        ),
        "HAZARDOUS" to listOf(
            "Aerosol Spray Can" to "💨",
            "Expired Medicine & Pills" to "💊",
            "Household Paint / Solvent Can" to "🎨",
            "Used Motor Engine Oil" to "🛢️"
        ),
        "TEXTILE" to listOf(
            "Used Fabric / Old Clothing" to "👕",
            "Denim Jeans Pants" to "👖",
            "Towel & Fabric Scrap" to "🧵"
        )
    )

    val currentSamplePresets = categoryPresetsMap[activeCategoryFilter] ?: categoryPresetsMap["ALL"]!!

    fun formatTimestamp(ts: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            sdf.format(Date(ts))
        } catch (e: Exception) {
            "Recent"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("scanner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Title & Header Description
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBm) "Pengimbas Sisa Pintar AI" else "AI Waste Scanner",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EcoGreenPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Gemini Vision AI",
                            color = EcoGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isBm)
                        "Muat naik gambar sisa untuk dianalisis oleh Gemini Vision, dan simpan rekod imbasan dalam Pangkalan Data Room."
                    else
                        "Upload waste images to classify with Gemini Vision, and store past scans locally in Room Database.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Module Navigation Tabs: Scan vs Recycling History
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (scannerTab == "SCAN") EcoGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { scannerTab = "SCAN" }
                            .testTag("scanner_tab_scan")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📷", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBm) "Imbas Sisa" else "Scan Waste",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (scannerTab == "SCAN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (scannerTab == "HISTORY") EcoGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { scannerTab = "HISTORY" }
                            .testTag("scanner_tab_history")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📜", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBm) "Sejarah (${scanHistory.size})" else "History (${scanHistory.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (scannerTab == "HISTORY") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: SCAN WASTE
        // ==========================================
        if (scannerTab == "SCAN") {
            // Kotak Pratonton Gambar Bersepadu (Box Reka Bentuk Kemas & Adaptif Tema)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .testTag("scanner_preview_placeholder"),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        // Paparkan gambar dari URI
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Gambar Sisa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "Scanned Item",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder "Tiada foto dipilih"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Kamera",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isBm) "Tiada foto dipilih" else "No image selected",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isBm) "Ambil foto atau pilih daripada galeri" else "Take a photo or pick from gallery",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Loading indicator & Subtle AI Processing Animations jika sedang diproses
                    if (isAnalyzing || isLocalProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (isBm) "⚡ ML Kit AI sedang menganalisis sisa..." else "⚡ ML Kit AI is identifying waste...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Butang Kamera & Galeri (Bersebelahan & Memproses Terus dengan ML Kit)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Butang Kamera
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scanner_camera_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBm) "Kamera" else "Camera", fontWeight = FontWeight.Bold)
                    }

                    // Butang Galeri
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scanner_upload_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBm) "Galeri" else "Gallery", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category Filters & Instant Preset Chips for Ultra-Accurate Testing
            item {
                Column {
                    Text(
                        text = if (isBm) "1. Pilih Kategori Sisa Pelbagai (AI Multi-Category):" else "1. Select Waste Category (AI Multi-Category):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(6.dp))
                    
                    val categories = listOf(
                        "ALL" to (if (isBm) "🌟 Semua (30+ Sisa)" else "🌟 All (30+ Items)"),
                        "PLASTIC_RESINS" to (if (isBm) "♳-♹ Kod Resin (1-7)" else "♳-♹ Resin Codes (1-7)"),
                        "E_WASTE" to (if (isBm) "⚡ E-Sisa Elektronik" else "⚡ E-Waste"),
                        "PLASTIC" to (if (isBm) "🍶 Plastik Pelbagai" else "🍶 All Plastics"),
                        "GLASS" to (if (isBm) "🫙 Kaca" else "🫙 Glass"),
                        "PAPER" to (if (isBm) "📦 Kertas & Kadbod" else "📦 Paper & Box"),
                        "METAL" to (if (isBm) "🥫 Logam & Tin" else "🥫 Metal & Cans"),
                        "ORGANIC" to (if (isBm) "🍎 Organik & Kompos" else "🍎 Organic"),
                        "HAZARDOUS" to (if (isBm) "⚠️ Sisa Berbahaya" else "⚠️ Hazardous"),
                        "TEXTILE" to (if (isBm) "👕 Tekstil & Pakaian" else "👕 Textiles")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { (catKey, label) ->
                            FilterChip(
                                selected = activeCategoryFilter == catKey,
                                onClick = { activeCategoryFilter = catKey },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (activeCategoryFilter == catKey) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("filter_cat_$catKey")
                            )
                        }
                    }

                    // Special Plastic Resin Quick-Switch Bar (1-7) if plastic or all selected
                    if (activeCategoryFilter == "PLASTIC_RESINS" || activeCategoryFilter == "PLASTIC" || activeCategoryFilter == "ALL") {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ColorPlastic.copy(alpha = 0.10f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorPlastic.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("♳", fontSize = 14.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Panduan Kod Resin Plastik (1 hingga 7):" else "Plastic Resin Guide (Codes 1 to 7):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val resinList = listOf(
                                        "♳ PET 1" to "Plastic Mineral Water Bottle (PET #1)",
                                        "♴ HDPE 2" to "Shampoo / Detergent Bottle (HDPE #2)",
                                        "♵ PVC 3" to "PVC Plastic Pipe (PVC #3)",
                                        "♶ LDPE 4" to "Plastic Shopping Bag (LDPE #4)",
                                        "♷ PP 5" to "Food Container / Cap (PP #5)",
                                        "♸ PS 6" to "Polystyrene Foam / Cutlery (PS #6)",
                                        "♹ OTHER 7" to "Multi-Layer Snack Pack (OTHER #7)"
                                    )
                                    items(resinList) { (tag, itemName) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedPresetName == itemName) Color(0xFF0284C7) else MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedPresetName = itemName
                                                    viewModel.scanPreset(itemName)
                                                }
                                        ) {
                                            Text(
                                                text = tag,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (selectedPresetName == itemName) Color.White else Color(0xFF0284C7),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isBm) "2. Sampel Sisa untuk Diimbas Serta-Merta:" else "2. Diverse Sample Presets to Scan Instantly:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentSamplePresets) { (name, icon) ->
                            FilterChip(
                                selected = selectedPresetName == name,
                                onClick = {
                                    selectedPresetName = name
                                    viewModel.scanPreset(name)
                                },
                                label = { Text(name, fontSize = 11.sp) },
                                leadingIcon = { Text(icon, fontSize = 12.sp) },
                                modifier = Modifier.testTag("scanner_preset_${name.replace(" ", "_").replace("(", "").replace(")", "").replace("#", "")}")
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Manual Waste Search / Direct AI Classification Input Bar
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Waste",
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            TextField(
                                value = manualWasteQuery,
                                onValueChange = { manualWasteQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (isBm) "Taip apa jua sisa (PET 1-7, bateri, kaca, kotak, cat)..." else "Type any waste (PET 1-7, battery, glass, cardboard, paint)...",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_waste_search_input")
                            )
                            if (manualWasteQuery.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val query = manualWasteQuery.trim()
                                        selectedPresetName = query
                                        viewModel.scanPreset(query)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("submit_manual_waste_query")
                                ) {
                                    Text(if (isBm) "Imbas AI" else "Scan AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Trigger Analysis Button if image is staged
            if (currentBitmap != null && scanResult == null && !isAnalyzing) {
                item {
                    Button(
                        onClick = { viewModel.analyzeStagedImage(selectedPresetName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scanner_analyze_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Analyze")
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBm) "Analisis dengan Gemini Vision" else "Analyze with Gemini Vision", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Detailed Scan Result View
            scanResult?.let { result ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scanner_result_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Saved to Room Database badge notification
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EcoGreenPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💾", fontSize = 13.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (isBm) "Disimpan ke Sejarah Room DB" else "Saved to Room Database History",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoGreenPrimary
                                        )
                                    }
                                    Text(
                                        text = if (isBm) "Buka Sejarah →" else "View History →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EcoGreenPrimary,
                                        modifier = Modifier.clickable { scannerTab = "HISTORY" }
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Item Name & Category Badge Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = result.itemName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                    Text(
                                        text = "${if (isBm) "Bahan" else "Material"}: ${result.material}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = result.category.color.copy(alpha = 0.18f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(result.category.iconEmoji, fontSize = 14.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = result.category.getDisplayName(language),
                                            fontWeight = FontWeight.Bold,
                                            color = result.category.color,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Dedicated Protocol Banners by Category (e.g. E-Waste, Plastic Resin 1-7, Glass, Paper, Metal, Organic, Hazardous)
                            when (result.category) {
                                WasteCategory.E_WASTE -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⚡", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "PROTOKOL KHAS E-WASTE (SISA ELEKTRONIK)" else "SPECIAL E-WASTE PROTOCOL",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "Sisa elektronik mengandungi komponen berharga (seperti tembaga & emas) serta bahan kimia berisiko. Sila hantar ke Pusat Pengumpulan E-Sisa atau Kiosk Khas untuk +${result.estimatedPoints} mata ganjaran!"
                                                else
                                                    "Electronic waste contains valuable metals and toxic chemicals. Bring to a certified E-Waste collection kiosk for +${result.estimatedPoints} reward points!",
                                                fontSize = 11.sp,
                                                color = Color(0xFF78350F)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.PLASTIC -> {
                                    // Extract resin info if applicable
                                    val resinName = when {
                                        result.material.contains("PET", ignoreCase = true) || result.itemName.contains("PET", ignoreCase = true) || result.material.contains("Terephthalate", ignoreCase = true) -> "♳ KOD RESIN 1: PET (Polyethylene Terephthalate)"
                                        result.material.contains("HDPE", ignoreCase = true) || result.itemName.contains("HDPE", ignoreCase = true) -> "♴ KOD RESIN 2: HDPE (High-Density Polyethylene)"
                                        result.material.contains("PVC", ignoreCase = true) || result.itemName.contains("PVC", ignoreCase = true) -> "♵ KOD RESIN 3: PVC (Polyvinyl Chloride)"
                                        result.material.contains("LDPE", ignoreCase = true) || result.itemName.contains("LDPE", ignoreCase = true) -> "♶ KOD RESIN 4: LDPE (Low-Density Polyethylene)"
                                        result.material.contains("PP", ignoreCase = true) || result.itemName.contains("PP", ignoreCase = true) || result.material.contains("Polypropylene", ignoreCase = true) -> "♷ KOD RESIN 5: PP (Polypropylene)"
                                        result.material.contains("PS", ignoreCase = true) || result.itemName.contains("PS", ignoreCase = true) || result.material.contains("Polystyrene", ignoreCase = true) -> "♸ KOD RESIN 6: PS (Polystyrene / Styrofoam)"
                                        else -> "♹ KOD RESIN 7: OTHER (Plastik Komposit & Pelbagai)"
                                    }
                                    val resinTip = when {
                                        result.material.contains("PET", ignoreCase = true) || result.itemName.contains("PET", ignoreCase = true) -> if (isBm) "Bahan berkualiti tinggi dan paling mudah dikitar semula. Bilas, kemekkan dan buang dalam Tong Biru." else "Highest recyclability grade. Rinse, crush and place in Blue Bin."
                                        result.material.contains("HDPE", ignoreCase = true) || result.itemName.contains("HDPE", ignoreCase = true) -> if (isBm) "Plastik tebal tahan bahan kimia. Bilas sisa sabun/susu dan masukkan ke Tong Biru." else "Rigid chemical-resistant plastic. Rinse residues and deposit in Blue Bin."
                                        result.material.contains("PVC", ignoreCase = true) -> if (isBm) "Bahan keras untuk paip & pembinaan. Hantar ke pusat kitar semula sisa binaan PBT." else "Rigid construction plastic. Send to municipal building waste recyclers."
                                        result.material.contains("LDPE", ignoreCase = true) -> if (isBm) "Plastik lembut & fleksibel. Kumpulkan dalam satu beg dan hantar ke kiosk pasar raya / Tong Biru." else "Soft flexible film. Bundle together and drop at supermarket collection."
                                        result.material.contains("PP", ignoreCase = true) -> if (isBm) "Tahan haba & selamat untuk makanan. Cuci minyak makanan sehingga bersih sebelum kitar." else "Heat resistant food-safe plastic. Clean grease thoroughly before recycling."
                                        result.material.contains("PS", ignoreCase = true) -> if (isBm) "Gabus polisterin ringan. Jangan bakar. Hantar ke fasiliti pemadatan khas atau tong plastik." else "Lightweight foam. Do NOT burn. Bring to specialized foam recyclers."
                                        else -> if (isBm) "Plastik campuran/berlapis. Bawa ke pusat beli balik 3R atau kiosk inovasi kitar semula." else "Multi-layer plastic. Bring to 3R buy-back or upcycling hubs."
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFE0F2FE),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🍶", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = resinName,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF075985)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = resinTip,
                                                fontSize = 11.sp,
                                                color = Color(0xFF0C4A6E)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.GLASS -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFFEF9C3),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCA8A04)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🫙", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "PANDUAN KITAR SEMULA KACA (TONG COKLAT)" else "GLASS RECYCLING PROTOCOL",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF854D0E)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "Bilas bersih balang/botol kaca, tanggalkan penutup logam atau plastik, dan masukkan ke dalam Tong Coklat (Kaca)."
                                                else
                                                    "Rinse glass jars/bottles, remove caps, and handle carefully into the Brown Recycling Bin.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF713F12)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.PAPER -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFFFEDD5),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEA580C)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("📦", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "PANDUAN KITAR SEMULA KERTAS & KADBOD" else "PAPER & CARDBOARD PROTOCOL",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF9A3412)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "Ratakan kotak kadbod, pastikan kertas kering dan bebas daripada minyak makanan atau pita pelekat."
                                                else
                                                    "Flatten cardboard boxes, ensure paper is clean, dry, and free of food grease.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7C2D12)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.METAL -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFFFF7ED),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF97316)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🥫", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "PANDUAN KITAR SEMULA LOGAM & TIN (TONG JINGGA)" else "METAL & ALUMINUM PROTOCOL",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF9A3412)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "Kosongkan cecair, bilas sedikit, kemekkan tin untuk jimat ruang dan masukkan ke dalam Tong Kitar Semula Logam (Jingga)."
                                                else
                                                    "Empty liquid, rinse clean, flatten cans to conserve space and place into Orange Metal Recycling Bin.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7C2D12)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.ORGANIC -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFECFDF5),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🍎", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "PANDUAN KOMPOS & SISA ORGANIK" else "ORGANIC COMPOSTING PROTOCOL",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF065F46)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "Asingkan pelekat dan plastik. Masukkan ke dalam tong kompos sekolah atau gaul dengan daun kering untuk dijadikan baja tanah."
                                                else
                                                    "Remove plastic stickers. Place in school compost bin or mix with dry leaves to create rich natural soil fertilizer.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                WasteCategory.HAZARDOUS -> {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFFEE2E2),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⚠️", fontSize = 18.sp)
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (isBm) "AMARAN BAHAN BERBAHAYA / TOKSIK" else "HAZARDOUS MATERIAL WARNING",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF991B1B)
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = if (isBm)
                                                    "JANGAN buang dalam tong sampah biasa atau kitar semula am. Hubungi PBT atau hantar ke pusat pengumpulan sisa berjadual berlesen."
                                                else
                                                    "DO NOT discard in general bins. Contact local municipal council for scheduled toxic chemical disposal.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                else -> {}
                            }

                            // Stats Row: Recyclable, Confidence, Points
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (result.recyclable) EcoGreenPrimary.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (result.recyclable) (if (isBm) "Boleh Kitar" else "Recyclable") else (if (isBm) "Bukan Kitar" else "Non-Recyclable"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (result.recyclable) EcoGreenPrimary else Color(0xFFEF4444)
                                        )
                                        Text(
                                            text = if (result.recyclable) "✅ Sesuai 3R" else "❌ Sisa Am",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "+${result.estimatedPoints} Pts",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${result.estimatedWeightKg} kg",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "-${result.carbonSavedKg} kg",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF047857)
                                        )
                                        Text(
                                            text = "CO₂ Dijimat",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(12.dp))

                            // Step-by-Step Recycling Preparation Instructions
                            Text(
                                text = if (isBm) "Arahan Penyediaan Sisa (3R):" else "Step-by-Step Preparation Instructions:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(8.dp))

                            val instructionsToDisplay = if (isBm && result.instructionsBm.isNotEmpty()) {
                                result.instructionsBm
                            } else {
                                result.instructions
                            }

                            instructionsToDisplay.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}. ",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Primary Action: Recycle Now
                            Button(
                                onClick = { viewModel.recycleScannedItem() },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("scanner_recycle_now_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Recycle")
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${EcoStrings.recycleNow(language)} (+${result.estimatedPoints} pts)",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Secondary Actions: Find Bin, Ask AI, Scan Another
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.setMapCategoryFilter(result.category)
                                        viewModel.navigateTo(Screen.MAP)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scanner_find_bin_button")
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = "Map", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBm) "Cari Tong" else "Find Bin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(Screen.DISPOSAL_CHAT) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scanner_ask_ai_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Ask AI", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBm) "Tanya AI" else "Ask AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                TextButton(
                                    onClick = { viewModel.clearScan() },
                                    modifier = Modifier.testTag("scanner_scan_another_button")
                                ) {
                                    Text(if (isBm) "Semula" else "Reset", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: RECYCLING HISTORY (PERSISTED USING ROOM DATABASE)
        // =========================================================================
        if (scannerTab == "HISTORY") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBm) "Pangkalan Data Sejarah Kitar Semula" else "Recycling History (Room DB)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isBm)
                                "Rekod item yang diimbas kekal disimpan secara luar talian (Room Database)."
                            else
                                "Scanned records persisted locally offline using Room Database.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (scanHistory.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearHistoryDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBm) "Kosongkan" else "Clear", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                    }
                }
            }

            if (scanHistory.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_history_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📦", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (isBm) "Tiada Rekod Sejarah Lagi" else "No Scan History Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isBm)
                                    "Semua bahan sisa yang anda imbas dengan kamera atau sampel akan disimpan secara automatik dalam Room Database."
                                else
                                    "Items you scan with camera or presets will be automatically saved to your Room Database history.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = { scannerTab = "SCAN" },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Scan", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBm) "Mula Imbas Sisa" else "Start Scanning", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(scanHistory, key = { it.id }) { item ->
                    val isExpanded = expandedHistoryId == item.id
                    val category = try {
                        WasteCategory.valueOf(item.categoryName)
                    } catch (e: Exception) {
                        WasteCategory.GENERAL
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row: Item Name, Category, and Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = category.color.copy(alpha = 0.15f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(category.iconEmoji, fontSize = 20.sp)
                                        }
                                    }

                                    Spacer(Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = item.itemName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.material,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(" • ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            Text(
                                                text = formatTimestamp(item.timestamp),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteHistoryItem(item.id) },
                                    modifier = Modifier.testTag("delete_history_${item.id}")
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Badges & Metrics Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (item.recyclable) EcoGreenPrimary.copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (item.recyclable) (if (isBm) "Boleh Kitar" else "Recyclable") else (if (isBm) "Sisa Am" else "Non-Recyclable"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.recyclable) EcoGreenPrimary else Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "+${item.estimatedPoints} pts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "-${item.carbonSavedKg} kg CO₂",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Expandable Step-by-Step Instructions Section
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    HorizontalDivider()
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = if (isBm) "Arahan Kitar Semula Asal:" else "Original Recycling Instructions:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    )
                                    Spacer(Modifier.height(6.dp))

                                    val raw = if (isBm && item.instructionsBmRaw.isNotBlank()) item.instructionsBmRaw else item.instructionsRaw
                                    val steps = raw.split("\n").filter { it.isNotBlank() }

                                    steps.forEachIndexed { idx, step ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ",
                                                fontWeight = FontWeight.Bold,
                                                color = EcoGreenPrimary,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = step,
                                                fontSize = 11.sp,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Action Buttons: Revisit in Scanner vs Toggle Instructions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        expandedHistoryId = if (isExpanded) null else item.id
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("toggle_instructions_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isExpanded) (if (isBm) "Tutup Arahan" else "Hide Steps") else (if (isBm) "Lihat Arahan" else "View Steps"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.loadFromHistory(item)
                                        scannerTab = "SCAN"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("revisit_instructions_${item.id}")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Revisit", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isBm) "Muat Semula" else "Revisit Item",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Clearing History
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            icon = { Text("🗑️", fontSize = 28.sp) },
            title = {
                Text(
                    text = if (isBm) "Kosongkan Sejarah Kitar Semula?" else "Clear Recycling History?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBm)
                        "Adakah anda pasti mahu memadam semua rekod imbasan sisa daripada Room Database tempatan anda?"
                    else
                        "Are you sure you want to delete all past scanned item records from your local Room Database?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (isBm) "Padam Semua" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(if (isBm) "Batal" else "Cancel")
                }
            }
        )
    }
}

/**
 * Subtle animations overlay displayed over the camera UI while Gemini AI processes
 * and classifies the scanned waste item. Features:
 * - Sweeping laser scan beam with vertical gradient trail
 * - Pulsing viewfinder corner reticles / bounding box
 * - Radar ripple wave radiating from center
 * - Subtle tech grid matrix
 * - Animated stage status ticker with smooth vertical transitions
 */
@Composable
fun AiCameraScanningAnimationOverlay(
    isAnalyzing: Boolean,
    isBm: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isAnalyzing) return

    val infiniteTransition = rememberInfiniteTransition(label = "ai_camera_scanner_transition")

    // 1. Scanner Laser Beam Vertical Position (0f to 1f)
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    // 2. Subtle Reticle Corner Pulse (alpha 0.55f to 1f)
    val cornerPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )

    // 3. Central Radar Ripple Expanding Wave (0f to 1f)
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )

    // 4. Subtle Border Glow Breathing (0.35f to 0.85f)
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    // Dynamic AI Stage Ticker (Cycles every 1.4 seconds)
    var stageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isAnalyzing) {
        var count = 0
        while (true) {
            kotlinx.coroutines.delay(1400)
            count = (count + 1) % 3
            stageIndex = count
        }
    }

    val stageTexts = if (isBm) {
        listOf(
            "🔍 Mengimbas kontur & geometri objek...",
            "🧠 Gemini menganalisis komposisi sisa...",
            "♻️ Menjana panduan pelupusan 3R..."
        )
    } else {
        listOf(
            "🔍 Scanning object contours & geometry...",
            "🧠 Gemini analyzing waste composition...",
            "♻️ Formulating 3R disposal instructions..."
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        EcoGreenPrimary.copy(alpha = borderGlow),
                        EcoMint.copy(alpha = borderGlow * 0.7f),
                        EcoCyan.copy(alpha = borderGlow)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(Color.Black.copy(alpha = 0.45f))
            .testTag("camera_scanning_overlay")
    ) {
        // A. Subtle Canvas Layers: Grid Matrix, Corner Reticles, Radar Waves, and Laser Beam
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_scanner_beam")
        ) {
            val width = size.width
            val height = size.height

            // 1. Subtle Computer Vision Grid lines (horizontal & vertical subtle scan lines)
            val gridStep = 44.dp.toPx()
            var x = gridStep
            while (x < width) {
                drawLine(
                    color = EcoMint.copy(alpha = 0.08f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridStep
            }
            var y = gridStep
            while (y < height) {
                drawLine(
                    color = EcoMint.copy(alpha = 0.08f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridStep
            }

            // 2. Central Radar Pulse Ring
            val maxRadius = (width.coerceAtMost(height) / 2f) * 0.85f
            drawCircle(
                color = EcoMint.copy(alpha = rippleAlpha),
                radius = maxRadius * rippleScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Futuristic Bounding Box / Corner Reticles
            val margin = 20.dp.toPx()
            val bracketLen = 28.dp.toPx()
            val bracketStroke = 3.dp.toPx()
            val bracketColor = EcoGreenPrimary.copy(alpha = cornerPulseAlpha)

            // Top-Left ┌
            drawLine(bracketColor, Offset(margin, margin), Offset(margin + bracketLen, margin), bracketStroke)
            drawLine(bracketColor, Offset(margin, margin), Offset(margin, margin + bracketLen), bracketStroke)

            // Top-Right ┐
            drawLine(bracketColor, Offset(width - margin, margin), Offset(width - margin - bracketLen, margin), bracketStroke)
            drawLine(bracketColor, Offset(width - margin, margin), Offset(width - margin, margin + bracketLen), bracketStroke)

            // Bottom-Left └
            drawLine(bracketColor, Offset(margin, height - margin), Offset(margin + bracketLen, height - margin), bracketStroke)
            drawLine(bracketColor, Offset(margin, height - margin), Offset(margin, height - margin - bracketLen), bracketStroke)

            // Bottom-Right ┘
            drawLine(bracketColor, Offset(width - margin, height - margin), Offset(width - margin - bracketLen, height - margin), bracketStroke)
            drawLine(bracketColor, Offset(width - margin, height - margin), Offset(width - margin, height - margin - bracketLen), bracketStroke)

            // 4. Moving AI Laser Scanning Beam & Gradient Trail
            val laserY = height * laserProgress
            val trailHeight = 45.dp.toPx()

            // Soft glowing trail behind the laser
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        EcoGreenPrimary.copy(alpha = 0.22f),
                        EcoMint.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = (laserY - trailHeight).coerceAtLeast(0f),
                    endY = (laserY + trailHeight).coerceAtMost(height)
                ),
                topLeft = Offset(0f, (laserY - trailHeight).coerceAtLeast(0f)),
                size = Size(width, trailHeight * 2f)
            )

            // Bright core laser line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        EcoMint.copy(alpha = 0.6f),
                        Color.White,
                        EcoMint.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(width, laserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // B. Top Status HUD: "AI SCANNING IN PROGRESS"
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.65f))
                .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EcoGreenPrimary.copy(alpha = cornerPulseAlpha))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isBm) "ANALISIS AI SEDANG BERJALAN" else "AI SCANNING IN PROGRESS",
                color = EcoMint,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }

        // C. Center Modern Indicator & Dynamic Processing Stages
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("ai_processing_indicator"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                CircularProgressIndicator(
                    color = EcoMint,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 3.5.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Text("✨", fontSize = 20.sp)
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.72f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.45f))
            ) {
                AnimatedContent(
                    targetState = stageTexts[stageIndex],
                    transitionSpec = {
                        (slideInVertically { h -> h } + fadeIn()) togetherWith
                                (slideOutVertically { h -> -h } + fadeOut())
                    },
                    label = "ai_stage_text"
                ) { targetText ->
                    Text(
                        text = targetText,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Google Gemini Vision AI",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
