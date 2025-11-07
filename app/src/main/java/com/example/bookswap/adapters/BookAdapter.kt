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

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit,
    private val useGridLayout: Boolean = false // ✅ NEW PARAMETER
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

            // ✅ Handle pricing for both Google Books and regular books
            if (book.isGoogleBook) {
                if (book.price <= 0.0) {
                    bookPrice.text = "View on Google"
                    bookPrice.setTextColor(itemView.context.getColor(R.color.orange_primary))
                } else {
                    bookPrice.text = "$%.2f".format(book.price)
                    bookPrice.setTextColor(itemView.context.getColor(R.color.orange_primary))
                }
            } else {
                bookPrice.text = "R%.2f".format(book.price)
                bookPrice.setTextColor(itemView.context.getColor(R.color.orange_primary))
            }

            // Show condition only if it's not a Google Book
            if (!book.isGoogleBook) {
                bookCondition.text = book.condition.displayName
                bookCondition.visibility = View.VISIBLE
            } else {
                bookCondition.visibility = View.GONE
            }

            // Load image
            val imageUrl = when {
                book.imageUrl.isNotEmpty() -> book.imageUrl
                book.photoUrls.isNotEmpty() -> book.photoUrls[0]
                else -> null
            }

            if (imageUrl != null) {
                Glide.with(itemView.context)
                    .load(imageUrl)
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
        // ✅ Choose layout based on useGridLayout parameter
        val layoutId = if (useGridLayout) {
            R.layout.item_book_grid
        } else {
            R.layout.item_book
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
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