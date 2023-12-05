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

                    val categoryOfferMap = HashMap<String, List<String>>()

                    for (ProductDataItem in data) {

                        val category = ProductDataItem.category

                        if (!categoryOfferMap.containsKey(category)) {
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
                            categoryOfferMap[category] = selectedOffers
                        }

                        val selectedOffers = categoryOfferMap[category] ?: emptyList()

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
                            category = ProductDataItem.category,
                            description = ProductDataItem.description,
                            id = ProductDataItem.id,
                            image = ProductDataItem.image,
                            price = ProductDataItem.price,
                            rating = ProductDataItem.rating,
                            title = ProductDataItem.title,
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

    fun filterProductsByCategory(categories: List<String>) {
        val products = productDataItem.value

        if (products != null) {
            Log.d("FILTER1", "filterProductsByCategory: $categories")

            val filteredList = products.filter { categories.contains(it.category) }
            val filteredData = ProductData()
            filteredData.addAll(filteredList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun filterProductsByCategoryAndOffer(categories: List<String>, offer: String) {
        val products = productDataItem.value

        if (products != null) {
            Log.d("FILTER2", "filterProductsByCategoryAndOffer: $categories")

            val filteredList =
                products.filter { categories.contains(it.category) && it.offerList.contains(offer) }
            val filteredData = ProductData()
            filteredData.addAll(filteredList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun sortProducts(sortType: String, categories: List<String>, selectedOffer: String?) {
        val products = productDataItem.value

        if (products != null) {
            Log.d(
                "SORT",
                "sortProducts type: $sortType, selectedCategory: $categories, selectedOffer: $selectedOffer"
            )

            val filteredList = when {
                categories.isNotEmpty() && selectedOffer == null ->
                    products.filter { categories.contains(it.category) }
                categories.isNotEmpty() && selectedOffer != null ->
                    products.filter {
                        categories.contains(it.category) && it.offerList.contains(
                            selectedOffer
                        )
                    }
                else ->
                    products
            }

            val sortedList = when (sortType) {
                "Rating" -> filteredList.sortedByDescending { it.rating.rate }
                "Price" -> filteredList.sortedBy { it.price }
                else -> filteredList
            }

            val filteredData = ProductData()
            filteredData.addAll(sortedList)
            productDataItemFiltered.value = filteredData
        }
    }

    fun searchProductsByCategory(query: String, categories: List<String>, selectedOffer: String?) {
        val products = productDataItem.value

        if (products != null) {
            val filteredList = products.takeIf {
                categories.isNotEmpty() && selectedOffer != null
            }?.filter {
                categories.contains(it.category) && it.offerList.contains(selectedOffer)
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
