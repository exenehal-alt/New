package com.example.model

import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

enum class CanvasSizePreset(
    val id: String,
    val labelBn: String,
    val widthInches: Float,
    val heightInches: Float
) {
    BOOK_5_5_X_8_5("book_55_85", "৫.৫ × ৮.৫ ইঞ্চি (বই)", 5.5f, 8.5f),
    A5("a5", "A5 (৫.৮ × ৮.৩\")", 5.83f, 8.27f),
    A4("a4", "A4 (৮.৩ × ১১.৭\")", 8.27f, 11.69f),
    PORTRAIT_4_5("portrait_45", "৪ × ৫ ইঞ্চি (কার্ড)", 4.0f, 5.0f),
    SQUARE_6_6("square_66", "৬ × ৬ ইঞ্চি (বর্গ)", 6.0f, 6.0f),
    PHOTO_MATCH("photo_match", "ছবির সাইজ অনুযায়ী", 5.5f, 8.5f),
    CUSTOM("custom", "কাস্টম সাইজ", 5.5f, 8.5f);

    companion object {
        fun fromDimensions(w: Float, h: Float): CanvasSizePreset {
            return entries.firstOrNull {
                it != PHOTO_MATCH && it != CUSTOM &&
                    kotlin.math.abs(it.widthInches - w) < 0.05f &&
                    kotlin.math.abs(it.heightInches - h) < 0.05f
            } ?: CUSTOM
        }
    }
}

enum class DrawingTool(
    val id: String,
    val labelBn: String
) {
    PEN("pen", "কলম"),
    BRUSH("brush", "তুলি"),
    HIGHLIGHTER("highlighter", "হাইলাইটার"),
    LINE("line", "সরলরেখা"),
    ERASER("eraser", "ইরেজার");

    companion object {
        fun fromId(id: String): DrawingTool = entries.firstOrNull { it.id == id } ?: PEN
    }
}

enum class PoemTextAlign(
    val id: String,
    val labelBn: String
) {
    CENTER("center", "মাঝখানে (বই)"),
    LEFT("left", "বামে"),
    NOTEBOOK_INDENT("indent", "খাতার ছন্দ (বিন্যাস)");

    companion object {
        fun fromId(id: String): PoemTextAlign = entries.firstOrNull { it.id == id } ?: CENTER
    }
}

data class PaperThemePreset(
    val id: String,
    val labelBn: String,
    val paperHex: Long,
    val inkHex: Long,
    val accentHex: Long
)

val PaperThemePresets = listOf(
    PaperThemePreset("cream", "ক্রিম পাণ্ডুলিপি", 0xFFF7F2E7L, 0xFF1D1A16L, 0xFFB85D3BL),
    PaperThemePreset("ivory", "শুভ্র কাগজ", 0xFFFFFFFFL, 0xFF1A1D24L, 0xFF2B4570L),
    PaperThemePreset("parchment", "প্রাচীন পুঁথি", 0xFFF0E4CCL, 0xFF2F2116L, 0xFF8C3B2BL),
    PaperThemePreset("sage", "স্নিগ্ধ সবুজ", 0xFFEBF2ECL, 0xFF1A2B23L, 0xFF3A6B5CL),
    PaperThemePreset("blush", "সন্ধ্যা গোলাপি", 0xFFF7ECEEL, 0xFF2D1B22L, 0xFF9E475BL),
    PaperThemePreset("midnight", "রাত্রি ক্যানভাস", 0xFF182032L, 0xFFF4EFE4L, 0xFFE5BA6FL)
)

val CuratedDrawingColors = listOf(
    0xFF1D1A16L, // Charcoal Ink
    0xFF1C2541L, // Deep Indigo
    0xFFB85D3BL, // Terracotta
    0xFF8B1E3FL, // Crimson Rose
    0xFF3A6B5CL, // Forest Sage
    0xFFC8963EL, // Antique Gold
    0xFF3B6E8CL, // River Teal
    0xFF6B4C7AL, // Plum Ink
    0xFFF4EFE4L  // Cream White
)

data class NormalizedPoint(
    val x: Float, // 0f..1f relative to canvas width
    val y: Float  // 0f..1f relative to canvas height
)

data class DrawStroke(
    val id: String,
    val tool: DrawingTool,
    val colorArgb: Long,
    val strokeWidthNorm: Float, // width relative to 400dp base canvas width
    val alpha: Float,
    val points: List<NormalizedPoint>
)

enum class AiImageModel(
    val modelId: String,
    val labelBn: String,
    val descriptionBn: String
) {
    NANO_BANANA_2_1(
        modelId = "gemini-nano-banana-2.1",
        labelBn = "Nano Banana 2.1",
        descriptionBn = "দ্রুত কবিতা-থেকে-ছবি ও এডিটিং"
    ),
    GEMINI_3_PRO_IMAGE(
        modelId = "gemini-3-pro-image-preview",
        labelBn = "Gemini 3 Pro Image",
        descriptionBn = "উচ্চ-মানের ১K / ২K / ৪K প্রো আর্টওয়ার্ক"
    )
}

enum class AiImageSize(val label: String, val pixels: Int) {
    SIZE_1K("1K", 1024),
    SIZE_2K("2K", 2048),
    SIZE_4K("4K", 4096)
}

enum class StudioTab(val labelBn: String) {
    POEM("কবিতা ও ফন্ট"),
    DRAW("ড্রয়িং টুল"),
    ART("ছবি ও AI"),
    OCR("খাতা OCR")
}

data class ManuscriptPage(
    val id: Long = 0L,
    val title: String,
    val author: String,
    val poemBody: String,
    val pageLabel: String,
    val canvasWidthInches: Float = 5.5f,
    val canvasHeightInches: Float = 8.5f,
    val fontId: String = "noto_serif",
    val fontSizeSp: Float = 14f,
    val titleSizeSp: Float = 24f,
    val lineHeightMultiplier: Float = 1.48f,
    val isBold: Boolean = true,
    val textAlign: PoemTextAlign = PoemTextAlign.CENTER,
    val paperThemeId: String = "cream",
    val paperColorArgb: Long = 0xFFF7F2E7L,
    val inkColorArgb: Long = 0xFF1D1A16L,
    val accentColorArgb: Long = 0xFFB85D3BL,
    val showDivider: Boolean = true,
    val showBorder: Boolean = true,
    val autoFitText: Boolean = true,
    val backgroundImagePath: String? = null,
    val backgroundImageAlpha: Float = 0.28f,
    val strokes: List<DrawStroke> = emptyList(),
    val extractedPalette: List<Long> = emptyList(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val aspectRatio: Float
        get() = (canvasWidthInches / canvasHeightInches).coerceIn(0.35f, 2.5f)
}

object StrokeJsonSerializer {
    fun encodeStrokes(strokes: List<DrawStroke>): String {
        val arr = JSONArray()
        strokes.forEach { stroke ->
            val obj = JSONObject().apply {
                put("id", stroke.id)
                put("tool", stroke.tool.id)
                put("color", stroke.colorArgb)
                put("width", stroke.strokeWidthNorm.toDouble())
                put("alpha", stroke.alpha.toDouble())
                val pts = JSONArray()
                stroke.points.forEach { p ->
                    pts.put(JSONArray().put(p.x.toDouble()).put(p.y.toDouble()))
                }
                put("points", pts)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decodeStrokes(json: String): List<DrawStroke> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val ptsArr = obj.getJSONArray("points")
                    val pts = buildList {
                        for (j in 0 until ptsArr.length()) {
                            val pair = ptsArr.getJSONArray(j)
                            add(NormalizedPoint(pair.getDouble(0).toFloat(), pair.getDouble(1).toFloat()))
                        }
                    }
                    add(
                        DrawStroke(
                            id = obj.optString("id", "s_$i"),
                            tool = DrawingTool.fromId(obj.optString("tool", "pen")),
                            colorArgb = obj.optLong("color", 0xFF1D1A16L),
                            strokeWidthNorm = obj.optDouble("width", 4.0).toFloat(),
                            alpha = obj.optDouble("alpha", 1.0).toFloat(),
                            points = pts
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun encodeColors(colors: List<Long>): String {
        val arr = JSONArray()
        colors.forEach { arr.put(it) }
        return arr.toString()
    }

    fun decodeColors(json: String): List<Long> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    add(arr.getLong(i))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

fun Long.toComposeColor(): Color = Color(this.toInt())
