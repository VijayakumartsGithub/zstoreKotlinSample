package com.example.mvvmkotlinsample.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mvvmkotlinsample.R
import com.example.mvvmkotlinsample.databinding.ProductItemBinding
import com.example.mvvmkotlinsample.model.data.ProductDataItem
import de.hdodenhof.circleimageview.CircleImageView

class MainAdapter : RecyclerView.Adapter<MainViewHolder>() {

    var products = mutableListOf<ProductDataItem>()

    fun setProductsList(products: List<ProductDataItem>) {
        this.products = products.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProductItemBinding.inflate(inflater, parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val product = products[position]
        /*Product Title*/
        holder.binding.textView.text = product.title
        /*Product Image*/
        Glide.with(holder.itemView.context).load(product.image).into(holder.binding.imageView)
        /*Product Rating*/
        holder.binding.ratingTextView.text = product.rating.rate.toString()
        /*Rating Count*/
        holder.binding.totalRatingTextView.text = "(${product.rating.count.toString()})"
        /*Product Price*/
        holder.binding.offerAmount.text = "$${product.price.toString()}"
        /*Rating*/
        holder.binding.ratingBar.rating = product.rating.rate.toFloat()
        /*Color Swatches*/
        val colorsSet = HashSet<String>()
        val linearLayout: LinearLayout = holder.binding.colorSwatches
        val inflater = LayoutInflater.from(holder.itemView.context)
        linearLayout.removeAllViews()
        for (color in product.colorsList) {
            if (!colorsSet.contains(color)) {
                val colorSwatchView =
                    inflater.inflate(R.layout.color_swatches, linearLayout, false) as CircleImageView

                val colorResId = getResIdForColor(holder.itemView.context, color)
                colorSwatchView.setColorFilter(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        colorResId
                    )
                )
                linearLayout.addView(colorSwatchView)

                colorsSet.add(color)
            }
        }
    }

    private fun getResIdForColor(context: Context, colorName: String): Int {
        return context.resources.getIdentifier(colorName, "color", context.packageName)
    }


    override fun getItemCount(): Int {
        return products.size
    }

}

class MainViewHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {

}