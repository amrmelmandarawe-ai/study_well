package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.SharedIncomingBatch
import com.example.data.model.SharedIncomingFile
import com.example.data.model.SubjectEnum
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun SharedFilesDialog(
    batch: SharedIncomingBatch,
    currentSelectedSubject: SubjectEnum,
    onDismiss: () -> Unit,
    onOpenAsDocument: (file: SharedIncomingFile, subject: SubjectEnum) -> Unit,
    onOpenWithAiTutor: (file: SharedIncomingFile, subject: SubjectEnum, base64: String?) -> Unit,
    onSaveToLibrary: (file: SharedIncomingFile, subject: SubjectEnum, type: String) -> Unit,
    onBatchImportAll: (files: List<SharedIncomingFile>, subject: SubjectEnum) -> Unit,
    onRouteToAdminUpload: ((List<SharedIncomingFile>) -> Unit)? = null,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubject by remember { mutableStateOf(currentSelectedSubject) }
    var isImportingAll by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("shared_files_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekGold400.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = SleekGold400,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Received Shared Files",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (batch.items.size == 1) "1 shared file received"
                                else "${batch.items.size} shared files received",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Selector
                Text(
                    text = "Assign to Subject:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SubjectEnum.values()) { sub ->
                        FilterChip(
                            selected = selectedSubject == sub,
                            onClick = { selectedSubject = sub },
                            label = { Text(sub.title, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = sub.color.copy(alpha = 0.2f),
                                selectedLabelColor = sub.color
                            )
                        )
                    }
                }

                // Batch Import Button for Multiple Files
                if (batch.items.size > 1 && isAdmin) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isImportingAll = true
                            onBatchImportAll(batch.items, selectedSubject)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedSubject.color
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isImportingAll
                    ) {
                        if (isImportingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importing all files...")
                        } else {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save All ${batch.items.size} Files to ${selectedSubject.title}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Files List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(batch.items) { fileItem ->
                        SharedFileRowItem(
                            item = fileItem,
                            subject = selectedSubject,
                            isAdmin = isAdmin,
                            onOpenDoc = { onOpenAsDocument(fileItem, selectedSubject) },
                            onAiSolve = {
                                val base64 = convertFileToBase64(fileItem.localFilePath)
                                onOpenWithAiTutor(fileItem, selectedSubject, base64)
                            },
                            onSave = { type -> onSaveToLibrary(fileItem, selectedSubject, type) }
                        )
                    }
                }

                if (onRouteToAdminUpload != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onRouteToAdminUpload(batch.items)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload to Syllabus as Admin (Pick Subject & Section)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedFileRowItem(
    item: SharedIncomingFile,
    subject: SubjectEnum,
    isAdmin: Boolean,
    onOpenDoc: () -> Unit,
    onAiSolve: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedSectionType by remember {
        mutableStateOf(
            when {
                item.isVideo -> "VIDEO"
                item.isPdf -> "BOOK"
                item.isImage -> "NOTE"
                else -> "NOTE"
            }
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Icon / Thumbnail
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        item.isPdf -> Color(0xFFE53935).copy(alpha = 0.15f)
                        item.isImage -> Color(0xFF43A047).copy(alpha = 0.15f)
                        item.isVideo -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    },
                    modifier = Modifier.size(46.dp)
                ) {
                    if (item.isImage && item.localFilePath != null) {
                        AsyncImage(
                            model = File(item.localFilePath),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = when {
                                item.isPdf -> Icons.Default.Description
                                item.isImage -> Icons.Default.Image
                                item.isVideo -> Icons.Default.PlayCircleOutline
                                else -> Icons.Default.MenuBook
                            },
                            contentDescription = null,
                            tint = when {
                                item.isPdf -> Color(0xFFE53935)
                                item.isImage -> Color(0xFF43A047)
                                item.isVideo -> Color(0xFF1E88E5)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Meta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = when {
                                    item.isPdf -> "PDF"
                                    item.isImage -> "IMAGE"
                                    item.isVideo -> "VIDEO"
                                    else -> "DOCUMENT"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 9.sp
                            )
                        }
                        Text(
                            text = if (item.pageCount > 0) "${item.sizeFormatted} • ${item.pageCount} pages" else item.sizeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Upload to Section in ${subject.title}:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sections = listOf(
                    "NOTE" to "Notes",
                    "BOOK" to "Books",
                    "SHEET" to "Sheets",
                    "MARKSCHEME" to "Mark Schemes",
                    "VIDEO" to "Videos"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sections) { (type, label) ->
                        val isSelected = selectedSectionType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SleekGold400 else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) SleekGold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { selectedSectionType = type }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SleekNavy950 else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // If it's an Image / Question
                if (item.isImage) {
                    Button(
                        onClick = onAiSolve,
                        colors = ButtonDefaults.buttonColors(containerColor = SleekGold400),
                        shape = RoundedCornerShape(8.dp),
                        modifier = if (isAdmin) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SleekNavy950, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Solve with AI", color = SleekNavy950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { onSave(selectedSectionType) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload to ${subject.title}", fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } else if (item.isPdf || item.isText) {
                    Button(
                        onClick = onOpenDoc,
                        colors = ButtonDefaults.buttonColors(containerColor = subject.color),
                        shape = RoundedCornerShape(8.dp),
                        modifier = if (isAdmin) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open & Read", fontSize = 12.sp)
                    }

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { onSave(selectedSectionType) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload to ${subject.title}", fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                } else if (item.isVideo) {
                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { onSave("VIDEO") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Video to ${subject.title}", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onOpenDoc,
                            colors = ButtonDefaults.buttonColors(containerColor = subject.color),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open & Play Video", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun convertFileToBase64(filePath: String?): String? {
    if (filePath == null) return null
    return try {
        val file = File(filePath)
        if (!file.exists()) return null
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val maxDimension = 1024
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val bytes = stream.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (_: Exception) {
        null
    }
}
