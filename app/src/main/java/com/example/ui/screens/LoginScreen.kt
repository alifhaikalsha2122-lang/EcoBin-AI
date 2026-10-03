package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.ui.EcoViewModel
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.repository.language.collectAsState()
    val isBm = language == AppLanguage.BM

    val currentName by viewModel.userName.collectAsState()
    val currentEmail by viewModel.userEmail.collectAsState()
    val currentLocation by viewModel.userLocation.collectAsState()
    val currentOccupation by viewModel.userOccupation.collectAsState()
    val currentIcNumber by viewModel.userIcNumber.collectAsState()
    val currentPhoneNumber by viewModel.userPhoneNumber.collectAsState()

    var usernameInput by remember { mutableStateOf(if (currentName.contains("Tetamu") || currentName.contains("Guest")) "" else currentName) }
    var emailInput by remember { mutableStateOf(currentEmail) }
    var locationInput by remember { mutableStateOf(currentLocation) }
    var occupationInput by remember { mutableStateOf(currentOccupation) }
    var icInput by remember { mutableStateOf(currentIcNumber) }
    var phoneInput by remember { mutableStateOf(currentPhoneNumber) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }
    var customGoogleEmailInput by remember { mutableStateOf("alifhaikalsha21202@gmail.com") }

    LaunchedEffect(Unit) {
        viewModel.initDatabase(context)
    }

    LaunchedEffect(currentName, currentEmail, currentLocation, currentOccupation, currentIcNumber, currentPhoneNumber) {
        if (usernameInput.isBlank() && !currentName.contains("Tetamu") && !currentName.contains("Guest")) {
            usernameInput = currentName
        }
        if (emailInput.isBlank() || emailInput == "aiman.hakim@gmail.com") {
            emailInput = currentEmail
        }
        if (locationInput.isBlank()) locationInput = currentLocation
        if (occupationInput.isBlank()) occupationInput = currentOccupation
        if (icInput.isBlank()) icInput = currentIcNumber
        if (phoneInput.isBlank()) phoneInput = currentPhoneNumber
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        EcoSurfaceVariantLight,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
            // Language Switcher Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "🌱 Inisiatif Komuniti Hijau & Kelestarian",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Language Switch Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { viewModel.toggleLanguage() }
                            .testTag("onboarding_language_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isBm) "🇲🇾 BM" else "🇬🇧 EN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Translate, contentDescription = "Toggle Language", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // App Brand Logo & Hero
            item {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "ECOBIN AI Official Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = EcoGreenPrimary)
                        .clip(RoundedCornerShape(26.dp))
                        .border(2.dp, Color(0xFF10B981), RoundedCornerShape(26.dp))
                        .testTag("app_brand_logo_image")
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "ECOBIN AI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EcoGreenPrimary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Pop-up Vibrant Badge for "Sistem Kitar Semula Pintar & Kelestarian Pelajar"
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = Color(0xFF10B981),
                            ambientColor = Color(0xFF059669)
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF047857), // Deep emerald
                                    Color(0xFF059669), // Vivid emerald green
                                    Color(0xFF10B981), // Bright glowing mint
                                    Color(0xFF0D9488)  // Tech teal
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFA7F3D0),
                                    Color.White,
                                    Color(0xFFA7F3D0)
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .testTag("hero_subtitle_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("✨", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isBm)
                                "Sistem Kitar Semula Pintar & Kelestarian Pelajar"
                            else
                                "Smart School Recycling & Student Sustainability",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("🌱", fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Username Entry Card Before Main Screen (User Request Core)
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EcoGreenPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBm) "Daftar Masuk Pengguna" else "User Sign In",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = if (isBm) "Sila lengkapkan profil anda sebelum ke paparan utama" else "Please complete your profile before entering the main dashboard",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // New User 0-Points Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_and_zero_points_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌱", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isBm)
                                        "Pengguna baharu: Mata ganjaran bermula dari 0 Eco Points!"
                                    else
                                        "New users: Reward points start from 0 Eco Points!",
                                    fontSize = 11.sp,
                                    color = EcoGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Quick Demo Profiles Chips
                        Text(
                            text = if (isBm) "Atau pilih profil pantas:" else "Or pick a quick profile:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SuggestionChip(
                                onClick = {
                                    usernameInput = "Aiman Hakim"
                                    emailInput = "aiman.hakim@gmail.com"
                                    locationInput = "Petaling Jaya, Selangor"
                                    occupationInput = "Eksekutif Kelestarian"
                                    icInput = "020512-10-5432"
                                    phoneInput = "+60 12-345 6789"
                                    errorMessage = null
                                },
                                label = { Text("Aiman (PJ)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("quick_user_aiman")
                            )
                            SuggestionChip(
                                onClick = {
                                    usernameInput = "Sarah Qistina"
                                    emailInput = "sarah.qistina@gmail.com"
                                    locationInput = "Kuala Lumpur"
                                    occupationInput = "Jurutera Alam Sekitar"
                                    icInput = "010814-14-6542"
                                    phoneInput = "+60 13-987 6543"
                                    errorMessage = null
                                },
                                label = { Text("Sarah (KL)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("quick_user_sarah")
                            )
                            SuggestionChip(
                                onClick = {
                                    usernameInput = "Amir Danish"
                                    emailInput = "amir.danish@gmail.com"
                                    locationInput = "Cyberjaya, Selangor"
                                    occupationInput = "Pengurus Projek Hijau"
                                    icInput = "990325-10-5123"
                                    phoneInput = "+60 19-456 7890"
                                    errorMessage = null
                                },
                                label = { Text("Amir (Cyber)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("quick_user_amir")
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // 1. Username Input Field
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = {
                                usernameInput = it
                                if (it.trim().length >= 2) errorMessage = null
                            },
                            label = { Text(if (isBm) "Nama Pengguna" else "Username") },
                            placeholder = { Text(if (isBm) "Contoh: Aiman Hakim" else "e.g. Aiman Hakim") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username", tint = EcoGreenPrimary) },
                            singleLine = true,
                            isError = errorMessage != null,
                            supportingText = {
                                if (errorMessage != null) {
                                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text(if (isBm) "Nama anda akan dipaparkan di paparan utama & profil" else "Your name will appear on the dashboard & profile")
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_username_input")
                        )

                        Spacer(Modifier.height(6.dp))

                        // 2. Alamat Emel Input Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(if (isBm) "Alamat Emel" else "Email Address") },
                            placeholder = { Text("Contoh: alifhaikalsha21202@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF0284C7)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_email_input")
                        )

                        Spacer(Modifier.height(6.dp))

                        // 3. Lokasi Anda Input Field
                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it },
                            label = { Text(if (isBm) "Lokasi Anda" else "Your Location") },
                            placeholder = { Text(if (isBm) "Contoh: Petaling Jaya, Selangor" else "e.g. Petaling Jaya, Selangor") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFFD97706)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_location_input")
                                .testTag("onboarding_school_input")
                        )

                        Spacer(Modifier.height(8.dp))

                        // 4. Pekerjaan Input Field
                        OutlinedTextField(
                            value = occupationInput,
                            onValueChange = { occupationInput = it },
                            label = { Text(if (isBm) "Pekerjaan" else "Occupation") },
                            placeholder = { Text(if (isBm) "Contoh: Eksekutif Kelestarian / Pelajar / Swasta" else "e.g. Sustainability Officer / Student") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = "Occupation", tint = Color(0xFF0284C7)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_occupation_input")
                                .testTag("onboarding_class_input")
                        )

                        Spacer(Modifier.height(8.dp))

                        // 5. Nombor Kad Pengenalan (No. IC) Input Field
                        OutlinedTextField(
                            value = icInput,
                            onValueChange = { icInput = it },
                            label = { Text(if (isBm) "Nombor Kad Pengenalan (No. IC)" else "Identification Card No. (IC)") },
                            placeholder = { Text("Contoh: 020512-10-5432") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "IC Number", tint = Color(0xFF8B5CF6)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_ic_input")
                        )

                        Spacer(Modifier.height(8.dp))

                        // 6. Nombor Telefon Input Field
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text(if (isBm) "Nombor Telefon" else "Phone Number") },
                            placeholder = { Text("Contoh: +60 12-345 6789") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Number", tint = EcoGreenPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_phone_input")
                        )

                        Spacer(Modifier.height(18.dp))

                        // Primary Action Button: Enter Main Screen
                        Button(
                            onClick = {
                                val trimmed = usernameInput.trim()
                                if (trimmed.length < 2) {
                                    errorMessage = if (isBm)
                                        "Sila masukkan nama pengguna sekurang-kurangnya 2 aksara"
                                    else
                                        "Please enter a username with at least 2 characters"
                                } else {
                                    val isReturningUser = viewModel.repository.hasExistingPoints(trimmed)
                                    viewModel.enterAppWithUsername(
                                        username = trimmed,
                                        location = locationInput.trim(),
                                        occupation = occupationInput.trim(),
                                        icNumber = icInput.trim(),
                                        phoneNumber = phoneInput.trim(),
                                        email = emailInput.trim()
                                    )
                                    val pointsMsg = if (isReturningUser) "" else if (isBm) " (Mata ganjaran: 0 Eco Points)" else " (Reward points: 0 Eco Points)"
                                    Toast.makeText(
                                        context,
                                        if (isBm) "Maklumat disimpan! Selamat datang, $trimmed$pointsMsg" else "Profile saved! Welcome, $trimmed$pointsMsg",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("enter_main_screen_button")
                        ) {
                            Text(
                                text = if (isBm) "Teruskan ke Paparan Utama ➔" else "Continue to Main Screen ➔",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Divider with text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isBm) " atau " else " or ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(14.dp))

                        // Google Email SSO Login Button
                        OutlinedButton(
                            onClick = { showGoogleSignInDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_sso_button")
                        ) {
                            Text("🇬", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBm) "Log Masuk dengan Google Email" else "Sign in with Google Email",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Guest Mode Button
                        TextButton(
                            onClick = {
                                viewModel.continueAsGuest()
                                Toast.makeText(
                                    context,
                                    if (isBm) "Masuk sebagai Tetamu" else "Continuing as Guest",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guest_login_button")
                        ) {
                            Text(
                                text = if (isBm) "Masuk Sebagai Tetamu (Tanpa Log Masuk)" else "Enter as Guest (No Login)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Google Email Sign-In Dialog
        if (showGoogleSignInDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleSignInDialog = false },
                modifier = Modifier.testTag("google_sso_dialog"),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🇬", fontSize = 28.sp)
                    }
                },
                title = {
                    Text(
                        text = if (isBm) "Log Masuk dengan Google Email" else "Sign In with Google Email",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isBm)
                                "Pilih akaun Google aktif anda atau masukkan mana-mana Google Email untuk meneruskan ke ECOBIN AI:"
                            else
                                "Select your active Google account or enter any Google Email to continue to ECOBIN AI:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Active Google Account Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loginWithGoogle(
                                        googleEmail = "alifhaikalsha21202@gmail.com",
                                        name = "Alif Haikal"
                                    )
                                    showGoogleSignInDialog = false
                                    Toast.makeText(
                                        context,
                                        if (isBm) "Log masuk Google (alifhaikalsha21202@gmail.com) berjaya!" else "Signed in with Google (alifhaikalsha21202@gmail.com)!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("active_google_account_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4285F4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Alif Haikal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = EcoGreenPrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (isBm) "Akaun Anda" else "Your Account",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EcoGreenPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "alifhaikalsha21202@gmail.com",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("➔", fontSize = 14.sp, color = EcoGreenPrimary)
                            }
                        }

                        // Divider with label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isBm) " atau Google Email lain " else " or other Google Email ",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        // Custom Google Email Input Field
                        OutlinedTextField(
                            value = customGoogleEmailInput,
                            onValueChange = { customGoogleEmailInput = it },
                            label = { Text(if (isBm) "Google Email Pilihan" else "Preferred Google Email") },
                            placeholder = { Text("nama@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Google Email", tint = Color(0xFF4285F4)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_email_custom_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val emailToUse = customGoogleEmailInput.trim()
                            if (emailToUse.contains("@") && emailToUse.contains(".")) {
                                viewModel.loginWithGoogle(emailToUse)
                                showGoogleSignInDialog = false
                                Toast.makeText(
                                    context,
                                    if (isBm) "Log masuk Google ($emailToUse) berjaya!" else "Signed in with Google ($emailToUse)!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isBm) "Sila masukkan Google Email yang sah" else "Please enter a valid Google Email",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_google_email_button")
                    ) {
                        Text(if (isBm) "Log Masuk Email Ini" else "Sign In with This Email")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showGoogleSignInDialog = false },
                        modifier = Modifier.testTag("cancel_google_dialog_button")
                    ) {
                        Text(if (isBm) "Batal" else "Cancel")
                    }
                }
            )
        }
    }
}
