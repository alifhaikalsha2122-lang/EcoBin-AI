package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

enum class ClusterPointType {
    RECYCLING_CENTER,
    BIN_COLLECTION,
    SCHOOL
}

data class EcoMapClusterItem(
    val id: String,
    val titleText: String,
    val snippetText: String,
    val lat: Double,
    val lng: Double,
    val pointType: ClusterPointType,
    val details: String = "",
    val acceptedCategories: String = "Plastik, Kertas, Logam"
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lat, lng)
    override fun getTitle(): String = titleText
    override fun getSnippet(): String = snippetText
    override fun getZIndex(): Float? = 0f
}

enum class AppLanguage {
    BM, EN
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    fun getDisplayName(language: AppLanguage): String = when (this) {
        SYSTEM -> if (language == AppLanguage.BM) "Ikut Sistem" else "System"
        LIGHT -> if (language == AppLanguage.BM) "Mod Cerah" else "Light Mode"
        DARK -> if (language == AppLanguage.BM) "Mod Gelap" else "Dark Mode"
    }

    fun getEmoji(): String = when (this) {
        SYSTEM -> "⚙️"
        LIGHT -> "☀️"
        DARK -> "🌙"
    }
}

enum class WasteCategory(
    val titleEn: String,
    val titleBm: String,
    val iconEmoji: String,
    val color: Color
) {
    PLASTIC("Plastic", "Plastik", "♻️", ColorPlastic),
    PAPER("Paper", "Kertas", "📄", ColorPaper),
    GLASS("Glass", "Kaca", "🍾", ColorGlass),
    METAL("Metal", "Logam", "🥫", ColorMetal),
    E_WASTE("E-Waste", "E-Sisa", "🔋", ColorEWaste),
    ORGANIC("Organic", "Organik", "🍎", ColorOrganic),
    HAZARDOUS("Hazardous", "Berbahaya", "⚠️", ColorHazardous),
    GENERAL("General Waste", "Sisa Am", "🗑️", ColorGeneral);

    fun getDisplayName(language: AppLanguage): String =
        if (language == AppLanguage.BM) titleBm else titleEn
}

data class WasteScanResult(
    val itemName: String,
    val category: WasteCategory,
    val material: String,
    val recyclable: Boolean,
    val hazardous: Boolean,
    val confidence: Float,
    val instructions: List<String>,
    val instructionsBm: List<String>,
    val estimatedPoints: Int,
    val estimatedWeightKg: Float = 0.05f,
    val carbonSavedKg: Float = 0.08f
)

data class RecyclingLocation(
    val id: String,
    val name: String,
    val type: String, // Recycling Centre, School Collection Point, Recycling Bin, Collection Hub
    val typeBm: String,
    val distanceKm: Double,
    val address: String,
    val acceptedCategories: List<WasteCategory>,
    val openingHours: String,
    val openingHoursBm: String,
    val latitude: Double,
    val longitude: Double,
    val isSchoolPoint: Boolean = false
)

data class EcoChallenge(
    val id: String,
    val titleEn: String,
    val titleBm: String,
    val descEn: String,
    val descBm: String,
    val current: Int,
    val target: Int,
    val pointsReward: Int,
    val deadline: String,
    val isCompleted: Boolean = false
)

data class EcoBadge(
    val id: String,
    val titleEn: String,
    val titleBm: String,
    val descEn: String,
    val descBm: String,
    val iconEmoji: String,
    val unlocked: Boolean,
    val progress: Int,
    val max: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val itemsRecycled: Int,
    val badge: String,
    val schoolOrClass: String = ""
)

enum class CommunityPostType {
    ALL,
    CAMPAIGN,
    LEADERBOARD,
    ACTIVITY
}

data class CommunityComment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val author: String,
    val role: String,
    val text: String,
    val timeAgo: String = "Just now"
)

data class CommunityPost(
    val id: String,
    val author: String,
    val role: String,
    val timeAgo: String,
    val timeAgoBm: String,
    val contentEn: String,
    val contentBm: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val tag: String = "#EcoAction",
    val type: CommunityPostType = CommunityPostType.ACTIVITY,
    val avatarEmoji: String = "🌱",
    val campaignProgress: Float? = null,
    val campaignTargetText: String? = null,
    val campaignDaysLeft: Int? = null,
    val leaderboardRank: Int? = null,
    val leaderboardPoints: String? = null,
    val impactBadge: String? = null,
    val comments: List<CommunityComment> = emptyList()
)

data class RecyclingActivity(
    val id: String,
    val itemName: String,
    val category: WasteCategory,
    val points: Int,
    val timestamp: String,
    val isVerified: Boolean = true
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isHazardAlert: Boolean = false,
    val timestamp: String = ""
)

data class TrendDataPoint(
    val label: String,
    val labelBm: String,
    val count: Int
)

data class GroundedMapsLocation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val description: String,
    val mapsUrl: String,
    val acceptedItems: String,
    val rating: String = "4.8 ⭐",
    val distanceOrArea: String = "Klang Valley / Nearby"
)

data class SchoolStats(
    val totalStudents: Int = 428,
    val totalPoints: Int = 18640,
    val totalItems: Int = 1430,
    val wasteDivertedKg: Float = 345.5f,
    val topClass: String = "5 Cemerlang (4,250 pts)",
    val topStudent: String = "Aiman Hakim (1,240 pts)",
    val weeklyTargetKg: Float = 100.0f,
    val weeklyCurrentKg: Float = 78.4f
)

enum class Screen {
    LOGIN,
    HOME,
    SCANNER,
    MAP,
    CHALLENGES,
    LEADERBOARD,
    IMPACT,
    COMMUNITY,
    PROFILE,
    DISPOSAL_CHAT,
    RECYCLING_GUIDE,
    SCHOOL_DASHBOARD,
    PROOF_UPLOAD,
    PRESENTATION_DEMO
}
