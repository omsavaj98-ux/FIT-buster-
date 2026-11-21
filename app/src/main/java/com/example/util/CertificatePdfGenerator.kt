package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.CertificateRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object CertificatePdfGenerator {

    data class PdfExportResult(
        val internalFile: File,
        val shareUri: Uri,
        val savedToDownloads: Boolean,
        val displayLocation: String
    )

    suspend fun generateCertificatePdf(
        context: Context,
        certificate: CertificateRecord,
        signatureBitmap: Bitmap?
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        try {
            val pageWidth = 842 // A4 landscape in points (approx)
            val pageHeight = 595

            val pdf = PdfDocument()
            val certDir = File(context.filesDir, "certificates").apply { mkdirs() }
            val safeFileName = "FIT_BUSTER_${certificate.certificateCode.replace(Regex("[^A-Za-z0-9_-]"), "_")}.pdf"
            val internalFile = File(certDir, safeFileName)
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // Deep Black Background
                canvas.drawColor(Color.parseColor("#080A0D"))

                // Outer Neon Lime Border
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                    color = Color.parseColor("#C6FF00")
                }
                canvas.drawRoundRect(RectF(22f, 22f, pageWidth - 22f, pageHeight - 22f), 16f, 16f, borderPaint)

                // Inner Subtle Border
                val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.parseColor("#2A3240")
                }
                canvas.drawRoundRect(RectF(36f, 36f, pageWidth - 36f, pageHeight - 36f), 12f, 12f, innerBorderPaint)

                // Top Brand Header
                val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#C6FF00")
                    textSize = 22f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.16f
                }
                canvas.drawText("FIT BUSTER", 64f, 84f, brandPaint)

                val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#9EA9B8")
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    letterSpacing = 0.2f
                }
                canvas.drawText("OFFICIAL FITNESS ACHIEVEMENT CERTIFICATE", 64f, 106f, subHeaderPaint)

                // Seal Emblem top-right
                val sealBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_achievement_seal)
                if (sealBitmap != null) {
                    val sealDest = RectF(pageWidth - 156f, 52f, pageWidth - 60f, 148f)
                    canvas.drawBitmap(sealBitmap, null, sealDest, Paint(Paint.FILTER_BITMAP_FLAG))
                }

                // Divider line
                val dividerPaint = Paint().apply {
                    color = Color.parseColor("#232A35")
                    strokeWidth = 1.5f
                }
                canvas.drawLine(64f, 132f, pageWidth - 172f, 132f, dividerPaint)

                // Certificate Title
                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F4F7F5")
                    textSize = 30f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText(certificate.certificateName, 64f, 186f, titlePaint)

                // Presented To label
                val presentedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#9EA9B8")
                    textSize = 13f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                canvas.drawText("THIS CERTIFICATE IS PROUDLY PRESENTED TO", 64f, 226f, presentedPaint)

                // Recipient Actual Account Name
                val recipientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#C6FF00")
                    textSize = 36f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                }
                val recipientDisplay = certificate.recipientName.ifBlank { "FIT BUSTER Athlete" }
                canvas.drawText(recipientDisplay, 64f, 274f, recipientPaint)

                // Description wrapped safely
                val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#DDE3EA")
                    textSize = 15f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                drawWrappedText(canvas, certificate.achievementDescription, 64f, 316f, pageWidth - 140f, 22f, descPaint)

                // Medal Earned Pill
                val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#182012")
                }
                val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.parseColor("#C6FF00")
                }
                val pillRect = RectF(64f, 376f, 430f, 416f)
                canvas.drawRoundRect(pillRect, 10f, 10f, pillBgPaint)
                canvas.drawRoundRect(pillRect, 10f, 10f, pillBorderPaint)
                val medalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#C6FF00")
                    textSize = 13f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText("MEDAL EARNED: ${certificate.medalEarned}", 80f, 401f, medalPaint)

                // Bottom Left Metadata: Completion Date & Certificate ID
                val metaLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#7D8898")
                    textSize = 11f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val metaValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F4F7F5")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
                canvas.drawText("COMPLETION DATE", 64f, 480f, metaLabelPaint)
                canvas.drawText(FitBusterDateUtil.formatDisplayDate(certificate.completionDateKey), 64f, 502f, metaValuePaint)

                canvas.drawText("CERTIFICATE ID", 260f, 480f, metaLabelPaint)
                canvas.drawText(certificate.certificateCode, 260f, 502f, metaValuePaint)

                // Bottom Right: Uploaded Original Signature (UNTAMPERED, NEVER REDRAWN) above Authorized Signature label
                val sigBoxLeft = pageWidth - 280f
                val sigBoxRight = pageWidth - 64f
                val sigLineY = 508f

                if (signatureBitmap != null) {
                    val maxW = sigBoxRight - sigBoxLeft - 16f
                    val maxH = 74f
                    val scale = minOf(maxW / signatureBitmap.width.coerceAtLeast(1), maxH / signatureBitmap.height.coerceAtLeast(1))
                    val drawW = signatureBitmap.width * scale
                    val drawH = signatureBitmap.height * scale
                    val left = sigBoxLeft + ((sigBoxRight - sigBoxLeft) - drawW) / 2f
                    val top = sigLineY - 8f - drawH
                    val destRect = RectF(left, top, left + drawW, top + drawH)
                    canvas.drawBitmap(signatureBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                } else {
                    val missingSigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#6B7685")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                    }
                    canvas.drawText("Upload signature in Profile -> Signature Settings", sigBoxLeft + 8f, sigLineY - 16f, missingSigPaint)
                }

                val sigLinePaint = Paint().apply {
                    color = Color.parseColor("#C6FF00")
                    strokeWidth = 1.5f
                }
                canvas.drawLine(sigBoxLeft, sigLineY, sigBoxRight, sigLineY, sigLinePaint)

                val authLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#DDE3EA")
                    textSize = 12f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                canvas.drawText("Authorized Signature", sigBoxLeft + 44f, sigLineY + 20f, authLabelPaint)

                pdf.finishPage(page)

                // Save to app private certificates dir for sharing
                FileOutputStream(internalFile).use { out -> pdf.writeTo(out) }
            } finally {
                runCatching { pdf.close() }
            }

            // Also save to public Downloads on API 29+ via MediaStore (zero storage permissions needed!)
            var savedToDownloads = false
            var displayLoc = "App Certificates Folder"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safeFileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FIT_BUSTER")
                    }
                    val resolver = context.contentResolver
                    val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (downloadUri != null) {
                        resolver.openOutputStream(downloadUri)?.use { out ->
                            internalFile.inputStream().use { input -> input.copyTo(out) }
                        }
                        savedToDownloads = true
                        displayLoc = "Downloads/FIT_BUSTER/$safeFileName"
                    }
                }
            }

            val shareUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                internalFile
            )
            Result.success(PdfExportResult(internalFile, shareUri, savedToDownloads, displayLoc))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareCertificatePdf(context: Context, shareUri: Uri, certificateName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_SUBJECT, "FIT BUSTER Achievement - $certificateName")
            putExtra(Intent.EXTRA_TEXT, "Earned $certificateName in FIT BUSTER!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Certificate PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ) {
        val words = text.split(" ")
        var line = StringBuilder()
        var y = startY
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line.toString(), startX, y, paint)
                line = StringBuilder(word)
                y += lineHeight
            } else {
                line = StringBuilder(testLine)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), startX, y, paint)
        }
    }
}
