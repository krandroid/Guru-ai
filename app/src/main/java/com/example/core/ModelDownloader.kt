package com.example.core

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

class ModelDownloader(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadAiBrain(modelUrl: String, fileName: String): Long {
        // Tentukan lokasi penyimpanan internal aplikasi agar aman
        val targetFile = File(context.filesDir, fileName)
        
        // Jika file sudah ada, tidak perlu download lagi
        if (targetFile.exists()) {
            return -1L
        }

        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Mengunduh Otak AI KelasAI")
            .setDescription("Mengunduh model GGUF untuk koreksi offline...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true) // Izinkan pakai kuota data/seluler
            .setAllowedOverRoaming(true)

        // Kembalikan ID unduhan untuk pelacakan progress bar di UI Compose
        return downloadManager.enqueue(request)
    }
    
    fun checkDownloadStatus(downloadId: Long): Int {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (columnIndex != -1) {
                val status = cursor.getInt(columnIndex)
                cursor.close()
                return status
            }
            cursor.close()
        }
        return DownloadManager.STATUS_FAILED
    }
}
