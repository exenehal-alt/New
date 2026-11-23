package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrawStroke
import com.example.model.DrawingTool
import com.example.model.ManuscriptPage
import com.example.model.NormalizedPoint
import com.example.model.PoemTextAlign
import com.example.model.toComposeColor
import com.example.ui.theme.BengaliFontOption
import com.example.util.ColorExtractor

@Composable
fun ManuscriptCanvasStage(
    page: ManuscriptPage,
    isDrawMode: Boolean,
    selectedTool: DrawingTool,
    brushColorArgb: Long,
    strokeWidthNorm: Float,
    brushAlpha: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    onStrokeCompleted: (List<NormalizedPoint>) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleDrawMode: () -> Unit,
    onEnableAutoFit: () -> Unit,
    onOpenSizePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBitmap = remember(page.backgroundImagePath) {
        page.backgroundImagePath?.let { ColorExtractor.decodeBoundedBitmap(it, 1024)?.asImageBitmap() }
    }

    val activePoints = remember { mutableStateListOf<NormalizedPoint>() }
    var textOverflowDetected by remember(page.poemBody, page.fontSizeSp, page.canvasWidthInches, page.canvasHeightInches, page.autoFitText) {
        mutableStateOf(false)
    }
    var autoFitScale by remember(
        page.poemBody,
        page.title,
        page.author,
        page.fontSizeSp,
        page.titleSizeSp,
        page.lineHeightMultiplier,
        page.fontId,
        page.canvasWidthInches,
        page.canvasHeightInches,
        page.autoFitText
    ) {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(page.autoFitText) {
        if (page.autoFitText) {
            autoFitScale = 1f
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxW = maxWidth
        val maxH = maxHeight
        val targetAspect = page.aspectRatio

        val availableAspect = if (maxH.value > 0f) maxW.value / maxH.value else 1f
        val sizeModifier = if (availableAspect > targetAspect) {
            Modifier
                .fillMaxHeight()
                .aspectRatio(targetAspect)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(targetAspect)
        }

        Box(modifier = sizeModifier, contentAlignment = Alignment.Center) {
            val paperColor = page.paperColorArgb.toComposeColor()
            val inkColor = page.inkColorArgb.toComposeColor()
            val accentColor = page.accentColorArgb.toComposeColor()
            val fontOption = BengaliFontOption.fromId(page.fontId)
            val boldShadow = if (page.isBold) {
                Shadow(
                    color = inkColor.copy(alpha = 0.42f),
                    offset = Offset(0.8f, 0f),
                    blurRadius = 0.25f
                )
            } else null

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("manuscript_canvas_surface"),
                shape = RoundedCornerShape(4.dp),
                color = paperColor,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .clipToBounds()
                ) {
                    val canvasWidthDp = maxWidth.value
                    // Reference width for 5.5 in book page is 396pt; scale typography proportionally
                    val pageScale = (canvasWidthDp / 340f).coerceIn(0.58f, 1.8f)

                    // 1. Optional Background Image bounded strictly inside canvas
                    if (bgBitmap != null) {
                        Image(
                            bitmap = bgBitmap,
                            contentDescription = "ক্যানভাস ব্যাকগ্রাউন্ড ছবি",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = page.backgroundImageAlpha.coerceIn(0.05f, 1f) },
                            contentScale = ContentScale.Crop
                        )
                    }

                    // 2. Optional Literary Border Frame
                    if (page.showBorder) {
                        val borderPad = (10f * pageScale).dp
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(borderPad)
                                .border(
                                    width = 1.dp,
                                    color = accentColor.copy(alpha = 0.36f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }

                    // 3. Strictly Bounded Bengali Poetry Layout
                    val innerPadH = (20f * pageScale).dp
                    val innerPadTop = (22f * pageScale).dp
                    val innerPadBottom = (16f * pageScale).dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = innerPadH, end = innerPadH, top = innerPadTop, bottom = innerPadBottom)
                            .clipToBounds(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val effScale = pageScale * autoFitScale

                        if (page.title.isNotBlank()) {
                            Text(
                                text = page.title,
                                style = TextStyle(
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (page.titleSizeSp * effScale).sp,
                                    lineHeight = (page.titleSizeSp * 1.25f * effScale).sp,
                                    color = inkColor,
                                    shadow = Shadow(
                                        color = inkColor.copy(alpha = 0.45f),
                                        offset = Offset(0.9f, 0f),
                                        blurRadius = 0.3f
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (page.author.isNotBlank()) {
                            Spacer(modifier = Modifier.height((3f * effScale).dp))
                            Text(
                                text = page.author,
                                style = TextStyle(
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (page.fontSizeSp * 0.92f * effScale).sp,
                                    color = inkColor.copy(alpha = 0.85f)
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (page.showDivider) {
                            Spacer(modifier = Modifier.height((7f * effScale).dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height((8f * effScale).dp)
                            ) {
                                val midY = size.height / 2f
                                val midX = size.width / 2f
                                drawLine(
                                    color = accentColor.copy(alpha = 0.55f),
                                    start = Offset(0f, midY),
                                    end = Offset(midX - 8f, midY),
                                    strokeWidth = 1.2f
                                )
                                drawLine(
                                    color = accentColor.copy(alpha = 0.55f),
                                    start = Offset(midX + 8f, midY),
                                    end = Offset(size.width, midY),
                                    strokeWidth = 1.2f
                                )
                                val r = 3.5f
                                val diamond = Path().apply {
                                    moveTo(midX, midY - r)
                                    lineTo(midX + r, midY)
                                    lineTo(midX, midY + r)
                                    lineTo(midX - r, midY)
                                    close()
                                }
                                drawPath(diamond, color = accentColor.copy(alpha = 0.8f))
                            }
                            Spacer(modifier = Modifier.height((8f * effScale).dp))
                        } else {
                            Spacer(modifier = Modifier.height((10f * effScale).dp))
                        }

                        // Poem body strictly constrained inside remaining space
                        val composeAlign = when (page.textAlign) {
                            PoemTextAlign.CENTER -> TextAlign.Center
                            PoemTextAlign.LEFT, PoemTextAlign.NOTEBOOK_INDENT -> TextAlign.Start
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clipToBounds(),
                            contentAlignment = when (page.textAlign) {
                                PoemTextAlign.CENTER -> Alignment.TopCenter
                                PoemTextAlign.LEFT, PoemTextAlign.NOTEBOOK_INDENT -> Alignment.TopStart
                            }
                        ) {
                            Text(
                                text = page.poemBody,
                                style = TextStyle(
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = if (page.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = (page.fontSizeSp * effScale).sp,
                                    lineHeight = (page.fontSizeSp * page.lineHeightMultiplier * effScale).sp,
                                    color = inkColor,
                                    shadow = boldShadow
                                ),
                                textAlign = composeAlign,
                                overflow = TextOverflow.Clip,
                                onTextLayout = { layoutResult ->
                                    val overflows = layoutResult.didOverflowHeight || layoutResult.didOverflowWidth
                                    if (page.autoFitText && overflows && autoFitScale > 0.52f) {
                                        autoFitScale = (autoFitScale * 0.93f).coerceAtLeast(0.52f)
                                    } else {
                                        textOverflowDetected = overflows
                                    }
                                }
                            )
                        }

                        if (page.pageLabel.isNotBlank()) {
                            Spacer(modifier = Modifier.height((6f * pageScale).dp))
                            Text(
                                text = page.pageLabel,
                                style = TextStyle(
                                    fontFamily = fontOption.fontFamily,
                                    fontWeight = if (page.isBold) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = (11f * pageScale).sp,
                                    color = inkColor.copy(alpha = 0.78f),
                                    shadow = boldShadow
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // 4. Interactive Vector Drawing Canvas Layer (Offscreen compositing for accurate Eraser)
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .then(
                                if (isDrawMode) {
                                    Modifier.pointerInput(selectedTool, brushColorArgb, strokeWidthNorm, brushAlpha) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                activePoints.clear()
                                                val nx = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                val ny = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                                activePoints.add(NormalizedPoint(nx, ny))
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                val nx = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                                val ny = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                                                if (selectedTool == DrawingTool.LINE && activePoints.size >= 2) {
                                                    activePoints[1] = NormalizedPoint(nx, ny)
                                                } else {
                                                    activePoints.add(NormalizedPoint(nx, ny))
                                                }
                                            },
                                            onDragEnd = {
                                                if (activePoints.isNotEmpty()) {
                                                    onStrokeCompleted(activePoints.toList())
                                                    activePoints.clear()
                                                }
                                            },
                                            onDragCancel = {
                                                activePoints.clear()
                                            }
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        for (stroke in page.strokes) {
                            drawSingleStroke(stroke)
                        }
                        if (activePoints.isNotEmpty()) {
                            drawSingleStroke(
                                DrawStroke(
                                    id = "preview",
                                    tool = selectedTool,
                                    colorArgb = brushColorArgb,
                                    strokeWidthNorm = strokeWidthNorm,
                                    alpha = brushAlpha,
                                    points = activePoints.toList()
                                )
                            )
                        }
                    }

                    // 5. Subtle Canvas Badges (Size Pill + Draw / Auto-fit action)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onOpenSizePicker),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${page.canvasWidthInches}\" × ${page.canvasHeightInches}\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isDrawMode) {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(50),
                                shadowElevation = 2.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = onUndo,
                                        enabled = canUndo,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "Undo",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = onRedo,
                                        enabled = canRedo,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Redo,
                                            contentDescription = "Redo",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onToggleDrawMode),
                            color = if (isDrawMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            shape = CircleShape,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isDrawMode) Icons.Default.Brush else Icons.Default.Edit,
                                    contentDescription = "Draw mode toggle",
                                    tint = if (isDrawMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isDrawMode) "আঁকা চালু" else "পড়া/লিখা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDrawMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Overflow helper badge if user turned off Auto-Fit and increased font too large
                    if (textOverflowDetected && !page.autoFitText) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onEnableAutoFit),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "অটো-ফিট করুন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSingleStroke(stroke: DrawStroke) {
    if (stroke.points.isEmpty()) return
    val strokePx = ((stroke.strokeWidthNorm / 400f) * size.width).coerceAtLeast(1.5f)
    val isEraser = stroke.tool == DrawingTool.ERASER
    val drawColor = if (isEraser) Color.Black else stroke.colorArgb.toComposeColor().copy(alpha = stroke.alpha.coerceIn(0.05f, 1f))
    val blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver

    val path = Path()
    val first = stroke.points.first()
    path.moveTo(first.x * size.width, first.y * size.height)

    if (stroke.points.size == 1) {
        drawCircle(
            color = drawColor,
            radius = strokePx / 2f,
            center = Offset(first.x * size.width, first.y * size.height),
            blendMode = blendMode
        )
        return
    }

    if (stroke.tool == DrawingTool.LINE) {
        val last = stroke.points.last()
        path.lineTo(last.x * size.width, last.y * size.height)
    } else {
        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            val midX = (prev.x + curr.x) * 0.5f * size.width
            val midY = (prev.y + curr.y) * 0.5f * size.height
            path.quadraticTo(prev.x * size.width, prev.y * size.height, midX, midY)
        }
        val last = stroke.points.last()
        path.lineTo(last.x * size.width, last.y * size.height)
    }

    drawPath(
        path = path,
        color = drawColor,
        style = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        blendMode = blendMode
    )
}
