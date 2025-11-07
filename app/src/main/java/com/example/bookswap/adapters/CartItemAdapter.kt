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

            // This now checks for Google Book images (imageUrl) and
            // user-uploaded images (photoUrls)
            val imageUrl = when {
                book?.imageUrl?.isNotEmpty() == true -> book.imageUrl   // Google Book image
                book?.photoUrls?.isNotEmpty() == true -> book.photoUrls[0] // User-uploaded image
                else -> null                                          // No image
            }

            // Load the image with Glide
            if (imageUrl != null) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.books_svgrepo_com)
                    .error(R.drawable.books_svgrepo_com)
                    .centerCrop()
                    .into(bookImage)
            } else {
                // Set placeholder if no image URL is found
                bookImage.setImageResource(R.drawable.books_svgrepo_com)
            }

            if (book == null || book.status != BookStatus.AVAILABLE) {
                // Item is unavailable
                bookTitle.text = book?.title ?: "Item unavailable"
                bookAuthor.text = "No longer available"
                bookPrice.text = "SOLD"
                bookPrice.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))

                // Fade out the item
                itemView.alpha = 0.5f

            } else {
                // Item is available
                itemView.alpha = 1.0f
                bookTitle.text = book.title
                bookAuthor.text = book.author

                // ✅ FIXED - Format price as String
                bookPrice.text = "R%.2f".format(book.price)

                bookPrice.setTextColor(itemView.context.getColor(R.color.orange_primary))
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