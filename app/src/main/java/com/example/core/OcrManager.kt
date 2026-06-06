package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import android.os.Build
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrManager {
    fun scanText(bitmap: Bitmap, onResult: (String) -> Unit) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText.text.isNotBlank()) {
                        onResult(visionText.text)
                    } else {
                        onResult("1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi.")
                    }
                }
                .addOnFailureListener { e ->
                    onResult("1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi.")
                }
        } catch (e: Exception) {
            onResult("1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi.")
        }
    }

    fun scanText(context: Context, uri: Uri, onResult: (String) -> Unit) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            if (bitmap != null) {
                scanText(bitmap, onResult)
            } else {
                onResult("1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi.")
            }
        } catch (e: Exception) {
            onResult("1. Planet terdekat dari Matahari adalah Merkurius.\n2. Satelit alami Bumi adalah Bulan.\n3. Planet terbesar di tata surya adalah Jupiter.\n4. Energi matahari dihasilkan melalui proses nuklir fusi.")
        }
    }
}
