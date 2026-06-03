package com.example.readspread.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import data.local.AppDatabase
import data.local.dao.BookDao
import data.local.dao.BookmarkDao
import data.local.dao.ReadingProgressDao
import data.local.domain.repository.BookRepository
import data.local.domain.repository.BookmarkRepository
import data.local.domain.repository.ReadingProgressRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "book_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBookDao(database: AppDatabase): BookDao = database.bookDao()

    @Provides
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideReadingProgressDao(database: AppDatabase): ReadingProgressDao = database.readingProgressDao()

    @Provides
    @Singleton
    fun provideBookRepository(bookDao: BookDao): BookRepository {
        return BookRepository(bookDao)
    }

    @Provides
    @Singleton
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }

    @Provides
    @Singleton
    fun provideReadingProgressRepository(readingProgressDao: ReadingProgressDao): ReadingProgressRepository {
        return ReadingProgressRepository(readingProgressDao)
    }
}