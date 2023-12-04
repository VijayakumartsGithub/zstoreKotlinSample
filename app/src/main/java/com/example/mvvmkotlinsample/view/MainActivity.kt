package com.example.mvvmkotlinsample

import android.content.res.ColorStateList
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mvvmkotlinsample.databinding.ActivityMainBinding
import com.example.mvvmkotlinsample.model.data.OfferLayoutData
import com.example.mvvmkotlinsample.model.interfaces.RetrofitService
import com.example.mvvmkotlinsample.model.repo.MainRepository
import com.example.mvvmkotlinsample.view.adapters.MainAdapter
import com.example.mvvmkotlinsample.viewModel.MainViewModel
import com.example.mvvmkotlinsample.viewModel.MyViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private val retrofitService = RetrofitService.getRetrofitInstance()
    val adapter = MainAdapter()
    private val addedCategories = mutableSetOf<String>()
    lateinit var filterFAB: FloatingActionButton
    private var selectedCategoryValue: String? = null
    private var selectedOfferValue: String? = null
    private lateinit var searchView: SearchView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel =
            ViewModelProvider(this, MyViewModelFactory(MainRepository(retrofitService))).get(
                MainViewModel::class.java
            )

        filterFAB = binding.floatingActionButton
        searchView = binding.searchView

        /*SET CATEGORIES CHIP*/
        viewModel.productDataItem.observe(this, Observer {
            it?.let {
                var isFirstChip = true

                if (it.isNotEmpty()) {
                    selectedCategoryValue = it[0].category
                    selectedOfferValue = null

                    for (ProductDataItem in it) {
                        if (!addedCategories.contains(ProductDataItem.category)) {
                            Log.d(TAG, "categories: " + ProductDataItem.category)
                            /*Chip*/
                            val chip = Chip(this)
                            chip.setTypeface(null, Typeface.BOLD)
                            /*Chip Text*/
                            chip.text = ProductDataItem.category
                            val textColorResId = if (isFirstChip) R.color.orange else R.color.black
                            chip.setTextColor(ContextCompat.getColor(this, textColorResId))
                            /*Chip Selection*/
                            chip.isSelected = isFirstChip
                            /*Chip BackgroundColor*/
                            val backgroundColorResId =
                                if (isFirstChip) R.color.orangeLight else R.color.white
                            chip.setChipBackgroundColorResource(backgroundColorResId)
                            /*Chip Stroke*/
                            val strokeWidthValue =
                                if (isFirstChip) 5.0F else 1.0F
                            chip.chipStrokeWidth = strokeWidthValue
                            val strokeColorResId =
                                if (isFirstChip) R.color.orange else R.color.red
                            chip.setChipStrokeColorResource(strokeColorResId)

                            chip.setOnClickListener {
                                for (index in 0 until binding.categoryChips.childCount) {
                                    val child = binding.categoryChips.getChildAt(index)
                                    if (child is Chip) {
                                        /*Chip Selection*/
                                        child.isSelected = false
                                        /*Chip BackgroundColor*/
                                        child.setChipBackgroundColorResource(R.color.white)
                                        /*Chip Stroke*/
                                        child.chipStrokeWidth = 1.0F
                                        child.setChipStrokeColorResource(R.color.red)
                                        child.setTextColor(ContextCompat.getColor(this, R.color.black))
                                    }
                                }

                                /*Chip Selection*/
                                chip.isSelected = true
                                /*Chip BackgroundColor*/
                                chip.setChipBackgroundColorResource(R.color.orangeLight)
                                /*Chip Stroke*/
                                chip.chipStrokeWidth = 5.0F
                                chip.setChipStrokeColorResource(R.color.orange)
                                chip.setTextColor(ContextCompat.getColor(this, R.color.orange))
                                selectedCategoryValue = ProductDataItem.category
                                selectedOfferValue = null

                                /*SET OFFERS*/
                                addCardView(ProductDataItem.offerList, ProductDataItem.category)

                                /*FILTER RECYCLERVIEW*/
                                viewModel.filterProductsByCategory(ProductDataItem.category)
                            }

                            binding.categoryChips.addView(chip)

                            addedCategories.add(ProductDataItem.category)
                            isFirstChip = false
                        }
                    }

                    //SET OFFERS FOR FIRST CATEGORY
                    /*SET OFFERS*/
                    addCardView(it[0].offerList, it[0].category)

                    /*FILTER RECYCLERVIEW*/
                    viewModel.filterProductsByCategory(it[0].category)
                }
            }
        })

        /*SET ADAPTER*/
        binding.recyclerViewWidget.adapter = adapter

        viewModel.productDataItemFiltered.observe(this, Observer {
            it?.let {
                Log.d(TAG, "onCreate: Data $it")
                adapter.setProductsList(it)
            }
        })
        viewModel.errorMessage.observe(this, Observer {
        })

        viewModel.getProducts()

        /*Alert Dialog*/
        val checkedItem = intArrayOf(-1)

        filterFAB.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Filter order: From Top to Bottom")

            val listItems = arrayOf("Rating", "Price")

            alertDialog.setSingleChoiceItems(listItems, checkedItem[0]) { dialog, which ->
                checkedItem[0] = which

                Log.d(TAG, "onClicked FAB: Data ${listItems[checkedItem[0]]}")

                viewModel.sortProducts(listItems[checkedItem[0]], selectedCategoryValue, selectedOfferValue)

                dialog.dismiss()
            }

            val customAlertDialog = alertDialog.create()
            customAlertDialog.show()
        }

        /*Search View*/
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!selectedCategoryValue.isNullOrBlank() && !selectedOfferValue.isNullOrBlank()) {
                    viewModel.searchProductsByCategory(newText.orEmpty(), selectedCategoryValue, selectedOfferValue)
                }
                return true
            }
        })
    }

    private fun addCardView(offersList: List<String>, selectedCategory: String) {
        val linearLayout: LinearLayout = binding.offerLayout

        val inflater = LayoutInflater.from(this)

        linearLayout.removeAllViews()
        binding.offerChips.removeAllViews()

        val offerLayoutDataList: List<OfferLayoutData> = listOf(
            OfferLayoutData(offerId = "Hdfc Bank Credit Card", layoutId = R.layout.offer_1),
            OfferLayoutData(offerId = "Big Billion Day Offer", layoutId = R.layout.offer_2),
            OfferLayoutData(offerId = "Great Indian Festival Offer", layoutId = R.layout.offer_3)
        )

        Log.d(TAG, "SELECTED OFFER: Data $offersList")

        for (OfferLayoutData in offerLayoutDataList) {
            for (offer in offersList) {
                if (offer == OfferLayoutData.offerId) {
                    val cardView = inflater.inflate(OfferLayoutData.layoutId, linearLayout, false)
                    cardView.setOnClickListener {
                        /*FILTER RECYCLERVIEW*/
                        viewModel.filterProductsByCategoryAndOffer(
                            selectedCategory,
                            OfferLayoutData.offerId
                        )

                        Log.d(TAG, "SELECTED OFFER: Data ${OfferLayoutData.offerId}")

                        binding.offerChips.removeAllViews()

                        val chip = Chip(this)
                        chip.text = "Applied: ${OfferLayoutData.offerId}"
                        chip.setTypeface(null, Typeface.BOLD)
                        chip.isCloseIconVisible = true
                        chip.isSelected = true
                        /*Chip BackgroundColor*/
                        chip.setChipBackgroundColorResource(R.color.white)
                        /*Chip Stroke*/
                        chip.chipStrokeWidth = 5.0F
                        chip.setChipStrokeColorResource(R.color.blue)
                        chip.setTextColor(ContextCompat.getColor(this, R.color.blue))
                        val closeIconColor = ContextCompat.getColor(this, R.color.blue)
                        chip.closeIconTint = ColorStateList.valueOf(closeIconColor)

                        chip.setOnCloseIconClickListener {
                            binding.offerChips.removeView(chip)

                            viewModel.filterProductsByCategory(selectedCategory)

                            selectedOfferValue = null
                        }

                        binding.offerChips.addView(chip)

                        selectedOfferValue = OfferLayoutData.offerId
                    }
                    linearLayout.addView(cardView)
                }
            }
        }
    }
}