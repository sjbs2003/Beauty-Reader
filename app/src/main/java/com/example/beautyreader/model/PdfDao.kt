package com.example.beautyreader.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface PDFDao {
    @Query("SELECT * FROM pdfs")
    fun getAllPDFs(): Flow<List<PdfEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPDF(pdf: PdfEntity)

    @Delete
    suspend fun deletePDF(pdf: PdfEntity)

    @Query("DELETE FROM pdfs WHERE lastOpenedDate < :date")
    suspend fun deleteOldPDFs(date: LocalDateTime)

    @Query("SELECT DISTINCT userName FROM pdfs WHERE userName IS NOT NULL LIMIT 1")
    fun getUserName(): Flow<String?>

    @Query("UPDATE pdfs SET userName = :userName")
    suspend fun updateUserName(userName: String)
}