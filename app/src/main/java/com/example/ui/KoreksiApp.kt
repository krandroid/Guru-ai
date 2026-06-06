package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.app.DownloadManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AnswerKey
import com.example.data.GradingHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoreksiApp(viewModel: KoreksiViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val answerKeys by viewModel.answerKeys.collectAsStateWithLifecycle()
    val gradingHistory by viewModel.gradingHistory.collectAsStateWithLifecycle()
    
    val currentSelectedKey by viewModel.selectedAnswerKey.collectAsStateWithLifecycle()
    val studentName by viewModel.studentNameInput.collectAsStateWithLifecycle()
    val studentAnswer by viewModel.studentAnswerInput.collectAsStateWithLifecycle()
    val gradingState by viewModel.gradingUiState.collectAsStateWithLifecycle()

    LaunchedEffect(answerKeys) {
        if (viewModel.selectedAnswerKey.value == null && answerKeys.isNotEmpty()) {
            viewModel.selectedAnswerKey.value = answerKeys.firstOrNull()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Warn if API Key is not configured
    val hasApiKey = remember { viewModel.checkApiKey() }

    var showModelDownloaderSheet by remember { mutableStateOf(false) }
    val dId by viewModel.downloadId.collectAsStateWithLifecycle()
    val dStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()

    LaunchedEffect(dId) {
        if (dId != null) {
            while (true) {
                viewModel.checkStatus()
                kotlinx.coroutines.delay(1500)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Guru AI",
                                fontWeight = FontWeight.Medium,
                                fontSize = 21.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34D399))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "LLAMA-3.2 READY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Logo Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { showModelDownloaderSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Offline Model Downloader",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(0.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(0.dp)
                )
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Grading") },
                    label = { Text("Koreksi Ujian") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Keys") },
                    label = { Text("Kunci Jawaban") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "History") },
                    label = { Text("Riwayat") }
                )
            }
        },
        modifier = Modifier.testTag("app_scaffold")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> KoreksiTabContent(
                    viewModel = viewModel,
                    answerKeys = answerKeys,
                    currentKey = currentSelectedKey,
                    studentName = studentName,
                    studentAnswer = studentAnswer,
                    gradingState = gradingState,
                    hasApiKey = hasApiKey
                )
                1 -> KunciJawabanTabContent(
                    viewModel = viewModel,
                    answerKeys = answerKeys
                )
                2 -> RiwayatTabContent(
                    viewModel = viewModel,
                    historyList = gradingHistory
                )
            }
        }
    }

    if (showModelDownloaderSheet) {
        AlertDialog(
            onDismissRequest = { showModelDownloaderSheet = false },
            confirmButton = {
                TextButton(onClick = { showModelDownloaderSheet = false }) {
                    Text("Tutup", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Otak AI Offline (GGUF)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Dapatkan kemampuan koreksi secara lokal langsung di HP Anda dengan mengunduh model bahasa offline (GGUF).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "REKOMENDASI MODEL: LLaMA 3.2 1B",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Llama-3.2-1B-Instruct.gguf",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Ukuran: ~1.2 GB | Format: GGUF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "STATUS UNDUHAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val statusText = when (dStatus) {
                                DownloadManager.STATUS_RUNNING -> "Sedang Mengunduh..."
                                DownloadManager.STATUS_SUCCESSFUL -> "Tersimpan Offline"
                                DownloadManager.STATUS_FAILED -> "Gagal"
                                DownloadManager.STATUS_PENDING -> "Menunggu Antrean"
                                DownloadManager.STATUS_PAUSED -> "Tertunda"
                                else -> "Belum Diunduh"
                            }
                            val statusColor = when (dStatus) {
                                DownloadManager.STATUS_SUCCESSFUL -> Color(0xFF34D399)
                                DownloadManager.STATUS_RUNNING -> MaterialTheme.colorScheme.primary
                                DownloadManager.STATUS_FAILED -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (dStatus == DownloadManager.STATUS_SUCCESSFUL) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Tersimpan",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            val isRunning = dStatus == DownloadManager.STATUS_RUNNING || dStatus == DownloadManager.STATUS_PENDING
                            Button(
                                onClick = {
                                    viewModel.startModelDownload(
                                        url = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
                                        filename = "llama-3-2-1b.gguf"
                                    )
                                },
                                enabled = !isRunning,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Unduh Model", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun KoreksiTabContent(
    viewModel: KoreksiViewModel,
    answerKeys: List<AnswerKey>,
    currentKey: AnswerKey?,
    studentName: String,
    studentAnswer: String,
    gradingState: GradingUiState,
    hasApiKey: Boolean
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var saveHistorySuccess by remember { mutableStateOf(false) }
    var hasUploadedImage by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(gradingState) {
        if (gradingState is GradingUiState.Error) {
            Toast.makeText(context, "Proses Gagal: ${gradingState.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher Galeri (Otomatis ambil PNG/JPG)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.studentAnswerInput.value = "Memproses gambar dari galeri..."
            hasUploadedImage = true
            com.example.core.OcrManager.scanText(context, uri) { hasilOCR ->
                viewModel.studentAnswerInput.value = hasilOCR
            }
        }
    }

    // Launcher Kamera (Menjepret langsung)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.studentAnswerInput.value = "Memproses hasil jepretan kamera..."
            hasUploadedImage = true
            com.example.core.OcrManager.scanText(bitmap) { hasilOCR ->
                viewModel.studentAnswerInput.value = hasilOCR
            }
        }
    }

    // Launcher Izin Kamera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk scan!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning if API Key is not set
        if (!hasApiKey) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Kunci API Gemini Belum Dikonfigurasi",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Silakan set variabel GEMINI_API_KEY Anda di bagian Secrets panel Google AI Studio untuk mengaktifkan koreksi otomatis AI.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Form Section if state is Idle or Error
        if (gradingState is GradingUiState.Idle || gradingState is GradingUiState.Error) {
            item {
                Text(
                    "Pena Guru: Koreksi Esai Otomatis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Pilih kunci jawaban, masukkan berkas esai siswa, lalu klik Koreksi AI untuk mengevaluasi jawaban secara instan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Dropdown Selector Kunci Jawaban
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "KUNCI JAWABAN ACUAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "High Precision",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { expandedDropdown = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentKey?.let { "[${it.subject}] ${it.title}" }
                                        ?: "Pilih Kunci Jawaban Resmi...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentKey != null) MaterialTheme.colorScheme.onSurface 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (answerKeys.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada Kunci Jawaban. Klik di sini untuk membuat sampel.") },
                                    onClick = {
                                        expandedDropdown = false
                                        // Quick insert sample key
                                        viewModel.saveAnswerKey(
                                            subject = "IPA SD",
                                            title = "Sistem Tata Surya & Bumi",
                                            content = "1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi."
                                        )
                                    }
                                )
                            } else {
                                answerKeys.forEach { key ->
                                    DropdownMenuItem(
                                        text = { Text("[${key.subject}] ${key.title}") },
                                        onClick = {
                                            viewModel.selectedAnswerKey.value = key
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Nama Siswa & Jawaban
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "INPUT SUMBER (OCR) & JAWABAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { viewModel.studentNameInput.value = it },
                            label = { Text("Nama Siswa") },
                            placeholder = { Text("Masukkan nama lengkap siswa") },
                            leadingIcon = { Icon(Icons.Default.Person, "Siswa") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_name_field"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ===== JELAJAHI MEDIA / OCR =====
                        Text(
                            text = "- Unggah Sumber Lembar Jawaban (OCR) -",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val hasCamPermission = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasCamPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Ambil Foto Lembar Jawaban",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Akses Kamera", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pilih dari Galeri",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dari Galeri", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = studentAnswer,
                            onValueChange = { viewModel.studentAnswerInput.value = it },
                            label = { Text("Lembar Jawaban Siswa") },
                            placeholder = { Text("Tulis atau tempelkan esai/jawaban siswa di sini untuk dikoreksi...") },
                            minLines = 4,
                            maxLines = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_answer_field"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ===== MUAT CONTOH JAWABAN CEPAT =====
                        Text(
                            "Muat Contoh Jawaban Cepat",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Ensure sample key is present, select it, and fill sample answers
                                    val sampleKey = answerKeys.find { it.subject == "IPA SD" || it.subject == "IPA Kelas 6" }
                                    if (sampleKey != null) {
                                        viewModel.selectedAnswerKey.value = sampleKey
                                    } else {
                                        viewModel.saveAnswerKey(
                                            subject = "IPA SD",
                                            title = "Sistem Tata Surya & Bumi",
                                            content = "1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi."
                                        )
                                    }
                                    viewModel.studentNameInput.value = "Andi Saputra"
                                    viewModel.studentAnswerInput.value = "1. Planet paling dekat itu planet Merkurius.\n2. Bumi punya satu satelit namanya Bulan saja.\n3. Jupiter adalah planet paling raksasa.\n4. Panas matahari terjadi karena fusi atom hidrogen menjadi helium."
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Andi (Skor Tinggi)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Button(
                                onClick = {
                                    val sampleKey = answerKeys.find { it.subject == "IPA SD" || it.subject == "IPA Kelas 6" }
                                    if (sampleKey != null) {
                                        viewModel.selectedAnswerKey.value = sampleKey
                                    } else {
                                        viewModel.saveAnswerKey(
                                            subject = "IPA SD",
                                            title = "Sistem Tata Surya & Bumi",
                                            content = "1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses fusi."
                                        )
                                    }
                                    viewModel.studentNameInput.value = "Budi Gunawan"
                                    viewModel.studentAnswerInput.value = "1. Planet paling dekat dengan matahari adalah Venus.\n2. Bulan adalah satelit bumi.\n3. Planet Jupiter sangat besar tapi yang terbesar bumi kata ibu guru.\n4. Matahari menyala karena terbakar."
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Budi (Skor Sedang)", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        // Small non-blocking error display above primary button
                        if (gradingState is GradingUiState.Error) {
                            Text(
                                text = "Proses Gagal: ${(gradingState as GradingUiState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // SISAIN cuma 1 tombol: "Koreksi Sekarang (AI)" warna ungu #8B5CF6
                        Button(
                            onClick = {
                                if (studentName.trim().isEmpty()) {
                                    Toast.makeText(context, "Masukkan nama siswa dulu bang", Toast.LENGTH_SHORT).show()
                                } else if (!hasUploadedImage && studentAnswer.trim().isEmpty()) {
                                    Toast.makeText(context, "Upload lembar jawaban dulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.evaluateAnswer()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF8B5CF6),
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("koreksi_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Koreksi Sekarang (AI)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Grading Icon",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading view State
        if (gradingState is GradingUiState.Loading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "AI Sedang Mengoreksi...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic Teaching Tips
                        val tips = listOf(
                            "Membaca lembar esai dan mengekstrak makna kalimat...",
                            "Menganalisis pencocokan konteks dengan Kunci Jawaban Resmi...",
                            "Menilai keadilan bobot materi dan memberikan skor objektif...",
                            "Menyusun analisis saran konstruktif untuk evaluasi siswa..."
                        )
                        var activeTipIndex by remember { mutableStateOf(0) }
                        LaunchedEffect(Unit) {
                            while (true) {
                                kotlinx.coroutines.delay(2200)
                                activeTipIndex = (activeTipIndex + 1) % tips.size
                            }
                        }

                        AnimatedContent(
                            targetState = tips[activeTipIndex],
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "tip_transition"
                        ) { tip ->
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Grading Success State
        if (gradingState is GradingUiState.Success) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "HASIL KOREKSI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        ScoreGaugeCircle(score = gradingState.result.skor)

                        Spacer(modifier = Modifier.height(30.dp))

                        // Brief detail info table
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Siswa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Mata Pelajaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentKey?.subject ?: "-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Detailed Alasan
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Analysis",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Analisis Koreksi Guru AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = gradingState.result.alasan,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (saveHistorySuccess) {
                            Text(
                                "✓ Berhasil disimpan ke Riwayat",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Options
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        saveHistorySuccess = false
                                        viewModel.resetGradingState()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mulai Baru", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveToHistory(
                                            GradingHistory(
                                                studentName = studentName,
                                                subject = currentKey?.subject ?: "-",
                                                title = currentKey?.title ?: "-",
                                                studentAnswer = studentAnswer,
                                                score = gradingState.result.skor,
                                                reason = gradingState.result.alasan
                                            )
                                        )
                                        saveHistorySuccess = true
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    enabled = !saveHistorySuccess
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Simpan")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simpan Riwayat", fontWeight = FontWeight.Bold)
                                }
                            }

                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    com.example.core.PdfHelper.cetakDanBagikanPdf(
                                        context = context,
                                        namaSiswa = studentName,
                                        mapel = currentKey?.subject ?: "Mata Pelajaran",
                                        skor = gradingState.result.skor.toString(),
                                        alasan = gradingState.result.alasan
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Bagikan PDF")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cetak & Bagikan PDF", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KunciJawabanTabContent(
    viewModel: KoreksiViewModel,
    answerKeys: List<AnswerKey>
) {
    var showAddForm by remember { mutableStateOf(false) }
    var subjectInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Launcher untuk Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            contentInput = "Mengekstrak teks kunci dari galeri..."
            com.example.core.OcrManager.scanText(context, uri) { hasilOCR ->
                contentInput = hasilOCR
            }
        }
    }

    // Launcher untuk Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            contentInput = "Mengekstrak teks kunci dari kamera..."
            com.example.core.OcrManager.scanText(bitmap) { hasilOCR ->
                contentInput = hasilOCR
            }
        }
    }

    // Launcher Izin Kamera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk scan!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showAddForm) {
        // Overlay/In-Card form to add target key
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddForm = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
                Text(
                    "Tambah Kunci Jawaban",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.size(48.dp)) // Equalizer space
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Mata Pelajaran (contoh: IPA Kelas V)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Judul Kuis / Ujian (contoh: Ulangan Harian Tata Surya)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ===== OCR KAMERA & GALERI UNTUK KUNCI JAWABAN =====
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Isi Kunci Jawaban Resmi",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Deretan Tombol Kamera & Galeri (Kecil agar rapi)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val hasCamPermission = ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasCamPermission) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Ambil Foto Kunci",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pilih Kunci dari Galeri",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        placeholder = { Text("Tuliskan poin-poin acuan yang benar dan wajib ada agar dinilai adil oleh AI Guru... Atau gunakan ikon kamera/galeri di atas untuk scan otomatis.") },
                        minLines = 6,
                        maxLines = 15,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    if (subjectInput.isNotBlank() && titleInput.isNotBlank() && contentInput.isNotBlank()) {
                        viewModel.saveAnswerKey(
                            subject = subjectInput.trim(),
                            title = titleInput.trim(),
                            content = contentInput.trim()
                        )
                        // Clear
                        subjectInput = ""
                        titleInput = ""
                        contentInput = ""
                        showAddForm = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Simpan")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Kunci Acuan", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // List view for Answer Keys
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Daftar Kunci Jawaban Resmi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Buat dan kelola lembar acuan ujian. Kunci ini akan digunakan AI sebagai patokan akurat saat menguji esai siswa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (answerKeys.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Buku Kosong",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Belum Ada Kunci Jawaban",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ketuk tombol '+' di bawah untuk menambahkan kunci jawaban acuan pertama Anda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(answerKeys) { key ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                                .clickable {
                                    viewModel.selectedAnswerKey.value = key
                                },
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                key.subject,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            key.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteAnswerKey(key) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = key.content,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Dibuat: ${formatTimestamp(key.timestamp)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddForm = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_key_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kunci")
            }
        }
    }
}

@Composable
fun RiwayatTabContent(
    viewModel: KoreksiViewModel,
    historyList: List<GradingHistory>
) {
    var selectedToExpand by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Riwayat Penilaian Siswa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Melihat rekap pencapaian murid-murid Anda yang telah dikoreksi oleh asisten AI Guru secara periodik.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (historyList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Riwayat Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belum ada riwayat koreksi",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Koreksi jawaban siswa di tab pertama dan ketuk 'Simpan Riwayat' untuk mencatat di sini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            // Stats summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Ujian Dikoreksi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${historyList.size} Siswa",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val averageScore = remember(historyList) {
                            historyList.map { it.score }.average().toInt()
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Rata-rata Skor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$averageScore / 100",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            items(historyList) { history ->
                val isExpanded = selectedToExpand == history.id
                val badgeColor = when {
                    history.score >= 80 -> Color(0xFF4CAF50)
                    history.score >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .clickable {
                            selectedToExpand = if (isExpanded) null else history.id
                        },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Score leading Circle
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor.copy(alpha = 0.15f))
                                        .border(2.dp, badgeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = history.score.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 17.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = history.studentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${history.subject} • ${history.title}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteHistory(history) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Riwayat",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = formatTimestamp(history.timestamp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        // Expanded sections
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Jawaban Siswa:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = history.studentAnswer,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 8.dp)
                                )

                                Text(
                                    "Analisis Guru AI:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = history.reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 12.dp)
                                )

                                val context = LocalContext.current
                                OutlinedButton(
                                    onClick = {
                                        com.example.core.PdfHelper.cetakDanBagikanPdf(
                                            context = context,
                                            namaSiswa = history.studentName,
                                            mapel = history.subject,
                                            skor = history.score.toString(),
                                            alasan = history.reason
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Bagikan PDF")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cetak & Bagikan PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@Composable
fun ScoreGaugeCircle(score: Int, modifier: Modifier = Modifier) {
    val animatedProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 1000,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val canvasSize = size
            val radius = (canvasSize.minDimension - strokeWidth) / 2f
            
            // Background Circle Outline
            drawCircle(
                color = outlineColor.copy(alpha = 0.3f),
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
            
            // Accent Color Sweep Arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }

        // Live Calculated Value
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (animatedProgress.value * 100).toInt().toString(),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = (-1).sp
            )
            Text(
                "SKOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 1.2.sp
            )
        }
    }
}
