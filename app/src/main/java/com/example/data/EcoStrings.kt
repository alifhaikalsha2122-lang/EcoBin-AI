package com.example.data

import com.example.model.AppLanguage

object EcoStrings {
    fun appTagline(lang: AppLanguage) = if (lang == AppLanguage.BM)
        "Imbas. Kitar Semula. Kumpul Mata. Cipta Impak."
    else
        "Scan. Recycle. Earn. Make an Impact."

    fun welcomeGreeting(lang: AppLanguage) = if (lang == AppLanguage.BM)
        "Hai, Wira Lestari! 🌱"
    else
        "Hi, Eco Warrior! 🌱"

    fun home(lang: AppLanguage) = if (lang == AppLanguage.BM) "Utama" else "Home"
    fun scanner(lang: AppLanguage) = if (lang == AppLanguage.BM) "Imbas AI" else "AI Scanner"
    fun map(lang: AppLanguage) = if (lang == AppLanguage.BM) "Peta Kitar" else "Recycling Map"
    fun challenges(lang: AppLanguage) = if (lang == AppLanguage.BM) "Cabaran" else "Challenges"
    fun leaderboard(lang: AppLanguage) = if (lang == AppLanguage.BM) "Kedudukan" else "Leaderboard"
    fun impact(lang: AppLanguage) = if (lang == AppLanguage.BM) "Impak Saya" else "My Eco Impact"
    fun community(lang: AppLanguage) = if (lang == AppLanguage.BM) "Komuniti" else "Community"
    fun profile(lang: AppLanguage) = if (lang == AppLanguage.BM) "Profil" else "Profile"
    fun askAi(lang: AppLanguage) = if (lang == AppLanguage.BM) "Tanya AI" else "Ask AI"
    fun guide(lang: AppLanguage) = if (lang == AppLanguage.BM) "Panduan 3R" else "Recycling Guide"
    fun schoolDashboard(lang: AppLanguage) = if (lang == AppLanguage.BM) "Dashboard Sekolah" else "School Dashboard"
    fun proofUpload(lang: AppLanguage) = if (lang == AppLanguage.BM) "Bukti Kitar" else "Recycle Proof"
    fun presentationDemo(lang: AppLanguage) = if (lang == AppLanguage.BM) "Demo Pertandingan" else "Innovation Demo"

    fun quickScanWaste(lang: AppLanguage) = if (lang == AppLanguage.BM) "Imbas Sisa" else "Scan Waste"
    fun quickFindBin(lang: AppLanguage) = if (lang == AppLanguage.BM) "Cari Tong Kitar" else "Find Recycling Bin"
    fun quickAskAi(lang: AppLanguage) = if (lang == AppLanguage.BM) "Tanya AI" else "Ask AI"
    fun quickDailyChallenge(lang: AppLanguage) = if (lang == AppLanguage.BM) "Cabaran Harian" else "Daily Challenge"

    fun totalItemsRecycled(lang: AppLanguage) = if (lang == AppLanguage.BM) "Jumlah Sisa Dikitar" else "Total Recycled"
    fun ecoPoints(lang: AppLanguage) = if (lang == AppLanguage.BM) "Mata Eco" else "Eco Points"
    fun wasteDiverted(lang: AppLanguage) = if (lang == AppLanguage.BM) "Sisa Dielakkan" else "Waste Diverted"
    fun carbonAvoided(lang: AppLanguage) = if (lang == AppLanguage.BM) "Anggaran CO₂ Dijimatkan" else "Estimated Carbon Impact"
    fun estimatedTag(lang: AppLanguage) = if (lang == AppLanguage.BM) "*Anggaran Ilustrasi Sahaja" else "*Illustrative Estimate"

    fun recentActivity(lang: AppLanguage) = if (lang == AppLanguage.BM) "Aktiviti Terkini" else "Recent Activity"
    fun continueChallenge(lang: AppLanguage) = if (lang == AppLanguage.BM) "Teruskan Cabaran" else "Continue Challenge"
    fun recycleNow(lang: AppLanguage) = if (lang == AppLanguage.BM) "Kitar Semula Sekarang" else "Recycle Now"
    fun scanAnother(lang: AppLanguage) = if (lang == AppLanguage.BM) "Imbas Item Lain" else "Scan Another Item"

    fun recyclableYes(lang: AppLanguage) = if (lang == AppLanguage.BM) "Boleh Dikitar Semula ✅" else "Recyclable: Yes ✅"
    fun recyclableNo(lang: AppLanguage) = if (lang == AppLanguage.BM) "Tidak Boleh Dikitar Semula ❌" else "Non-Recyclable ❌"

    fun sdgTitle(lang: AppLanguage) = "SDG 12 – Responsible Consumption and Production"
    fun sdgDesc(lang: AppLanguage) = if (lang == AppLanguage.BM)
        "ECOBIN AI menyokong amalan pengurusan sisa lestari melalui kecerdasan buatan, pendidikan alam sekitar dan pembudayaan kitar semula di sekolah & komuniti."
    else
        "ECOBIN AI promotes responsible waste segregation and circular economy practices through AI vision, education, and gamification."
}
