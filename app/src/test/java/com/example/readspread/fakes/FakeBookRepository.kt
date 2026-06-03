package com.example.readspread.fakes

import data.local.domain.repository.BookRepository
import data.local.entity.Book
import kotlinx.coroutines.flow.Flow

class FakeBookRepository(private val dao: FakeBookDao = FakeBookDao()) : BookRepository(dao) {
    // Используем конструктор родителя, передавая фейковый DAO
}