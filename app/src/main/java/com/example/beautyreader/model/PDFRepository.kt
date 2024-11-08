package com.example.beautyreader.model

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class PDFRepository(private val pdfDao: PDFDao) {

    val allPDFs: Flow<List<PdfEntity>> = pdfDao.getAllPDFs()

    suspend fun insertPDF(pdf: PdfEntity) {
        pdfDao.insertPDF(pdf)
    }

    suspend fun deletePDF(pdf: PdfEntity) {
        pdfDao.deletePDF(pdf)
    }

    suspend fun deleteOldPDFs() {
        val fiveDaysAgo = LocalDateTime.now().minusDays(5)
        pdfDao.deleteOldPDFs(fiveDaysAgo)
    }
}