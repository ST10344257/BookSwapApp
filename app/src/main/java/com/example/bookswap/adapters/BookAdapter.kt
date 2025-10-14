package com.example.bookswap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookswap.R
import com.example.bookswap.data.models.Book
import com.example.bookswap.utils.PriceUtils

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookImage: ImageView = itemView.findViewById(R.id.bookImage)
        private val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        private val bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor)
        private val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
        private val bookCondition: TextView = itemView.findViewById(R.id.bookCondition)

        fun bind(book: Book) {
            bookTitle.text = book.title
            bookAuthor.text = book.author
            bookPrice.text = PriceUtils.formatPrice(book.price)
            bookCondition.text = book.condition.displayName

            // Load image with Glide
            if (book.photoUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(book.photoUrls[0])
                    .placeholder(R.drawable.books_svgrepo_com)
                    .error(R.drawable.books_svgrepo_com)
                    .centerCrop()
                    .into(bookImage)
            } else {
                bookImage.setImageResource(R.drawable.books_svgrepo_com)
            }

            itemView.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}