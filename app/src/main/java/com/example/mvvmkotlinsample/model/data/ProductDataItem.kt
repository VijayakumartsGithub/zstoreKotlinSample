package com.example.mvvmkotlinsample.model.data

data class ProductDataItem(
    val category: String,
    val description: String,
    val id: Int,
    val image: String,
    val price: Double,
    val rating: Rating,
    val title: String,
    val offerList: List<String>,
    val colorsList: List<String>,
)