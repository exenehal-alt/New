package com.example.data.local

import com.example.model.ManuscriptPage
import com.example.model.PoemTextAlign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ManuscriptRepository(private val dao: ManuscriptDao) {

    val allPages: Flow<List<ManuscriptPage>> = dao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.upsert(ManuscriptEntity.fromDomain(createStarterManuscript()))
        }
    }

    suspend fun savePage(page: ManuscriptPage): Long {
        val updated = page.copy(updatedAtMillis = System.currentTimeMillis())
        return dao.upsert(ManuscriptEntity.fromDomain(updated))
    }

    suspend fun createNewPage(pageNumber: Int): Long {
        val bengaliDigits = pageNumber.toString()
            .map { ch -> if (ch in '0'..'9') ('০' + (ch - '0')) else ch }
            .joinToString("")
            .padStart(2, '০')
        val blank = ManuscriptPage(
            title = "নতুন কবিতা",
            author = "কবির নাম",
            poemBody = "এখানে আপনার কবিতার লাইনগুলো লিখুন,\nঅথবা খাতার ছবি থেকে লেখা রূপান্তর করুন।",
            pageLabel = "পৃষ্ঠা - $bengaliDigits",
            canvasWidthInches = 5.5f,
            canvasHeightInches = 8.5f,
            isBold = true,
            fontSizeSp = 14f,
            autoFitText = true
        )
        return dao.upsert(ManuscriptEntity.fromDomain(blank))
    }

    suspend fun deletePage(id: Long) {
        dao.deleteById(id)
    }

    companion object {
        fun createStarterManuscript(): ManuscriptPage = ManuscriptPage(
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
            canvasWidthInches = 5.5f,
            canvasHeightInches = 8.5f,
            fontId = "noto_serif",
            fontSizeSp = 14f,
            titleSizeSp = 24f,
            lineHeightMultiplier = 1.48f,
            isBold = true,
            textAlign = PoemTextAlign.CENTER,
            paperThemeId = "cream",
            paperColorArgb = 0xFFF7F2E7L,
            inkColorArgb = 0xFF1D1A16L,
            accentColorArgb = 0xFFB85D3BL,
            showDivider = true,
            showBorder = true,
            autoFitText = true
        )
    }
}
