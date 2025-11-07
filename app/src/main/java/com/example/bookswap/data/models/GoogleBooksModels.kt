package com.example.bookswap.data.models

data class GoogleBooksResponse(
    val items: List<GoogleBookItem>? = emptyList(),
    val totalItems: Int = 0
)

data class GoogleBookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>? = emptyList(),
    val publishedDate: String? = "",
    val description: String? = "",
    val imageLinks: ImageLinks? = null,
    val categories: List<String>? = emptyList(),
    val saleInfo: SaleInfo? = null,
    val previewLink: String? = null,
    val canonicalVolumeLink: String? = null
)

data class ImageLinks(
    val thumbnail: String? = null,
    val smallThumbnail: String? = null
)

data class SaleInfo(
    val country: String? = null,
    val saleability: String? = null,
    val isEbook: Boolean? = false,
    val listPrice: ListPrice? = null,
    val retailPrice: ListPrice? = null,
    val buyLink: String? = null
)

data class ListPrice(
    val amount: Double? = null,
    val currencyCode: String? = "USD"
)