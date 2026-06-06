package com.example.ui

import android.app.Application
import android.app.DownloadManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GeminiApiClient
import com.example.api.GenerationConfig
import com.example.api.KoreksiResult
import com.example.api.Part
import com.example.core.ModelDownloader
import com.example.data.AnswerKey
import com.example.data.GradingHistory
import com.example.data.KoreksiDatabase
import com.example.data.KoreksiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GradingUiState {
    object Idle : GradingUiState
    object Loading : GradingUiState
    data class Success(val result: KoreksiResult) : GradingUiState
    data class Error(val message: String) : GradingUiState
}

class KoreksiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KoreksiRepository
    private val modelDownloader = ModelDownloader(application)

    init {
        val database = KoreksiDatabase.getDatabase(application)
        repository = KoreksiRepository(database.koreksiDao())
    }

    // Download states
    private val _downloadId = MutableStateFlow<Long?>(null)
    val downloadId = _downloadId.asStateFlow()

    private val _downloadStatus = MutableStateFlow<Int?>(null)
    val downloadStatus = _downloadStatus.asStateFlow()

    fun startModelDownload(url: String, filename: String) {
        val id = modelDownloader.downloadAiBrain(url, filename)
        if (id != -1L) {
            _downloadId.value = id
            _downloadStatus.value = DownloadManager.STATUS_RUNNING
        } else {
            _downloadStatus.value = DownloadManager.STATUS_SUCCESSFUL
        }
    }

    fun checkStatus() {
        val id = _downloadId.value
        if (id != null) {
            val status = modelDownloader.checkDownloadStatus(id)
            _downloadStatus.value = status
        }
    }

    // Streams
    val answerKeys: StateFlow<List<AnswerKey>> = repository.allAnswerKeys
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val gradingHistory: StateFlow<List<GradingHistory>> = repository.allGradingHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form states
    val selectedAnswerKey = MutableStateFlow<AnswerKey?>(null)
    val studentNameInput = MutableStateFlow("")
    val studentAnswerInput = MutableStateFlow("")
    
    // Grading process state
    private val _gradingUiState = MutableStateFlow<GradingUiState>(GradingUiState.Idle)
    val gradingUiState: StateFlow<GradingUiState> = _gradingUiState.asStateFlow()

    // Database Actions
    fun saveAnswerKey(subject: String, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAnswerKey(
                AnswerKey(subject = subject, title = title, content = content)
            )
        }
    }

    fun deleteAnswerKey(answerKey: AnswerKey) {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedAnswerKey.value?.id == answerKey.id) {
                selectedAnswerKey.value = null
            }
            repository.deleteAnswerKey(answerKey)
        }
    }

    fun saveToHistory(history: GradingHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGradingHistory(history)
        }
    }

    fun deleteHistory(history: GradingHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGradingHistory(history)
        }
    }

    fun resetGradingState() {
        _gradingUiState.value = GradingUiState.Idle
    }

    fun checkApiKey(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "GEMINI_API_KEY"
    }

    fun evaluateAnswer() {
        val key = selectedAnswerKey.value
        val studentAnswer = studentAnswerInput.value.trim()
        val studentName = studentNameInput.value.trim()

        if (key == null) {
            _gradingUiState.value = GradingUiState.Error("Pilih Kunci Jawaban terlebih dahulu.")
            return
        }
        if (studentAnswer.isEmpty()) {
            _gradingUiState.value = GradingUiState.Error("Isi lembar jawaban siswa terlebih dahulu.")
            return
        }
        if (studentName.isEmpty()) {
            _gradingUiState.value = GradingUiState.Error("Masukkan nama siswa.")
            return
        }

        if (!checkApiKey()) {
            _gradingUiState.value = GradingUiState.Error("Kunci API Gemini tidak ditemukan atau masih default placeholder. Harap konfigurasikan kunci API riil di panel 'Secrets' AI Studio.")
            return
        }

        _gradingUiState.value = GradingUiState.Loading

        viewModelScope.launch {
            try {
                val prompt = """
                    Kamu adalah Sistem Koreksi Ujian Otomatis yang bertindak sebagai Guru berpengalaman. Tugasmu adalah memeriksa jawaban siswa berdasarkan Kunci Jawaban secara objektif, adil, ramah, dan teliti.
                    
                    KUNCI JAWABAN RESMI:
                    ${key.content}
                    
                    HASIL SCAN JAWABAN SISWA:
                    $studentAnswer
                    
                    PERINTAH EVALUASI:
                    1. Analisis kesesuaian makna antara jawaban siswa dan kunci jawaban resmi.
                    2. Berikan penilaian skor dari rentang 0 sampai 100.
                    3. Berikan analisis atau alasan koreksi dalam bahasa Indonesia yang ringkas dan jelas (maksimal 2 kalimat).
                    4. Output harus berupa JSON murni dengan properti 'skor' (integer) dan 'alasan' (string). Semua output text lain di luar JSON dilarang.
                    
                    CONTOH OUTPUT JSON:
                    {"skor": 85, "alasan": "Sebagian besar jawaban benar dan sesuai dengan kunci, namun penjelasan untuk soal nomor 3 kurang lengkap."}
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.1f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    GeminiApiClient.service.generateContent(
                        apiKey = BuildConfig.GEMINI_API_KEY,
                        request = request
                    )
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawText != null) {
                    val cleanedJson = extractJsonClean(rawText)
                    val adapter = GeminiApiClient.moshiInstance.adapter(KoreksiResult::class.java)
                    val result = withContext(Dispatchers.Default) {
                        adapter.fromJson(cleanedJson)
                    }
                    if (result != null) {
                        _gradingUiState.value = GradingUiState.Success(result)
                    } else {
                        _gradingUiState.value = GradingUiState.Error("Gagal memparsing respons dari AI.")
                    }
                } else {
                    _gradingUiState.value = GradingUiState.Error("AI mengembalikan jawaban kosong.")
                }
            } catch (e: Exception) {
                _gradingUiState.value = GradingUiState.Error("Kesalahan koneksi atau API: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    private fun extractJsonClean(rawResponse: String): String {
        val regex = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL)
        return regex.find(rawResponse)?.value ?: "{\"skor\": 0, \"alasan\": \"Gagal memproses parsing output\"}"
    }
}
