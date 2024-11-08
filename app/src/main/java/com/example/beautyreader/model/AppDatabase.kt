package com.example.beautyreader.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PdfEntity::class], version = 1)
@TypeConverters(com.example.beautyreader.model.Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun pdfDao(): PDFDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}