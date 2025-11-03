package com.nhd.news.di

import android.content.Context
import com.nhd.news.data.local.ArticleDao
import com.nhd.news.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNewsDatabase(
        @ApplicationContext context: Context
    ): NewsDatabase {
        return NewsDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideArticleDao(database: NewsDatabase): ArticleDao {
        return database.articleDao()
    }
}

