package com.example.bookcycle.utilities

data class BorrowRequest(
    val id: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val ownerId: String = "",
    val status: String = "",
    val timestamp: Long = 0,
    val requestType: String = "borrow"
)
