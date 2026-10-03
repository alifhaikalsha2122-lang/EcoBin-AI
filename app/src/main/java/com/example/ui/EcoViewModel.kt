package com.example.ui

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EcoRepository
import com.example.data.GeminiService
import com.example.data.MlKitWasteClassifier
import com.example.data.WasteDetectionResult
import com.example.data.db.AppDatabase
import com.example.data.db.RecyclingHistoryRepository
import com.example.data.db.ScannedItemEntity
import com.example.model.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.MapsInitializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EcoViewModel(
    val repository: EcoRepository = EcoRepository(),
    initialScreen: Screen = Screen.LOGIN
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(initialScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Room Database Recycling History
    private var _historyRepository: RecyclingHistoryRepository? = null

    private val _scanHistory = MutableStateFlow<List<ScannedItemEntity>>(emptyList())
    val scanHistory: StateFlow<List<ScannedItemEntity>> = _scanHistory.asStateFlow()

    // Community Feed Filter & Interaction State
    private val _selectedFeedType = MutableStateFlow(CommunityPostType.ALL)
    val selectedFeedType: StateFlow<CommunityPostType> = _selectedFeedType.asStateFlow()

    // User Profile & Authentication State
    val userName: StateFlow<String> = repository.userNameFlow
    val userEmail: StateFlow<String> = repository.userEmail
    val userLocation: StateFlow<String> = repository.userLocationFlow
    val userOccupation: StateFlow<String> = repository.userOccupationFlow
    val userIcNumber: StateFlow<String> = repository.userIcNumberFlow
    val userPhoneNumber: StateFlow<String> = repository.userPhoneNumberFlow
    val userProfileImage: StateFlow<String> = repository.userProfileImage

    fun updateProfileImage(newImage: String) {
        repository.updateProfileImage(newImage)
    }

    // Backward-compatible aliases for userSchool and userClass
    val userSchool: StateFlow<String> get() = userLocation
    val userClass: StateFlow<String> get() = userOccupation
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setLanguage(lang: AppLanguage) {
        repository.setLanguage(lang)
    }

    fun toggleLanguage() {
        repository.toggleLanguage()
    }

    fun updateUsername(newName: String) {
        repository.updateUsername(newName)
    }

    fun updateProfile(
        name: String,
        location: String,
        occupation: String,
        icNumber: String = repository.userIcNumber,
        phoneNumber: String = repository.userPhoneNumber,
        email: String = repository.userEmail.value
    ) {
        repository.updateProfile(name, location, occupation, icNumber, phoneNumber, email)
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
        repository.login(usernameOrEmail, location, occupation, icNumber, phoneNumber, email, resetPointsForNewUser)
    }

    fun loginWithGoogle(
        googleEmail: String,
        name: String? = null,
        location: String = "Petaling Jaya, Selangor",
        occupation: String = "Eksekutif Kelestarian",
        icNumber: String = "020512-10-5432",
        phoneNumber: String = "+60 12-345 6789"
    ) {
        repository.loginWithGoogleEmail(googleEmail, name, location, occupation, icNumber, phoneNumber)
        navigateTo(Screen.HOME)
    }

    fun enterAppWithUsername(
        username: String,
        location: String = "Petaling Jaya, Selangor",
        occupation: String = "Eksekutif Kelestarian",
        icNumber: String = "020512-10-5432",
        phoneNumber: String = "+60 12-345 6789",
        email: String? = null
    ) {
        val hasExisting = repository.hasExistingPoints(username)
        login(username, location, occupation, icNumber, phoneNumber, email, resetPointsForNewUser = !hasExisting)
        navigateTo(Screen.HOME)
    }

    fun continueAsGuest() {
        logout()
        navigateTo(Screen.HOME)
    }

    fun logout() {
        repository.logout()
    }

    fun setFeedTypeFilter(type: CommunityPostType) {
        _selectedFeedType.value = type
    }

    fun addPostComment(postId: String, text: String) {
        if (text.isBlank()) return
        repository.addCommentToPost(postId, "${userName.value} (You)", userOccupation.value, text.trim())
    }

    fun publishCommunityPost(content: String, type: CommunityPostType, tag: String) {
        if (content.isBlank()) return
        repository.createCommunityPost(
            author = "${userName.value} (You)",
            role = userOccupation.value,
            contentEn = content,
            contentBm = content,
            type = type,
            tag = tag,
            impactBadge = "+15 pts"
        )
    }

    fun initDatabase(context: Context) {
        repository.initPreferences(context)
        if (_historyRepository == null) {
            val db = AppDatabase.getDatabase(context)
            val repo = RecyclingHistoryRepository(db.scannedItemDao())
            setHistoryRepository(repo)
        }
    }

    fun setHistoryRepository(repo: RecyclingHistoryRepository) {
        _historyRepository = repo
        viewModelScope.launch {
            repo.allHistory.collect { list ->
                _scanHistory.value = list
            }
        }
    }

    fun saveScanToHistory(result: WasteScanResult) {
        viewModelScope.launch {
            _historyRepository?.saveScanResult(result)
            if (_historyRepository == null) {
                val entity = ScannedItemEntity(
                    id = System.currentTimeMillis(),
                    itemName = result.itemName,
                    categoryName = result.category.name,
                    material = result.material,
                    recyclable = result.recyclable,
                    hazardous = result.hazardous,
                    confidence = result.confidence,
                    instructionsRaw = result.instructions.joinToString("\n"),
                    instructionsBmRaw = result.instructionsBm.joinToString("\n"),
                    estimatedPoints = result.estimatedPoints,
                    estimatedWeightKg = result.estimatedWeightKg,
                    carbonSavedKg = result.carbonSavedKg
                )
                _scanHistory.update { listOf(entity) + it.filter { old -> old.itemName != entity.itemName } }
            }
        }
    }

    fun loadFromHistory(item: ScannedItemEntity) {
        _scanResult.value = item.toWasteScanResult()
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            _historyRepository?.deleteItem(id)
            _scanHistory.update { it.filter { item -> item.id != id } }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            _historyRepository?.clearHistory()
            _scanHistory.value = emptyList()
        }
    }

    // Scanner state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _scanResult = MutableStateFlow<WasteScanResult?>(null)
    val scanResult: StateFlow<WasteScanResult?> = _scanResult.asStateFlow()

    private val _currentScannedBitmap = MutableStateFlow<Bitmap?>(null)
    val currentScannedBitmap: StateFlow<Bitmap?> = _currentScannedBitmap.asStateFlow()

    fun setStagedImage(bitmap: Bitmap?) {
        _currentScannedBitmap.value = bitmap
        _scanResult.value = null
    }

    fun handleMlKitDetectionResult(detection: WasteDetectionResult) {
        _scanResult.value = detection.scanResult
        saveScanToHistory(detection.scanResult)
    }

    fun analyzeStagedImage(presetHint: String? = null) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            delay(500)
            val bmp = _currentScannedBitmap.value
            val lang = repository.language.value
            val result = if (bmp != null) {
                // First attempt on-device Google ML Kit image labeling
                try {
                    val mlKitResult = MlKitWasteClassifier.analyzeBitmap(bmp)
                    val labelHint = (mlKitResult.rawLabels + listOfNotNull(presetHint)).joinToString(", ")
                    GeminiService.analyzeWasteImage(bmp, labelHint, lang)
                } catch (e: Exception) {
                    GeminiService.analyzeWasteImage(bmp, presetHint, lang)
                }
            } else {
                GeminiService.getSmartFallbackResult(presetHint ?: "Plastic Water Bottle")
            }
            _scanResult.value = result
            saveScanToHistory(result)
            _isAnalyzing.value = false
        }
    }

    // Chatbot state
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "m-init",
                isUser = false,
                text = "🌱 Hai! Saya Penolong Pintar Pelupusan ECOBIN AI. Tanyakan apa sahaja tentang cara kitar semula barangan, mengasingkan sisa berbahaya atau mencari tong yang sesuai!",
                timestamp = "Just now"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Safety Awareness: Hazardous Waste Alert logic layer state
    private val _showHazardousAlert = MutableStateFlow<String?>(null)
    val showHazardousAlert: StateFlow<String?> = _showHazardousAlert.asStateFlow()

    fun dismissHazardousAlert() {
        _showHazardousAlert.value = null
    }

    fun triggerHazardousAlert(reason: String) {
        _showHazardousAlert.value = reason
    }

    fun checkHazardousKeywords(input: String): Boolean {
        val lower = input.lowercase()
        val hazardousKeywords = listOf(
            "battery", "batteries", "bateri",
            "chemical", "chemicals", "kimia",
            "acid", "asid", "paint", "cat",
            "mercury", "merkuri", "pesticide", "racun",
            "bleach", "peluntur", "toxic", "toksik",
            "corrosive", "flammable", "solvent", "pelarut"
        )
        return hazardousKeywords.any { lower.contains(it) }
    }

    // Map filters & selection
    private val _selectedMapCategory = MutableStateFlow<WasteCategory?>(null)
    val selectedMapCategory: StateFlow<WasteCategory?> = _selectedMapCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow<RecyclingLocation?>(repository.locations.firstOrNull())
    val selectedLocation: StateFlow<RecyclingLocation?> = _selectedLocation.asStateFlow()

    // Google Maps Compose Marker Clusters State
    val mapClusterItems: List<EcoMapClusterItem> = repository.mapClusterItems

    private val _selectedClusterType = MutableStateFlow<ClusterPointType?>(null)
    val selectedClusterType: StateFlow<ClusterPointType?> = _selectedClusterType.asStateFlow()

    private val _selectedClusterItem = MutableStateFlow<EcoMapClusterItem?>(repository.mapClusterItems.firstOrNull())
    val selectedClusterItem: StateFlow<EcoMapClusterItem?> = _selectedClusterItem.asStateFlow()

    fun setSelectedClusterType(type: ClusterPointType?) {
        _selectedClusterType.value = type
    }

    fun selectClusterItem(item: EcoMapClusterItem?) {
        _selectedClusterItem.value = item
    }

    // Google Maps Grounding State (gemini-3.5-flash with googleMaps tool)
    private val _groundedMapsLocations = MutableStateFlow<List<GroundedMapsLocation>>(
        GeminiService.getGroundedRecyclingLocationsFallback("", repository.language.value).second
    )
    val groundedMapsLocations: StateFlow<List<GroundedMapsLocation>> = _groundedMapsLocations.asStateFlow()

    private val _isSearchingMaps = MutableStateFlow(false)
    val isSearchingMaps: StateFlow<Boolean> = _isSearchingMaps.asStateFlow()

    private val _groundedSummaryText = MutableStateFlow<String?>(null)
    val groundedSummaryText: StateFlow<String?> = _groundedSummaryText.asStateFlow()

    fun searchRecyclingCentersWithGoogleMaps(query: String) {
        viewModelScope.launch {
            _isSearchingMaps.value = true
            val (summary, places) = GeminiService.findRecyclingCentersWithGoogleMaps(
                query = query,
                locationContext = "Malaysia",
                language = repository.language.value
            )
            _groundedSummaryText.value = summary
            _groundedMapsLocations.value = places
            _isSearchingMaps.value = false
        }
    }

    // Google Maps Robust Availability & Fallback State
    private val _isMapAvailable = MutableStateFlow(false)
    val isMapAvailable: StateFlow<Boolean> = _isMapAvailable.asStateFlow()

    private val _mapConfigError = MutableStateFlow<String?>(
        "Peta Google Maps memerlukan Google Play Services aktif dan Konfigurasi Kunci API yang sah pada peranti ini."
    )
    val mapConfigError: StateFlow<String?> = _mapConfigError.asStateFlow()

    private val _isCheckingMap = MutableStateFlow(false)
    val isCheckingMap: StateFlow<Boolean> = _isCheckingMap.asStateFlow()

    fun checkMapAvailability(context: Context, forceRetry: Boolean = false) {
        viewModelScope.launch {
            _isCheckingMap.value = true
            try {
                // 1. Check Google Play Services
                val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                if (availability != ConnectionResult.SUCCESS) {
                    _isMapAvailable.value = false
                    val lang = repository.language.value
                    _mapConfigError.value = if (lang == AppLanguage.BM) {
                        "Google Play Services tidak tersedia atau perlu dikemas kini pada peranti ini (Kod: $availability). Mod luar talian diaktifkan."
                    } else {
                        "Google Play Services is unavailable or outdated on this device (Code: $availability). Offline mode active."
                    }
                    _isCheckingMap.value = false
                    return@launch
                }

                // 2. Check Android Manifest API Key
                val appInfo = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
                if (apiKey.isNullOrBlank() || apiKey.contains("Placeholder") || apiKey == "DEFAULT_KEY") {
                    _isMapAvailable.value = false
                    val lang = repository.language.value
                    _mapConfigError.value = if (lang == AppLanguage.BM) {
                        "Kunci Google Maps API belum dikonfigurasikan atau menggunakan pemegang tempat lalai. Menggunakan senarai pusat kitar semula luar talian."
                    } else {
                        "Google Maps API Key is not configured or using default placeholder. Using offline recycling centers directory."
                    }
                    _isCheckingMap.value = false
                    return@launch
                }

                // 3. Test MapsInitializer
                MapsInitializer.initialize(context)
                _isMapAvailable.value = true
                _mapConfigError.value = null
            } catch (e: Throwable) {
                _isMapAvailable.value = false
                val lang = repository.language.value
                _mapConfigError.value = if (lang == AppLanguage.BM) {
                    "Ralat permulaan peta: ${e.localizedMessage ?: "Konfigurasi peta tidak sah"}"
                } else {
                    "Map initialization error: ${e.localizedMessage ?: "Invalid map configuration"}"
                }
            } finally {
                _isCheckingMap.value = false
            }
        }
    }

    fun setMapUnavailableExplicitly(errorMsg: String) {
        _isMapAvailable.value = false
        _mapConfigError.value = errorMsg
    }

    fun setMapAvailableExplicitly(available: Boolean) {
        _isMapAvailable.value = available
        if (available) _mapConfigError.value = null
    }

    // Proof Upload State
    private val _isVerifyingProof = MutableStateFlow(false)
    val isVerifyingProof: StateFlow<Boolean> = _isVerifyingProof.asStateFlow()

    private val _proofAwardedPoints = MutableStateFlow<Int?>(null)
    val proofAwardedPoints: StateFlow<Int?> = _proofAwardedPoints.asStateFlow()

    // Carbon Calculator
    private val _calculatorCategory = MutableStateFlow(WasteCategory.PLASTIC)
    val calculatorCategory: StateFlow<WasteCategory> = _calculatorCategory.asStateFlow()

    private val _calculatorUnits = MutableStateFlow(10)
    val calculatorUnits: StateFlow<Int> = _calculatorUnits.asStateFlow()

    // Presentation Demo Step (0 to 6)
    private val _demoStep = MutableStateFlow(0)
    val demoStep: StateFlow<Int> = _demoStep.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled

    fun toggleNotifications() {
        repository.toggleNotifications()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }

    fun setMapCategoryFilter(category: WasteCategory?) {
        _selectedMapCategory.value = category
    }

    fun selectLocation(loc: RecyclingLocation) {
        _selectedLocation.value = loc
    }

    fun setCalculatorCategory(cat: WasteCategory) {
        _calculatorCategory.value = cat
    }

    fun setCalculatorUnits(units: Int) {
        _calculatorUnits.value = units.coerceIn(1, 500)
    }

    fun scanPreset(itemName: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _currentScannedBitmap.value = null
            delay(650) // Realistic scanning animation
            val result = GeminiService.getSmartFallbackResult(itemName)
            _scanResult.value = result
            saveScanToHistory(result)
            _isAnalyzing.value = false
        }
    }

    fun scanImage(bitmap: Bitmap?, hint: String? = null) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _currentScannedBitmap.value = bitmap
            val lang = repository.language.value
            val result = GeminiService.analyzeWasteImage(bitmap, hint, lang)
            _scanResult.value = result
            saveScanToHistory(result)
            _isAnalyzing.value = false
        }
    }

    fun clearScan() {
        _scanResult.value = null
        _currentScannedBitmap.value = null
    }

    fun recycleScannedItem() {
        val result = _scanResult.value ?: return
        repository.addRecyclingActivity(result)
        navigateTo(Screen.IMPACT)
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        // Safety Awareness Logic Layer: check for battery, chemical, or toxic keywords
        if (checkHazardousKeywords(text)) {
            _showHazardousAlert.value = text
        }

        val userMsg = ChatMessage(
            id = "user-${System.currentTimeMillis()}",
            isUser = true,
            text = text,
            timestamp = "Now"
        )
        _chatMessages.update { it + userMsg }
        _isChatLoading.value = true

        viewModelScope.launch {
            val (answer, isHazard) = GeminiService.askDisposalAssistant(text, repository.language.value)
            val botMsg = ChatMessage(
                id = "bot-${System.currentTimeMillis()}",
                isUser = false,
                text = answer,
                isHazardAlert = isHazard,
                timestamp = "Now"
            )
            _chatMessages.update { it + botMsg }
            _isChatLoading.value = false
        }
    }

    fun submitProofUpload() {
        viewModelScope.launch {
            _isVerifyingProof.value = true
            _proofAwardedPoints.value = null
            delay(1200) // Simulated AI verification
            _isVerifyingProof.value = false
            _proofAwardedPoints.value = 15

            val proofResult = WasteScanResult(
                itemName = "Recycling Proof Submission",
                category = WasteCategory.PLASTIC,
                material = "Verified Material",
                recyclable = true,
                hazardous = false,
                confidence = 0.95f,
                instructions = listOf("Activity successfully verified by ECOBIN AI!"),
                instructionsBm = listOf("Aktiviti berjaya disahkan oleh ECOBIN AI!"),
                estimatedPoints = 15,
                estimatedWeightKg = 0.15f,
                carbonSavedKg = 0.22f
            )
            repository.addRecyclingActivity(proofResult)
        }
    }

    fun setDemoStep(step: Int) {
        _demoStep.value = step.coerceIn(0, 6)
    }
}
