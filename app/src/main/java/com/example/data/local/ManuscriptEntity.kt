package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ManuscriptPage
import com.example.model.PoemTextAlign
import com.example.model.StrokeJsonSerializer

@Entity(tableName = "manuscripts")
data class ManuscriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val author: String,
    val poemBody: String,
    val pageLabel: String,
    val canvasWidthInches: Float,
    val canvasHeightInches: Float,
    val fontId: String,
    val fontSizeSp: Float,
    val titleSizeSp: Float,
    val lineHeightMultiplier: Float,
    val isBold: Boolean,
    val textAlignId: String,
    val paperThemeId: String,
    val paperColorArgb: Long,
    val inkColorArgb: Long,
    val accentColorArgb: Long,
    val showDivider: Boolean,
    val showBorder: Boolean,
    val autoFitText: Boolean,
    val backgroundImagePath: String?,
    val backgroundImageAlpha: Float,
    val strokesJson: String,
    val extractedPaletteJson: String,
    val updatedAtMillis: Long
) {
    fun toDomain(): ManuscriptPage = ManuscriptPage(
        id = id,
        title = title,
        author = author,
        poemBody = poemBody,
        pageLabel = pageLabel,
        canvasWidthInches = canvasWidthInches,
        canvasHeightInches = canvasHeightInches,
        fontId = fontId,
        fontSizeSp = fontSizeSp,
        titleSizeSp = titleSizeSp,
        lineHeightMultiplier = lineHeightMultiplier,
        isBold = isBold,
        textAlign = PoemTextAlign.fromId(textAlignId),
        paperThemeId = paperThemeId,
        paperColorArgb = paperColorArgb,
        inkColorArgb = inkColorArgb,
        accentColorArgb = accentColorArgb,
        showDivider = showDivider,
        showBorder = showBorder,
        autoFitText = autoFitText,
        backgroundImagePath = backgroundImagePath,
        backgroundImageAlpha = backgroundImageAlpha,
        strokes = StrokeJsonSerializer.decodeStrokes(strokesJson),
        extractedPalette = StrokeJsonSerializer.decodeColors(extractedPaletteJson),
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(page: ManuscriptPage): ManuscriptEntity = ManuscriptEntity(
            id = page.id,
            title = page.title,
            author = page.author,
            poemBody = page.poemBody,
            pageLabel = page.pageLabel,
            canvasWidthInches = page.canvasWidthInches,
            canvasHeightInches = page.canvasHeightInches,
            fontId = page.fontId,
            fontSizeSp = page.fontSizeSp,
            titleSizeSp = page.titleSizeSp,
            lineHeightMultiplier = page.lineHeightMultiplier,
            isBold = page.isBold,
            textAlignId = page.textAlign.id,
            paperThemeId = page.paperThemeId,
            paperColorArgb = page.paperColorArgb,
            inkColorArgb = page.inkColorArgb,
            accentColorArgb = page.accentColorArgb,
            showDivider = page.showDivider,
            showBorder = page.showBorder,
            autoFitText = page.autoFitText,
            backgroundImagePath = page.backgroundImagePath,
            backgroundImageAlpha = page.backgroundImageAlpha,
            strokesJson = StrokeJsonSerializer.encodeStrokes(page.strokes),
            extractedPaletteJson = StrokeJsonSerializer.encodeColors(page.extractedPalette),
            updatedAtMillis = page.updatedAtMillis
        )
    }
}
