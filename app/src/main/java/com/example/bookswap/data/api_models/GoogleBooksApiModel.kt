package com.example.bookswap.data.api_models

// This file contains data classes that perfectly match the JSON response
// from the Google Books API.

data class GoogleBookResponse(
    val items: List<GoogleBookItem>?
)

data class GoogleBookItem(
    val id: String,
    val volumeInfo: VolumeInfo,
    val saleInfo: SaleInfo?
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)

data class SaleInfo(
    val buyLink: String?,
    val listPrice: Price?,
    val retailPrice: Price?
)

data class Price(
    val amount: Double?,
    val currencyCode: String?
)
