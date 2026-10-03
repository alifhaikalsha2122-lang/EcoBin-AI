package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.EcoViewModel
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.Clustering
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val language by viewModel.repository.language.collectAsState()
    val selectedClusterType by viewModel.selectedClusterType.collectAsState()
    val selectedClusterItem by viewModel.selectedClusterItem.collectAsState()
    val groundedLocations by viewModel.groundedMapsLocations.collectAsState()
    val isSearchingMaps by viewModel.isSearchingMaps.collectAsState()
    val groundedSummary by viewModel.groundedSummaryText.collectAsState()
    val isBm = language == AppLanguage.BM

    val isMapAvailable by viewModel.isMapAvailable.collectAsState()
    val mapConfigError by viewModel.mapConfigError.collectAsState()
    val isCheckingMap by viewModel.isCheckingMap.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkMapAvailability(context)
    }

    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("COMPOSE_MAP") } // COMPOSE_MAP, GROUNDING_SEARCH

    // Filtered cluster items for Google Maps Compose
    val allClusterItems = viewModel.mapClusterItems
    val filteredClusterItems = remember(selectedClusterType, allClusterItems) {
        if (selectedClusterType == null) {
            allClusterItems
        } else {
            allClusterItems.filter { it.pointType == selectedClusterType }
        }
    }

    // Default camera position: Petaling Jaya / Kuala Lumpur center
    val defaultCenter = LatLng(3.1319, 101.6250)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 12.5f)
    }

    val quickSearches = if (isBm) listOf(
        "Pusat Kitar Semula Petaling Jaya",
        "Kiosk E-Sisa & Bateri",
        "Buy-Back Centre Kuala Lumpur",
        "Kitar Semula Minyak Masak",
        "Tzu Chi Recycling Hub"
    ) else listOf(
        "Petaling Jaya Recycling Hubs",
        "E-Waste & Battery Drop-offs",
        "Buy-Back Centre Kuala Lumpur",
        "Used Cooking Oil Recycling",
        "Tzu Chi Recycling Hub"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("map_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
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
                                text = if (isBm) "Peta Kitar Semula" else "Recycling Map",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EcoGreenPrimary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍", fontSize = 11.sp)
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "Google Maps Compose",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EcoGreenPrimary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isBm)
                                "Peta Google Maps Compose dengan gugusan penanda (marker clustering) bagi pusat kitar semula, tong pengumpulan dan sekolah."
                            else
                                "Google Maps Compose map with marker clusters for recycling centers, bin collection points, and schools.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // View Mode Switcher: Google Maps Compose (Marker Clusters) vs AI Grounding Search
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
                        color = if (activeTab == "COMPOSE_MAP") EcoGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "COMPOSE_MAP" }
                            .testTag("tab_compose_map")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗺️", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBm) "Peta Google Maps" else "Google Maps Clusters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (activeTab == "COMPOSE_MAP") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeTab == "GROUNDING_SEARCH") EcoGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { activeTab = "GROUNDING_SEARCH" }
                            .testTag("tab_grounding_search")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isBm) "Carian AI Grounding" else "AI Maps Grounding",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (activeTab == "GROUNDING_SEARCH") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 1: GOOGLE MAPS COMPOSE WITH MARKER CLUSTERS (USER REQUEST REQUIREMENT)
        // =========================================================================
        if (activeTab == "COMPOSE_MAP") {
            // Cluster Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("map_cluster_filters")
                ) {
                    item {
                        FilterChip(
                            selected = selectedClusterType == null,
                            onClick = { viewModel.setSelectedClusterType(null) },
                            label = { Text(if (isBm) "Semua Lokasi (${allClusterItems.size})" else "All Locations (${allClusterItems.size})", fontSize = 11.sp) },
                            leadingIcon = { Text("📍", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_cluster_all")
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedClusterType == ClusterPointType.RECYCLING_CENTER,
                            onClick = {
                                viewModel.setSelectedClusterType(
                                    if (selectedClusterType == ClusterPointType.RECYCLING_CENTER) null else ClusterPointType.RECYCLING_CENTER
                                )
                            },
                            label = { Text(if (isBm) "Pusat Kitar Semula" else "Recycling Centers", fontSize = 11.sp) },
                            leadingIcon = { Text("♻️", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_cluster_centers")
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedClusterType == ClusterPointType.BIN_COLLECTION,
                            onClick = {
                                viewModel.setSelectedClusterType(
                                    if (selectedClusterType == ClusterPointType.BIN_COLLECTION) null else ClusterPointType.BIN_COLLECTION
                                )
                            },
                            label = { Text(if (isBm) "Tong Pengumpulan" else "Bin Points", fontSize = 11.sp) },
                            leadingIcon = { Text("🗑️", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_cluster_bins")
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedClusterType == ClusterPointType.SCHOOL,
                            onClick = {
                                viewModel.setSelectedClusterType(
                                    if (selectedClusterType == ClusterPointType.SCHOOL) null else ClusterPointType.SCHOOL
                                )
                            },
                            label = { Text(if (isBm) "Sekolah" else "Schools", fontSize = 11.sp) },
                            leadingIcon = { Text("🏫", fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_cluster_schools")
                        )
                    }
                }
            }

            if (!isMapAvailable) {
                // =====================================================================
                // ROBUST FALLBACK STATE: 'Map Unavailable' Card & Retry Button
                // =====================================================================
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_unavailable_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🗺️", fontSize = 24.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBm) "Peta Tidak Tersedia" else "Map Unavailable",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    Text(
                                        text = if (isBm) "Ralat Konfigurasi Peta / Mod Luar Talian Diaktifkan" else "Map Configuration Error / Offline Mode Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = mapConfigError ?: if (isBm)
                                    "Google Maps tidak dapat dimuatkan kerana ralat konfigurasi (Google Play Services atau Kunci API belum aktif pada peranti ini). Anda masih boleh melihat senarai alamat pusat kitar semula luar talian dan membuka navigasi luaran di bawah."
                                else
                                    "Google Maps failed to load due to a configuration error (Google Play Services or active API Key missing). You can still browse offline recycling center addresses and launch external navigation below.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            )

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.checkMapAvailability(context, forceRetry = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isCheckingMap,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("map_retry_button")
                                ) {
                                    if (isCheckingMap) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBm) "Menyemak..." else "Checking...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBm) "Cuba Semula" else "Retry Loading", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val gmmIntentUri = Uri.parse("geo:3.1319,101.6250?q=" + Uri.encode("Pusat Kitar Semula Petaling Jaya"))
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=Recycling+Centers+Petaling+Jaya")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(webIntent)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open External", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isBm) "Peta Luaran" else "External Maps", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Google Maps Compose Map Container with Clustering
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .testTag("google_maps_compose_container")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Google Map Compose with Clustering
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    myLocationButtonEnabled = false,
                                    compassEnabled = true
                                ),
                                properties = MapProperties(
                                    isMyLocationEnabled = false
                                )
                            ) {
                                // Marker Clustering using maps-compose-utils Clustering component
                                Clustering(
                                    items = filteredClusterItems,
                                    onClusterClick = { cluster ->
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    cluster.position,
                                                    cameraPositionState.position.zoom + 2f
                                                )
                                            )
                                        }
                                        true
                                    },
                                    onClusterItemClick = { item ->
                                        viewModel.selectClusterItem(item)
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(LatLng(item.lat, item.lng), 14f)
                                            )
                                        }
                                        true
                                    },
                                    clusterContent = { cluster ->
                                        // Custom visual cluster bubble
                                        Surface(
                                            shape = CircleShape,
                                            color = EcoGreenPrimary,
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .border(3.dp, Color.White, CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "${cluster.size}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "lokasi",
                                                        fontSize = 7.sp,
                                                        color = Color.White.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    clusterItemContent = { item ->
                                        // Custom individual marker icon based on cluster type
                                        val (emoji, bgColor) = when (item.pointType) {
                                            ClusterPointType.RECYCLING_CENTER -> "♻️" to EcoGreenPrimary
                                            ClusterPointType.BIN_COLLECTION -> "🗑️" to Color(0xFF0284C7)
                                            ClusterPointType.SCHOOL -> "🏫" to Color(0xFFD97706)
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = bgColor,
                                            shadowElevation = 3.dp,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .border(2.dp, Color.White, CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(emoji, fontSize = 16.sp)
                                            }
                                        }
                                    }
                                )
                            }

                            // Map Legend Overlay at Top Left
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EcoGreenPrimary))
                                        Spacer(Modifier.width(4.dp))
                                        Text("♻️ Pusat 3R", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF0284C7)))
                                        Spacer(Modifier.width(4.dp))
                                        Text("🗑️ Tong Pintar", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
                                        Spacer(Modifier.width(4.dp))
                                        Text("🏫 Sekolah", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Cluster Count Badge at Top Right
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "${filteredClusterItems.size} Penanda",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Re-center Floating Action Button at Bottom Right
                            FloatingActionButton(
                                onClick = {
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(defaultCenter, 12.5f)
                                        )
                                    }
                                },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(42.dp)
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 12.dp, bottom = 12.dp)
                                    .testTag("recenter_map_button")
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Selected Marker Information Card
                selectedClusterItem?.let { item ->
                    item {
                        val (badgeText, badgeColor, emoji) = when (item.pointType) {
                            ClusterPointType.RECYCLING_CENTER -> Triple(if (isBm) "Pusat Kitar Semula" else "Recycling Center", EcoGreenPrimary, "♻️")
                            ClusterPointType.BIN_COLLECTION -> Triple(if (isBm) "Tong Pengumpulan" else "Bin Collection Point", Color(0xFF0284C7), "🗑️")
                            ClusterPointType.SCHOOL -> Triple(if (isBm) "Sekolah Komuniti" else "School Collection Point", Color(0xFFD97706), "🏫")
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_marker_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 22.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.titleText,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = item.snippetText,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeColor
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Text(
                                    text = item.details,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )

                                Spacer(Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "📦 Bahan: ${item.acceptedCategories}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        try {
                                            val geoUri = "geo:${item.lat},${item.lng}?q=${Uri.encode(item.titleText)}"
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            // Safe fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("directions_button")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = "Directions", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isBm) "Panduan Arah Google Maps" else "Google Maps Directions",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // LIST OF OFFLINE-AVAILABLE RECYCLING CENTER ADDRESSES
            // =========================================================================
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = if (isBm) "Senarai Alamat Pusat Kitar Semula Luar Talian (${filteredClusterItems.size})" else "Offline-Available Recycling Centers (${filteredClusterItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.testTag("offline_recycling_centers_list")
                    )
                    Text(
                        text = if (isBm)
                            "Semua lokasi disimpan di dalam peranti secara luar talian dengan alamat fizikal penuh dan waktu operasi."
                        else
                            "All locations are stored on-device with full physical addresses, accepted waste, and operating hours.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(filteredClusterItems) { clusterItem ->
                val isSelected = selectedClusterItem?.id == clusterItem.id
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectClusterItem(clusterItem)
                            if (isMapAvailable) {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(clusterItem.lat, clusterItem.lng), 14f)
                                    )
                                }
                            }
                        }
                        .testTag("cluster_card_${clusterItem.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (iconEmoji, badgeColor, badgeLabel) = when (clusterItem.pointType) {
                                ClusterPointType.RECYCLING_CENTER -> Triple("♻️", EcoGreenPrimary, if (isBm) "Pusat 3R" else "3R Center")
                                ClusterPointType.BIN_COLLECTION -> Triple("🗑️", Color(0xFF0284C7), if (isBm) "Tong Pintar" else "Smart Bin")
                                ClusterPointType.SCHOOL -> Triple("🏫", Color(0xFFD97706), if (isBm) "Hab Sekolah" else "School Hub")
                            }

                            Text(iconEmoji, fontSize = 24.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clusterItem.titleText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "🕒 ${clusterItem.snippetText}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Full physical address
                        Row(verticalAlignment = Alignment.Top) {
                            Text("📍", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = clusterItem.details,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Accepted categories
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "📦 ${clusterItem.acceptedCategories}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        // Action Buttons: Open External Navigation & Copy Address
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val geoUri = Uri.parse("geo:${clusterItem.lat},${clusterItem.lng}?q=" + Uri.encode("${clusterItem.titleText}, ${clusterItem.details}"))
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode("${clusterItem.titleText}, ${clusterItem.details}"))).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(webIntent)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("navigate_offline_center_${clusterItem.id}")
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Navigate", modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBm) "Buka Peta" else "Open Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(clusterItem.details))
                                    Toast.makeText(
                                        context,
                                        if (isBm) "Alamat disalin: ${clusterItem.titleText}" else "Address copied to clipboard",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_offline_address_${clusterItem.id}")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Address", modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBm) "Salin Alamat" else "Copy", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: GOOGLE MAPS AI GROUNDING SEARCH
        // =========================================================================
        if (activeTab == "GROUNDING_SEARCH") {
            // Google Maps Search Input Field
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔍", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Cari Pusat Kitar Semula Google Maps" else "Search Google Maps for Recycling",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        if (isBm) "Taip nama lokasi, bandar atau jenis sisa..." else "Enter area, city or waste type...",
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("maps_search_input")
                            )

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val q = if (searchQuery.isNotBlank()) searchQuery else "Pusat Kitar Semula"
                                    viewModel.searchRecyclingCentersWithGoogleMaps(q)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("maps_grounding_search_button")
                            ) {
                                Text(
                                    text = if (isBm) "Cari" else "Search",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quick Search Suggestions
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickSearches) { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        searchQuery = suggestion
                                        viewModel.searchRecyclingCentersWithGoogleMaps(suggestion)
                                    },
                                    label = { Text(suggestion, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Loading state
            if (isSearchingMaps) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = EcoGreenPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Menghubungi Google Maps Tool..." else "Connecting to Google Maps Tool...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isBm) "Memproses maklumat grounding Gemini 3.5 Flash" else "Processing Gemini 3.5 Flash grounding data",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Summary banner from Gemini Maps Grounding
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📍", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Google Maps AI Grounding" else "Google Maps Grounded Results",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D4ED8),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = groundedSummary ?: if (isBm)
                                "Memaparkan pusat kitar semula rasmi, kiosk 'buy-back' 3R, dan pusat e-sisa berhampiran dengan arah navigasi Google Maps."
                            else
                                "Displaying official recycling centres, 3R buy-back hubs, and e-waste stations with direct Google Maps navigation.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Grounded Locations List
            items(groundedLocations) { loc ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grounded_location_card_${loc.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EcoGreenPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📍", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = loc.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = loc.distanceOrArea,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = loc.rating,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Address",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = loc.address,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = loc.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "♻️ ${loc.acceptedItems}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loc.mapsUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to web search
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_in_google_maps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Open Google Maps",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Buka Navigasi Google Maps" else "Open in Google Maps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
