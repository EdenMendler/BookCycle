package com.example.bookcycle.utilities

import java.io.Serializable

data class BookData(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val borrowerId: String = "",
    val borrowerName: String = ""
) : Serializable
