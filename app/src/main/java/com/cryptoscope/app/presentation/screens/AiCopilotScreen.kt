package com.cryptoscope.app.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptoscope.app.core.agentic.AgenticEngine
import com.cryptoscope.app.core.agentic.MissionReport
import com.cryptoscope.app.core.agentic.MissionStep
import com.cryptoscope.app.core.database.AppDatabase
import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.core.database.SmartWalletEntity
import com.cryptoscope.app.core.database.TrapLogEntity
import com.cryptoscope.app.core.llm.ChatMessage
import com.cryptoscope.app.core.llm.CustomLlmClient
import com.cryptoscope.app.core.llm.LlmConfig
import com.cryptoscope.app.core.llm.LlmConfigManager
import com.cryptoscope.app.presentation.theme.CryptoGold
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoRed
import com.cryptoscope.app.presentation.theme.DarkBackground
import com.cryptoscope.app.presentation.theme.DarkCard
import com.cryptoscope.app.presentation.theme.DarkSurface
import com.cryptoscope.app.presentation.theme.TextPrimary
import com.cryptoscope.app.presentation.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AiCopilotScreen(
    database: AppDatabase,
    followedWallets: List<SmartWalletEntity>,
    openTrades: List<CopyTradeEntity>,
    trapLogs: List<TrapLogEntity>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var selectedMode by remember { mutableStateOf("AUTOPILOT") } // "AUTOPILOT" or "CHAT"
    var config by remember { mutableStateOf(LlmConfigManager.getConfig(context)) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Chat State
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                role = "assistant",
                content = "Halo! Saya adalah CryptoScope Agentic AI Copilot terhubung ke Custom LLM Anda.\n\n" +
                        "Dalam Mode Otonom, Anda cukup memberi satu perintah, dan saya akan memindai on-chain, mengaudit jebakan honeypot, lalu mengeksekusi trade secara otomatis.\n\n" +
                        "Pilih 'Agentic Autopilot' di atas untuk eksekusi sekali perintah, atau gunakan tab 'Chat' untuk konsultasi."
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }
    val chatListState = rememberLazyListState()

    // Agentic Autopilot State
    var isMissionRunning by remember { mutableStateOf(false) }
    var currentMissionSteps by remember { mutableStateOf<List<MissionStep>>(emptyList()) }
    var lastMissionReport by remember { mutableStateOf<MissionReport?>(null) }
    var customCommandText by remember { mutableStateOf("") }

    fun runMission(command: String) {
        if (isMissionRunning) return
        isMissionRunning = true
        currentMissionSteps = emptyList()
        lastMissionReport = null

        coroutineScope.launch {
            val stepsList = mutableListOf<MissionStep>()
            val report = AgenticEngine.executeAutonomousMission(
                command = command,
                database = database,
                llmConfig = config,
                onStepUpdate = { step ->
                    stepsList.removeAll { it.stepIndex == step.stepIndex }
                    stepsList.add(step)
                    currentMissionSteps = stepsList.sortedBy { it.stepIndex }
                }
            )
            isMissionRunning = false
            lastMissionReport = report
            Toast.makeText(context, "Misi Otonom Berhasil Dieksekusi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank() || isChatLoading) return
        val text = userText.trim()
        messages.add(ChatMessage(role = "user", content = text))
        inputText = ""
        isChatLoading = true

        coroutineScope.launch {
            val history = messages.takeLast(10)
            val result = CustomLlmClient.sendChat(history, config)
            isChatLoading = false
            result.onSuccess { reply ->
                messages.add(ChatMessage(role = "assistant", content = reply))
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Terjadi kesalahan saat menghubungi LLM."
                messages.add(ChatMessage(role = "assistant", content = "⚠️ Gagal mendapatkan respon dari Custom LLM:\n$errorMsg\n\nSilakan cek pengaturan URL endpoint, API key, atau pastikan server LLM aktif."))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Top Bar: Model Status & Settings Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CryptoGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("AGENTIC AI COPILOT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (config.endpoint.isNotBlank()) CryptoGreen else Color.Gray, CircleShape)
                            )
                        }
                        Text(
                            text = "${config.model} • ${config.endpoint.take(24)}...",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showSettingsDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoGreen),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Config", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 2. Mode Selector Tabs: "Agentic Autopilot" vs "Chat"
        TabRow(
            selectedTabIndex = if (selectedMode == "AUTOPILOT") 0 else 1,
            containerColor = DarkSurface,
            contentColor = CryptoGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedMode == "AUTOPILOT") 0 else 1]),
                    color = CryptoGreen
                )
            }
        ) {
            Tab(
                selected = selectedMode == "AUTOPILOT",
                onClick = { selectedMode = "AUTOPILOT" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("AGENTIC AUTOPILOT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedMode == "CHAT",
                onClick = { selectedMode = "CHAT" },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("CHAT ASSISTANT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        if (selectedMode == "AUTOPILOT") {
            // ==================== MODE AUTOPILOT ====================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CryptoGold)
                                Text("Eksekusi Otonom Sekali Perintah", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Agentic AI berjalan secara mandiri: memindai token smart wallet, menjalankan audit 7-layer Anti-Trap untuk mendeteksi honeypot, menghitung strategi via Custom LLM, dan mengeksekusi posisi di portofolio.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Quick 1-Click Action Buttons
                item {
                    Text("PILIHAN MISI SEKALI PERINTAH:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                runMission("Scan smart wallet terbaik, filter honeypot dan rugpull, eksekusi 1 posisi trading paling potensial sekarang.")
                            },
                            enabled = !isMissionRunning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🚀 Eksekusi Otonom: Cari & Buka Posisi Terbaik", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                runMission("Audit keamanan menyeluruh: periksa honeypot, likuiditas palsu, dan amankan modal portofolio.")
                            },
                            enabled = !isMissionRunning,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoGold)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CryptoGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🛡️ Eksekusi Otonom: Audit Keamanan & Netralkan Scam", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Custom Command Input
                item {
                    Text("ATAU BERIKAN PERINTAH KUSTOM:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCommandText,
                            onValueChange = { customCommandText = it },
                            placeholder = { Text("Contoh: Cari coin SOL aman, cek RSI, beli $500...", color = TextSecondary, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoGreen,
                                unfocusedBorderColor = Color(0xFF2B3648),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customCommandText.isNotBlank()) {
                                    runMission(customCommandText)
                                    customCommandText = ""
                                }
                            },
                            enabled = customCommandText.isNotBlank() && !isMissionRunning,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                        ) {
                            Text("Mulai", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Live Mission Stepper
                if (isMissionRunning || currentMissionSteps.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PROGRES EKSEKUSI OTONOM", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (isMissionRunning) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CryptoGreen, strokeWidth = 2.dp)
                                    } else {
                                        Text("SELESAI", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                currentMissionSteps.forEach { step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            if (step.isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (step.isWarning) CryptoGold else CryptoGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(step.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(step.detail, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Final Mission Report Debrief
                lastMissionReport?.let { report ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14202E))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = CryptoGreen)
                                    Text("LAPORAN DEBRIEF EKSEKUSI OTONOM", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Text(
                                    text = "🎯 Tindakan: ${report.actionTaken}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                report.blockedTrap?.let { trap ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CryptoRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("⚠️ JEBAKAN BERHASIL DIBLOKIR OTOMATIS", color = CryptoRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("${trap.tokenSymbol} (${trap.trapType}): ${trap.reason}", color = Color.White, fontSize = 11.sp)
                                            Text("Modal Selamat: +$${trap.estimatedLossAvoided.toInt()}", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }

                                report.executedTrade?.let { trade ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CryptoGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("✅ POSISI AKTIF DIBUKA DI PORTOFOLIO", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("${trade.tokenSymbol} @ $${trade.entryPrice} • Alokasi $${trade.virtualAmountUsd.toInt()} USD", color = TextPrimary, fontSize = 12.sp)
                                            Text("Periksa menu 'Home' untuk memantau performa atau menu 'Charts' untuk live chart.", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Text("Analisis LLM Otonom:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                SelectionContainer {
                                    Text(report.executiveSummary, color = Color(0xFFE2E8F0), fontSize = 12.sp, lineHeight = 17.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== MODE CHAT ASSISTANT ====================
            Column(modifier = Modifier.fillMaxSize()) {
                // Quick Action Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = {
                                sendChatMessage("Analisis status portofolio paper trading saat ini (${openTrades.size} posisi aktif).")
                            },
                            label = { Text("📊 Analisis Portofolio", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = DarkCard, labelColor = TextPrimary)
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = {
                                sendChatMessage("Audit smart wallet yang saya ikuti (${followedWallets.size} wallet).")
                            },
                            label = { Text("🛡️ Audit Smart Money", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = DarkCard, labelColor = TextPrimary)
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = {
                                sendChatMessage("Apa pola jebakan likuiditas dan honeypot yang paling sering terjadi?")
                            },
                            label = { Text("⚠️ Pola Jebakan Scam", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = DarkCard, labelColor = TextPrimary)
                        )
                    }
                }

                // Chat Messages
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFF1E2638), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) CryptoGreen.copy(alpha = 0.2f) else DarkCard
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    SelectionContainer {
                                        Text(
                                            text = msg.content,
                                            color = if (isUser) TextPrimary else Color(0xFFE2E8F0),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CryptoGreen, strokeWidth = 2.dp)
                                Text("AI sedang menganalisis...", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Input Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Tanyakan apa saja ke Custom LLM...", color = TextSecondary, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoGreen,
                                unfocusedBorderColor = Color(0xFF2B3648),
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { sendChatMessage(inputText) },
                            enabled = inputText.isNotBlank() && !isChatLoading,
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (inputText.isNotBlank() && !isChatLoading) CryptoGreen else Color(0xFF242C3D), CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Kirim", tint = if (inputText.isNotBlank() && !isChatLoading) Color.Black else TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Settings Modal Dialog
    if (showSettingsDialog) {
        LlmSettingsDialog(
            currentConfig = config,
            onDismiss = { showSettingsDialog = false },
            onSave = { newConfig ->
                config = newConfig
                LlmConfigManager.saveConfig(context, newConfig)
                showSettingsDialog = false
                Toast.makeText(context, "Konfigurasi LLM tersimpan", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun LlmSettingsDialog(
    currentConfig: LlmConfig,
    onDismiss: () -> Unit,
    onSave: (LlmConfig) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var endpoint by remember { mutableStateOf(currentConfig.endpoint) }
    var apiKey by remember { mutableStateOf(currentConfig.apiKey) }
    var model by remember { mutableStateOf(currentConfig.model) }
    var systemPrompt by remember { mutableStateOf(currentConfig.systemPrompt) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = CryptoGreen)
                Text("Kustom LLM Setting", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Pilih Preset Cepat:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = endpoint.contains(":11434"),
                                onClick = {
                                    endpoint = "http://192.168.1.100:11434/v1"
                                    model = "llama3"
                                },
                                label = { Text("Ollama LAN", fontSize = 10.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = endpoint.contains("127.0.0.1"),
                                onClick = {
                                    endpoint = "http://127.0.0.1:11434/v1"
                                    model = "llama3"
                                },
                                label = { Text("Termux Local", fontSize = 10.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = endpoint.contains("groq.com"),
                                onClick = {
                                    endpoint = "https://api.groq.com/openai/v1"
                                    model = "llama-3.3-70b-versatile"
                                },
                                label = { Text("Groq Cloud", fontSize = 10.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = endpoint.contains("openrouter.ai"),
                                onClick = {
                                    endpoint = "https://openrouter.ai/api/v1"
                                    model = "meta-llama/llama-3-8b-instruct"
                                },
                                label = { Text("OpenRouter", fontSize = 10.sp) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = endpoint.contains("openai.com"),
                                onClick = {
                                    endpoint = "https://api.openai.com/v1"
                                    model = "gpt-4o-mini"
                                },
                                label = { Text("OpenAI", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        label = { Text("Base URL / Endpoint") },
                        placeholder = { Text("http://192.168.x.x:11434/v1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Nama Model") },
                        placeholder = { Text("llama3 / gpt-4o-mini / deepseek-chat") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key (Kosongkan jika local Ollama)") },
                        placeholder = { Text("sk-...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Test Connection Area
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTesting = true
                                testResult = null
                                coroutineScope.launch {
                                    val tempConfig = LlmConfig(endpoint = endpoint, apiKey = apiKey, model = model, systemPrompt = systemPrompt)
                                    val res = CustomLlmClient.testConnection(tempConfig)
                                    isTesting = false
                                    res.onSuccess {
                                        isTestSuccess = true
                                        testResult = it
                                    }.onFailure {
                                        isTestSuccess = false
                                        testResult = it.message ?: "Gagal terhubung."
                                    }
                                }
                            },
                            enabled = !isTesting && endpoint.isNotBlank(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testing...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tes Koneksi", fontSize = 12.sp)
                            }
                        }
                    }

                    testResult?.let { msg ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTestSuccess) CryptoGreen.copy(alpha = 0.15f) else CryptoRed.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isTestSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isTestSuccess) CryptoGreen else CryptoRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = msg,
                                    color = if (isTestSuccess) CryptoGreen else CryptoRed,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(LlmConfig(endpoint = endpoint, apiKey = apiKey, model = model, systemPrompt = systemPrompt))
                },
                colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
            ) {
                Text("Simpan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = DarkCard
    )
}

