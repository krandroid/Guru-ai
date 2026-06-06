package com.example.core

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    fun cetakDanBagikanPdf(
        context: Context,
        namaSiswa: String,
        mapel: String,
        skor: String,
        alasan: String
    ) {
        // 1. Inisialisasi Dokumen PDF (Ukuran standar A4: 595 x 842 pt)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 2. Siapkan Paint
        val paint = Paint().apply { isAntiAlias = true }
        
        // Background Page: Clean White
        canvas.drawColor(Color.parseColor("#FAFAFA"))

        // --- DRAW UPPER BANNER ---
        // Elegant deep purplish theme matching our Material 3 style
        paint.color = Color.parseColor("#2E2B4B") // Deep Dark violet
        canvas.drawRect(RectF(30f, 30f, 565f, 160f), paint)

        // Title inside banner
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("LAPORAN EVALUASI SISWA", 50f, 85f, paint)

        // Subtitle inside banner
        paint.color = Color.parseColor("#B39DDB") // Light violet/purple accent
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Koreksi & Penilaian Otomatis oleh AI Guru KelasAI", 50f, 115f, paint)

        // Clean white thin dividing line inside banner
        paint.color = Color.parseColor("#3F3B66")
        canvas.drawRect(RectF(50f, 130f, 545f, 132f), paint)

        // --- INFORMATION SECTION ---
        // Drawing information cards
        paint.color = Color.parseColor("#FFFFFF")
        val infoCard = RectF(30f, 180f, 565f, 320f)
        canvas.drawRoundRect(infoCard, 16f, 16f, paint)

        // Draw delicate outline around information card
        paint.color = Color.parseColor("#E0E0E0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.25f
        canvas.drawRoundRect(infoCard, 16f, 16f, paint)

        // Reset to fill style for labels
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#666666")
        paint.textSize = 12f
        canvas.drawText("MATA PELAJARAN", 55f, 215f, paint)
        canvas.drawText("NAMA SISWA", 55f, 265f, paint)

        // Bold Values
        paint.color = Color.parseColor("#1F1F1F")
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawText(mapel.uppercase(), 55f, 238f, paint)
        canvas.drawText(namaSiswa, 55f, 288f, paint)

        // Draw score box inside info section
        paint.color = Color.parseColor("#EEF2F6")
        val scoreBox = RectF(400f, 200f, 540f, 300f)
        canvas.drawRoundRect(scoreBox, 12f, 12f, paint)

        // Score outline (Green/Indigo theme)
        paint.color = Color.parseColor("#6200EE")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(scoreBox, 12f, 12f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#6200EE")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        
        // Align score text
        val scoreWidth = paint.measureText(skor)
        val scoreX = 470f - (scoreWidth / 2)
        canvas.drawText(skor, scoreX, 255f, paint)

        // Out of 100 label
        paint.color = Color.parseColor("#555555")
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val totalLabel = "SKOR / 100"
        val labelWidth = paint.measureText(totalLabel)
        val labelX = 470f - (labelWidth / 2)
        canvas.drawText(totalLabel, labelX, 282f, paint)

        // --- ANALYSIS SECTION ---
        paint.color = Color.parseColor("#FFFFFF")
        val analysisCard = RectF(30f, 340f, 565f, 750f)
        canvas.drawRoundRect(analysisCard, 16f, 16f, paint)

        paint.color = Color.parseColor("#E0E0E0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.25f
        canvas.drawRoundRect(analysisCard, 16f, 16f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#6200EE")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("CATATAN EVALUASI GURU AI:", 55f, 375f, paint)

        // Thin separator
        paint.color = Color.parseColor("#E8E8E8")
        paint.strokeWidth = 1f
        canvas.drawLine(55f, 395f, 540f, 395f, paint)

        // Draw parsed/wrapped Text using StaticLayout (Avoid text clipping or runout of page)
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#333333")
            textSize = 13f
            isAntiAlias = true
        }
        val wrapWidth = 485 // 540 - 55

        val staticLayout = StaticLayout.Builder.obtain(alasan, 0, alasan.length, textPaint, wrapWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(3.0f, 1.1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(55f, 415f)
        staticLayout.draw(canvas)
        canvas.restore()

        // --- FOOTER NOTE ---
        paint.color = Color.parseColor("#888888")
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Laporan ini sah dan dibuat secara digital.", 50f, 790f, paint)
        canvas.drawText("Terima kasih telah menggunakan KelasAI.", 50f, 805f, paint)

        pdfDocument.finishPage(page)

        // 4. Simpan PDF ke folder temporary cache
        val folderPdf = File(context.cacheDir, "pdfs")
        if (!folderPdf.exists()) {
            folderPdf.mkdirs()
        }

        val namaFile = "Hasil_Koreksi_${namaSiswa.replace(" ", "_")}.pdf"
        val filePdf = File(folderPdf, namaFile)

        try {
            pdfDocument.writeTo(FileOutputStream(filePdf))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        // 5. Panggil Intent Share ke WhatsApp / Aplikasi Lain
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", filePdf)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Hasil Koreksi $namaSiswa")
            putExtra(Intent.EXTRA_TEXT, "Berikut terlampir laporan koreksi otomatis untuk siswa an. $namaSiswa")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Bagikan PDF melalui..."))
    }
}
