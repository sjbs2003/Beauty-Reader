package com.example.beautyreader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BookContent(
    val title: String,
    val pages: List<String>,
    val totalPages: Int
)

class PDFViewModel : ViewModel() {
    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _bookContent = MutableStateFlow<BookContent?>(null)
    val bookContent = _bookContent.asStateFlow()

    fun loadPDF(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Initialize PDFBox
                    PDFBoxResourceLoader.init(context)

                    val inputStream = context.contentResolver.openInputStream(uri)
                    val document = PDDocument.load(inputStream)
                    val stripper = PDFTextStripper()
                    val pages = mutableListOf<String>()

                    for (i in 0 until document.numberOfPages) {
                        stripper.startPage = i + 1
                        stripper.endPage = i + 1
                        pages.add(stripper.getText(document))
                    }

                    _bookContent.value = BookContent(
                        title = uri.lastPathSegment ?: "Unknown Book",
                        pages = pages,
                        totalPages = document.numberOfPages
                    )

                    document.close()
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // You might want to add error handling here
                }
            }
        }
    }

    fun navigateToPage(page: Int) {
        _currentPage.value = page.coerceIn(0, (bookContent.value?.totalPages ?: 1) - 1)
    }
}