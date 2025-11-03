package com.nhd.news.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for offline caching
 */
@Database(
    entities = [ArticleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {
    
    abstract fun articleDao(): ArticleDao
    
    companion object {
        private const val DATABASE_NAME = "nhd_news_db"
        
        @Volatile
        private var INSTANCE: NewsDatabase? = null
        
        fun getInstance(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

