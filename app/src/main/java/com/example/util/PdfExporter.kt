package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.model.DrawingTool
import com.example.model.ManuscriptPage
import com.example.model.PoemTextAlign
import com.example.ui.theme.BengaliFontOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class PdfExportResult(
    val shareUri: Uri,
    val displayFileName: String,
    val savedToDownloads: Boolean
)

object PdfExporter {

    suspend fun exportManuscriptToPdf(
        context: Context,
        page: ManuscriptPage
    ): Result<PdfExportResult> = withContext(Dispatchers.IO) {
        try {
            // 1 inch = 72 PostScript points (5.5 x 8.5 in => 396 x 612 pt)
            val pageWidthPt = (page.canvasWidthInches * 72f).roundToInt().coerceIn(216, 1200)
            val pageHeightPt = (page.canvasHeightInches * 72f).roundToInt().coerceIn(216, 1400)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            drawPageOnCanvas(context, canvas, page, pageWidthPt.toFloat(), pageHeightPt.toFloat())
            pdfDocument.finishPage(pdfPage)

            val safeTitle = page.title.trim()
                .replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
                .trim('_')
                .ifBlank { "pandulipi" }
            val fileName = "Pandulipi_${safeTitle}_${System.currentTimeMillis() % 10000}.pdf"

            val pdfDir = File(context.filesDir, "pdfs").apply { mkdirs() }
            val internalFile = File(pdfDir, fileName)
            FileOutputStream(internalFile).use { fos ->
                pdfDocument.writeTo(fos)
            }

            var savedToDownloads = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Pandulipi")
                    }
                    val resolver = context.contentResolver
                    val downloadsUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (downloadsUri != null) {
                        resolver.openOutputStream(downloadsUri)?.use { out ->
                            internalFile.inputStream().use { input -> input.copyTo(out) }
                        }
                        savedToDownloads = true
                    }
                } catch (_: Exception) {
                    savedToDownloads = false
                }
            }

            pdfDocument.close()

            val shareUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                internalFile
            )

            Result.success(
                PdfExportResult(
                    shareUri = shareUri,
                    displayFileName = fileName,
                    savedToDownloads = savedToDownloads
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawPageOnCanvas(
        context: Context,
        canvas: Canvas,
        page: ManuscriptPage,
        width: Float,
        height: Float
    ) {
        canvas.save()
        canvas.clipRect(0f, 0f, width, height)

        // 1. Paper background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = page.paperColorArgb.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // 2. Optional uploaded / generated background image (bounded to canvas)
        page.backgroundImagePath?.let { path ->
            ColorExtractor.decodeBoundedBitmap(path, maxDimension = 1600)?.let { bmp ->
                val bmpRatio = bmp.width.toFloat() / bmp.height.toFloat()
                val pageRatio = width / height
                val destRect = RectF(0f, 0f, width, height)
                val srcRect = if (bmpRatio > pageRatio) {
                    val cropW = (bmp.height * pageRatio).roundToInt()
                    val left = ((bmp.width - cropW) / 2).coerceAtLeast(0)
                    Rect(left, 0, left + cropW, bmp.height)
                } else {
                    val cropH = (bmp.width / pageRatio).roundToInt()
                    val top = ((bmp.height - cropH) / 2).coerceAtLeast(0)
                    Rect(0, top, bmp.width, top + cropH)
                }
                val imgPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                    alpha = (page.backgroundImageAlpha.coerceIn(0.05f, 1f) * 255).roundToInt()
                }
                canvas.drawBitmap(bmp, srcRect, destRect, imgPaint)
                bmp.recycle()
            }
        }

        // 3. Optional literary inner frame border
        val margin = width * 0.065f
        if (page.showBorder) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = page.accentColorArgb.toInt()
                alpha = 95
                style = Paint.Style.STROKE
                strokeWidth = 1.1f
            }
            canvas.drawRect(margin * 0.6f, margin * 0.6f, width - margin * 0.6f, height - margin * 0.6f, borderPaint)
        }

        // 4. Vector drawing strokes (using offscreen layer so Eraser works accurately)
        if (page.strokes.isNotEmpty()) {
            val layerId = canvas.saveLayer(0f, 0f, width, height, null)
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            for (stroke in page.strokes) {
                if (stroke.points.isEmpty()) continue
                val strokePx = (stroke.strokeWidthNorm / 400f) * width
                strokePaint.strokeWidth = strokePx.coerceAtLeast(1f)
                if (stroke.tool == DrawingTool.ERASER) {
                    strokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    strokePaint.color = android.graphics.Color.TRANSPARENT
                    strokePaint.alpha = 255
                } else {
                    strokePaint.xfermode = null
                    strokePaint.color = stroke.colorArgb.toInt()
                    strokePaint.alpha = (stroke.alpha.coerceIn(0.05f, 1f) * 255).roundToInt()
                }

                val path = Path()
                val first = stroke.points.first()
                path.moveTo(first.x * width, first.y * height)
                if (stroke.points.size == 1) {
                    path.lineTo(first.x * width + 0.5f, first.y * height + 0.5f)
                } else {
                    for (i in 1 until stroke.points.size) {
                        val prev = stroke.points[i - 1]
                        val curr = stroke.points[i]
                        val midX = (prev.x + curr.x) * 0.5f * width
                        val midY = (prev.y + curr.y) * 0.5f * height
                        path.quadTo(prev.x * width, prev.y * height, midX, midY)
                    }
                    val last = stroke.points.last()
                    path.lineTo(last.x * width, last.y * height)
                }
                canvas.drawPath(path, strokePaint)
            }
            canvas.restoreToCount(layerId)
        }

        // 5. Typography strictly bounded inside safe margins
        val fontOption = BengaliFontOption.fromId(page.fontId)
        val baseTypeface = ResourcesCompat.getFont(context, fontOption.fontResId) ?: Typeface.SERIF
        val styledTypeface = Typeface.create(
            baseTypeface,
            if (page.isBold) Typeface.BOLD else Typeface.NORMAL
        )

        val contentLeft = margin * 1.15f
        val contentWidth = (width - contentLeft * 2f).roundToInt().coerceAtLeast(100)
        val topPadding = margin * 1.2f
        val bottomReserved = margin * 1.35f
        val availableTotalHeight = (height - topPadding - bottomReserved).coerceAtLeast(80f)

        // Scale font sizes proportionally to canvas width vs 396pt (5.5 in)
        val widthScale = width / 396f
        var titleSizePt = page.titleSizeSp * widthScale
        var bodySizePt = page.fontSizeSp * widthScale
        var authorSizePt = (page.fontSizeSp * 0.95f) * widthScale

        val layoutAlign = when (page.textAlign) {
            PoemTextAlign.CENTER -> Layout.Alignment.ALIGN_CENTER
            PoemTextAlign.LEFT, PoemTextAlign.NOTEBOOK_INDENT -> Layout.Alignment.ALIGN_NORMAL
        }

        fun buildStaticLayout(text: String, textSize: Float, bold: Boolean, align: Layout.Alignment): StaticLayout {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = if (bold) Typeface.create(baseTypeface, Typeface.BOLD) else styledTypeface
                isFakeBoldText = bold
                this.textSize = textSize
                color = page.inkColorArgb.toInt()
            }
            return StaticLayout.Builder.obtain(text, 0, text.length, tp, contentWidth)
                .setAlignment(align)
                .setLineSpacing(0f, page.lineHeightMultiplier)
                .setIncludePad(false)
                .build()
        }

        var titleLayout = buildStaticLayout(page.title.ifBlank { " " }, titleSizePt, true, Layout.Alignment.ALIGN_CENTER)
        var authorLayout = buildStaticLayout(page.author, authorSizePt, false, Layout.Alignment.ALIGN_CENTER)
        var bodyLayout = buildStaticLayout(page.poemBody, bodySizePt, page.isBold, layoutAlign)

        val dividerSpace = if (page.showDivider) 18f * widthScale else 8f * widthScale
        val titleGap = 4f * widthScale
        val authorGap = 10f * widthScale

        if (page.autoFitText) {
            var shrink = 0.96f
            var attempts = 0
            while (
                (titleLayout.height + titleGap + authorLayout.height + authorGap + dividerSpace + bodyLayout.height) > availableTotalHeight &&
                attempts < 18
            ) {
                titleSizePt *= shrink
                authorSizePt *= shrink
                bodySizePt = max(7.5f, bodySizePt * shrink)
                titleLayout = buildStaticLayout(page.title.ifBlank { " " }, titleSizePt, true, Layout.Alignment.ALIGN_CENTER)
                authorLayout = buildStaticLayout(page.author, authorSizePt, false, Layout.Alignment.ALIGN_CENTER)
                bodyLayout = buildStaticLayout(page.poemBody, bodySizePt, page.isBold, layoutAlign)
                attempts++
            }
        }

        // Clip text rendering strictly to safe inner bounds
        canvas.save()
        canvas.clipRect(contentLeft, topPadding, contentLeft + contentWidth, height - bottomReserved)

        var cursorY = topPadding
        if (page.title.isNotBlank()) {
            canvas.save()
            canvas.translate(contentLeft, cursorY)
            titleLayout.draw(canvas)
            canvas.restore()
            cursorY += titleLayout.height + titleGap
        }

        if (page.author.isNotBlank()) {
            canvas.save()
            canvas.translate(contentLeft, cursorY)
            authorLayout.draw(canvas)
            canvas.restore()
            cursorY += authorLayout.height + authorGap
        }

        if (page.showDivider) {
            val lineY = cursorY + dividerSpace * 0.45f
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = page.accentColorArgb.toInt()
                alpha = 150
                strokeWidth = 0.9f * widthScale
            }
            val midX = width / 2f
            val span = contentWidth * 0.34f
            canvas.drawLine(midX - span, lineY, midX - 8f * widthScale, lineY, linePaint)
            canvas.drawLine(midX + 8f * widthScale, lineY, midX + span, lineY, linePaint)
            val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = page.accentColorArgb.toInt()
                style = Paint.Style.FILL
            }
            val r = 2.6f * widthScale
            val diamond = Path().apply {
                moveTo(midX, lineY - r)
                lineTo(midX + r, lineY)
                lineTo(midX, lineY + r)
                lineTo(midX - r, lineY)
                close()
            }
            canvas.drawPath(diamond, diamondPaint)
            cursorY += dividerSpace
        }

        canvas.save()
        canvas.translate(contentLeft, cursorY)
        bodyLayout.draw(canvas)
        canvas.restore()

        canvas.restore() // end safe text clip

        // 6. Page Footer label ("পৃষ্ঠা - ০১")
        if (page.pageLabel.isNotBlank()) {
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = styledTypeface
                isFakeBoldText = page.isBold
                textSize = 11f * widthScale
                color = page.inkColorArgb.toInt()
                alpha = 190
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(page.pageLabel, width / 2f, height - margin * 0.72f, footerPaint)
        }

        canvas.restore()
    }
}
