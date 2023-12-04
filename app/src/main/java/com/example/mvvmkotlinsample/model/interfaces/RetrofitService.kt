package com.example.mvvmkotlinsample.model.interfaces

import com.example.mvvmkotlinsample.model.data.ProductData
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RetrofitService {

    @GET("products")
    fun getProducts(): Call<ProductData>

    companion object {
        private var retrofitService: RetrofitService? = null

        fun getRetrofitInstance(): RetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://fakestoreapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                retrofitService = retrofit.create(RetrofitService::class.java)
            }
            return retrofitService!!
        }
    }

}