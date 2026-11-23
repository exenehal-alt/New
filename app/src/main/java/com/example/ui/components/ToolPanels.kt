package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ai.AiArtworkResult
import com.example.data.ai.HandwritingOcrResult
import com.example.data.local.StudioPreferences
import com.example.model.AiImageModel
import com.example.model.AiImageSize
import com.example.model.CanvasSizePreset
import com.example.model.CuratedDrawingColors
import com.example.model.DrawingTool
import com.example.model.ManuscriptPage
import com.example.model.PaperThemePreset
import com.example.model.PaperThemePresets
import com.example.model.PoemTextAlign
import com.example.model.toComposeColor
import com.example.ui.AsyncTaskState
import com.example.ui.StudioUiState
import com.example.ui.theme.BengaliFontOption
import com.example.util.ColorExtractor
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoemTypographyPanel(
    page: ManuscriptPage,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPoemBodyChange: (String) -> Unit,
    onPageLabelChange: (String) -> Unit,
    onFontSelected: (String) -> Unit,
    onBoldChanged: (Boolean) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onLineHeightChanged: (Float) -> Unit,
    onTextAlignChanged: (PoemTextAlign) -> Unit,
    onAutoFitChanged: (Boolean) -> Unit,
    onDividerChanged: (Boolean) -> Unit,
    onBorderChanged: (Boolean) -> Unit,
    onPaperThemeSelected: (PaperThemePreset) -> Unit,
    onCanvasPresetSelected: (CanvasSizePreset) -> Unit,
    onOpenCanvasSizeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Poem Text Inputs
        SectionHeader(title = "কবিতা লিখুন বা সম্পাদনা করুন")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = page.title,
                onValueChange = onTitleChange,
                label = { Text("শিরোনাম") },
                singleLine = true,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("poem_title_input")
            )
            OutlinedTextField(
                value = page.pageLabel,
                onValueChange = onPageLabelChange,
                label = { Text("পৃষ্ঠা") },
                singleLine = true,
                modifier = Modifier.weight(0.7f)
            )
        }

        OutlinedTextField(
            value = page.author,
            onValueChange = onAuthorChange,
            label = { Text("কবির নাম") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("poem_author_input")
        )

        OutlinedTextField(
            value = page.poemBody,
            onValueChange = onPoemBodyChange,
            label = { Text("কবিতার লাইনসমূহ (খাতার ছন্দ অনুযায়ী)") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("poem_body_input")
        )

        HorizontalDivider()

        // Bold, Font Size & Auto-Fit Row
        SectionHeader(title = "ফন্ট স্টাইল, সাইজ ও বোল্ড")

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = page.isBold,
                onClick = { onBoldChanged(!page.isBold) },
                label = { Text("Bold লেখা") },
                leadingIcon = {
                    Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.testTag("bold_toggle_chip")
            )

            FilterChip(
                selected = page.autoFitText,
                onClick = { onAutoFitChanged(!page.autoFitText) },
                label = { Text("অটো-ফিট") },
                modifier = Modifier.testTag("autofit_toggle_chip")
            )

            Spacer(modifier = Modifier.weight(1f))

            // Quick - / + font size stepper
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onFontSizeChanged(page.fontSizeSp - 1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "ফন্ট ছোট করুন", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${page.fontSizeSp.roundToInt()}sp",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onFontSizeChanged(page.fontSizeSp + 1f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "ফন্ট বড় করুন", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("লেখার সাইজ: ${page.fontSizeSp.roundToInt()} sp", style = MaterialTheme.typography.bodyMedium)
                Text("লাইন স্পেস: ${String.format("%.1f", page.lineHeightMultiplier)}x", style = MaterialTheme.typography.bodyMedium)
            }
            Slider(
                value = page.fontSizeSp,
                onValueChange = onFontSizeChanged,
                valueRange = 10f..28f
            )
            Slider(
                value = page.lineHeightMultiplier,
                onValueChange = onLineHeightChanged,
                valueRange = 1.2f..2.1f
            )
        }

        // 8 Authentic Bengali Font Styles
        Text("বাংলা ফন্ট স্টাইল বেছে নিন:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BengaliFontOption.entries.forEach { font ->
                val isSelected = font.id == page.fontId
                Card(
                    modifier = Modifier
                        .width(142.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onFontSelected(font.id) }
                        .testTag("font_card_${font.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "আমার সোনার বাংলা",
                            style = TextStyle(
                                fontFamily = font.fontFamily,
                                fontWeight = if (page.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = font.displayNameBn,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = font.styleTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Alignment & Page Ornaments
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PoemTextAlign.entries.forEach { align ->
                val icon = when (align) {
                    PoemTextAlign.CENTER -> Icons.Default.FormatAlignCenter
                    PoemTextAlign.LEFT -> Icons.AutoMirrored.Filled.FormatAlignLeft
                    PoemTextAlign.NOTEBOOK_INDENT -> Icons.Default.FormatIndentIncrease
                }
                FilterChip(
                    selected = page.textAlign == align,
                    onClick = { onTextAlignChanged(align) },
                    label = { Text(align.labelBn) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            FilterChip(
                selected = page.showDivider,
                onClick = { onDividerChanged(!page.showDivider) },
                label = { Text("অলংকার লাইন") }
            )
            FilterChip(
                selected = page.showBorder,
                onClick = { onBorderChanged(!page.showBorder) },
                label = { Text("বর্ডার ফ্রেম") }
            )
        }

        HorizontalDivider()

        // Canvas Size Presets + Custom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "ক্যানভাস সাইজ (${page.canvasWidthInches}\" × ${page.canvasHeightInches}\")")
            OutlinedButton(onClick = onOpenCanvasSizeDialog) {
                Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("কাস্টম সাইজ")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                CanvasSizePreset.BOOK_5_5_X_8_5,
                CanvasSizePreset.A5,
                CanvasSizePreset.A4,
                CanvasSizePreset.PORTRAIT_4_5,
                CanvasSizePreset.SQUARE_6_6
            ).forEach { preset ->
                val active = kotlin.math.abs(page.canvasWidthInches - preset.widthInches) < 0.06f &&
                    kotlin.math.abs(page.canvasHeightInches - preset.heightInches) < 0.06f
                FilterChip(
                    selected = active,
                    onClick = { onCanvasPresetSelected(preset) },
                    label = { Text(preset.labelBn) }
                )
            }
        }

        // Paper & Ink Themes
        Text("কাগজ ও কালির থিম:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaperThemePresets.forEach { preset ->
                val selected = page.paperThemeId == preset.id
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPaperThemeSelected(preset) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    color = preset.paperHex.toComposeColor()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(preset.accentHex.toComposeColor())
                        )
                        Text(
                            text = preset.labelBn,
                            style = MaterialTheme.typography.labelMedium,
                            color = preset.inkHex.toComposeColor()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrawingToolboxPanel(
    state: StudioUiState,
    onToggleDrawMode: (Boolean) -> Unit,
    onSelectTool: (DrawingTool) -> Unit,
    onSelectColor: (Long) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Draw Mode Switch Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (state.isDrawMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ক্যানভাসে আঁকা মোড",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (state.isDrawMode) "এখন আঙুল দিয়ে সরাসরি ৫.৫×৮.৫ পেজে আঁকতে পারবেন" else "চালু করলে ক্যানভাসে আঁকা যাবে",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isDrawMode,
                    onCheckedChange = onToggleDrawMode
                )
            }
        }

        // Drawing Tools
        SectionHeader(title = "ড্রয়িং টুলবক্স")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DrawingTool.entries.forEach { tool ->
                val icon = when (tool) {
                    DrawingTool.PEN -> Icons.Default.Create
                    DrawingTool.BRUSH -> Icons.Default.Brush
                    DrawingTool.HIGHLIGHTER -> Icons.Default.Highlight
                    DrawingTool.LINE -> Icons.Default.ShowChart
                    DrawingTool.ERASER -> Icons.Default.CleaningServices
                }
                FilterChip(
                    selected = state.selectedTool == tool,
                    onClick = { onSelectTool(tool) },
                    label = { Text(tool.labelBn) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("tool_${tool.id}")
                )
            }
        }

        // Undo / Redo / Clear Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = state.currentPage.strokes.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("পূর্বাবস্থা")
            }
            OutlinedButton(
                onClick = onRedo,
                enabled = state.redoStack.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("পুনরায়")
            }
            OutlinedButton(
                onClick = onClear,
                enabled = state.currentPage.strokes.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("মুছুন")
            }
        }

        // Color Palette (Curated + Photo Extracted!)
        val extracted = state.currentPage.extractedPalette
        if (extracted.isNotEmpty()) {
            Text("আপলোড করা ছবি থেকে সংগৃহীত রঙ:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                extracted.forEach { argb ->
                    ColorSwatchCircle(
                        colorArgb = argb,
                        selected = state.brushColorArgb == argb && state.selectedTool != DrawingTool.ERASER,
                        onClick = { onSelectColor(argb) }
                    )
                }
            }
        }

        Text("তুলি ও কালির রঙ:", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CuratedDrawingColors.forEach { argb ->
                ColorSwatchCircle(
                    colorArgb = argb,
                    selected = state.brushColorArgb == argb && state.selectedTool != DrawingTool.ERASER,
                    onClick = { onSelectColor(argb) }
                )
            }
        }

        // Stroke Width & Opacity Sliders
        Column {
            Text("তুলির পুরুত্ব: ${state.strokeWidthNorm.roundToInt()} px", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = state.strokeWidthNorm,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..36f
            )
            Text("স্বচ্ছতা (Opacity): ${(state.brushAlpha * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = state.brushAlpha,
                onValueChange = onOpacityChange,
                valueRange = 0.15f..1.0f
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtStudioPanel(
    state: StudioUiState,
    preferences: StudioPreferences,
    onPickReferencePhoto: (android.net.Uri, Boolean, Boolean) -> Unit,
    onApplyExtractedStyle: () -> Unit,
    onMatchPhotoSize: () -> Unit,
    onBackgroundAlphaChange: (Float) -> Unit,
    onClearBackground: () -> Unit,
    onSelectAiModel: (AiImageModel) -> Unit,
    onSelectImageSize: (AiImageSize) -> Unit,
    onGenerateArtwork: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var applyAsBg by remember { mutableStateOf(true) }
    var matchCanvasSize by remember { mutableStateOf(true) }
    var stylePrompt by remember { mutableStateOf("জলরঙ ও নরম পাণ্ডুলিপি আবহ") }
    var useRefForAi by remember { mutableStateOf(true) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickReferencePhoto(uri, applyAsBg, matchCanvasSize)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Editorial Studio Hero Banner
        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.height(104.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_hero),
                    contentDescription = "Pandulipi Art Studio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text(
                            text = "কবিতা ও ছবির শৈলী স্টুডিও",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "আপনার ছবির রঙ ও মাপে ক্যানভাস সাজান অথবা কবিতা থেকে ছবি তৈরি করুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Reference Photo & Real On-Device Color Extraction
        SectionHeader(title = "১. ছবি থেকে রঙ ও শৈলী সংগ্রহ")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = applyAsBg,
                onClick = { applyAsBg = !applyAsBg },
                label = { Text("ব্যাকগ্রাউন্ড হিসেবে দিন") }
            )
            FilterChip(
                selected = matchCanvasSize,
                onClick = { matchCanvasSize = !matchCanvasSize },
                label = { Text("ছবির সাইজে ক্যানভাস") }
            )
        }

        Button(
            onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pick_reference_photo_button")
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("গ্যালারি থেকে ছবি আপলোড করুন")
        }

        state.referenceAnalysis?.let { analysis ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "সংগৃহীত শৈলী: ${analysis.styleSummaryBn} (${analysis.matchedWidthInches}\" × ${analysis.matchedHeightInches}\")",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        analysis.paletteArgb.forEach { argb ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(argb.toComposeColor())
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onApplyExtractedStyle,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রঙ ক্যানভাসে নিন")
                        }
                        OutlinedButton(
                            onClick = onMatchPhotoSize,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ছবির সাইজ মেলান")
                        }
                    }
                }
            }
        }

        if (state.currentPage.backgroundImagePath != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ছবির স্বচ্ছতা: ${(state.currentPage.backgroundImageAlpha * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = onClearBackground) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ছবি সরান")
                }
            }
            Slider(
                value = state.currentPage.backgroundImageAlpha,
                onValueChange = onBackgroundAlphaChange,
                valueRange = 0.08f..0.95f
            )
        }

        HorizontalDivider()

        // AI Poem-to-Artwork Generation (with gemini-nano-banana-2.1 and gemini-3-pro-image-preview + 1K/2K/4K)
        SectionHeader(title = "২. কবিতা থেকে AI আর্টওয়ার্ক তৈরি")

        AiFirebaseNoticeBanner(
            text = "সরাসরি Gemini AI ইমেজ জেনারেশন (gemini-nano-banana-2.1 ও gemini-3-pro-image-preview) চালু করতে Firebase AI Logic প্রয়োজন। এখন নমুনা মোডে প্রিভিউ আর্টওয়ার্ক তৈরি হবে।"
        )

        Text("AI মডেল নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AiImageModel.entries.forEach { model ->
                FilterChip(
                    selected = preferences.preferredAiModel == model,
                    onClick = { onSelectAiModel(model) },
                    label = { Text("${model.labelBn} (${model.modelId})") }
                )
            }
        }

        Text("ছবির রেজোলিউশন (Image Size):", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiImageSize.entries.forEach { size ->
                FilterChip(
                    selected = preferences.preferredImageSize == size,
                    onClick = { onSelectImageSize(size) },
                    label = { Text(size.label) },
                    modifier = Modifier.testTag("image_size_${size.label}")
                )
            }
        }

        OutlinedTextField(
            value = stylePrompt,
            onValueChange = { stylePrompt = it },
            label = { Text("আর্টওয়ার্কের ধরণ (যেমন: জলরঙ, প্রাচীন পুঁথি, সন্ধ্যা আলো)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.referenceAnalysis != null) {
            FilterChip(
                selected = useRefForAi,
                onClick = { useRefForAi = !useRefForAi },
                label = { Text("আপলোড করা ছবির রঙ ও শৈলী ব্যবহার করুন") }
            )
        }

        Button(
            onClick = { onGenerateArtwork(stylePrompt, useRefForAi) },
            enabled = state.aiArtworkState !is AsyncTaskState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_poem_artwork_button")
        ) {
            if (state.aiArtworkState is AsyncTaskState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("আর্টওয়ার্ক তৈরি হচ্ছে…")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("কবিতা থেকে ছবি তৈরি করুন (${preferences.preferredImageSize.label})")
            }
        }

        when (val artState = state.aiArtworkState) {
            is AsyncTaskState.Success<AiArtworkResult> -> {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = artState.data.noticeBn,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            is AsyncTaskState.Error -> {
                Text(artState.message, color = MaterialTheme.colorScheme.error)
            }
            else -> Unit
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun HandwritingOcrPanel(
    state: StudioUiState,
    onPickNotebookPhoto: (android.net.Uri, Boolean) -> Unit,
    onRunOcr: () -> Unit,
    onApplyToCanvas: (String, String, String, String, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var matchPhotoSize by remember { mutableStateOf(true) }
    var makeBold by remember { mutableStateOf(true) }

    var draftTitle by remember(state.ocrState) {
        mutableStateOf((state.ocrState as? AsyncTaskState.Success<HandwritingOcrResult>)?.data?.title ?: state.currentPage.title)
    }
    var draftAuthor by remember(state.ocrState) {
        mutableStateOf((state.ocrState as? AsyncTaskState.Success<HandwritingOcrResult>)?.data?.author ?: state.currentPage.author)
    }
    var draftBody by remember(state.ocrState) {
        mutableStateOf((state.ocrState as? AsyncTaskState.Success<HandwritingOcrResult>)?.data?.poemBody ?: state.currentPage.poemBody)
    }
    var draftPage by remember(state.ocrState) {
        mutableStateOf((state.ocrState as? AsyncTaskState.Success<HandwritingOcrResult>)?.data?.pageLabel ?: state.currentPage.pageLabel)
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickNotebookPhoto(uri, matchPhotoSize)
        }
    }

    val notebookBitmap = remember(state.ocrPhotoPath) {
        state.ocrPhotoPath?.let { ColorExtractor.decodeBoundedBitmap(it, 720)?.asImageBitmap() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "খাতায় লেখা কবিতা → ডিজিটাল Bold লেখা")

        AiFirebaseNoticeBanner(
            text = "হাতের লেখা স্বয়ংক্রিয়ভাবে পড়তে (Gemini Vision OCR) Firebase AI Logic প্রয়োজন। নমুনা মোডে আপনি খাতার ছবি আপলোড করে ছবির সাইজে ক্যানভাস লক করতে ও নমুনা ডিজিটাল Bold রূপান্তর সম্পাদনা করতে পারবেন।"
        )

        FilterChip(
            selected = matchPhotoSize,
            onClick = { matchPhotoSize = !matchPhotoSize },
            label = { Text("খাতার ছবির সাইজে ক্যানভাস ও লেখা সীমাবদ্ধ করুন") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_notebook_photo_button")
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("খাতার ছবি নিন")
            }

            Button(
                onClick = onRunOcr,
                enabled = state.ocrPhotoPath != null && state.ocrState !is AsyncTaskState.Loading,
                modifier = Modifier
                    .weight(1f)
                    .testTag("convert_handwriting_button")
            ) {
                if (state.ocrState is AsyncTaskState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডিজিটাল করুন")
                }
            }
        }

        if (notebookBitmap != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "আপলোড করা খাতার ছবি (${state.ocrPhotoWidthIn ?: 5.5}\" × ${state.ocrPhotoHeightIn ?: 8.5}\")",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Image(
                        bitmap = notebookBitmap,
                        contentDescription = "খাতায় লেখা কবিতার ছবি",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (state.ocrState is AsyncTaskState.Success<HandwritingOcrResult>) {
            val result = state.ocrState.data
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.noticeBn,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Editable line-preserved digital output + Bold toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ডিজিটাল রূপান্তরিত লেখা:", style = MaterialTheme.typography.labelMedium)
            FilterChip(
                selected = makeBold,
                onClick = { makeBold = !makeBold },
                label = { Text("Bold অক্ষর") },
                leadingIcon = { Icon(Icons.Default.FormatBold, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draftTitle,
                onValueChange = { draftTitle = it },
                label = { Text("শিরোনাম") },
                singleLine = true,
                modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
                value = draftPage,
                onValueChange = { draftPage = it },
                label = { Text("পৃষ্ঠা") },
                singleLine = true,
                modifier = Modifier.weight(0.8f)
            )
        }
        OutlinedTextField(
            value = draftAuthor,
            onValueChange = { draftAuthor = it },
            label = { Text("কবি") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = draftBody,
            onValueChange = { draftBody = it },
            label = { Text("লাইন-বিন্যাসসহ কবিতা") },
            minLines = 5,
            maxLines = 9,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onApplyToCanvas(draftTitle, draftAuthor, draftBody, draftPage, makeBold, matchPhotoSize)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apply_ocr_to_canvas_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bold ডিজিটাল লেখা ক্যানভাসে বসান")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun AiFirebaseNoticeBanner(text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ColorSwatchCircle(
    colorArgb: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(colorArgb.toComposeColor())
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "নির্বাচিত রঙ",
                tint = if (colorArgb == 0xFFF4EFE4L) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
