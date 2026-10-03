package com.example.util

import android.content.Context
import android.content.Intent
import com.example.model.AppLanguage
import com.example.model.EcoBadge

object EcoShareUtils {

    fun shareBadge(context: Context, badge: EcoBadge, language: AppLanguage) {
        val isBm = language == AppLanguage.BM
        val badgeTitle = if (isBm) badge.titleBm else badge.titleEn
        val badgeDesc = if (isBm) badge.descBm else badge.descEn

        val message = if (isBm) {
            """
            🏅 Saya baru sahaja membuka Lencana Eko: ${badge.iconEmoji} "$badgeTitle" di ECOBIN AI!
            
            📜 "$badgeDesc"
            
            Jom bersama saya mengamalkan amalan kitar semula 3R dan kurangkan sisa di sekolah kita! 🌱✨
            
            #ECOBIN #EcoWarrior #SMKBintangEco #KitarSemula #ZeroWaste
            """.trimIndent()
        } else {
            """
            🏅 I just unlocked the Eco Badge: ${badge.iconEmoji} "$badgeTitle" on ECOBIN AI!
            
            📜 "$badgeDesc"
            
            Join me in practicing the 3Rs and reducing waste at our school! 🌱✨
            
            #ECOBIN #EcoWarrior #SMKBintangEco #RecycleNow #ZeroWaste
            """.trimIndent()
        }

        launchShareIntent(
            context = context,
            subject = if (isBm) "Lencana Eko Baharu: $badgeTitle!" else "New Eco Badge: $badgeTitle!",
            text = message
        )
    }

    fun shareRecyclingMilestones(
        context: Context,
        userName: String,
        userSchool: String,
        userClass: String,
        levelName: String,
        points: Int,
        totalItems: Int,
        carbonSavedKg: Float,
        unlockedBadgesCount: Int,
        language: AppLanguage,
        userLocation: String = userSchool,
        userOccupation: String = userClass
    ) {
        val isBm = language == AppLanguage.BM

        val loc = userLocation.ifBlank { userSchool }
        val occ = userOccupation.ifBlank { userClass }

        val message = if (isBm) {
            """
            🌿 Pencapaian Kelestarian Kitar Semula Saya di ECOBIN AI!
            
            👤 Nama: $userName ($occ, $loc)
            🏆 Tahap: $levelName
            
            📊 Statistik Hijau:
            ⭐ Mata Eko Terkumpul: $points pts
            ♻️ Barangan Dikitar Semula: $totalItems item
            🌍 Penjimatan Karbon: ${String.format(java.util.Locale.US, "%.1f", carbonSavedKg)} kg CO₂
            🏅 Lencana Dibuka: $unlockedBadgesCount lencana
            
            Setiap tindakan kecil memberi impak besar kepada bumi kita. Jom kita kitar semula hari ini! 💚
            
            #ECOBIN #EcoHero #GreenCampus #3RMalaysia #ZeroWaste
            """.trimIndent()
        } else {
            """
            🌿 My Recycling Milestones on ECOBIN AI!
            
            👤 Name: $userName ($occ, $loc)
            🏆 Tier: $levelName
            
            📊 Green Statistics:
            ⭐ Total Eco Points: $points pts
            ♻️ Items Recycled: $totalItems items
            🌍 Carbon Prevented: ${String.format(java.util.Locale.US, "%.1f", carbonSavedKg)} kg CO₂
            🏅 Badges Unlocked: $unlockedBadgesCount badges
            
            Every small green action creates a cleaner planet. Start recycling with us! 💚
            
            #ECOBIN #EcoHero #GreenCampus #RecyclingMilestones #ZeroWaste
            """.trimIndent()
        }

        launchShareIntent(
            context = context,
            subject = if (isBm) "Pencapaian Kitar Semula Saya di ECOBIN AI" else "My Recycling Milestones on ECOBIN AI",
            text = message
        )
    }

    fun shareCommunityPost(
        context: Context,
        author: String,
        content: String,
        tag: String,
        language: AppLanguage
    ) {
        val isBm = language == AppLanguage.BM
        val message = if (isBm) {
            """
            📢 Perkongsian Komuniti Lestari dari $author di ECOBIN AI:
            
            "$content"
            
            $tag #ECOBIN #SMKBintangEco
            """.trimIndent()
        } else {
            """
            📢 School Eco Community Share from $author on ECOBIN AI:
            
            "$content"
            
            $tag #ECOBIN #SMKBintangEco
            """.trimIndent()
        }

        launchShareIntent(
            context = context,
            subject = if (isBm) "Catatan Komuniti ECOBIN: $author" else "ECOBIN Community Post: $author",
            text = message
        )
    }

    fun createShareIntent(subject: String, text: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return Intent.createChooser(sendIntent, subject).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun launchShareIntent(context: Context, subject: String, text: String) {
        try {
            val chooser = createShareIntent(subject, text)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
