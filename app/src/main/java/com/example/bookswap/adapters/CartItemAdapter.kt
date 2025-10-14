package com.example.bookswap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookswap.R
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.data.models.CartItem
import com.example.bookswap.utils.PriceUtils

class CartItemAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onRemoveClick: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookImage: ImageView = itemView.findViewById(R.id.cartBookImage)
        private val bookTitle: TextView = itemView.findViewById(R.id.cartBookTitle)
        private val bookAuthor: TextView = itemView.findViewById(R.id.cartBookAuthor)
        private val bookPrice: TextView = itemView.findViewById(R.id.cartBookPrice)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveFromCart)

        fun bind(cartItem: CartItem, position: Int) {
            val book = cartItem.book

            if (book == null || book.status != BookStatus.AVAILABLE) {
                // Item is unavailable
                bookTitle.text = book?.title ?: "Item unavailable"
                bookAuthor.text = "No longer available"
                bookPrice.text = "SOLD"
                bookPrice.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))

                // Fade out the item
                itemView.alpha = 0.5f

                if (book != null) {
                    if (book.photoUrls.isNotEmpty()) {
                        Glide.with(itemView.context)
                            .load(book.photoUrls[0])
                            .placeholder(R.drawable.books_svgrepo_com)
                            .centerCrop()
                            .into(bookImage)
                    }
                } else {
                    bookImage.setImageResource(R.drawable.books_svgrepo_com)
                }
            } else {
                // Item is available
                itemView.alpha = 1.0f
                bookTitle.text = book.title
                bookAuthor.text = book.author
                bookPrice.text = PriceUtils.formatPrice(book.price)
                bookPrice.setTextColor(itemView.context.getColor(R.color.orange_primary))

                if (book.photoUrls.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(book.photoUrls[0])
                        .placeholder(R.drawable.books_svgrepo_com)
                        .centerCrop()
                        .into(bookImage)
                } else {
                    bookImage.setImageResource(R.drawable.books_svgrepo_com)
                }
            }

            btnRemove.setOnClickListener {
                onRemoveClick(cartItem, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position], position)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCart(newItems: MutableList<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        cartItems.removeAt(position)
        notifyItemRemoved(position)
    }
}