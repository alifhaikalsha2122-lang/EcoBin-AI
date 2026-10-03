package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EcoStrings
import com.example.model.AppLanguage
import com.example.model.Screen
import com.example.ui.theme.*

sealed class BottomNavDestination(
    val screen: Screen,
    val titleEn: String,
    val titleBm: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val testTag: String
) {
    object Home : BottomNavDestination(
        screen = Screen.HOME,
        titleEn = "Home",
        titleBm = "Utama",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    )

    object Scanner : BottomNavDestination(
        screen = Screen.SCANNER,
        titleEn = "AI Scanner",
        titleBm = "Imbas AI",
        filledIcon = Icons.Filled.CameraAlt,
        outlinedIcon = Icons.Outlined.CameraAlt,
        testTag = "nav_scanner"
    )

    object Map : BottomNavDestination(
        screen = Screen.MAP,
        titleEn = "Recycling Map",
        titleBm = "Peta Kitar",
        filledIcon = Icons.Filled.Place,
        outlinedIcon = Icons.Outlined.Place,
        testTag = "nav_map"
    )

    object Challenges : BottomNavDestination(
        screen = Screen.CHALLENGES,
        titleEn = "Challenges",
        titleBm = "Cabaran",
        filledIcon = Icons.Filled.EmojiEvents,
        outlinedIcon = Icons.Outlined.EmojiEvents,
        testTag = "nav_challenges"
    )

    object Profile : BottomNavDestination(
        screen = Screen.PROFILE,
        titleEn = "Profile",
        titleBm = "Profil",
        filledIcon = Icons.Filled.Person,
        outlinedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )

    companion object {
        val items = listOf(Home, Scanner, Map, Challenges, Profile)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTopBar(
    title: String,
    language: AppLanguage,
    userPoints: Int,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onOpenDemo: () -> Unit = {},
    onOpenChat: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        maxLines = 1
                    )
                }
                Text(
                    text = "ECOBIN AI • Smart Recycling",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(EcoGreenPrimary, EcoMint)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 20.sp)
                }
            }
        },
        actions = {
            // Demo Mode Presentation Badge Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EcoGreenDark,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenDemo() }
                    .testTag("demo_mode_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡ DEMO", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Points Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 12.sp)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "$userPoints",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Language Toggle Switch
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleLanguage() }
                    .testTag("language_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BM) "🇲🇾 BM" else "🇬🇧 EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun EcoBottomNavBar(
    currentScreen: Screen,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        BottomNavDestination.items.forEach { dest ->
            val isSelected = when (dest.screen) {
                Screen.HOME -> currentScreen == Screen.HOME || currentScreen == Screen.PRESENTATION_DEMO
                Screen.SCANNER -> currentScreen == Screen.SCANNER || currentScreen == Screen.DISPOSAL_CHAT || currentScreen == Screen.PROOF_UPLOAD
                Screen.MAP -> currentScreen == Screen.MAP || currentScreen == Screen.RECYCLING_GUIDE
                Screen.CHALLENGES -> currentScreen == Screen.CHALLENGES || currentScreen == Screen.LEADERBOARD || currentScreen == Screen.IMPACT || currentScreen == Screen.SCHOOL_DASHBOARD
                Screen.PROFILE -> currentScreen == Screen.PROFILE || currentScreen == Screen.COMMUNITY
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(dest.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) dest.filledIcon else dest.outlinedIcon,
                        contentDescription = if (language == AppLanguage.BM) dest.titleBm else dest.titleEn,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = if (language == AppLanguage.BM) dest.titleBm else dest.titleEn,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(dest.testTag)
            )
        }
    }
}
