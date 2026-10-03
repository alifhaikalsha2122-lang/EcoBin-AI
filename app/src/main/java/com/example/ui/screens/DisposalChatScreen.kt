package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ChatMessage
import com.example.model.Screen
import com.example.model.WasteCategory
import com.example.ui.EcoViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DisposalChatScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.repository.language.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    val showHazardousAlert by viewModel.showHazardousAlert.collectAsState()
    val isBm = language == AppLanguage.BM

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = if (isBm) listOf(
        "Di mana patut saya buang bateri terpakai?",
        "Bagaimana lupus sisa cecair kimia pelarut?",
        "Bagaimana cara kitar semula botol plastik?",
        "Bolehkah bekas makanan berminyak dikitar?",
        "Cara selamat lupus lampu kalimantang merkuri?"
    ) else listOf(
        "Where should I throw dead lithium batteries?",
        "How to safely dispose chemical solvents?",
        "How do I recycle a PET plastic bottle?",
        "Can I recycle greasy food packaging?",
        "How to safely dispose mercury fluorescent bulbs?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("disposal_chat_screen")
    ) {
        // Chat Header Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EcoGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧠", fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBm) "Penolong Pelupusan Pintar AI" else "AI Disposal Assistant",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isBm) "Panduan Keselamatan & Kelestarian Gemini AI" else "Gemini AI Safety & Recycling Guide",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2)
                ) {
                    Text(
                        text = "⚠️ Safe Mode",
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Quick Suggestion Chips (Includes battery & chemical questions)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(quickQuestions) { question ->
                SuggestionChip(
                    onClick = {
                        inputText = question
                        viewModel.sendChatMessage(question)
                        inputText = ""
                    },
                    label = { Text(question, fontSize = 11.sp) }
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = EcoGreenPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBm) "ECOBIN AI sedang menganalisis sisa..." else "ECOBIN AI is evaluating disposal safety...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // Input Field & Send Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        if (isBm) "Tanya tentang sisa, bateri, atau bahan kimia..." else "Ask about waste, batteries, chemicals...",
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EcoGreenPrimary)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White
                )
            }
        }
    }

    // Hazardous Waste Alert Popup (Triggered when user inquires about battery or chemical)
    if (showHazardousAlert != null) {
        val triggerKeyword = showHazardousAlert!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissHazardousAlert() },
            modifier = Modifier.testTag("hazardous_waste_alert_dialog"),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hazard Alert",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (isBm) "⚠️ AMARAN SISA BERBAHAYA!" else "⚠️ HAZARDOUS WASTE ALERT!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBm)
                            "Sistem keselamatan ECOBIN AI mengesan rujukan kepada bahan berisiko: '$triggerKeyword'."
                        else
                            "ECOBIN AI safety layer detected high-risk waste terms: '$triggerKeyword'.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isBm)
                            "❌ JANGAN buang bateri, bahan kimia, atau pelarut ke dalam tong sampah biasa atau tong kitar semula umum! Bahan ini mengandungi asid kakisan, logam berat berbahaya (litium, plumbum, raksa) yang boleh meletup, terbakar, atau mencemarkan air tanah."
                        else
                            "❌ NEVER dispose of batteries, chemical containers, or toxic liquids into regular trash or standard recycling bins! They contain heavy metals (lithium, lead, mercury) and corrosive acids that cause severe fires and groundwater contamination.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isBm) "Langkah Keselamatan Wajib:" else "Mandatory Safety Protocol:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isBm)
                                    "1. Balut terminal bateri dengan pita pelekat.\n2. Kekalkan cecair kimia dalam botol asal tertutup.\n3. Hantar ke Pusat E-Sisa atau Kiosk Khas Sekolah."
                                else
                                    "1. Tape battery terminals to prevent short-circuit sparks.\n2. Keep chemicals in sealed original containers.\n3. Drop off at certified E-Waste or school Eco Hubs.",
                                fontSize = 10.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissHazardousAlert()
                        viewModel.setMapCategoryFilter(WasteCategory.HAZARDOUS)
                        viewModel.navigateTo(Screen.MAP)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("alert_find_ewaste_button")
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Find Drop-off", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBm) "Cari Kiosk E-Sisa" else "Find E-Waste Drop-off", fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissHazardousAlert() },
                    modifier = Modifier.testTag("alert_dismiss_button")
                ) {
                    Text(if (isBm) "Faham & Patuh" else "Understood", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (msg.isHazardAlert) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF2F2),
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .widthIn(max = 300.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hazard Alert",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "HAZARDOUS WASTE ALERT",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) EcoGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.text,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
            }
        }
    }
}
