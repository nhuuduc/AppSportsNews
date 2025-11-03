package com.nhd.news.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nhd.news.data.api.*
import com.nhd.news.data.preferences.AuthPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = ApiConfig.BASE_URL
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(authPreferences: AuthPreferences): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val token = authPreferences.getSessionToken()
            
            // Always send token if available for user-specific data (like is_liked)
            // Backend will handle auth requirements per endpoint
            val request = if (token != null) {
                android.util.Log.d("AuthInterceptor", "Adding Bearer token to request: ${original.url}")
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Accept-Encoding", "identity")
                    .build()
            } else {
                android.util.Log.d("AuthInterceptor", "No token available for request: ${original.url}")
                original.newBuilder()
                    .header("Accept-Encoding", "identity")
                    .build()
            }
            
            val response = chain.proceed(request)
            
            // Log response for debugging
            if (!response.isSuccessful) {
                val responseBody = response.peekBody(Long.MAX_VALUE).string()
                android.util.Log.e("NetworkError", "URL: ${original.url}")
                android.util.Log.e("NetworkError", "Code: ${response.code}")
                android.util.Log.e("NetworkError", "Body: $responseBody")
            }
            
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArticleApiService(retrofit: Retrofit): ArticleApiService {
        return retrofit.create(ArticleApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService {
        return retrofit.create(CategoryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentApiService(retrofit: Retrofit): CommentApiService {
        return retrofit.create(CommentApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoriteApiService(retrofit: Retrofit): FavoriteApiService {
        return retrofit.create(FavoriteApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchApiService(retrofit: Retrofit): MatchApiService {
        return retrofit.create(MatchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTeamApiService(retrofit: Retrofit): TeamApiService {
        return retrofit.create(TeamApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVideoApiService(retrofit: Retrofit): VideoApiService {
        return retrofit.create(VideoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTagApiService(retrofit: Retrofit): TagApiService {
        return retrofit.create(TagApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApiService(retrofit: Retrofit): SearchApiService {
        return retrofit.create(SearchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSystemApiService(retrofit: Retrofit): SystemApiService {
        return retrofit.create(SystemApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }
}
