package com.example.mvvmkotlinsample.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvmkotlinsample.model.data.ProductData
import com.example.mvvmkotlinsample.model.data.ProductDataItem
import com.example.mvvmkotlinsample.model.repo.MainRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Integer.min
import kotlin.random.Random

class MainViewModel constructor(private val mainRepository: MainRepository) : ViewModel() {

    val productDataItemFiltered = MutableLiveData<ProductData>()
    val errorMessage = MutableLiveData<String>()
    var productDataItem = MutableLiveData<ProductData>()


    fun getProducts() {
        val response = mainRepository.getAllProducts()

        response.enqueue(object : Callback<ProductData> {
            override fun onResponse(
                call: Call<ProductData>,
                response: Response<ProductData>
            ) {
                val data = response.body()
                if (data != null) {
                    Log.d("ON RESPONSE", "data: $data")

                    val productDataItemValue = ProductData()

                    for (ProductDataItemValue in data) {

                        /*OFFERS LIST*/
                        val offerListsData = listOf(
                            "Hdfc Bank Credit Card",
                            "Big Billion Day Offer",
                            "Great Indian Festival Offer"
                        )
                        val startingIndexOffer =
                            if (offerListsData.isNotEmpty()) Random.nextInt(offerListsData.size) else 0
                        val selectedOffers = offerListsData.subList(
                            startingIndexOffer,
                            min(startingIndexOffer + offerListsData.size, offerListsData.size)
                        )

                        /*COLORS LIST*/
                        val colorListsData =
                            listOf("blue", "purple_200", "red", "black", "teal_200")
                        val startingIndex =
                            if (colorListsData.isNotEmpty()) Random.nextInt(colorListsData.size) else 0
                        val selectedColors = colorListsData.subList(
                            startingIndex,
                            min(startingIndex + colorListsData.size, colorListsData.size)
                        )

                        val productDataItem = ProductDataItem(
                            category = ProductDataItemValue.category,
                            description = ProductDataItemValue.description,
                            id = ProductDataItemValue.id,
                            image = ProductDataItemValue.image,
                            price = ProductDataItemValue.price,
                            rating = ProductDataItemValue.rating,
                            title = ProductDataItemValue.title,
                            offerList = selectedOffers,
                            colorsList = selectedColors,
                        )

                        productDataItemValue.add(productDataItem)
                    }


                    productDataItem.postValue(productDataItemValue)
                }
            }

            override fun onFailure(call: Call<ProductData>, t: Throwable) {
                errorMessage.postValue(t.message)
            }
        })
    }

    fun filterProductsByCategory(category: String) {
        val products = productDataItem.value

        if (products != null) {
            Log.d("FILTER1", "filterProductsByCategory: $category")

            val filteredList = products.filter { it.category == category }
            val filteredData = ProductData()
            filteredData.addAll(filteredList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun filterProductsByCategoryAndOffer(category: String, offer: String) {
        val products = productDataItem.value

        if (products != null) {
            Log.d("FILTER2", "filterProductsByCategoryAndOffer: $category")

            val filteredList =
                products.filter { it.category == category && it.offerList.contains(offer) }
            val filteredData = ProductData()
            filteredData.addAll(filteredList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun sortProducts(sortType: String, selectedCategory: String?, selectedOffer: String?) {
        val products = productDataItem.value

        if (products != null) {
            Log.d(
                "SORT",
                "sortProducts type: $sortType, selectedCategory: $selectedCategory, selectedOffer: $selectedOffer"
            )

            val filteredList = when {
                selectedCategory != null && selectedOffer == null ->
                    products.filter { it.category == selectedCategory }
                selectedCategory != null && selectedOffer != null ->
                    products.filter {
                        it.category == selectedCategory && it.offerList.contains(
                            selectedOffer
                        )
                    }
                else ->
                    products
            }

            val sortedList = when (sortType) {
                "Rating" -> filteredList.sortedBy { it.rating.rate }
                "Price" -> filteredList.sortedBy { it.price }
                else -> filteredList
            }

            val filteredData = ProductData()
            filteredData.addAll(sortedList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun searchProductsByCategory(query: String, selectedCategory: String?, selectedOffer: String?) {
        val products = productDataItem.value

        if (products != null) {
            val filteredList = products.takeIf {
                selectedCategory != null && selectedOffer != null
            }?.filter {
                it.category == selectedCategory && it.offerList.contains(selectedOffer)
            }

            val filteredQueryList = filteredList?.filter {
                it.title.contains(query, ignoreCase = true)
            } ?: emptyList()

            val filteredData = ProductData()
            filteredData.addAll(filteredQueryList)
            productDataItemFiltered.value = filteredData
        }
    }

}
