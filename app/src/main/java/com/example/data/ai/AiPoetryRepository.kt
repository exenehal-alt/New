package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.example.model.AiImageModel
import com.example.model.AiImageSize
import com.example.model.PoemTextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

data class AiArtworkResult(
    val savedImagePath: String,
    val modelUsed: AiImageModel,
    val imageSize: AiImageSize,
    val isPlaceholderSample: Boolean,
    val noticeBn: String
)

data class HandwritingOcrResult(
    val title: String,
    val author: String,
    val poemBody: String,
    val pageLabel: String,
    val suggestedAlignment: PoemTextAlign,
    val isPlaceholderSample: Boolean,
    val noticeBn: String
)

interface AiPoetryRepository {
    val isPlaceholderMode: Boolean

    suspend fun transformPoemToArtwork(
        context: Context,
        title: String,
        poemBody: String,
        stylePrompt: String,
        paletteArgb: List<Long>,
        model: AiImageModel,
        imageSize: AiImageSize,
        aspectRatio: Float
    ): Result<AiArtworkResult>

    suspend fun createArtworkFromReference(
        context: Context,
        title: String,
        poemBody: String,
        referenceBitmap: Bitmap,
        paletteArgb: List<Long>,
        stylePrompt: String,
        model: AiImageModel,
        imageSize: AiImageSize,
        aspectRatio: Float
    ): Result<AiArtworkResult>

    suspend fun transcribeHandwrittenNotebook(
        notebookBitmap: Bitmap
    ): Result<HandwritingOcrResult>
}

/**
 * Placeholder implementation used because Firebase AI Logic setup was declined.
 * All AI capabilities route cleanly through [AiPoetryRepository] so enabling Firebase
 * later can swap in `Firebase.ai` (`gemini-nano-banana-2.1`, `gemini-3-pro-image-preview`,
 * and `gemini-3.8-flash`) with zero UI refactoring.
 */
class PlaceholderAiPoetryRepository : AiPoetryRepository {
    override val isPlaceholderMode: Boolean = true

    override suspend fun transformPoemToArtwork(
        context: Context,
        title: String,
        poemBody: String,
        stylePrompt: String,
        paletteArgb: List<Long>,
        model: AiImageModel,
        imageSize: AiImageSize,
        aspectRatio: Float
    ): Result<AiArtworkResult> = withContext(Dispatchers.IO) {
        delay(650L)
        val file = renderSampleMoodWash(
            context = context,
            palette = paletteArgb.ifEmpty { listOf(0xFFF7F2E7L, 0xFFE5C8A8L, 0xFFB85D3BL, 0xFF3A6B5CL) },
            aspectRatio = aspectRatio
        )
        Result.success(
            AiArtworkResult(
                savedImagePath = file.absolutePath,
                modelUsed = model,
                imageSize = imageSize,
                isPlaceholderSample = true,
                noticeBn = "নমুনা মোড: সরাসরি ${model.modelId} (${imageSize.label}) দিয়ে ইমেজ তৈরি করতে Firebase AI Logic চালু করা প্রয়োজন। আপাতত কবিতার উপযোগী নমুনা ওয়াশ ব্যাকগ্রাউন্ড তৈরি করা হয়েছে।"
            )
        )
    }

    override suspend fun createArtworkFromReference(
        context: Context,
        title: String,
        poemBody: String,
        referenceBitmap: Bitmap,
        paletteArgb: List<Long>,
        stylePrompt: String,
        model: AiImageModel,
        imageSize: AiImageSize,
        aspectRatio: Float
    ): Result<AiArtworkResult> = withContext(Dispatchers.IO) {
        delay(650L)
        val file = renderSampleMoodWash(
            context = context,
            palette = paletteArgb.ifEmpty { listOf(0xFFF3E9DCL, 0xFFD49A6AL, 0xFF5A7D7CL, 0xFF283845L) },
            aspectRatio = aspectRatio
        )
        Result.success(
            AiArtworkResult(
                savedImagePath = file.absolutePath,
                modelUsed = model,
                imageSize = imageSize,
                isPlaceholderSample = true,
                noticeBn = "নমুনা মোড: ছবির আসল রঙগুলো ডিভাইস থেকেই সংগ্রহ করা হয়েছে। পূর্ণাঙ্গ এআই স্টাইল ট্রান্সফারের (${model.modelId} - ${imageSize.label}) জন্য Firebase AI Logic প্রয়োজন।"
            )
        )
    }

    override suspend fun transcribeHandwrittenNotebook(
        notebookBitmap: Bitmap
    ): Result<HandwritingOcrResult> = withContext(Dispatchers.IO) {
        delay(550L)
        Result.success(
            HandwritingOcrResult(
                title = "ছোট্ট জিজ্ঞাসা",
                author = "হাসিবুল প্রান্ত",
                poemBody = """ইন্টার জীবনের প্রয়োজনে যেদিন
    ছেড়েছিলাম ঘর,
জানতাম না, সেই বিদায়ই একদিন কেড়ে
    নেবে আমার আশ্রয়স্থল।

তুমি ছিলে আমার জীবনের এক বিশাল
    বটবৃক্ষ,
নিজে রোদে পুড়ে আমাকে রেখেছিলে স্নিগ্ধ
    ছায়ার নিচে।

তোমার নিজের পকেট শূন্য, অথচ আমার
    আবদারের শেষ ছিল না
নিজের অভাবের কথা গোপন রেখে আমার
    প্রয়োজন অপূর্ণ রাখোনি কখনও।""",
                pageLabel = "পৃষ্ঠা - ০১",
                suggestedAlignment = PoemTextAlign.NOTEBOOK_INDENT,
                isPlaceholderSample = true,
                noticeBn = "নমুনা OCR ফলাফল: সরাসরি হাতের লেখা পড়তে (Gemini Vision OCR) Firebase AI Logic চালু করা প্রয়োজন। নিচে খাতার বিন্যাস অনুযায়ী নমুনা ডিজিটাল ও Bold লেখা দেওয়া হলো—আপনি প্রয়োজনমতো এডিট করতে পারবেন।"
            )
        )
    }

    private fun renderSampleMoodWash(
        context: Context,
        palette: List<Long>,
        aspectRatio: Float
    ): File {
        val w = 720
        val h = (w / aspectRatio.coerceIn(0.4f, 2.0f)).roundToInt().coerceIn(360, 1280)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val c0 = palette.getOrElse(0) { 0xFFF7F2E7L }.toInt()
        val c1 = palette.getOrElse(1) { 0xFFE8D5C4L }.toInt()
        val c2 = palette.getOrElse(2) { 0xFFB85D3BL }.toInt()
        val c3 = palette.getOrElse(3) { 0xFF3A6B5CL }.toInt()

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(c0, c1, c0),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        // Soft watercolor horizon wash so Bengali poem remains legible inside bounds
        val washPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c2
            alpha = 46
        }
        val hill1 = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.25f, h * 0.66f, w * 0.58f, h * 0.88f, w.toFloat(), h * 0.72f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        canvas.drawPath(hill1, washPaint)

        val accentWash = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c3
            alpha = 38
        }
        canvas.drawCircle(w * 0.8f, h * 0.16f, w * 0.22f, accentWash)

        val dir = File(context.filesDir, "backgrounds").apply { mkdirs() }
        val file = File(dir, "sample_art_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 95, it) }
        bmp.recycle()
        return file
    }
}
