package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EcoRepository {

    // Persistent Storage (SharedPreferences)
    var sharedPreferences: SharedPreferences? = null
        private set

    fun initPreferences(context: Context) {
        if (sharedPreferences != null) return
        val prefs = context.applicationContext.getSharedPreferences("ecobin_app_prefs", Context.MODE_PRIVATE)
        sharedPreferences = prefs
        loadSavedData(prefs)
    }

    private fun loadSavedData(prefs: SharedPreferences) {
        val hasSavedUser = prefs.getBoolean("has_saved_user", false)
        if (hasSavedUser) {
            prefs.getString("user_name", null)?.let { if (it.isNotBlank()) _userName.value = it }
            prefs.getString("user_email", null)?.let { if (it.isNotBlank()) _userEmail.value = it }
            prefs.getString("user_location", null)?.let { if (it.isNotBlank()) _userLocation.value = it }
            prefs.getString("user_occupation", null)?.let { if (it.isNotBlank()) _userOccupation.value = it }
            prefs.getString("user_ic_number", null)?.let { if (it.isNotBlank()) _userIcNumber.value = it }
            prefs.getString("user_phone_number", null)?.let { if (it.isNotBlank()) _userPhoneNumber.value = it }
            _isLoggedIn.value = prefs.getBoolean("is_logged_in", true)

            _userPoints.value = prefs.getInt("user_points", 0)
            _pointsThisWeek.value = prefs.getInt("points_this_week", 0)
            _pointsThisMonth.value = prefs.getInt("points_this_month", 0)
            _totalItemsRecycled.value = prefs.getInt("total_items_recycled", 0)
            _wasteDivertedKg.value = prefs.getFloat("waste_diverted_kg", 0f)
            _carbonImpactKg.value = prefs.getFloat("carbon_impact_kg", 0f)

            _plasticCount.value = prefs.getInt("plastic_count", 0)
            _paperCount.value = prefs.getInt("paper_count", 0)
            _glassCount.value = prefs.getInt("glass_count", 0)
            _metalCount.value = prefs.getInt("metal_count", 0)
            _eWasteCount.value = prefs.getInt("ewaste_count", 0)
            _userProfileImage.value = prefs.getString("user_profile_image", "avatar:scholar") ?: "avatar:scholar"
            val savedTheme = prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name)
            _themeMode.value = try {
                AppThemeMode.valueOf(savedTheme ?: AppThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                AppThemeMode.SYSTEM
            }
            val savedLang = prefs.getString("app_language", AppLanguage.BM.name)
            _language.value = try {
                AppLanguage.valueOf(savedLang ?: AppLanguage.BM.name)
            } catch (e: Exception) {
                AppLanguage.BM
            }
        } else {
            // New user defaults: guaranteed 0 points
            _userPoints.value = 0
            _pointsThisWeek.value = 0
            _pointsThisMonth.value = 0
            _totalItemsRecycled.value = 0
            _wasteDivertedKg.value = 0f
            _carbonImpactKg.value = 0f
            _plasticCount.value = 0
            _paperCount.value = 0
            _glassCount.value = 0
            _metalCount.value = 0
            _eWasteCount.value = 0
            _userProfileImage.value = "avatar:scholar"
            val savedTheme = prefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name)
            _themeMode.value = try {
                AppThemeMode.valueOf(savedTheme ?: AppThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                AppThemeMode.SYSTEM
            }
        }
    }

    fun hasExistingPoints(username: String): Boolean {
        val prefs = sharedPreferences ?: return false
        val savedName = prefs.getString("user_name", "")
        return savedName.equals(username, ignoreCase = true) && prefs.getInt("user_points", 0) > 0
    }

    fun persistUserData() {
        val prefs = sharedPreferences ?: return
        prefs.edit().apply {
            putBoolean("has_saved_user", true)
            putString("user_name", _userName.value)
            putString("user_email", _userEmail.value)
            putString("user_location", _userLocation.value)
            putString("user_occupation", _userOccupation.value)
            putString("user_ic_number", _userIcNumber.value)
            putString("user_phone_number", _userPhoneNumber.value)
            putString("user_profile_image", _userProfileImage.value)
            putBoolean("is_logged_in", _isLoggedIn.value)

            putInt("user_points", _userPoints.value)
            putInt("points_this_week", _pointsThisWeek.value)
            putInt("points_this_month", _pointsThisMonth.value)
            putInt("total_items_recycled", _totalItemsRecycled.value)
            putFloat("waste_diverted_kg", _wasteDivertedKg.value)
            putFloat("carbon_impact_kg", _carbonImpactKg.value)

            putInt("plastic_count", _plasticCount.value)
            putInt("paper_count", _paperCount.value)
            putInt("glass_count", _glassCount.value)
            putInt("metal_count", _metalCount.value)
            putInt("ewaste_count", _eWasteCount.value)
            putString("app_theme_mode", _themeMode.value.name)
            putString("app_language", _language.value.name)
            apply()
        }
    }

    // App Theme Mode State (SYSTEM, LIGHT, DARK)
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        sharedPreferences?.edit()?.putString("app_theme_mode", mode.name)?.apply()
    }

    // App Language State
    private val _language = MutableStateFlow(AppLanguage.BM)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        sharedPreferences?.edit()?.putString("app_language", lang.name)?.apply()
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.BM) AppLanguage.EN else AppLanguage.BM
        setLanguage(next)
    }

    // Profile Image (Custom Uri or Avatar Preset)
    private val _userProfileImage = MutableStateFlow<String>("avatar:scholar")
    val userProfileImage: StateFlow<String> = _userProfileImage.asStateFlow()

    fun updateProfileImage(newImage: String) {
        if (newImage.isNotBlank()) {
            _userProfileImage.value = newImage
            persistUserData()
        }
    }

    // User Profile Stats & Authentication
    private val _userName = MutableStateFlow("Aiman Hakim")
    val userNameFlow: StateFlow<String> = _userName.asStateFlow()
    val userName: String get() = _userName.value

    private val _userEmail = MutableStateFlow("aiman.hakim@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userLocation = MutableStateFlow("Petaling Jaya, Selangor")
    val userLocationFlow: StateFlow<String> = _userLocation.asStateFlow()
    val userLocation: String get() = _userLocation.value

    // Backward-compatibility aliases for existing calls
    val userSchoolFlow: StateFlow<String> get() = userLocationFlow
    val userSchool: String get() = _userLocation.value

    private val _userOccupation = MutableStateFlow("Eksekutif Kelestarian")
    val userOccupationFlow: StateFlow<String> = _userOccupation.asStateFlow()
    val userOccupation: String get() = _userOccupation.value

    // Backward-compatibility aliases for existing calls
    val userClassFlow: StateFlow<String> get() = userOccupationFlow
    val userClass: String get() = _userOccupation.value

    private val _userIcNumber = MutableStateFlow("020512-10-5432")
    val userIcNumberFlow: StateFlow<String> = _userIcNumber.asStateFlow()
    val userIcNumber: String get() = _userIcNumber.value

    private val _userPhoneNumber = MutableStateFlow("+60 12-345 6789")
    val userPhoneNumberFlow: StateFlow<String> = _userPhoneNumber.asStateFlow()
    val userPhoneNumber: String get() = _userPhoneNumber.value

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun updateUsername(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            _userName.value = trimmed
            persistUserData()
        }
    }

    fun updateProfile(
        name: String,
        location: String,
        occupation: String,
        icNumber: String = _userIcNumber.value,
        phoneNumber: String = _userPhoneNumber.value,
        email: String = _userEmail.value
    ) {
        if (name.isNotBlank()) _userName.value = name.trim()
        if (location.isNotBlank()) _userLocation.value = location.trim()
        if (occupation.isNotBlank()) _userOccupation.value = occupation.trim()
        if (icNumber.isNotBlank()) _userIcNumber.value = icNumber.trim()
        if (phoneNumber.isNotBlank()) _userPhoneNumber.value = phoneNumber.trim()
        if (email.isNotBlank()) _userEmail.value = email.trim()
        persistUserData()
    }

    fun login(
        usernameOrEmail: String,
        location: String = "Petaling Jaya, Selangor",
        occupation: String = "Eksekutif Kelestarian",
        icNumber: String = "020512-10-5432",
        phoneNumber: String = "+60 12-345 6789",
        email: String? = null,
        resetPointsForNewUser: Boolean = false
    ) {
        val trimmed = usernameOrEmail.trim()
        if (trimmed.isNotEmpty()) {
            if (email != null && email.isNotBlank()) {
                _userEmail.value = email.trim()
                _userName.value = trimmed
            } else if (trimmed.contains("@")) {
                _userEmail.value = trimmed
                val extractedName = trimmed.substringBefore("@").replace(".", " ").split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
                _userName.value = extractedName
            } else {
                _userName.value = trimmed
                _userEmail.value = "${trimmed.lowercase().replace(" ", ".")}@gmail.com"
            }
            if (location.isNotBlank()) {
                _userLocation.value = location.trim()
            }
            if (occupation.isNotBlank()) {
                _userOccupation.value = occupation.trim()
            }
            if (icNumber.isNotBlank()) {
                _userIcNumber.value = icNumber.trim()
            }
            if (phoneNumber.isNotBlank()) {
                _userPhoneNumber.value = phoneNumber.trim()
            }
            _isLoggedIn.value = true

            if (resetPointsForNewUser) {
                _userPoints.value = 0
                _pointsThisWeek.value = 0
                _pointsThisMonth.value = 0
                _totalItemsRecycled.value = 0
                _wasteDivertedKg.value = 0f
                _carbonImpactKg.value = 0f
                _plasticCount.value = 0
                _paperCount.value = 0
                _glassCount.value = 0
                _metalCount.value = 0
                _eWasteCount.value = 0
                _recentActivities.value = emptyList()
            }
            persistUserData()
        }
    }

    fun loginWithGoogleEmail(
        googleEmail: String,
        name: String? = null,
        location: String = _userLocation.value,
        occupation: String = _userOccupation.value,
        icNumber: String = _userIcNumber.value,
        phoneNumber: String = _userPhoneNumber.value
    ) {
        val cleanEmail = googleEmail.trim().lowercase()
        val displayName = if (!name.isNullOrBlank()) {
            name.trim()
        } else {
            val prefix = cleanEmail.substringBefore("@")
            prefix.replace(".", " ").replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
        _userEmail.value = cleanEmail
        _userName.value = displayName
        if (location.isNotBlank()) _userLocation.value = location.trim()
        if (occupation.isNotBlank()) _userOccupation.value = occupation.trim()
        if (icNumber.isNotBlank()) _userIcNumber.value = icNumber.trim()
        if (phoneNumber.isNotBlank()) _userPhoneNumber.value = phoneNumber.trim()
        _isLoggedIn.value = true

        val prefs = sharedPreferences
        val savedEmail = prefs?.getString("user_email", null)
        if (savedEmail != cleanEmail) {
            // New user account: reward points start from 0
            _userPoints.value = 0
            _pointsThisWeek.value = 0
            _pointsThisMonth.value = 0
            _totalItemsRecycled.value = 0
            _wasteDivertedKg.value = 0f
            _carbonImpactKg.value = 0f
            _plasticCount.value = 0
            _paperCount.value = 0
            _glassCount.value = 0
            _metalCount.value = 0
            _eWasteCount.value = 0
            _recentActivities.value = emptyList()
        }
        persistUserData()
    }

    fun logout() {
        _isLoggedIn.value = false
        _userName.value = "Tetamu (Guest)"
        _userEmail.value = "guest@ecobin.ai"
        _userLocation.value = "Kuala Lumpur"
        _userOccupation.value = "Orang Awam"
        _userIcNumber.value = "-"
        _userPhoneNumber.value = "-"
        _userPoints.value = 0
        persistUserData()
    }

    // New users start with 0 points
    private val _userPoints = MutableStateFlow(0)
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    private val _pointsThisWeek = MutableStateFlow(0)
    val pointsThisWeek: StateFlow<Int> = _pointsThisWeek.asStateFlow()

    private val _pointsThisMonth = MutableStateFlow(0)
    val pointsThisMonth: StateFlow<Int> = _pointsThisMonth.asStateFlow()

    private val _totalItemsRecycled = MutableStateFlow(0)
    val totalItemsRecycled: StateFlow<Int> = _totalItemsRecycled.asStateFlow()

    private val _wasteDivertedKg = MutableStateFlow(0f)
    val wasteDivertedKg: StateFlow<Float> = _wasteDivertedKg.asStateFlow()

    private val _carbonImpactKg = MutableStateFlow(0f)
    val carbonImpactKg: StateFlow<Float> = _carbonImpactKg.asStateFlow()

    // Waste Breakdown counts
    private val _plasticCount = MutableStateFlow(0)
    val plasticCount: StateFlow<Int> = _plasticCount.asStateFlow()

    private val _paperCount = MutableStateFlow(0)
    val paperCount: StateFlow<Int> = _paperCount.asStateFlow()

    private val _glassCount = MutableStateFlow(0)
    val glassCount: StateFlow<Int> = _glassCount.asStateFlow()

    private val _metalCount = MutableStateFlow(0)
    val metalCount: StateFlow<Int> = _metalCount.asStateFlow()

    private val _eWasteCount = MutableStateFlow(0)
    val eWasteCount: StateFlow<Int> = _eWasteCount.asStateFlow()

    // Recent Activities (Starts empty for fresh users)
    private val _recentActivities = MutableStateFlow<List<RecyclingActivity>>(emptyList())
    val recentActivities: StateFlow<List<RecyclingActivity>> = _recentActivities.asStateFlow()

    // Challenges
    private val _challenges = MutableStateFlow(
        listOf(
            EcoChallenge(
                id = "ch-1",
                titleEn = "Recycle 3 Plastic Bottles",
                titleBm = "Kitar Semula 3 Botol Plastik",
                descEn = "Clean, flatten, and deposit plastic bottles in the blue bin.",
                descBm = "Bersihkan, kemekkan dan masukkan botol plastik ke tong biru.",
                current = 0,
                target = 3,
                pointsReward = 25,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            ),
            EcoChallenge(
                id = "ch-2",
                titleEn = "Safe E-Waste / Battery Drop-off",
                titleBm = "Lupus 1 E-Sisa / Bateri / Kabel",
                descEn = "Deposit old batteries or broken cables into the e-waste bin.",
                descBm = "Masukkan bateri lama, pengecas atau kabel rosak ke dalam tong e-sisa.",
                current = 0,
                target = 1,
                pointsReward = 40,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            ),
            EcoChallenge(
                id = "ch-3",
                titleEn = "Recycle 2 Paper / Cardboard Boxes",
                titleBm = "Kitar Semula 2 Kotak / Kertas",
                descEn = "Collect clean paper, textbooks, or cartons for recycling.",
                descBm = "Kumpul kertas terpakai, buku nota atau kotak kadbod bersih.",
                current = 0,
                target = 2,
                pointsReward = 20,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            ),
            EcoChallenge(
                id = "ch-4",
                titleEn = "Recycle 1 Glass Bottle or Jar",
                titleBm = "Kitar Semula 1 Balang / Botol Kaca",
                descEn = "Rinse glass jar/bottle and place in the Brown recycling bin.",
                descBm = "Bilas balang jem atau botol sos dan letakkan dalam tong coklat.",
                current = 0,
                target = 1,
                pointsReward = 30,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            ),
            EcoChallenge(
                id = "ch-5",
                titleEn = "Daily AI Waste Scanner Mission",
                titleBm = "Imbas Sisa Pertama Hari Ini",
                descEn = "Use the Gemini AI Camera to identify and learn proper disposal.",
                descBm = "Gunakan kamera Gemini AI untuk kenal pasti jenis sisa dan panduan pelupusan.",
                current = 0,
                target = 1,
                pointsReward = 15,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            ),
            EcoChallenge(
                id = "ch-6",
                titleEn = "Recycle 2 Aluminum Cans",
                titleBm = "Kitar Semula 2 Tin Aluminium / Logam",
                descEn = "Crush metal beverage cans and drop into the Orange recycling bin.",
                descBm = "Kemikkan tin minuman aluminium dan masukkan ke tong jingga.",
                current = 0,
                target = 2,
                pointsReward = 25,
                deadline = "Hari Ini (24 Jam)",
                isCompleted = false
            )
        )
    )
    val challenges: StateFlow<List<EcoChallenge>> = _challenges.asStateFlow()

    // Badges (starts at 0 progress for new users)
    private val _badges = MutableStateFlow(
        listOf(
            EcoBadge("b1", "Eco Beginner", "Pemula Hijau", "Complete your first waste scan", "Lengkapkan imbasan sisa pertama", "🌱", false, 0, 1),
            EcoBadge("b2", "Plastic Saver", "Wira Plastik", "Recycle 15 plastic items", "Kitar semula 15 item plastik", "♻️", false, 0, 15),
            EcoBadge("b3", "Paper Hero", "Wira Kertas", "Recycle 20 paper items", "Kitar semula 20 sisa kertas", "📄", false, 0, 20),
            EcoBadge("b4", "E-Waste Guardian", "Penjaga E-Sisa", "Dispose 3 electronic/battery items safely", "Lupus 3 item elektronik/bateri dengan selamat", "🔋", false, 0, 3),
            EcoBadge("b5", "Green Champion", "Juara Kelestarian", "Earn 500 Eco Points", "Kumpul 500 Eco Points", "🏆", false, 0, 500),
            EcoBadge("b6", "Planet Protector", "Pelindung Bumi", "Divert 25kg of waste from landfills", "Selamatkan 25kg sisa daripada tapak pelupusan", "🌍", false, 0, 25)
        )
    )
    val badges: StateFlow<List<EcoBadge>> = _badges.asStateFlow()

    // Recycling Locations
    val locations = listOf(
        RecyclingLocation(
            id = "loc-1",
            name = "SMK Bintang Hijau Eco Hub",
            type = "School Collection Point",
            typeBm = "Pusat Pengumpulan Sekolah",
            distanceKm = 0.2,
            address = "Dataran Perhimpunan, SMK Bintang Hijau",
            acceptedCategories = listOf(WasteCategory.PLASTIC, WasteCategory.PAPER, WasteCategory.METAL, WasteCategory.E_WASTE),
            openingHours = "Mon - Fri: 7:30 AM - 4:30 PM",
            openingHoursBm = "Isnin - Jumaat: 7:30 PG - 4:30 PTG",
            latitude = 3.1412,
            longitude = 101.6865,
            isSchoolPoint = true
        ),
        RecyclingLocation(
            id = "loc-2",
            name = "Smart Community 3R Station",
            type = "Recycling Centre",
            typeBm = "Pusat Kitar Semula Komuniti",
            distanceKm = 0.8,
            address = "Jalan Bintang 4, Taman Komuniti Lestari",
            acceptedCategories = listOf(WasteCategory.PLASTIC, WasteCategory.PAPER, WasteCategory.GLASS, WasteCategory.METAL, WasteCategory.E_WASTE, WasteCategory.HAZARDOUS),
            openingHours = "Daily: 8:00 AM - 6:00 PM",
            openingHoursBm = "Setiap Hari: 8:00 PG - 6:00 PTG",
            latitude = 3.1455,
            longitude = 101.6912
        ),
        RecyclingLocation(
            id = "loc-3",
            name = "Dewan Komuniti E-Waste Kiosk",
            type = "Collection Point",
            typeBm = "Kiosk Pengumpulan E-Sisa",
            distanceKm = 1.3,
            address = "Lobi Dewan Komuniti Sentral",
            acceptedCategories = listOf(WasteCategory.E_WASTE, WasteCategory.HAZARDOUS),
            openingHours = "24 Hours Accessible",
            openingHoursBm = "Dibuka 24 Jam",
            latitude = 3.1388,
            longitude = 101.6821
        ),
        RecyclingLocation(
            id = "loc-4",
            name = "Taman Tasik Smart Solar Bin",
            type = "Recycling Bin",
            typeBm = "Tong Pintar Kitar Semula",
            distanceKm = 1.7,
            address = "Pintu Masuk Utama Taman Tasik",
            acceptedCategories = listOf(WasteCategory.PLASTIC, WasteCategory.METAL),
            openingHours = "Public Park Hours (6 AM - 10 PM)",
            openingHoursBm = "Waktu Taman Awam (6 PG - 10 MLM)",
            latitude = 3.1492,
            longitude = 101.6790
        )
    )

    // Leaderboard
    val individualLeaderboard = listOf(
        LeaderboardEntry(1, "Aiman Hakim (You)", 1240, 112, "🏆 Green Champion", "5 Cemerlang • SMK Bintang Hijau"),
        LeaderboardEntry(2, "Sarah Qistina", 1180, 98, "🌟 Zero-Waste Master", "5 Inovasi • SMK Bintang Hijau"),
        LeaderboardEntry(3, "Amir Danish", 1050, 89, "♻️ Plastic Saver", "4 Cemerlang • SMK Bintang Hijau"),
        LeaderboardEntry(4, "Nurul Izzah", 940, 81, "📄 Paper Hero", "5 Bestari • SMK Bintang Hijau"),
        LeaderboardEntry(5, "Chong Wei Lun", 890, 74, "🔋 E-Waste Guardian", "4 Dinamik • SMK Bintang Hijau"),
        LeaderboardEntry(6, "Kavita Pillai", 820, 68, "🌱 Eco Explorer", "3 Gemilang • SMK Bintang Hijau"),
        LeaderboardEntry(7, "Daniel Lee", 760, 62, "🍾 Glass Recycler", "5 Kejora • SMK Cyberjaya"),
        LeaderboardEntry(8, "Siti Hajar", 710, 57, "🌿 Planet Protector", "4 Bestari • SMK Cyberjaya"),
        LeaderboardEntry(9, "Muhammad Harith", 650, 51, "🥉 Eco Defender", "3 Cemerlang • SMK Putrajaya"),
        LeaderboardEntry(10, "Priya Raman", 590, 46, "🌱 Eco Beginner", "4 Inovasi • SMK Seri Saujana")
    )

    val classLeaderboard = listOf(
        LeaderboardEntry(1, "5 Cemerlang", 4250, 395, "🥇 Juara Bulan Ini", "SMK Bintang Hijau"),
        LeaderboardEntry(2, "5 Inovasi", 3890, 342, "🥈 Naib Juara", "SMK Bintang Hijau"),
        LeaderboardEntry(3, "4 Cemerlang", 3410, 298, "🥉 Tempat Ketiga", "SMK Bintang Hijau"),
        LeaderboardEntry(4, "5 Bestari", 2980, 260, "⭐ Tempat Ke-4", "SMK Bintang Hijau"),
        LeaderboardEntry(5, "4 Dinamik", 2450, 210, "⭐ Tempat Ke-5", "SMK Bintang Hijau")
    )

    val schoolLeaderboard = listOf(
        LeaderboardEntry(1, "SMK Bintang Hijau", 18640, 1430, "🏆 Sekolah Lestari Emas", "Zon Sentral"),
        LeaderboardEntry(2, "SMK Bukit Damai", 16210, 1280, "🥈 Sekolah Lestari Perak", "Zon Utara"),
        LeaderboardEntry(3, "Kolej Vokasional Indah", 14750, 1140, "🥉 Sekolah Lestari Gangsa", "Zon Barat"),
        LeaderboardEntry(4, "SMK Sains Bestari", 13200, 990, "🌿 Harapan Hijau", "Zon Timur")
    )

    // Community Feed: School recycling campaigns, leader board updates, and environmental activities
    private val _communityPosts = MutableStateFlow(
        listOf(
            // 1. School Recycling Campaigns
            CommunityPost(
                id = "post-c1",
                author = "Jawatankuasa Lestari Sekolah",
                role = "Kempen Rasmi SMK Bintang",
                timeAgo = "1 hour ago",
                timeAgoBm = "1 jam lalu",
                contentEn = "📢 CAREton 2026 Beverage Carton Drive is officially on! Let's divert 500kg of drink cartons to create eco-roof tiles for homes. Current collection stands at 390kg! Only 5 days remaining to reach our goal!",
                contentBm = "📢 Kempen Pengumpulan Kotak Minuman UHT (CAREton) 2026 kini berlangsung! Sasaran 500kg kotak minuman bersih untuk dijadikan bumbung eko komuniti. Status semasa: 390kg terkumpul! Tinggal 5 hari lagi untuk capai sasaran!",
                likes = 68,
                tag = "#CAREton2026",
                type = CommunityPostType.CAMPAIGN,
                avatarEmoji = "📢",
                campaignProgress = 0.78f,
                campaignTargetText = "390 / 500 kg (78%)",
                campaignDaysLeft = 5,
                comments = listOf(
                    CommunityComment(author = "Farah Nabilah", role = "Pelajar 5 Inovasi", text = "Kelas kami dah bawa 45 kotak hari ni! 💪"),
                    CommunityComment(author = "Cikgu Azman", role = "Penyelaras 3R", text = "Syabas semua! Pastikan kotak dibilas & diratakan ya.")
                )
            ),
            CommunityPost(
                id = "post-c2",
                author = "Majlis Perwakilan Pelajar",
                role = "Pertandingan Antara Rumah Sukan",
                timeAgo = "4 hours ago",
                timeAgoBm = "4 jam lalu",
                contentEn = "🏆 Inter-House 3R Derby: Rumah Hijau leads the tally with 180kg, followed closely by Rumah Biru (150kg) and Rumah Merah (120kg). Segregate plastics & paper to win house points before Friday!",
                contentBm = "🏆 Pertandingan Kitar Semula Antara Rumah Sukan! Rumah Hijau mendahului dengan 180kg, diekori rapat Rumah Biru (150kg) & Rumah Merah (120kg). Bawa sisa plastik & kertas ke dewan sebelum Jumaat!",
                likes = 54,
                tag = "#SportsHouseRecycling",
                type = CommunityPostType.CAMPAIGN,
                avatarEmoji = "🏅",
                campaignProgress = 0.65f,
                campaignTargetText = "450 / 600 kg (75%)",
                campaignDaysLeft = 3
            ),

            // 2. Leaderboard Updates
            CommunityPost(
                id = "post-l1",
                author = "Sistem Papan Skor ECOBIN",
                role = "Kemas Kini Mingguan",
                timeAgo = "2 hours ago",
                timeAgoBm = "2 jam lalu",
                contentEn = "🔥 Weekly Class Leaderboard Update! Massive congratulations to 5 Cemerlang for capturing Rank #1 with 4,250 eco points! 5 Inovasi holds #2 with 3,890 pts. Top segregated material: Plastic PET Bottles.",
                contentBm = "🔥 Kemas Kini Papan Skor Kelas Mingguan! Tahniah kepada Kelas 5 Cemerlang merangkul Tempat Pertama dengan 4,250 mata eko! Kelas 5 Inovasi di tempat ke-2 dengan 3,890 mata. Bahan paling banyak dikitar: Botol Plastik PET.",
                likes = 82,
                tag = "#ClassLeaderboard",
                type = CommunityPostType.LEADERBOARD,
                avatarEmoji = "🏆",
                leaderboardRank = 1,
                leaderboardPoints = "4,250 pts (+360 minggu ini)",
                comments = listOf(
                    CommunityComment(author = "Aiman Hakim", role = "Ketua Kelas 5 Cemerlang", text = "Terima kasih rakan-rakan sekelas! Kekalkan momentum! 🥇🔥"),
                    CommunityComment(author = "Nurul Izzah", role = "5 Inovasi", text = "Tunggu minggu depan, 5 Inovasi akan kejar! 😉")
                )
            ),
            CommunityPost(
                id = "post-l2",
                author = "Sistem Papan Skor ECOBIN",
                role = "Rekod Individu Baharu",
                timeAgo = "Yesterday",
                timeAgoBm = "Semalam",
                contentEn = "⭐ Hall of Fame Milestone! Aiman Hakim (5 Cemerlang) has set a new individual record with 1,240 eco points from verified classroom sorting. Nurul Izzah holds spot #2 with 1,110 pts!",
                contentBm = "⭐ Rekod 'Hall of Fame' Individu Baharu! Aiman Hakim (5 Cemerlang) mencipta rekod baharu dengan 1,240 mata eko menerusi pengasingan sisa terverifikasi! Nurul Izzah di tempat ke-2 dengan 1,110 mata!",
                likes = 73,
                tag = "#TopEcoWarrior",
                type = CommunityPostType.LEADERBOARD,
                avatarEmoji = "⭐",
                leaderboardRank = 1,
                leaderboardPoints = "1,240 pts"
            ),

            // 3. Environmental Activity Posts
            CommunityPost(
                id = "post-a1",
                author = "Kelab Alam Sekitar SMK",
                role = "Aktiviti Kokurikulum",
                timeAgo = "3 hours ago",
                timeAgoBm = "3 jam lalu",
                contentEn = "🌱 Upcycling Workshop Success! Our members successfully turned 60 empty detergent containers into hanging planters for the school organic herb corridor. Real zero-waste action!",
                contentBm = "🌱 Bengkel Upcycling Berjaya! Ahli kelab berjaya mengubah 60 bekas sabun terpakai menjadi pasu gantung bagi koridor herba organik sekolah. Tindakan sisa sifar sebenar!",
                likes = 49,
                tag = "#UpcyclingWorkshop",
                type = CommunityPostType.ACTIVITY,
                avatarEmoji = "🪴",
                impactBadge = "-18.2 kg CO₂",
                comments = listOf(
                    CommunityComment(author = "Danish Rayyan", role = "Pelajar 4 Sains", text = "Cantik sangat pasu gantung kat koridor makmal! Boleh ajar kelas kami?")
                )
            ),
            CommunityPost(
                id = "post-a2",
                author = "Cikgu Rosli",
                role = "Guru Penasihat 3R",
                timeAgo = "5 hours ago",
                timeAgoBm = "5 jam lalu",
                contentEn = "Bokashi Composting Friday: Cafeteria fruit peels and canteen vegetable trimmings were loaded into our fermentation drums. Generating free organic fertilizer for the school campus trees!",
                contentBm = "Jumaat Kompos Bokashi: Sisa kulit buah dan lebihan sayur kantin dimasukkan ke dalam tong penapaian sekolah. Menghasilkan baja organik percuma untuk pokok kampus!",
                likes = 58,
                tag = "#FoodWasteCompost",
                type = CommunityPostType.ACTIVITY,
                avatarEmoji = "🍎",
                impactBadge = "-32.0 kg CO₂"
            ),
            CommunityPost(
                id = "post-a3",
                author = "Sarah Qistina",
                role = "Pelajar - 5 Inovasi",
                timeAgo = "Yesterday",
                timeAgoBm = "Semalam",
                contentEn = "Sorted and segregated 15 plastic beverage bottles after our afternoon SPM study group session. Uploaded photo proof to the AI Scanner! ♻️✨",
                contentBm = "Selesai mengasingkan 15 botol plastik minuman selepas sesi ulangkaji SPM petang semalam. Bukti foto telah dimuat naik ke AI Scanner! ♻️✨",
                likes = 36,
                tag = "#StudentEcoAction",
                type = CommunityPostType.ACTIVITY,
                avatarEmoji = "♻️",
                impactBadge = "+15 botol PET"
            )
        )
    )
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    // School Stats
    val schoolStats = SchoolStats()

    // Cluster items for Google Maps Compose marker clustering
    val mapClusterItems = listOf(
        // 1. Recycling Centers (Pusat Kitar Semula)
        EcoMapClusterItem(
            id = "rc-1",
            titleText = "IPC Recycling & Buy-Back Centre",
            snippetText = "Pusat 3R Buy-Back Utama PJ",
            lat = 3.1578,
            lng = 101.6119,
            pointType = ClusterPointType.RECYCLING_CENTER,
            details = "Level P1, IPC Shopping Centre, Mutiara Damansara. 8:00 AM - 10:00 PM.",
            acceptedCategories = "Plastik, Kertas, Logam, Minyak Masak, E-Sisa"
        ),
        EcoMapClusterItem(
            id = "rc-2",
            titleText = "PJ Eco Recycling Plaza (MBPJ)",
            snippetText = "Hab Komuniti 3R & Baja Kompos",
            lat = 3.0872,
            lng = 101.6214,
            pointType = ClusterPointType.RECYCLING_CENTER,
            details = "Jalan SS 8/39 Sungai Way. Pusat pelupusan e-sisa berlesen & fabrik terpakai.",
            acceptedCategories = "E-Sisa, Fabrik, Kaca, Minyak Masak, Kertas"
        ),
        EcoMapClusterItem(
            id = "rc-3",
            titleText = "Alam Flora 3R Buy-Back Centre",
            snippetText = "Pusat Belian Balik Bahan Kitar Semula",
            lat = 3.1492,
            lng = 101.6964,
            pointType = ClusterPointType.RECYCLING_CENTER,
            details = "Jalan Sultan Ismail, KL. Penimbangan segera dan bayaran tunai bahan terasing.",
            acceptedCategories = "Kotak Kadbod, Botol Plastik, Tin Logam, Kertas"
        ),
        EcoMapClusterItem(
            id = "rc-4",
            titleText = "Tzu Chi Eco-Awareness Centre",
            snippetText = "Pusat Kitar Semula Sukarelawan",
            lat = 3.2085,
            lng = 101.6372,
            pointType = ClusterPointType.RECYCLING_CENTER,
            details = "Jalan Kepong, KL. Pengasingan bahan terperinci oleh warga komuniti sukarelawan.",
            acceptedCategories = "Buku Lama, Plastik, Logam, Kaca, Pakaian"
        ),

        // 2. Bin Collection Points (Titik Pengumpulan Tong Kitar Semula)
        EcoMapClusterItem(
            id = "bin-1",
            titleText = "Smart Bin Hub Taman Tasik",
            snippetText = "Tong Pintar Sensor Automatik",
            lat = 3.1360,
            lng = 101.6190,
            pointType = ClusterPointType.BIN_COLLECTION,
            details = "Pintu Utama Taman Tasik. Tong pintar pemampatan berkuasa solar.",
            acceptedCategories = "Botol Plastik, Tin Minuman"
        ),
        EcoMapClusterItem(
            id = "bin-2",
            titleText = "SS2 Community Segregation Bins",
            snippetText = "Stesen Tong Kitar 3 Warna",
            lat = 3.1189,
            lng = 101.6224,
            pointType = ClusterPointType.BIN_COLLECTION,
            details = "Pusat Komersial SS2 Petaling Jaya. 3 tong berasingan (Biru, Coklat, Jingga).",
            acceptedCategories = "Kertas, Kaca, Tin & Plastik"
        ),
        EcoMapClusterItem(
            id = "bin-3",
            titleText = "Kelana Jaya Eco-Bin Station",
            snippetText = "Tong Kitar Semula Komuniti",
            lat = 3.1025,
            lng = 101.5978,
            pointType = ClusterPointType.BIN_COLLECTION,
            details = "Berhampiran Stesen LRT Kelana Jaya. Dibuka 24 jam.",
            acceptedCategories = "Plastik, Kertas, Logam"
        ),
        EcoMapClusterItem(
            id = "bin-4",
            titleText = "Damansara Perdana E-Waste Kiosk",
            snippetText = "Kiosk Khas Pelupusan E-Sisa",
            lat = 3.1672,
            lng = 101.6053,
            pointType = ClusterPointType.BIN_COLLECTION,
            details = "Drop-off bateri, telefon lama dan peralatan elektrik kecil.",
            acceptedCategories = "Bateri, Pengecas, Mentol, Gajet"
        ),

        // 3. Schools (Pusat Pengumpulan Sekolah)
        EcoMapClusterItem(
            id = "sch-1",
            titleText = "SMK Bintang (Eco-School Base)",
            snippetText = "Pangkalan Utama Kitar Semula Pelajar",
            lat = 3.1412,
            lng = 101.6135,
            pointType = ClusterPointType.SCHOOL,
            details = "Hab Pengumpulan Sekolah & Papan Skor Kelas. Zon drop-off di Foyer.",
            acceptedCategories = "Semua Kategori Kitar Semula"
        ),
        EcoMapClusterItem(
            id = "sch-2",
            titleText = "SK Taman Megah",
            snippetText = "Sekolah Rintis 'Green Flag' Eco-Schools",
            lat = 3.1165,
            lng = 101.6112,
            pointType = ClusterPointType.SCHOOL,
            details = "Program pengasingan sisa harian murid sekolah rendah.",
            acceptedCategories = "Kertas Ujian Lama, Botol Susu, Tin Minuman"
        ),
        EcoMapClusterItem(
            id = "sch-3",
            titleText = "SMK Damansara Utama",
            snippetText = "Kelab Alam Sekitar & Kitar Semula",
            lat = 3.1352,
            lng = 101.6288,
            pointType = ClusterPointType.SCHOOL,
            details = "Pusat kutipan berkala hari Rabu bagi sisa plastik kelas.",
            acceptedCategories = "Plastik, Kotak Minuman, Logam"
        ),
        EcoMapClusterItem(
            id = "sch-4",
            titleText = "SJK(C) Puay Chai 2",
            snippetText = "Hab E-Sisa & Kertas Sekolah",
            lat = 3.1489,
            lng = 101.6174,
            pointType = ClusterPointType.SCHOOL,
            details = "Pengumpulan mingguan surat khabar dan sisa kertas pembelajaran.",
            acceptedCategories = "Kertas, Majalah, Kadbod"
        )
    )

    // Weekly and Monthly Recycling Trends Placeholder Data
    val weeklyTrends = listOf(
        TrendDataPoint("Mon", "Isn", 4),
        TrendDataPoint("Tue", "Sel", 6),
        TrendDataPoint("Wed", "Rab", 3),
        TrendDataPoint("Thu", "Kha", 8),
        TrendDataPoint("Fri", "Jum", 5),
        TrendDataPoint("Sat", "Sab", 9),
        TrendDataPoint("Sun", "Ahd", 7)
    )

    val monthlyTrends = listOf(
        TrendDataPoint("May", "Mei", 28),
        TrendDataPoint("Jun", "Jun", 35),
        TrendDataPoint("Jul", "Jul", 42),
        TrendDataPoint("Aug", "Ogo", 39),
        TrendDataPoint("Sep", "Sep", 50),
        TrendDataPoint("Oct", "Okt", 45)
    )

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        _notificationsEnabled.update { !it }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun addRecyclingActivity(result: WasteScanResult) {
        val points = result.estimatedPoints
        _userPoints.update { it + points }
        _pointsThisWeek.update { it + points }
        _pointsThisMonth.update { it + points }
        _totalItemsRecycled.update { it + 1 }
        _wasteDivertedKg.update { it + result.estimatedWeightKg }
        _carbonImpactKg.update { it + result.carbonSavedKg }

        when (result.category) {
            WasteCategory.PLASTIC -> _plasticCount.update { it + 1 }
            WasteCategory.PAPER -> _paperCount.update { it + 1 }
            WasteCategory.GLASS -> _glassCount.update { it + 1 }
            WasteCategory.METAL -> _metalCount.update { it + 1 }
            WasteCategory.E_WASTE -> _eWasteCount.update { it + 1 }
            else -> {}
        }

        val newActivity = RecyclingActivity(
            id = "act-${System.currentTimeMillis()}",
            itemName = result.itemName,
            category = result.category,
            points = points,
            timestamp = "Just now",
            isVerified = true
        )
        _recentActivities.update { listOf(newActivity) + it.take(9) }

        // Update challenges progress dynamically
        _challenges.update { list ->
            list.map { ch ->
                if (!ch.isCompleted) {
                    val shouldIncrement = when (ch.id) {
                        "ch-1" -> result.category == WasteCategory.PLASTIC
                        "ch-2" -> result.category == WasteCategory.E_WASTE
                        "ch-3" -> result.category == WasteCategory.PAPER
                        "ch-4" -> result.category == WasteCategory.GLASS
                        "ch-5" -> true // any scan completes daily AI mission
                        "ch-6" -> result.category == WasteCategory.METAL
                        else -> false
                    }
                    if (shouldIncrement) {
                        val next = (ch.current + 1).coerceAtMost(ch.target)
                        ch.copy(current = next, isCompleted = next >= ch.target)
                    } else ch
                } else ch
            }
        }

        // Update badges dynamically
        _badges.update { list ->
            list.map { b ->
                val (curr, isUnlocked) = when (b.id) {
                    "b1" -> Pair(_totalItemsRecycled.value, _totalItemsRecycled.value >= 1)
                    "b2" -> Pair(_plasticCount.value, _plasticCount.value >= 15)
                    "b3" -> Pair(_paperCount.value, _paperCount.value >= 20)
                    "b4" -> Pair(_eWasteCount.value, _eWasteCount.value >= 3)
                    "b5" -> Pair(_userPoints.value, _userPoints.value >= 500)
                    "b6" -> Pair(_wasteDivertedKg.value.toInt(), _wasteDivertedKg.value >= 25f)
                    else -> Pair(b.progress, b.unlocked)
                }
                b.copy(progress = curr, unlocked = isUnlocked)
            }
        }
        persistUserData()
    }

    fun completeChallenge(challengeId: String) {
        _challenges.update { list ->
            list.map { ch ->
                if (ch.id == challengeId && !ch.isCompleted) {
                    _userPoints.update { it + ch.pointsReward }
                    _pointsThisWeek.update { it + ch.pointsReward }
                    ch.copy(current = ch.target, isCompleted = true)
                } else ch
            }
        }
        persistUserData()
    }

    fun likePost(postId: String) {
        _communityPosts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val newLiked = !post.isLiked
                    post.copy(
                        isLiked = newLiked,
                        likes = if (newLiked) post.likes + 1 else post.likes - 1
                    )
                } else post
            }
        }
    }

    fun addCommentToPost(postId: String, author: String, role: String, text: String) {
        _communityPosts.update { posts ->
            posts.map { post ->
                if (post.id == postId) {
                    val newComment = CommunityComment(
                        author = author,
                        role = role,
                        text = text
                    )
                    post.copy(comments = post.comments + newComment)
                } else {
                    post
                }
            }
        }
    }

    fun createCommunityPost(
        author: String,
        role: String,
        contentEn: String,
        contentBm: String,
        type: CommunityPostType,
        tag: String,
        impactBadge: String? = null
    ) {
        val newPost = CommunityPost(
            id = "post-${System.currentTimeMillis()}",
            author = author,
            role = role,
            timeAgo = "Just now",
            timeAgoBm = "Sebentar tadi",
            contentEn = contentEn,
            contentBm = contentBm,
            likes = 1,
            isLiked = true,
            tag = if (tag.startsWith("#")) tag else "#$tag",
            type = type,
            avatarEmoji = when (type) {
                CommunityPostType.CAMPAIGN -> "📢"
                CommunityPostType.LEADERBOARD -> "🏆"
                CommunityPostType.ACTIVITY -> "🌱"
                CommunityPostType.ALL -> "🌱"
            },
            impactBadge = impactBadge
        )
        _communityPosts.update { listOf(newPost) + it }
    }

    fun getLevelName(language: AppLanguage): String {
        val pts = _userPoints.value
        val isBm = language == AppLanguage.BM
        return when {
            pts >= 1000 -> if (isBm) "Pelindung Bumi" else "Planet Protector"
            pts >= 500 -> if (isBm) "Juara Kelestarian" else "Green Champion"
            pts >= 250 -> if (isBm) "Penjelajah Hijau" else "Eco Explorer"
            else -> if (isBm) "Pemula Hijau" else "Eco Beginner"
        }
    }

    fun getNextLevelTarget(): Pair<Int, Int> {
        val pts = _userPoints.value
        return when {
            pts >= 1000 -> Pair(pts, 1500)
            pts >= 500 -> Pair(pts, 1000)
            pts >= 250 -> Pair(pts, 500)
            else -> Pair(pts, 250)
        }
    }
}
