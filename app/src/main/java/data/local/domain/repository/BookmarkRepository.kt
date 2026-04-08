package data.local.domain.repository

import data.local.dao.BookmarkDao
import data.local.entity.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksForBook(bookId)
    }

    suspend fun addBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkById(bookmarkId: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(bookmarkId)
    }

    suspend fun updateBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookmarkDao.updateBookmark(bookmark)
    }
}