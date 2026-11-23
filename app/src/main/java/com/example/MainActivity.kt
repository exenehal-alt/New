package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.StudioPreferences
import com.example.model.CanvasSizePreset
import com.example.model.ManuscriptPage
import com.example.model.StudioTab
import com.example.ui.AsyncTaskState
import com.example.ui.AutoSaveStatus
import com.example.ui.PandulipiViewModel
import com.example.ui.StudioUiState
import com.example.ui.components.ArtStudioPanel
import com.example.ui.components.CanvasSizeDialog
import com.example.ui.components.DrawingToolboxPanel
import com.example.ui.components.HandwritingOcrPanel
import com.example.ui.components.ManuscriptCanvasStage
import com.example.ui.components.PagesManagerDialog
import com.example.ui.components.PdfExportResultDialog
import com.example.ui.components.PoemTypographyPanel
import com.example.ui.components.SyncAndAutoSaveDialog
import com.example.ui.theme.PandulipiTheme
import com.example.util.PdfExportResult

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PandulipiTheme {
                PandulipiStudioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandulipiStudioApp(
    viewModel: PandulipiViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val pages by viewModel.allPages.collectAsStateWithLifecycle()

    var showSizeDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showPagesDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(uiState.pdfExportState) {
        val err = (uiState.pdfExportState as? AsyncTaskState.Error)?.message
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(err)
            viewModel.dismissPdfExportDialog()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pandulipi",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                        SyncStatusChip(
                            autoSaveEnabled = prefs.autoSaveEnabled,
                            status = uiState.autoSaveStatus,
                            onClick = { showSyncDialog = true }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPagesDialog = true },
                        modifier = Modifier.testTag("pages_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CollectionsBookmark,
                            contentDescription = "পৃষ্ঠাসমূহ (${pages.size})"
                        )
                    }
                    Button(
                        onClick = { viewModel.exportToPdf(context) },
                        enabled = uiState.pdfExportState !is AsyncTaskState.Loading,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_pdf_button")
                    ) {
                        if (uiState.pdfExportState is AsyncTaskState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        },
        bottomBar = {
            BoxWithConstraints {
                if (maxWidth < 720.dp) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        StudioTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = uiState.activeTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(tabIcon(tab), contentDescription = tab.labelBn)
                                },
                                label = {
                                    Text(
                                        text = tab.labelBn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        StudioTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = uiState.activeTab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Icon(tabIcon(tab), contentDescription = tab.labelBn) },
                                label = { Text(tab.labelBn) }
                            )
                        }
                    }
                    VerticalDivider()
                    Box(
                        modifier = Modifier
                            .weight(1.08f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ManuscriptCanvasStage(
                            page = uiState.currentPage,
                            isDrawMode = uiState.isDrawMode,
                            selectedTool = uiState.selectedTool,
                            brushColorArgb = uiState.brushColorArgb,
                            strokeWidthNorm = uiState.strokeWidthNorm,
                            brushAlpha = uiState.brushAlpha,
                            canUndo = uiState.currentPage.strokes.isNotEmpty(),
                            canRedo = uiState.redoStack.isNotEmpty(),
                            onStrokeCompleted = viewModel::addStroke,
                            onUndo = viewModel::undoStroke,
                            onRedo = viewModel::redoStroke,
                            onToggleDrawMode = { viewModel.toggleDrawMode(!uiState.isDrawMode) },
                            onEnableAutoFit = { viewModel.setAutoFitText(true) },
                            onOpenSizePicker = { showSizeDialog = true }
                        )
                    }
                    VerticalDivider()
                    Surface(
                        modifier = Modifier
                            .weight(0.92f)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ActiveToolPanelContent(
                            uiState = uiState,
                            prefs = prefs,
                            viewModel = viewModel,
                            onOpenCanvasSizeDialog = { showSizeDialog = true }
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(0.51f)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        ManuscriptCanvasStage(
                            page = uiState.currentPage,
                            isDrawMode = uiState.isDrawMode,
                            selectedTool = uiState.selectedTool,
                            brushColorArgb = uiState.brushColorArgb,
                            strokeWidthNorm = uiState.strokeWidthNorm,
                            brushAlpha = uiState.brushAlpha,
                            canUndo = uiState.currentPage.strokes.isNotEmpty(),
                            canRedo = uiState.redoStack.isNotEmpty(),
                            onStrokeCompleted = viewModel::addStroke,
                            onUndo = viewModel::undoStroke,
                            onRedo = viewModel::redoStroke,
                            onToggleDrawMode = { viewModel.toggleDrawMode(!uiState.isDrawMode) },
                            onEnableAutoFit = { viewModel.setAutoFitText(true) },
                            onOpenSizePicker = { showSizeDialog = true }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Surface(
                        modifier = Modifier
                            .weight(0.49f)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ActiveToolPanelContent(
                            uiState = uiState,
                            prefs = prefs,
                            viewModel = viewModel,
                            onOpenCanvasSizeDialog = { showSizeDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showSizeDialog) {
        CanvasSizeDialog(
            currentWidthIn = uiState.currentPage.canvasWidthInches,
            currentHeightIn = uiState.currentPage.canvasHeightInches,
            hasReferencePhoto = uiState.referenceAnalysis != null,
            onApplyPreset = viewModel::applyCanvasPreset,
            onApplyCustom = viewModel::setCanvasInches,
            onDismiss = { showSizeDialog = false }
        )
    }

    if (showSyncDialog) {
        SyncAndAutoSaveDialog(
            autoSaveEnabled = prefs.autoSaveEnabled,
            onAutoSaveToggle = viewModel::setAutoSaveEnabled,
            onManualSave = viewModel::saveNow,
            onDismiss = { showSyncDialog = false }
        )
    }

    if (showPagesDialog) {
        PagesManagerDialog(
            pages = pages,
            currentPageId = uiState.currentPage.id,
            onSelectPage = viewModel::selectPage,
            onAddPage = viewModel::addNewPage,
            onDeletePage = viewModel::deletePage,
            onDismiss = { showPagesDialog = false }
        )
    }

    val pdfSuccess = (uiState.pdfExportState as? AsyncTaskState.Success<PdfExportResult>)?.data
    if (pdfSuccess != null) {
        PdfExportResultDialog(
            result = pdfSuccess,
            onDismiss = viewModel::dismissPdfExportDialog
        )
    }
}

@Composable
private fun ActiveToolPanelContent(
    uiState: StudioUiState,
    prefs: StudioPreferences,
    viewModel: PandulipiViewModel,
    onOpenCanvasSizeDialog: () -> Unit
) {
    val context = LocalContext.current
    when (uiState.activeTab) {
        StudioTab.POEM -> PoemTypographyPanel(
            page = uiState.currentPage,
            onTitleChange = viewModel::updateTitle,
            onAuthorChange = viewModel::updateAuthor,
            onPoemBodyChange = viewModel::updatePoemBody,
            onPageLabelChange = viewModel::updatePageLabel,
            onFontSelected = viewModel::setFontFamily,
            onBoldChanged = viewModel::setBold,
            onFontSizeChanged = viewModel::setFontSize,
            onLineHeightChanged = viewModel::setLineHeight,
            onTextAlignChanged = viewModel::setTextAlign,
            onAutoFitChanged = viewModel::setAutoFitText,
            onDividerChanged = viewModel::setShowDivider,
            onBorderChanged = viewModel::setShowBorder,
            onPaperThemeSelected = viewModel::applyPaperTheme,
            onCanvasPresetSelected = viewModel::applyCanvasPreset,
            onOpenCanvasSizeDialog = onOpenCanvasSizeDialog
        )

        StudioTab.DRAW -> DrawingToolboxPanel(
            state = uiState,
            onToggleDrawMode = viewModel::toggleDrawMode,
            onSelectTool = viewModel::selectDrawingTool,
            onSelectColor = viewModel::setBrushColor,
            onStrokeWidthChange = viewModel::setStrokeWidth,
            onOpacityChange = viewModel::setBrushOpacity,
            onUndo = viewModel::undoStroke,
            onRedo = viewModel::redoStroke,
            onClear = viewModel::clearAllStrokes
        )

        StudioTab.ART -> ArtStudioPanel(
            state = uiState,
            preferences = prefs,
            onPickReferencePhoto = { uri, asBg, matchSize ->
                viewModel.importReferencePhoto(context, uri, asBg, matchSize)
            },
            onApplyExtractedStyle = viewModel::applyExtractedStyleToCanvas,
            onMatchPhotoSize = { viewModel.applyCanvasPreset(CanvasSizePreset.PHOTO_MATCH) },
            onBackgroundAlphaChange = viewModel::setBackgroundImageAlpha,
            onClearBackground = viewModel::clearBackgroundImage,
            onSelectAiModel = viewModel::setPreferredAiModel,
            onSelectImageSize = viewModel::setPreferredImageSize,
            onGenerateArtwork = { prompt, useRef ->
                viewModel.generatePoemArtwork(context, prompt, useRef)
            }
        )

        StudioTab.OCR -> HandwritingOcrPanel(
            state = uiState,
            onPickNotebookPhoto = { uri, matchSize ->
                viewModel.importNotebookPhotoForOcr(context, uri, matchSize)
            },
            onRunOcr = viewModel::convertNotebookHandwritingToDigital,
            onApplyToCanvas = viewModel::applyOcrToCanvas
        )
    }
}

@Composable
private fun SyncStatusChip(
    autoSaveEnabled: Boolean,
    status: AutoSaveStatus,
    onClick: () -> Unit
) {
    val label = when {
        !autoSaveEnabled -> "ম্যানুয়াল"
        status == AutoSaveStatus.Saving -> "সেভ হচ্ছে…"
        else -> "অটো-সেভড"
    }
    val icon = when {
        !autoSaveEnabled -> Icons.Default.CloudOff
        status == AutoSaveStatus.Saving -> Icons.Default.Sync
        else -> Icons.Default.CloudDone
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("sync_status_chip"),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }
    }
}

private fun tabIcon(tab: StudioTab) = when (tab) {
    StudioTab.POEM -> Icons.Default.EditNote
    StudioTab.DRAW -> Icons.Default.Brush
    StudioTab.ART -> Icons.Default.AutoAwesome
    StudioTab.OCR -> Icons.Default.DocumentScanner
}
