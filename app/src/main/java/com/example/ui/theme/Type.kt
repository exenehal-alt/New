package com.example.ui.theme

import androidx.annotation.FontRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NotoSerifBengaliFamily = FontFamily(
    Font(R.font.noto_serif_bengali, FontWeight.Normal),
    Font(R.font.noto_serif_bengali, FontWeight.Medium),
    Font(R.font.noto_serif_bengali, FontWeight.SemiBold),
    Font(R.font.noto_serif_bengali, FontWeight.Bold)
)

val NotoSansBengaliFamily = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal),
    Font(R.font.noto_sans_bengali, FontWeight.Medium),
    Font(R.font.noto_sans_bengali, FontWeight.SemiBold),
    Font(R.font.noto_sans_bengali, FontWeight.Bold)
)

val HindSiliguriFamily = FontFamily(
    Font(R.font.hind_siliguri, FontWeight.Normal),
    Font(R.font.hind_siliguri, FontWeight.Medium),
    Font(R.font.hind_siliguri, FontWeight.SemiBold),
    Font(R.font.hind_siliguri, FontWeight.Bold)
)

val TiroBanglaFamily = FontFamily(
    Font(R.font.tiro_bangla, FontWeight.Normal),
    Font(R.font.tiro_bangla, FontWeight.Medium),
    Font(R.font.tiro_bangla, FontWeight.SemiBold),
    Font(R.font.tiro_bangla, FontWeight.Bold)
)

val GaladaFamily = FontFamily(
    Font(R.font.galada, FontWeight.Normal),
    Font(R.font.galada, FontWeight.Bold)
)

val AtmaFamily = FontFamily(
    Font(R.font.atma, FontWeight.Normal),
    Font(R.font.atma, FontWeight.Medium),
    Font(R.font.atma, FontWeight.SemiBold),
    Font(R.font.atma, FontWeight.Bold)
)

val BalooDa2Family = FontFamily(
    Font(R.font.baloo_da_2, FontWeight.Normal),
    Font(R.font.baloo_da_2, FontWeight.Medium),
    Font(R.font.baloo_da_2, FontWeight.SemiBold),
    Font(R.font.baloo_da_2, FontWeight.Bold)
)

val MinaFamily = FontFamily(
    Font(R.font.mina, FontWeight.Normal),
    Font(R.font.mina, FontWeight.Bold)
)

enum class BengaliFontOption(
    val id: String,
    val displayNameBn: String,
    val displayNameEn: String,
    val styleTag: String,
    val fontFamily: FontFamily,
    @FontRes val fontResId: Int
) {
    NOTO_SERIF(
        id = "noto_serif",
        displayNameBn = "নোটো সেরিফ বাংলা",
        displayNameEn = "Noto Serif Bengali",
        styleTag = "বই ও ছাপা (Book Serif)",
        fontFamily = NotoSerifBengaliFamily,
        fontResId = R.font.noto_serif_bengali
    ),
    TIRO_BANGLA(
        id = "tiro_bangla",
        displayNameBn = "তিরো বাংলা",
        displayNameEn = "Tiro Bangla",
        styleTag = "ক্লাসিক সাহিত্য (Literary)",
        fontFamily = TiroBanglaFamily,
        fontResId = R.font.tiro_bangla
    ),
    NOTO_SANS(
        id = "noto_sans",
        displayNameBn = "নোটো স্যান্স বাংলা",
        displayNameEn = "Noto Sans Bengali",
        styleTag = "আধুনিক ও স্পষ্ট (Modern)",
        fontFamily = NotoSansBengaliFamily,
        fontResId = R.font.noto_sans_bengali
    ),
    HIND_SILIGURI(
        id = "hind_siliguri",
        displayNameBn = "হিন্দ শিলিগুড়ি",
        displayNameEn = "Hind Siliguri",
        styleTag = "সম্পাদকীয় (Editorial)",
        fontFamily = HindSiliguriFamily,
        fontResId = R.font.hind_siliguri
    ),
    GALADA(
        id = "galada",
        displayNameBn = "গলাদা ক্যালিগ্রাফি",
        displayNameEn = "Galada",
        styleTag = "শিল্পিত হাতের ছোঁয়া (Calligraphy)",
        fontFamily = GaladaFamily,
        fontResId = R.font.galada
    ),
    ATMA(
        id = "atma",
        displayNameBn = "আত্মা ব্রাশ",
        displayNameEn = "Atma",
        styleTag = "তুলি ও চিত্র (Brush)",
        fontFamily = AtmaFamily,
        fontResId = R.font.atma
    ),
    BALOO_DA_2(
        id = "baloo_da_2",
        displayNameBn = "বালু দা ২",
        displayNameEn = "Baloo Da 2",
        styleTag = "কোমল ও বোল্ড (Rounded Bold)",
        fontFamily = BalooDa2Family,
        fontResId = R.font.baloo_da_2
    ),
    MINA(
        id = "mina",
        displayNameBn = "মিনা",
        displayNameEn = "Mina",
        styleTag = "জ্যামিতিক (Geometric)",
        fontFamily = MinaFamily,
        fontResId = R.font.mina
    );

    companion object {
        fun fromId(id: String): BengaliFontOption =
            entries.firstOrNull { it.id == id } ?: NOTO_SERIF
    }
}

val Typography = Typography(
    headlineSmall = TextStyle(
        fontFamily = NotoSerifBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSerifBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
