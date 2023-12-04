package com.example.mvvmkotlinsample.model.repo

import com.example.mvvmkotlinsample.model.interfaces.RetrofitService

class MainRepository constructor(private val retrofitService: RetrofitService) {
    fun getAllProducts() = retrofitService.getProducts()
}