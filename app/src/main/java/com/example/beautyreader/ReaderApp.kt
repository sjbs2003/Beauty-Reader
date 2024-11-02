package com.example.beautyreader

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

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
            ReaderTopBar(
                onUploadClick = { showFilePickerDialog = true }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            bookContent?.let { content ->
                ReaderContent(
                    content = content,
                    currentPage = currentPage,
                    onPageChange = viewModel::navigateToPage
                )
            } ?: EmptyStateView(
                onUploadClick = { showFilePickerDialog = true }
            )
        }
    }

    if (showFilePickerDialog) {
        FilePickerDialog(
            onDismiss = { showFilePickerDialog = false },
            onFileSelected = { uri ->
                viewModel.loadPDF(uri, context)  // Pass the context here
                showFilePickerDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    onUploadClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("Beauty Reader") },
        actions = {
            IconButton(onClick = onUploadClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload PDF"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
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
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 28.sp,
                letterSpacing = 0.5.sp
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 0
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous page")
        }

        Text(
            text = "${currentPage + 1} / $totalPages",
            style = MaterialTheme.typography.bodyMedium
        )

        IconButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages - 1
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next page")
        }
    }
}

@Composable
fun EmptyStateView(onUploadClick: () -> Unit) {
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
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Upload a PDF to start reading",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onUploadClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
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