package com.nhd.news.di

import com.nhd.news.data.api.PostApiService
import com.nhd.news.data.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }
}

