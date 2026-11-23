package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiArtworkResult
import com.example.data.ai.AiPoetryRepository
import com.example.data.ai.HandwritingOcrResult
import com.example.data.ai.PlaceholderAiPoetryRepository
import com.example.data.local.ManuscriptRepository
import com.example.data.local.PandulipiDatabase
import com.example.data.local.PreferencesRepository
import com.example.data.local.StudioPreferences
import com.example.model.AiImageModel
import com.example.model.AiImageSize
import com.example.model.CanvasSizePreset
import com.example.model.DrawStroke
import com.example.model.DrawingTool
import com.example.model.ManuscriptPage
import com.example.model.NormalizedPoint
import com.example.model.PaperThemePreset
import com.example.model.PoemTextAlign
import com.example.model.StudioTab
import com.example.util.ColorExtractor
import com.example.util.ExtractedPhotoAnalysis
import com.example.util.PdfExportResult
import com.example.util.PdfExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AutoSaveStatus {
    data object Saved : AutoSaveStatus
    data object Saving : AutoSaveStatus
    data object UnsavedManual : AutoSaveStatus
}

sealed interface AsyncTaskState<out T> {
    data object Idle : AsyncTaskState<Nothing>
    data object Loading : AsyncTaskState<Nothing>
    data class Success<T>(val data: T) : AsyncTaskState<T>
    data class Error(val message: String) : AsyncTaskState<Nothing>
}

data class StudioUiState(
    val activeTab: StudioTab = StudioTab.POEM,
    val currentPage: ManuscriptPage = ManuscriptRepository.createStarterManuscript(),
    val isLoaded: Boolean = false,
    val isDrawMode: Boolean = false,
    val selectedTool: DrawingTool = DrawingTool.PEN,
    val brushColorArgb: Long = 0xFF1C2541L,
    val strokeWidthNorm: Float = 5.5f,
    val brushAlpha: Float = 1.0f,
    val redoStack: List<DrawStroke> = emptyList(),
    val autoSaveStatus: AutoSaveStatus = AutoSaveStatus.Saved,
    val referenceAnalysis: ExtractedPhotoAnalysis? = null,
    val ocrPhotoPath: String? = null,
    val ocrPhotoRatio: Float? = null,
    val ocrPhotoWidthIn: Float? = null,
    val ocrPhotoHeightIn: Float? = null,
    val aiArtworkState: AsyncTaskState<AiArtworkResult> = AsyncTaskState.Idle,
    val ocrState: AsyncTaskState<HandwritingOcrResult> = AsyncTaskState.Idle,
    val pdfExportState: AsyncTaskState<PdfExportResult> = AsyncTaskState.Idle,
    val statusMessage: String? = null
)

class PandulipiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PandulipiDatabase.getInstance(application)
    private val manuscriptRepository = ManuscriptRepository(database.manuscriptDao())
    private val preferencesRepository = PreferencesRepository(application)
    val aiRepository: AiPoetryRepository = PlaceholderAiPoetryRepository()

    val preferences: StateFlow<StudioPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), StudioPreferences())

    val allPages: StateFlow<List<ManuscriptPage>> = manuscriptRepository.allPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            manuscriptRepository.ensureSeeded()
            preferencesRepository.preferences.collectLatest { prefs ->
                allPages.collectLatest { pages ->
                    if (pages.isNotEmpty() && !_uiState.value.isLoaded) {
                        val target = pages.firstOrNull { it.id == prefs.selectedPageId } ?: pages.first()
                        _uiState.update {
                            it.copy(
                                currentPage = target,
                                isLoaded = true,
                                autoSaveStatus = AutoSaveStatus.Saved
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tab: StudioTab) {
        _uiState.update {
            it.copy(
                activeTab = tab,
                isDrawMode = tab == StudioTab.DRAW
            )
        }
    }

    fun toggleDrawMode(enabled: Boolean) {
        _uiState.update { it.copy(isDrawMode = enabled) }
    }

    private fun updatePage(transform: (ManuscriptPage) -> ManuscriptPage) {
        _uiState.update { state ->
            val updated = transform(state.currentPage)
            state.copy(
                currentPage = updated,
                autoSaveStatus = if (preferences.value.autoSaveEnabled) AutoSaveStatus.Saving else AutoSaveStatus.UnsavedManual
            )
        }
        scheduleAutoSaveIfEnabled()
    }

    private fun scheduleAutoSaveIfEnabled() {
        autoSaveJob?.cancel()
        if (!preferences.value.autoSaveEnabled) return
        autoSaveJob = viewModelScope.launch {
            delay(550L)
            persistCurrentPage()
        }
    }

    fun saveNow() {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            persistCurrentPage()
        }
    }

    private suspend fun persistCurrentPage() {
        val page = _uiState.value.currentPage
        val savedId = manuscriptRepository.savePage(page)
        if (page.id == 0L && savedId > 0L) {
            _uiState.update { it.copy(currentPage = it.currentPage.copy(id = savedId)) }
            preferencesRepository.setSelectedPageId(savedId)
        }
        _uiState.update { it.copy(autoSaveStatus = AutoSaveStatus.Saved) }
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSaveEnabled(enabled)
            if (enabled) {
                persistCurrentPage()
            }
        }
    }

    // Poem & Typography edits
    fun updateTitle(title: String) = updatePage { it.copy(title = title) }
    fun updateAuthor(author: String) = updatePage { it.copy(author = author) }
    fun updatePoemBody(body: String) = updatePage { it.copy(poemBody = body) }
    fun updatePageLabel(label: String) = updatePage { it.copy(pageLabel = label) }

    fun setFontFamily(fontId: String) = updatePage { it.copy(fontId = fontId) }
    fun setBold(bold: Boolean) = updatePage { it.copy(isBold = bold) }
    fun setFontSize(sizeSp: Float) = updatePage { it.copy(fontSizeSp = sizeSp.coerceIn(10f, 32f)) }
    fun setTitleSize(sizeSp: Float) = updatePage { it.copy(titleSizeSp = sizeSp.coerceIn(16f, 40f)) }
    fun setLineHeight(mult: Float) = updatePage { it.copy(lineHeightMultiplier = mult.coerceIn(1.15f, 2.2f)) }
    fun setTextAlign(align: PoemTextAlign) = updatePage { it.copy(textAlign = align) }
    fun setAutoFitText(autoFit: Boolean) = updatePage { it.copy(autoFitText = autoFit) }
    fun setShowDivider(show: Boolean) = updatePage { it.copy(showDivider = show) }
    fun setShowBorder(show: Boolean) = updatePage { it.copy(showBorder = show) }

    fun applyPaperTheme(preset: PaperThemePreset) = updatePage {
        it.copy(
            paperThemeId = preset.id,
            paperColorArgb = preset.paperHex,
            inkColorArgb = preset.inkHex,
            accentColorArgb = preset.accentHex
        )
    }

    fun setCustomColors(paperArgb: Long? = null, inkArgb: Long? = null, accentArgb: Long? = null) {
        updatePage { page ->
            page.copy(
                paperThemeId = "custom",
                paperColorArgb = paperArgb ?: page.paperColorArgb,
                inkColorArgb = inkArgb ?: page.inkColorArgb,
                accentColorArgb = accentArgb ?: page.accentColorArgb
            )
        }
    }

    fun applyCanvasPreset(preset: CanvasSizePreset) {
        if (preset == CanvasSizePreset.PHOTO_MATCH) {
            val analysis = _uiState.value.referenceAnalysis
            if (analysis != null) {
                setCanvasInches(analysis.matchedWidthInches, analysis.matchedHeightInches)
            } else {
                _uiState.update { it.copy(statusMessage = "প্রথমে একটি ছবি আপলোড করুন, তাহলে ছবির মাপে ক্যানভাস সেট হবে।") }
            }
            return
        }
        setCanvasInches(preset.widthInches, preset.heightInches)
    }

    fun setCanvasInches(widthIn: Float, heightIn: Float) {
        val w = ((widthIn.coerceIn(3.0f, 12.0f) * 10f).toInt()) / 10f
        val h = ((heightIn.coerceIn(3.0f, 14.0f) * 10f).toInt()) / 10f
        updatePage { it.copy(canvasWidthInches = w, canvasHeightInches = h) }
    }

    // Drawing toolbox actions
    fun selectDrawingTool(tool: DrawingTool) {
        val defaultAlpha = when (tool) {
            DrawingTool.PEN -> 1.0f
            DrawingTool.BRUSH -> 0.65f
            DrawingTool.HIGHLIGHTER -> 0.32f
            DrawingTool.LINE -> 0.95f
            DrawingTool.ERASER -> 1.0f
        }
        val defaultWidth = when (tool) {
            DrawingTool.PEN -> 4.5f
            DrawingTool.BRUSH -> 14f
            DrawingTool.HIGHLIGHTER -> 22f
            DrawingTool.LINE -> 4f
            DrawingTool.ERASER -> 20f
        }
        _uiState.update {
            it.copy(
                selectedTool = tool,
                brushAlpha = defaultAlpha,
                strokeWidthNorm = defaultWidth,
                isDrawMode = true
            )
        }
    }

    fun setBrushColor(colorArgb: Long) {
        _uiState.update { it.copy(brushColorArgb = colorArgb) }
    }

    fun setStrokeWidth(widthNorm: Float) {
        _uiState.update { it.copy(strokeWidthNorm = widthNorm.coerceIn(1.5f, 40f)) }
    }

    fun setBrushOpacity(alpha: Float) {
        _uiState.update { it.copy(brushAlpha = alpha.coerceIn(0.15f, 1.0f)) }
    }

    fun addStroke(points: List<NormalizedPoint>) {
        if (points.isEmpty()) return
        val state = _uiState.value
        val newStroke = DrawStroke(
            id = "st_${System.currentTimeMillis()}_${state.currentPage.strokes.size}",
            tool = state.selectedTool,
            colorArgb = state.brushColorArgb,
            strokeWidthNorm = state.strokeWidthNorm,
            alpha = state.brushAlpha,
            points = points
        )
        _uiState.update { it.copy(redoStack = emptyList()) }
        updatePage { it.copy(strokes = it.strokes + newStroke) }
    }

    fun undoStroke() {
        val strokes = _uiState.value.currentPage.strokes
        if (strokes.isEmpty()) return
        val removed = strokes.last()
        _uiState.update { it.copy(redoStack = it.redoStack + removed) }
        updatePage { it.copy(strokes = strokes.dropLast(1)) }
    }

    fun redoStroke() {
        val redo = _uiState.value.redoStack
        if (redo.isEmpty()) return
        val restored = redo.last()
        _uiState.update { it.copy(redoStack = redo.dropLast(1)) }
        updatePage { it.copy(strokes = it.strokes + restored) }
    }

    fun clearAllStrokes() {
        if (_uiState.value.currentPage.strokes.isEmpty()) return
        _uiState.update { it.copy(redoStack = emptyList()) }
        updatePage { it.copy(strokes = emptyList()) }
    }

    // Reference Photo & Color Extraction
    fun importReferencePhoto(context: Context, uri: Uri, applyAsBackground: Boolean, matchCanvasSize: Boolean) {
        viewModelScope.launch {
            val result = ColorExtractor.importAndAnalyzePhoto(context, uri, baseWidthInches = 5.5f)
            result.onSuccess { analysis ->
                _uiState.update {
                    it.copy(
                        referenceAnalysis = analysis,
                        brushColorArgb = analysis.suggestedAccentArgb,
                        statusMessage = "ছবি থেকে ৬টি রঙ এবং ${analysis.matchedWidthInches}\" × ${analysis.matchedHeightInches}\" অনুপাত সংগ্রহ করা হয়েছে।"
                    )
                }
                updatePage { page ->
                    page.copy(
                        extractedPalette = analysis.paletteArgb,
                        backgroundImagePath = if (applyAsBackground) analysis.savedFilePath else page.backgroundImagePath,
                        backgroundImageAlpha = if (applyAsBackground) 0.34f else page.backgroundImageAlpha,
                        canvasWidthInches = if (matchCanvasSize) analysis.matchedWidthInches else page.canvasWidthInches,
                        canvasHeightInches = if (matchCanvasSize) analysis.matchedHeightInches else page.canvasHeightInches,
                        autoFitText = true
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(statusMessage = err.localizedMessage ?: "ছবি লোড করা যায়নি।") }
            }
        }
    }

    fun applyExtractedStyleToCanvas() {
        val analysis = _uiState.value.referenceAnalysis ?: return
        updatePage { page ->
            page.copy(
                paperThemeId = "photo_style",
                paperColorArgb = analysis.suggestedPaperArgb,
                inkColorArgb = analysis.suggestedInkArgb,
                accentColorArgb = analysis.suggestedAccentArgb,
                extractedPalette = analysis.paletteArgb
            )
        }
        _uiState.update {
            it.copy(
                brushColorArgb = analysis.suggestedAccentArgb,
                statusMessage = "ছবির রঙ ও শৈলী ক্যানভাসে প্রয়োগ করা হয়েছে।"
            )
        }
    }

    fun setBackgroundImageAlpha(alpha: Float) = updatePage {
        it.copy(backgroundImageAlpha = alpha.coerceIn(0.05f, 1.0f))
    }

    fun clearBackgroundImage() = updatePage {
        it.copy(backgroundImagePath = null)
    }

    // AI Model preferences & Artwork generation
    fun setPreferredAiModel(model: AiImageModel) {
        viewModelScope.launch { preferencesRepository.setPreferredAiModel(model) }
    }

    fun setPreferredImageSize(size: AiImageSize) {
        viewModelScope.launch { preferencesRepository.setPreferredImageSize(size) }
    }

    fun generatePoemArtwork(context: Context, stylePrompt: String, useReferenceIfAvailable: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiArtworkState = AsyncTaskState.Loading) }
            val page = _uiState.value.currentPage
            val prefs = preferences.value
            val refBitmap = if (useReferenceIfAvailable) {
                _uiState.value.referenceAnalysis?.savedFilePath?.let { ColorExtractor.decodeBoundedBitmap(it, 768) }
            } else null

            val result = if (refBitmap != null) {
                val res = aiRepository.createArtworkFromReference(
                    context = context,
                    title = page.title,
                    poemBody = page.poemBody,
                    referenceBitmap = refBitmap,
                    paletteArgb = page.extractedPalette,
                    stylePrompt = stylePrompt,
                    model = prefs.preferredAiModel,
                    imageSize = prefs.preferredImageSize,
                    aspectRatio = page.aspectRatio
                )
                refBitmap.recycle()
                res
            } else {
                aiRepository.transformPoemToArtwork(
                    context = context,
                    title = page.title,
                    poemBody = page.poemBody,
                    stylePrompt = stylePrompt,
                    paletteArgb = page.extractedPalette,
                    model = prefs.preferredAiModel,
                    imageSize = prefs.preferredImageSize,
                    aspectRatio = page.aspectRatio
                )
            }

            result.onSuccess { art ->
                _uiState.update { it.copy(aiArtworkState = AsyncTaskState.Success(art)) }
                updatePage {
                    it.copy(
                        backgroundImagePath = art.savedImagePath,
                        backgroundImageAlpha = 0.35f
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(aiArtworkState = AsyncTaskState.Error(err.localizedMessage ?: "আর্টওয়ার্ক তৈরি করা যায়নি।"))
                }
            }
        }
    }

    // Handwritten Notebook OCR
    fun importNotebookPhotoForOcr(context: Context, uri: Uri, matchCanvasBounds: Boolean) {
        viewModelScope.launch {
            val result = ColorExtractor.importAndAnalyzePhoto(context, uri, baseWidthInches = 5.5f)
            result.onSuccess { analysis ->
                _uiState.update {
                    it.copy(
                        ocrPhotoPath = analysis.savedFilePath,
                        ocrPhotoRatio = analysis.pixelWidth.toFloat() / analysis.pixelHeight.toFloat(),
                        ocrPhotoWidthIn = analysis.matchedWidthInches,
                        ocrPhotoHeightIn = analysis.matchedHeightInches,
                        ocrState = AsyncTaskState.Idle
                    )
                }
                if (matchCanvasBounds) {
                    updatePage {
                        it.copy(
                            canvasWidthInches = analysis.matchedWidthInches,
                            canvasHeightInches = analysis.matchedHeightInches,
                            autoFitText = true
                        )
                    }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(statusMessage = err.localizedMessage ?: "খাতার ছবি লোড করা যায়নি।") }
            }
        }
    }

    fun convertNotebookHandwritingToDigital() {
        val path = _uiState.value.ocrPhotoPath ?: run {
            _uiState.update { it.copy(statusMessage = "প্রথমে খাতায় লেখা কবিতার একটি ছবি নির্বাচন করুন।") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(ocrState = AsyncTaskState.Loading) }
            val bmp = ColorExtractor.decodeBoundedBitmap(path, 1280)
            if (bmp == null) {
                _uiState.update { it.copy(ocrState = AsyncTaskState.Error("ছবি ডিকোড করা যায়নি।")) }
                return@launch
            }
            val result = aiRepository.transcribeHandwrittenNotebook(bmp)
            bmp.recycle()
            result.onSuccess { ocr ->
                _uiState.update { it.copy(ocrState = AsyncTaskState.Success(ocr)) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(ocrState = AsyncTaskState.Error(err.localizedMessage ?: "হাতের লেখা রূপান্তর ব্যর্থ হয়েছে।"))
                }
            }
        }
    }

    fun applyOcrToCanvas(
        title: String,
        author: String,
        body: String,
        pageLabel: String,
        makeBold: Boolean,
        matchPhotoSize: Boolean
    ) {
        val wIn = _uiState.value.ocrPhotoWidthIn
        val hIn = _uiState.value.ocrPhotoHeightIn
        updatePage { page ->
            page.copy(
                title = title,
                author = author,
                poemBody = body,
                pageLabel = pageLabel,
                isBold = makeBold,
                autoFitText = true,
                canvasWidthInches = if (matchPhotoSize && wIn != null) wIn else page.canvasWidthInches,
                canvasHeightInches = if (matchPhotoSize && hIn != null) hIn else page.canvasHeightInches
            )
        }
        _uiState.update {
            it.copy(
                activeTab = StudioTab.POEM,
                isDrawMode = false,
                statusMessage = "ডিজিটাল Bold কবিতা ক্যানভাসে যুক্ত হয়েছে।"
            )
        }
    }

    // Page management
    fun selectPage(page: ManuscriptPage) {
        viewModelScope.launch {
            if (preferences.value.autoSaveEnabled) {
                persistCurrentPage()
            }
            preferencesRepository.setSelectedPageId(page.id)
            _uiState.update {
                it.copy(
                    currentPage = page,
                    redoStack = emptyList(),
                    autoSaveStatus = AutoSaveStatus.Saved
                )
            }
        }
    }

    fun addNewPage() {
        viewModelScope.launch {
            if (preferences.value.autoSaveEnabled) {
                persistCurrentPage()
            }
            val nextNum = allPages.value.size + 1
            val newId = manuscriptRepository.createNewPage(nextNum)
            preferencesRepository.setSelectedPageId(newId)
            val created = allPages.value.firstOrNull { it.id == newId }
            if (created != null) {
                _uiState.update { it.copy(currentPage = created, redoStack = emptyList()) }
            }
        }
    }

    fun deletePage(pageId: Long) {
        viewModelScope.launch {
            val pages = allPages.value
            if (pages.size <= 1) {
                _uiState.update { it.copy(statusMessage = "অন্তত একটি পাণ্ডুলিপি পৃষ্ঠা থাকতে হবে।") }
                return@launch
            }
            manuscriptRepository.deletePage(pageId)
            if (_uiState.value.currentPage.id == pageId) {
                val fallback = pages.firstOrNull { it.id != pageId } ?: ManuscriptRepository.createStarterManuscript()
                preferencesRepository.setSelectedPageId(fallback.id)
                _uiState.update { it.copy(currentPage = fallback, redoStack = emptyList()) }
            }
        }
    }

    // PDF Export
    fun exportToPdf(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(pdfExportState = AsyncTaskState.Loading) }
            persistCurrentPage()
            val result = PdfExporter.exportManuscriptToPdf(context, _uiState.value.currentPage)
            result.onSuccess { pdf ->
                _uiState.update { it.copy(pdfExportState = AsyncTaskState.Success(pdf)) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(pdfExportState = AsyncTaskState.Error(err.localizedMessage ?: "PDF তৈরি করা যায়নি।"))
                }
            }
        }
    }

    fun dismissPdfExportDialog() {
        _uiState.update { it.copy(pdfExportState = AsyncTaskState.Idle) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
