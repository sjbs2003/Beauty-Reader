package com.example.beautyreader

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beautyreader.model.PdfEntity
import java.time.LocalDateTime

@Composable
fun ReaderApp(
    viewModel: PDFViewModel = viewModel()
) {
    var showFilePickerDialog by remember { mutableStateOf(false) }
    val bookContent by viewModel.bookContent.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBar(
                onUploadClick = { showFilePickerDialog = true },
                currentPDF = bookContent?.let { content ->
                    PdfEntity(
                        uri = viewModel.currentPDFUri.toString() ?: "",
                        title = content.title,
                        lastOpenedDate = LocalDateTime.now(),
                        savedData = LocalDateTime.now()
                    )
                },
                onDeleteClick = {
                    viewModel.currentPDFUri.let { uri ->
                        viewModel.deletePDF(
                            PdfEntity(
                                uri = uri.toString(),
                                title = bookContent?.title ?: "Unknown book",
                                lastOpenedDate = LocalDateTime.now(),
                                savedData = LocalDateTime.now()
                            )
                        )
                        viewModel.clearCurrentBook()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            bookContent?.let { content ->
                ReaderContent(
                    content = content,
                    currentPage = currentPage,
                    onPageChange = viewModel::navigateToPage
                )
            } ?: EmptyState(
                onUploadClick = { showFilePickerDialog = true }
            )
        }
    }

    if (showFilePickerDialog) {
        FilePickerDialog(
            onDismiss = { showFilePickerDialog = false },
            onFileSelected = { uri ->
                viewModel.loadPDF(uri, context)
                showFilePickerDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.cleanOldPDFs()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onUploadClick: () -> Unit,
    currentPDF: PdfEntity?,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_book),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentPDF?.title ?: "Beauty Reader" ,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        },
        actions = {
            IconButton(onClick = onUploadClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload PDF"
                )
            }
            if (currentPDF != null) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            IconButton(onClick = { /* TODO: Add settings */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun ReaderContent(
    content: BookContent,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PageContent(
            text = content.pages[currentPage],
            modifier = Modifier.weight(1f)
        )

        PageControls(
            currentPage = currentPage,
            totalPages = content.totalPages,
            onPageChange = onPageChange
        )
    }
}

@Composable
fun PageContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                .padding(24.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PageControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentPage + 1).toFloat() / totalPages },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 0
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }

            Text(
                text = "Page ${currentPage + 1} of $totalPages",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            TextButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    onUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_book),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Reading Journey Starts Here",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upload a PDF to begin reading",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedButton(
            onClick = onUploadClick,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload PDF")
        }
    }
}

@Composable
fun FilePickerDialog(
    onDismiss: () -> Unit,
    onFileSelected: (Uri) -> Unit
) {
    LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onFileSelected(it)
        } ?: onDismiss()
    }

    LaunchedEffect(Unit) {
        launcher.launch("application/pdf")
    }
}