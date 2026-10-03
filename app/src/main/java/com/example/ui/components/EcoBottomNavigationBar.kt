package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.Screen

/**
 * Destinations for ECOBIN AI main navigation.
 */
sealed class EcoNavDestination(
    val screen: Screen,
    val labelEn: String,
    val labelBm: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val showBadge: Boolean = false
) {
    object Home : EcoNavDestination(
        screen = Screen.HOME,
        labelEn = "Home",
        labelBm = "Utama",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    )

    object Scanner : EcoNavDestination(
        screen = Screen.SCANNER,
        labelEn = "AI Scanner",
        labelBm = "Imbas AI",
        selectedIcon = Icons.Filled.DocumentScanner,
        unselectedIcon = Icons.Outlined.DocumentScanner,
        testTag = "nav_scanner"
    )

    object Map : EcoNavDestination(
        screen = Screen.MAP,
        labelEn = "Recycling Map",
        labelBm = "Peta Kitar",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        testTag = "nav_map"
    )

    object Challenges : EcoNavDestination(
        screen = Screen.CHALLENGES,
        labelEn = "Challenges",
        labelBm = "Cabaran",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        testTag = "nav_challenges",
        showBadge = true
    )

    object Profile : EcoNavDestination(
        screen = Screen.PROFILE,
        labelEn = "Profile",
        labelBm = "Profil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )

    companion object {
        val destinations = listOf(Home, Scanner, Map, Challenges, Profile)
    }
}

/**
 * Main BottomNavigationBar component for ECOBIN AI.
 */
@Composable
fun EcoBottomNavigationBar(
    currentScreen: Screen,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        EcoNavDestination.destinations.forEach { dest ->
            val isSelected = when (dest.screen) {
                Screen.HOME -> currentScreen == Screen.HOME || currentScreen == Screen.PRESENTATION_DEMO
                Screen.SCANNER -> currentScreen == Screen.SCANNER || currentScreen == Screen.DISPOSAL_CHAT || currentScreen == Screen.PROOF_UPLOAD
                Screen.MAP -> currentScreen == Screen.MAP || currentScreen == Screen.RECYCLING_GUIDE
                Screen.CHALLENGES -> currentScreen == Screen.CHALLENGES || currentScreen == Screen.LEADERBOARD || currentScreen == Screen.IMPACT || currentScreen == Screen.SCHOOL_DASHBOARD
                Screen.PROFILE -> currentScreen == Screen.PROFILE || currentScreen == Screen.COMMUNITY
                else -> currentScreen == dest.screen
            }

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "nav_icon_color"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(dest.screen) },
                icon = {
                    if (dest.showBadge) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("1", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = if (language == AppLanguage.BM) dest.labelBm else dest.labelEn,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = if (language == AppLanguage.BM) dest.labelBm else dest.labelEn,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = if (language == AppLanguage.BM) dest.labelBm else dest.labelEn,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        maxLines = 1
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
