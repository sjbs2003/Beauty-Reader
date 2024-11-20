package com.example.beautyreader.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "pdfs")
data class PdfEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val lastOpenedDate: LocalDateTime,
    val savedData: LocalDateTime,
    val userName: String? = null
)
