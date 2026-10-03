package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ECOBIN AI", appName)
  }

  @Test
  fun `verify five bottom navigation destinations`() {
    val items = com.example.ui.components.EcoNavDestination.destinations
    assertEquals(5, items.size)
    assertEquals(com.example.model.Screen.HOME, items[0].screen)
    assertEquals(com.example.model.Screen.SCANNER, items[1].screen)
    assertEquals(com.example.model.Screen.MAP, items[2].screen)
    assertEquals(com.example.model.Screen.CHALLENGES, items[3].screen)
    assertEquals(com.example.model.Screen.PROFILE, items[4].screen)
  }

  @Test
  fun `verify simple state variable manages screen navigation without external routing`() {
    val viewModel = com.example.ui.EcoViewModel()
    // Initial screen is LOGIN before reaching the main screen (paparan utama)
    assertEquals(com.example.model.Screen.LOGIN, viewModel.currentScreen.value)

    viewModel.enterAppWithUsername("Aiman Hakim")
    assertEquals(com.example.model.Screen.HOME, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.SCANNER)
    assertEquals(com.example.model.Screen.SCANNER, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.MAP)
    assertEquals(com.example.model.Screen.MAP, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.CHALLENGES)
    assertEquals(com.example.model.Screen.CHALLENGES, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.PROFILE)
    assertEquals(com.example.model.Screen.PROFILE, viewModel.currentScreen.value)
  }

  @Test
  fun `verify username entry before main screen and guest fallback`() {
    val viewModel = com.example.ui.EcoViewModel()
    assertEquals(com.example.model.Screen.LOGIN, viewModel.currentScreen.value)

    // User submits username before main screen
    viewModel.enterAppWithUsername("Farhan Daniel", "SMK Cyberjaya", "5 Inovasi")
    assertEquals(com.example.model.Screen.HOME, viewModel.currentScreen.value)
    assertEquals("Farhan Daniel", viewModel.userName.value)
    assertEquals("SMK Cyberjaya", viewModel.userSchool.value)
    assertEquals("5 Inovasi", viewModel.userClass.value)
    assertTrue(viewModel.isLoggedIn.value)

    // Returning to login and choosing guest mode
    viewModel.navigateTo(com.example.model.Screen.LOGIN)
    assertEquals(com.example.model.Screen.LOGIN, viewModel.currentScreen.value)

    viewModel.continueAsGuest()
    assertEquals(com.example.model.Screen.HOME, viewModel.currentScreen.value)
    assertFalse(viewModel.isLoggedIn.value)
    assertTrue(viewModel.userName.value.contains("Tetamu") || viewModel.userName.value.contains("Guest"))
  }

  @Test
  fun `verify home dashboard metrics and quick action destinations`() {
    val viewModel = com.example.ui.EcoViewModel()
    assertTrue(viewModel.repository.userPoints.value >= 0)
    assertTrue(viewModel.repository.totalItemsRecycled.value >= 0)
    assertTrue(viewModel.repository.wasteDivertedKg.value >= 0.0)

    // Quick Actions routing verification
    viewModel.navigateTo(com.example.model.Screen.SCANNER)
    assertEquals(com.example.model.Screen.SCANNER, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.MAP)
    assertEquals(com.example.model.Screen.MAP, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.DISPOSAL_CHAT)
    assertEquals(com.example.model.Screen.DISPOSAL_CHAT, viewModel.currentScreen.value)

    viewModel.navigateTo(com.example.model.Screen.CHALLENGES)
    assertEquals(com.example.model.Screen.CHALLENGES, viewModel.currentScreen.value)
  }

  @Test
  fun `verify leaderboard student rankings, eco points, and badges`() {
    val viewModel = com.example.ui.EcoViewModel()
    val leaderboard = viewModel.repository.individualLeaderboard

    // Verify ranked students list with mock data
    assertTrue(leaderboard.size >= 5)
    assertEquals(1, leaderboard[0].rank)
    assertTrue(leaderboard[0].points >= leaderboard[1].points)
    assertTrue(leaderboard[0].badge.isNotBlank())
    assertTrue(leaderboard[0].name.isNotBlank())
    assertTrue(leaderboard[1].points >= leaderboard[2].points)

    // Verify badges are present
    assertTrue(leaderboard.all { it.badge.isNotEmpty() })
    assertTrue(leaderboard.all { it.points > 0 })
  }

  @Test
  fun `verify daily challenges progress and complete challenge earns eco points`() {
    val viewModel = com.example.ui.EcoViewModel()
    val initialPoints = viewModel.repository.userPoints.value
    val initialChallenges = viewModel.repository.challenges.value

    // Verify active challenges list
    assertTrue(initialChallenges.isNotEmpty())
    val uncompletedChallenge = initialChallenges.firstOrNull { !it.isCompleted }
    assertNotNull(uncompletedChallenge)

    // Complete challenge and verify simulated Eco Points earned
    val challengeId = uncompletedChallenge!!.id
    val reward = uncompletedChallenge.pointsReward
    viewModel.repository.completeChallenge(challengeId)

    // Assert points increased by challenge reward
    val updatedPoints = viewModel.repository.userPoints.value
    assertEquals(initialPoints + reward, updatedPoints)

    // Assert challenge is now marked completed
    val updatedChallenge = viewModel.repository.challenges.value.first { it.id == challengeId }
    assertTrue(updatedChallenge.isCompleted)
    assertEquals(updatedChallenge.target, updatedChallenge.current)
  }

  @Test
  fun `verify hazardous keywords trigger safety awareness alert`() {
    val viewModel = com.example.ui.EcoViewModel()

    // Verify logic layer checks
    assertTrue(viewModel.checkHazardousKeywords("How do I throw an old lithium battery?"))
    assertTrue(viewModel.checkHazardousKeywords("Bahaya sisa cecair kimia pelarut"))
    assertTrue(viewModel.checkHazardousKeywords("Car battery disposal"))
    assertFalse(viewModel.checkHazardousKeywords("How to recycle clean paper?"))

    // Verify alert trigger on chat message
    assertNull(viewModel.showHazardousAlert.value)
    viewModel.sendChatMessage("Where can I dispose of this broken chemical bottle?")

    assertNotNull(viewModel.showHazardousAlert.value)
    assertTrue(viewModel.showHazardousAlert.value!!.contains("chemical"))

    // Verify dismiss
    viewModel.dismissHazardousAlert()
    assertNull(viewModel.showHazardousAlert.value)
  }

  @Test
  fun `verify weekly and monthly recycling trends placeholder data`() {
    val viewModel = com.example.ui.EcoViewModel()
    val weekly = viewModel.repository.weeklyTrends
    val monthly = viewModel.repository.monthlyTrends

    // Verify weekly trend points
    assertEquals(7, weekly.size)
    assertEquals("Mon", weekly[0].label)
    assertEquals("Sun", weekly[6].label)
    assertTrue(weekly.all { it.count > 0 })

    // Verify monthly trend points
    assertTrue(monthly.size >= 6)
    assertTrue(monthly.any { it.label == "Sep" })
    assertTrue(monthly.all { it.count > 0 })
    val totalMonthly = monthly.sumOf { it.count }
    assertTrue(totalMonthly > 100)
  }

  @Test
  fun `verify profile settings toggle notifications and switch languages`() {
    val viewModel = com.example.ui.EcoViewModel()

    // Test notification toggle
    assertTrue(viewModel.notificationsEnabled.value)
    viewModel.toggleNotifications()
    assertFalse(viewModel.notificationsEnabled.value)
    viewModel.setNotificationsEnabled(true)
    assertTrue(viewModel.notificationsEnabled.value)

    // Test language switching between English and Bahasa Melayu
    viewModel.setLanguage(com.example.model.AppLanguage.EN)
    assertEquals(com.example.model.AppLanguage.EN, viewModel.repository.language.value)

    viewModel.setLanguage(com.example.model.AppLanguage.BM)
    assertEquals(com.example.model.AppLanguage.BM, viewModel.repository.language.value)

    viewModel.toggleLanguage()
    assertEquals(com.example.model.AppLanguage.EN, viewModel.repository.language.value)
  }

  @Test
  fun `verify google maps grounding for recycling centers`() {
    val viewModel = com.example.ui.EcoViewModel()
    val initialPlaces = viewModel.groundedMapsLocations.value

    // Verify places list is populated with real-world Google Maps grounded locations
    assertTrue(initialPlaces.isNotEmpty())
    val firstPlace = initialPlaces[0]
    assertTrue(firstPlace.name.isNotBlank())
    assertTrue(firstPlace.address.isNotBlank())
    assertTrue(firstPlace.mapsUrl.startsWith("https://www.google.com/maps/"))
    assertTrue(firstPlace.acceptedItems.isNotBlank())

    // Search query with Google Maps
    viewModel.searchRecyclingCentersWithGoogleMaps("Petaling Jaya")
    val searchResults = viewModel.groundedMapsLocations.value
    assertTrue(searchResults.isNotEmpty())
    assertTrue(searchResults.any { it.name.contains("PJ") || it.address.contains("Petaling Jaya") || it.name.contains("IPC") })
  }

  @Test
  fun `verify google maps compose marker clusters for recycling centers, bin collection points, and schools`() {
    val viewModel = com.example.ui.EcoViewModel()
    val allClusters = viewModel.mapClusterItems

    // 1. Verify clusters contains all 3 required types
    val recyclingCenters = allClusters.filter { it.pointType == com.example.model.ClusterPointType.RECYCLING_CENTER }
    val binPoints = allClusters.filter { it.pointType == com.example.model.ClusterPointType.BIN_COLLECTION }
    val schools = allClusters.filter { it.pointType == com.example.model.ClusterPointType.SCHOOL }

    assertTrue("Must have recycling centers", recyclingCenters.isNotEmpty())
    assertTrue("Must have bin collection points", binPoints.isNotEmpty())
    assertTrue("Must have schools", schools.isNotEmpty())

    // 2. Verify LatLng and details on cluster items
    for (item in allClusters) {
      assertNotNull(item.position)
      assertTrue(item.lat != 0.0)
      assertTrue(item.lng != 0.0)
      assertTrue(item.titleText.isNotBlank())
      assertTrue(item.snippetText.isNotBlank())
      assertTrue(item.acceptedCategories.isNotBlank())
    }

    // 3. Verify filter by cluster point type
    viewModel.setSelectedClusterType(com.example.model.ClusterPointType.SCHOOL)
    assertEquals(com.example.model.ClusterPointType.SCHOOL, viewModel.selectedClusterType.value)

    viewModel.setSelectedClusterType(null)
    assertNull(viewModel.selectedClusterType.value)

    // 4. Verify item selection
    viewModel.selectClusterItem(schools.first())
    assertEquals(schools.first().id, viewModel.selectedClusterItem.value?.id)
  }

  @Test
  fun `verify room database recycling history persistence and instructions revisiting`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.db.AppDatabase.createInMemoryDatabase(context)
    val dao = db.scannedItemDao()
    val repo = com.example.data.db.RecyclingHistoryRepository(dao)

    // 1. Insert a scanned item result
    val scanResult = com.example.model.WasteScanResult(
      itemName = "Plastic Mineral Water Bottle",
      category = com.example.model.WasteCategory.PLASTIC,
      material = "PET (Polyethylene Terephthalate)",
      recyclable = true,
      hazardous = false,
      confidence = 0.95f,
      instructions = listOf("Empty liquid", "Rinse clean", "Crush bottle", "Put in plastic bin"),
      instructionsBm = listOf("Kosongkan air", "Bilas bersih", "Kemikkan botol", "Masukkan ke tong plastik"),
      estimatedPoints = 15,
      estimatedWeightKg = 0.02f,
      carbonSavedKg = 0.05f
    )

    val insertedId = repo.saveScanResult(scanResult)
    assertTrue("Inserted ID should be valid", insertedId > 0)

    // 2. Fetch from DAO
    val entity = dao.getById(insertedId)
    assertNotNull(entity)
    assertEquals("Plastic Mineral Water Bottle", entity!!.itemName)
    assertEquals("PLASTIC", entity.categoryName)
    assertTrue(entity.recyclable)
    assertFalse(entity.hazardous)

    // 3. Verify converting back to WasteScanResult preserves instructions
    val reconstructed = entity.toWasteScanResult()
    assertEquals(scanResult.itemName, reconstructed.itemName)
    assertEquals(scanResult.category, reconstructed.category)
    assertEquals(4, reconstructed.instructions.size)
    assertEquals("Empty liquid", reconstructed.instructions[0])
    assertEquals("Kosongkan air", reconstructed.instructionsBm[0])

    // 4. Verify Flow collection from Repository
    val historyItems = repo.allHistory.first()
    assertEquals(1, historyItems.size)
    assertEquals("Plastic Mineral Water Bottle", historyItems[0].itemName)
    assertEquals("PLASTIC", historyItems[0].categoryName)

    // Revisit item in ViewModel
    val viewModel = com.example.ui.EcoViewModel()
    viewModel.loadFromHistory(entity)
    assertNotNull(viewModel.scanResult.value)
    assertEquals("Plastic Mineral Water Bottle", viewModel.scanResult.value!!.itemName)
    assertEquals(4, viewModel.scanResult.value!!.instructions.size)

    // 5. Delete item from repository
    repo.deleteItem(insertedId)
    val remaining = repo.allHistory.first()
    assertTrue(remaining.isEmpty())

    db.close()
  }

  @Test
  fun `verify community screen feed displays campaigns, leaderboard updates, and activities`() {
    val viewModel = com.example.ui.EcoViewModel()
    val initialPosts = viewModel.repository.communityPosts.value

    // 1. Verify feed contains all 3 required types
    val campaigns = initialPosts.filter { it.type == com.example.model.CommunityPostType.CAMPAIGN }
    val leaderboardUpdates = initialPosts.filter { it.type == com.example.model.CommunityPostType.LEADERBOARD }
    val activityPosts = initialPosts.filter { it.type == com.example.model.CommunityPostType.ACTIVITY }

    assertTrue("Feed must contain school recycling campaigns", campaigns.isNotEmpty())
    assertTrue("Feed must contain leaderboard updates", leaderboardUpdates.isNotEmpty())
    assertTrue("Feed must contain environmental activity posts", activityPosts.isNotEmpty())

    // 2. Verify campaign has target progress
    val firstCampaign = campaigns.first()
    assertNotNull(firstCampaign.campaignProgress)
    assertTrue(firstCampaign.campaignProgress!! > 0f)
    assertNotNull(firstCampaign.campaignTargetText)

    // 3. Verify leaderboard post has rank and points
    val firstLeaderboard = leaderboardUpdates.first()
    assertNotNull(firstLeaderboard.leaderboardRank)
    assertNotNull(firstLeaderboard.leaderboardPoints)

    // 4. Verify feed filter
    viewModel.setFeedTypeFilter(com.example.model.CommunityPostType.CAMPAIGN)
    assertEquals(com.example.model.CommunityPostType.CAMPAIGN, viewModel.selectedFeedType.value)

    viewModel.setFeedTypeFilter(com.example.model.CommunityPostType.ALL)
    assertEquals(com.example.model.CommunityPostType.ALL, viewModel.selectedFeedType.value)

    // 5. Verify social like interaction
    val testPost = initialPosts.first()
    val initialLikes = testPost.likes
    viewModel.repository.likePost(testPost.id)
    val updatedPost = viewModel.repository.communityPosts.value.first { it.id == testPost.id }
    assertEquals(initialLikes + 1, updatedPost.likes)
    assertTrue(updatedPost.isLiked)

    // 6. Verify adding a comment
    val initialCommentCount = testPost.comments.size
    viewModel.addPostComment(testPost.id, "Hebatnya inisiatif sekolah kita!")
    val commentedPost = viewModel.repository.communityPosts.value.first { it.id == testPost.id }
    assertEquals(initialCommentCount + 1, commentedPost.comments.size)
    assertEquals("Hebatnya inisiatif sekolah kita!", commentedPost.comments.last().text)

    // 7. Verify creating a new community post
    val totalBefore = viewModel.repository.communityPosts.value.size
    viewModel.publishCommunityPost(
      content = "Mengumpul 20 tin aluminium di kantin!",
      type = com.example.model.CommunityPostType.ACTIVITY,
      tag = "#WiraKantin"
    )
    val totalAfter = viewModel.repository.communityPosts.value.size
    assertEquals(totalBefore + 1, totalAfter)
    val newlyPublished = viewModel.repository.communityPosts.value.first()
    assertEquals("Mengumpul 20 tin aluminium di kantin!", newlyPublished.contentBm)
  }

  @Suppress("DEPRECATION")
  @Test
  fun `verify social sharing intent for earned badges and recycling milestones`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Test Badge Sharing Intent
    val testBadge = com.example.model.EcoBadge(
      id = "badge-master",
      titleEn = "Master Recycler",
      titleBm = "Pakar Kitar Semula",
      descEn = "Recycled over 100 items",
      descBm = "Kitar semula lebih 100 barangan",
      iconEmoji = "🥇",
      unlocked = true,
      progress = 100,
      max = 100
    )

    val badgeChooser = com.example.util.EcoShareUtils.createShareIntent(
      subject = "Lencana Eko Baharu: Pakar Kitar Semula!",
      text = "Saya baru sahaja membuka Lencana Eko di ECOBIN AI!"
    )
    assertNotNull(badgeChooser)
    assertEquals(android.content.Intent.ACTION_CHOOSER, badgeChooser.action)

    val targetIntent = badgeChooser.getParcelableExtra<android.content.Intent>(android.content.Intent.EXTRA_INTENT)
    assertNotNull(targetIntent)
    assertEquals(android.content.Intent.ACTION_SEND, targetIntent!!.action)
    assertEquals("text/plain", targetIntent.type)
    assertEquals("Lencana Eko Baharu: Pakar Kitar Semula!", targetIntent.getStringExtra(android.content.Intent.EXTRA_SUBJECT))
    assertTrue(targetIntent.getStringExtra(android.content.Intent.EXTRA_TEXT)!!.contains("ECOBIN AI"))

    // 2. Test Milestones Sharing Intent content
    val viewModel = com.example.ui.EcoViewModel()
    val badges = viewModel.repository.badges.value
    val unlockedCount = badges.count { it.unlocked }

    val milestoneChooser = com.example.util.EcoShareUtils.createShareIntent(
      subject = "Pencapaian Kitar Semula Saya di ECOBIN AI",
      text = """
        Pencapaian Kelestarian Kitar Semula:
        Nama: ${viewModel.repository.userName}
        Mata: ${viewModel.repository.userPoints.value} pts
        Lencana: $unlockedCount
        #ECOBIN #EcoHero
      """.trimIndent()
    )
    assertNotNull(milestoneChooser)
    val milestoneIntent = milestoneChooser.getParcelableExtra<android.content.Intent>(android.content.Intent.EXTRA_INTENT)
    assertNotNull(milestoneIntent)
    assertEquals(android.content.Intent.ACTION_SEND, milestoneIntent!!.action)
    val milestoneText = milestoneIntent.getStringExtra(android.content.Intent.EXTRA_TEXT)!!
    assertTrue(milestoneText.contains("ECOBIN"))
    assertTrue(milestoneText.contains(viewModel.repository.userName))

    // 3. Test launchShareIntent does not crash
    com.example.util.EcoShareUtils.shareBadge(context, testBadge, com.example.model.AppLanguage.BM)
    com.example.util.EcoShareUtils.shareRecyclingMilestones(
      context = context,
      userName = "Aiman Hakim",
      userSchool = "SMK Bintang Hijau",
      userClass = "5 Cemerlang",
      levelName = "Green Champion",
      points = 520,
      totalItems = 45,
      carbonSavedKg = 12.5f,
      unlockedBadgesCount = 3,
      language = com.example.model.AppLanguage.BM
    )
  }

  @Test
  fun `verify google maps robust fallback state and offline directory`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = com.example.ui.EcoViewModel()

    // 1. Initial Map Availability check
    viewModel.checkMapAvailability(context, forceRetry = true)
    
    // In test environment without active Maps API key, isMapAvailable must default to false
    // and mapConfigError must provide informative error feedback
    assertFalse(viewModel.isMapAvailable.value)
    assertNotNull(viewModel.mapConfigError.value)
    assertTrue(
      viewModel.mapConfigError.value!!.contains("Kunci") ||
      viewModel.mapConfigError.value!!.contains("Google") ||
      viewModel.mapConfigError.value!!.contains("Play Services")
    )

    // 2. Explicit state overrides for UI testing
    viewModel.setMapAvailableExplicitly(true)
    assertTrue(viewModel.isMapAvailable.value)
    assertNull(viewModel.mapConfigError.value)

    viewModel.setMapUnavailableExplicitly("Configuration error: Missing Google Maps API key")
    assertFalse(viewModel.isMapAvailable.value)
    assertEquals("Configuration error: Missing Google Maps API key", viewModel.mapConfigError.value)

    // 3. Verify Offline-Available Recycling Centers Directory
    val offlineCenters = viewModel.mapClusterItems
    assertTrue("Offline recycling centers list must not be empty", offlineCenters.isNotEmpty())
    assertEquals(12, offlineCenters.size)

    val firstCenter = offlineCenters.first { it.id == "rc-1" }
    assertEquals("IPC Recycling & Buy-Back Centre", firstCenter.titleText)
    assertTrue(firstCenter.details.contains("IPC Shopping Centre"))
    assertTrue(firstCenter.lat > 0.0)
    assertTrue(firstCenter.lng > 0.0)
    assertTrue(firstCenter.acceptedCategories.contains("Plastik") || firstCenter.acceptedCategories.contains("Plastic"))

    // 4. Verify school hub is in offline list
    val schoolHub = offlineCenters.first { it.pointType == com.example.model.ClusterPointType.SCHOOL }
    assertTrue(schoolHub.titleText.contains("SMK"))
    assertEquals(com.example.model.ClusterPointType.SCHOOL, schoolHub.pointType)

    // 5. Verify filter works offline
    viewModel.setSelectedClusterType(com.example.model.ClusterPointType.SCHOOL)
    assertEquals(com.example.model.ClusterPointType.SCHOOL, viewModel.selectedClusterType.value)
    viewModel.setSelectedClusterType(null)
    assertNull(viewModel.selectedClusterType.value)
  }

  @Test
  fun `verify change username and login logout authentication features`() {
    val viewModel = com.example.ui.EcoViewModel()

    // 1. Initial State
    assertTrue(viewModel.isLoggedIn.value)
    assertEquals("Aiman Hakim", viewModel.userName.value)
    assertEquals("Aiman Hakim", viewModel.repository.userName)
    assertEquals("Petaling Jaya, Selangor", viewModel.userLocation.value)
    assertEquals("Eksekutif Kelestarian", viewModel.userOccupation.value)
    assertEquals("020512-10-5432", viewModel.userIcNumber.value)
    assertEquals("+60 12-345 6789", viewModel.userPhoneNumber.value)

    // 2. Change Username
    viewModel.updateUsername("Danial Ashraf")
    assertEquals("Danial Ashraf", viewModel.userName.value)
    assertEquals("Danial Ashraf", viewModel.repository.userName)

    // 3. Update Full Profile (Name, Location, Occupation, IC, Phone)
    viewModel.updateProfile("Sarah Qistina", "Kuala Lumpur", "Jurutera Alam Sekitar", "010814-14-6542", "+60 13-987 6543")
    assertEquals("Sarah Qistina", viewModel.userName.value)
    assertEquals("Kuala Lumpur", viewModel.userLocation.value)
    assertEquals("Jurutera Alam Sekitar", viewModel.userOccupation.value)
    assertEquals("010814-14-6542", viewModel.userIcNumber.value)
    assertEquals("+60 13-987 6543", viewModel.userPhoneNumber.value)

    // 4. Logout (Guest Mode)
    viewModel.logout()
    assertFalse(viewModel.isLoggedIn.value)
    assertTrue(viewModel.userName.value.contains("Tetamu") || viewModel.userName.value.contains("Guest"))

    // 5. Login with Username & Location
    viewModel.login("Amir Danish", "Cyberjaya, Selangor", "Pengurus Projek Hijau", "990325-10-5123", "+60 19-456 7890")
    assertTrue(viewModel.isLoggedIn.value)
    assertEquals("Amir Danish", viewModel.userName.value)
    assertEquals("Cyberjaya, Selangor", viewModel.userLocation.value)
    assertEquals("Pengurus Projek Hijau", viewModel.userOccupation.value)
    assertEquals("990325-10-5123", viewModel.userIcNumber.value)
    assertEquals("+60 19-456 7890", viewModel.userPhoneNumber.value)

    // 6. Login with Email
    viewModel.login("nurul.izzah@gmail.com", "Shah Alam, Selangor", "Penyelidik Kelestarian", "000719-10-8888", "+60 17-222 3344")
    assertTrue(viewModel.isLoggedIn.value)
    assertEquals("Nurul Izzah", viewModel.userName.value)
    assertEquals("nurul.izzah@gmail.com", viewModel.userEmail.value)
    assertEquals("Shah Alam, Selangor", viewModel.userLocation.value)
    assertEquals("Penyelidik Kelestarian", viewModel.userOccupation.value)
    assertEquals("000719-10-8888", viewModel.userIcNumber.value)
    assertEquals("+60 17-222 3344", viewModel.userPhoneNumber.value)
  }

  @Test
  fun `verify camera UI waste item processing animation state`() {
    val viewModel = com.example.ui.EcoViewModel()
    assertFalse(viewModel.isAnalyzing.value)

    // Staging bitmap triggers ready state
    val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
    viewModel.setStagedImage(bitmap)
    assertNotNull(viewModel.currentScannedBitmap.value)

    // Verify analyze staged image updates state
    viewModel.analyzeStagedImage("Plastic Water Bottle")
    assertNotNull(viewModel.currentScannedBitmap.value)
  }

  @Test
  fun `verify login with google email and update profile email`() {
    val viewModel = com.example.ui.EcoViewModel()

    // Login with Google Email
    viewModel.loginWithGoogle("alifhaikalsha21202@gmail.com", "Alif Haikal")
    assertTrue(viewModel.isLoggedIn.value)
    assertEquals("alifhaikalsha21202@gmail.com", viewModel.userEmail.value)
    assertEquals("Alif Haikal", viewModel.userName.value)

    // Update Profile Email
    viewModel.updateProfile(
        name = "Alif Haikal Shah",
        location = "Petaling Jaya, Selangor",
        occupation = "Eksekutif Kelestarian",
        icNumber = "020512-10-5432",
        phoneNumber = "+60 12-345 6789",
        email = "alif.shah@ecobin.ai"
    )
    assertEquals("Alif Haikal Shah", viewModel.userName.value)
    assertEquals("alif.shah@ecobin.ai", viewModel.userEmail.value)
  }

  @Test
  fun `verify persistent storage and new user reward points start from zero`() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val viewModel = com.example.ui.EcoViewModel()
    viewModel.initDatabase(context)

    // Verify initial new user points is 0
    assertEquals(0, viewModel.repository.userPoints.value)
    assertEquals(0, viewModel.repository.totalItemsRecycled.value)

    // Save profile data
    viewModel.enterAppWithUsername(
        username = "Ahmad Zaki",
        location = "Bangi, Selangor",
        occupation = "Pelajar Sekolah",
        icNumber = "080102-10-1234",
        phoneNumber = "+60 11-2233 4455",
        email = "zaki@gmail.com"
    )

    assertEquals("Ahmad Zaki", viewModel.userName.value)
    assertEquals("zaki@gmail.com", viewModel.userEmail.value)
    assertEquals("Bangi, Selangor", viewModel.userLocation.value)
    assertEquals("Pelajar Sekolah", viewModel.userOccupation.value)
    assertEquals("080102-10-1234", viewModel.userIcNumber.value)
    assertEquals("+60 11-2233 4455", viewModel.userPhoneNumber.value)
    assertEquals(0, viewModel.repository.userPoints.value)

    // Add recycling activity (points start from 0 and increase)
    val scan = com.example.model.WasteScanResult(
        itemName = "Mineral Water Bottle",
        category = com.example.model.WasteCategory.PLASTIC,
        material = "PET Plastic",
        recyclable = true,
        hazardous = false,
        confidence = 0.95f,
        instructions = listOf("Rinse clean", "Crush bottle"),
        instructionsBm = listOf("Bilas bersih", "Kemekkan botol"),
        estimatedPoints = 15,
        estimatedWeightKg = 0.05f,
        carbonSavedKg = 0.08f
    )
    viewModel.repository.addRecyclingActivity(scan)
    assertEquals(15, viewModel.repository.userPoints.value)
    assertEquals(1, viewModel.repository.totalItemsRecycled.value)

    // Recreate repository with same context preferences to verify persistent save
    val newRepo = com.example.data.EcoRepository()
    newRepo.initPreferences(context)
    assertEquals("Ahmad Zaki", newRepo.userName)
    assertEquals("zaki@gmail.com", newRepo.userEmail.value)
    assertEquals("Bangi, Selangor", newRepo.userLocation)
    assertEquals(15, newRepo.userPoints.value)
    assertEquals(1, newRepo.totalItemsRecycled.value)
  }
}
