package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ExtractedPhotoAnalysis(
    val savedFilePath: String,
    val pixelWidth: Int,
    val pixelHeight: Int,
    val matchedWidthInches: Float,
    val matchedHeightInches: Float,
    val paletteArgb: List<Long>,
    val suggestedPaperArgb: Long,
    val suggestedInkArgb: Long,
    val suggestedAccentArgb: Long,
    val styleSummaryBn: String
)

object ColorExtractor {

    suspend fun importAndAnalyzePhoto(
        context: Context,
        uri: Uri,
        baseWidthInches: Float = 5.5f
    ): Result<ExtractedPhotoAnalysis> = withContext(Dispatchers.IO) {
        try {
            val bgDir = File(context.filesDir, "backgrounds").apply { mkdirs() }
            val outFile = File(bgDir, "ref_${System.currentTimeMillis()}.jpg")

            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return@withContext Result.failure(IllegalStateException("ছবি পড়া সম্ভব হয়নি।"))

            val origW = decoded.width.coerceAtLeast(1)
            val origH = decoded.height.coerceAtLeast(1)

            // Save a clean bounded copy (max 1600px) so persistence works reliably offline
            val maxDim = 1600f
            val scale = minOf(1f, maxDim / max(origW, origH))
            val savedBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (origW * scale).roundToInt().coerceAtLeast(1),
                    (origH * scale).roundToInt().coerceAtLeast(1),
                    true
                )
            } else {
                decoded
            }

            FileOutputStream(outFile).use { fos ->
                savedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }

            val ratio = origW.toFloat() / origH.toFloat()
            val (matchedW, matchedH) = if (ratio <= 1f) {
                val w = baseWidthInches.coerceIn(3.5f, 8.5f)
                val h = (w / ratio).coerceIn(3.5f, 13.0f)
                roundOneDecimal(w) to roundOneDecimal(h)
            } else {
                val h = baseWidthInches.coerceIn(3.5f, 8.5f)
                val w = (h * ratio).coerceIn(3.5f, 12.0f)
                roundOneDecimal(w) to roundOneDecimal(h)
            }

            val palette = extractDominantPalette(savedBitmap)
            val (paper, ink, accent, styleDesc) = derivePaperAndInk(palette)

            if (savedBitmap !== decoded) decoded.recycle()
            savedBitmap.recycle()

            Result.success(
                ExtractedPhotoAnalysis(
                    savedFilePath = outFile.absolutePath,
                    pixelWidth = origW,
                    pixelHeight = origH,
                    matchedWidthInches = matchedW,
                    matchedHeightInches = matchedH,
                    paletteArgb = palette,
                    suggestedPaperArgb = paper,
                    suggestedInkArgb = ink,
                    suggestedAccentArgb = accent,
                    styleSummaryBn = styleDesc
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun decodeBoundedBitmap(path: String, maxDimension: Int = 1024): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val w = bounds.outWidth
        val h = bounds.outHeight
        if (w <= 0 || h <= 0) return null
        var sample = 1
        while (max(w / sample, h / sample) > maxDimension) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, opts)
    }

    fun extractDominantPalette(bitmap: Bitmap, count: Int = 6): List<Long> {
        val thumb = Bitmap.createScaledBitmap(bitmap, 72, 72, true)
        val pixels = IntArray(thumb.width * thumb.height)
        thumb.getPixels(pixels, 0, thumb.width, 0, 0, thumb.width, thumb.height)
        if (thumb !== bitmap) thumb.recycle()

        // Quantize RGB into 5-bit buckets (32x32x32)
        val buckets = HashMap<Int, MutableList<Int>>()
        for (px in pixels) {
            if (Color.alpha(px) < 128) continue
            val r = (Color.red(px) shr 4)
            val g = (Color.green(px) shr 4)
            val b = (Color.blue(px) shr 4)
            val key = (r shl 8) or (g shl 4) or b
            buckets.getOrPut(key) { mutableListOf() }.add(px)
        }

        val sortedClusters = buckets.values
            .sortedByDescending { it.size }
            .map { list ->
                var sr = 0L
                var sg = 0L
                var sb = 0L
                for (c in list) {
                    sr += Color.red(c)
                    sg += Color.green(c)
                    sb += Color.blue(c)
                }
                val n = list.size.toLong()
                Color.rgb((sr / n).toInt(), (sg / n).toInt(), (sb / n).toInt())
            }

        val selected = mutableListOf<Int>()
        for (candidate in sortedClusters) {
            if (selected.none { colorDistance(it, candidate) < 52.0 }) {
                selected.add(candidate)
                if (selected.size >= count) break
            }
        }
        if (selected.isEmpty()) {
            selected.addAll(listOf(0xFFF7F2E7.toInt(), 0xFF1D1A16.toInt(), 0xFFB85D3B.toInt()))
        }
        return selected.map { (it.toLong() and 0xFFFFFFFFL) }
    }

    private fun derivePaperAndInk(palette: List<Long>): Quad<Long, Long, Long, String> {
        val colors = palette.map { it.toInt() }
        val lightest = colors.maxByOrNull { luminance(it) } ?: 0xFFF7F2E7.toInt()
        val darkest = colors.minByOrNull { luminance(it) } ?: 0xFF1D1A16.toInt()
        val mostVibrant = colors.maxByOrNull { saturation(it) } ?: 0xFFB85D3B.toInt()

        val avgLum = colors.map { luminance(it) }.average()
        val isDarkMood = avgLum < 0.36

        val paper = if (isDarkMood) {
            darken(darkest, 0.72f)
        } else {
            // Soften lightest color toward warm readable manuscript paper
            blend(lightest, 0xFFF9F6EE.toInt(), 0.55f)
        }
        val ink = if (isDarkMood) {
            0xFFF5F0E6L
        } else {
            (darken(darkest, 0.65f).toLong() and 0xFFFFFFFFL) or 0xFF000000L
        }
        val accent = (mostVibrant.toLong() and 0xFFFFFFFFL) or 0xFF000000L

        val moodBn = when {
            isDarkMood -> "গভীর ও মায়াবী রাত্রি শৈলী"
            saturation(mostVibrant) > 0.45f -> "উষ্ণ ও বর্ণিল শৈলী"
            else -> "স্নিগ্ধ পাণ্ডুলিপি শৈলী"
        }
        return Quad((paper.toLong() and 0xFFFFFFFFL) or 0xFF000000L, ink, accent, moodBn)
    }

    private fun colorDistance(c1: Int, c2: Int): Double {
        val dr = Color.red(c1) - Color.red(c2)
        val dg = Color.green(c1) - Color.green(c2)
        val db = Color.blue(c1) - Color.blue(c2)
        return sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    private fun luminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0
        return 0.299 * r + 0.587 * g + 0.114 * b
    }

    private fun saturation(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[1]
    }

    private fun blend(c1: Int, c2: Int, ratioC2: Float): Int {
        val r1 = 1f - ratioC2
        val r = (Color.red(c1) * r1 + Color.red(c2) * ratioC2).roundToInt().coerceIn(0, 255)
        val g = (Color.green(c1) * r1 + Color.green(c2) * ratioC2).roundToInt().coerceIn(0, 255)
        val b = (Color.blue(c1) * r1 + Color.blue(c2) * ratioC2).roundToInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).roundToInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).roundToInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).roundToInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun roundOneDecimal(v: Float): Float = (v * 10f).roundToInt() / 10f

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
