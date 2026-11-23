package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CanvasSizePreset
import com.example.model.ManuscriptPage
import com.example.util.PdfExportResult

@Composable
fun CanvasSizeDialog(
    currentWidthIn: Float,
    currentHeightIn: Float,
    hasReferencePhoto: Boolean,
    onApplyPreset: (CanvasSizePreset) -> Unit,
    onApplyCustom: (Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var widthIn by remember(currentWidthIn) { mutableFloatStateOf(currentWidthIn) }
    var heightIn by remember(currentHeightIn) { mutableFloatStateOf(currentHeightIn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ক্যানভাস সাইজ পরিবর্তন করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ডিফল্ট বইয়ের সাইজ: ৫.৫ × ৮.৫ ইঞ্চি। যেকোনো প্রিসেট বা কাস্টম মাপ বেছে নিন:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        CanvasSizePreset.BOOK_5_5_X_8_5,
                        CanvasSizePreset.A5,
                        CanvasSizePreset.A4,
                        CanvasSizePreset.PORTRAIT_4_5,
                        CanvasSizePreset.SQUARE_6_6
                    ).forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                onApplyPreset(preset)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(preset.labelBn)
                        }
                    }
                    if (hasReferencePhoto) {
                        Button(
                            onClick = {
                                onApplyPreset(CanvasSizePreset.PHOTO_MATCH)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("আপলোড করা ছবির সাইজ অনুযায়ী")
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "কাস্টম প্রস্থ: ${String.format("%.1f", widthIn)} ইঞ্চি",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = widthIn,
                    onValueChange = { widthIn = it },
                    valueRange = 3.5f..11.0f
                )
                Text(
                    text = "কাস্টম উচ্চতা: ${String.format("%.1f", heightIn)} ইঞ্চি",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = heightIn,
                    onValueChange = { heightIn = it },
                    valueRange = 4.0f..13.5f
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onApplyCustom(widthIn, heightIn)
                onDismiss()
            }) {
                Text("কাস্টম সেট করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun SyncAndAutoSaveDialog(
    autoSaveEnabled: Boolean,
    onAutoSaveToggle: (Boolean) -> Unit,
    onManualSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("অটো-সেভ ও ক্লাউড সিঙ্ক") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("স্বয়ংক্রিয় সেভ (Auto-Save)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "প্রতিটি পরিবর্তন ডিভাইসে নিরাপদভাবে সংরক্ষণ হবে",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoSaveEnabled,
                            onCheckedChange = onAutoSaveToggle
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                            Text("ক্লাউড সিঙ্ক অবস্থা: অফলাইন মোড", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Firebase সেটআপ বাদ দেওয়ায় ক্লাউড সিঙ্ক বর্তমানে নিষ্ক্রিয় আছে, তবে আপনার সকল কবিতা ও ড্রয়িং ডিভাইসের লোকাল ডাটাবেজে অটো-সেভ হচ্ছে।",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onManualSave()
                onDismiss()
            }) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("এখনই সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ঠিক আছে")
            }
        }
    )
}

@Composable
fun PagesManagerDialog(
    pages: List<ManuscriptPage>,
    currentPageId: Long,
    onSelectPage: (ManuscriptPage) -> Unit,
    onAddPage: () -> Unit,
    onDeletePage: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("পাণ্ডুলিপি পৃষ্ঠাসমূহ (${pages.size})")
                TextButton(onClick = onAddPage) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন পাতা")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pages, key = { it.id }) { item ->
                    val selected = item.id == currentPageId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectPage(item)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = item.title.ifBlank { "শিরোনামহীন পাতা" },
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.pageLabel} • ${item.canvasWidthInches}\" × ${item.canvasHeightInches}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (pages.size > 1) {
                                IconButton(onClick = { onDeletePage(item.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "পাতা মুছুন",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun PdfExportResultDialog(
    result: PdfExportResult,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF সফলভাবে তৈরি হয়েছে") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ফাইল: ${result.displayFileName}", style = MaterialTheme.typography.titleMedium)
                if (result.savedToDownloads) {
                    Text(
                        text = "আপনার ডিভাইসের Downloads/Pandulipi ফোল্ডারে সেভ করা হয়েছে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Text(
                    text = "নিচের বাটন থেকে সরাসরি PDF খুলতে বা শেয়ার করতে পারবেন।",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, result.shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Pandulipi PDF শেয়ার করুন"))
                onDismiss()
            }) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("শেয়ার করুন")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = {
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(result.shareUri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching { context.startActivity(viewIntent) }
                    onDismiss()
                }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("খুলুন")
                }
                TextButton(onClick = onDismiss) {
                    Text("বন্ধ")
                }
            }
        }
    )
}
