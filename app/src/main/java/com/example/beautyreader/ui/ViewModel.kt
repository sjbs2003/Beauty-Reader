package com.example.beautyreader.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.beautyreader.model.AppDatabase
import com.example.beautyreader.model.PDFRepository
import com.example.beautyreader.model.PdfEntity
import com.example.beautyreader.model.UserPreferences
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

data class BookContent(
    val title: String,
    val pages: List<String>,
    val totalPages: Int
)

class PDFViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    val userName = userPreferences.userName

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _bookContent = MutableStateFlow<BookContent?>(null)
    val bookContent = _bookContent.asStateFlow()

    private val _isReaderMode = MutableStateFlow(false)
    val isReaderMode = _isReaderMode.asStateFlow()

    private val repository: PDFRepository
    val allPDFs: Flow<List<PdfEntity>>

    private var _currentPDFUri = MutableStateFlow<Uri?>(null)
    val currentPDFUri = _currentPDFUri.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PDFRepository(database.pdfDao())
        allPDFs = repository.allPDFs
    }

    fun clearCurrentBook() {
        _bookContent.value = null
        _currentPage.value = 0
        _currentPDFUri.value = null
    }

    fun loadPDF(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isReaderMode.value = true
            withContext(Dispatchers.IO) {
                try {
                    _currentPDFUri.value = uri
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

                    // Save PDF information
                    repository.insertPDF(
                        PdfEntity(
                            uri = uri.toString(),
                            title = uri.lastPathSegment ?: "Unknown Book",
                            lastOpenedDate = LocalDateTime.now(),
                            savedData = LocalDateTime.now()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun navigateToPage(page: Int) {
        _currentPage.value = page.coerceIn(0, (bookContent.value?.totalPages ?: 1) - 1)
    }

    fun deletePDF(pdf: PdfEntity){
        viewModelScope.launch {
            repository.deletePDF(pdf)
        }
    }

    // function to check and delete old PDFs
    fun cleanOldPDFs() {
        viewModelScope.launch {
            repository.deleteOldPDFs()
        }
    }

    fun navigateBack() {
        _isReaderMode.value = false
        clearCurrentBook()
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            userPreferences.saveUserName(name)
        }
    }
}