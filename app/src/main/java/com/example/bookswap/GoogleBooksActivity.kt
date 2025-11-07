package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.repository.GoogleBooksRepository
import com.example.bookswap.databinding.ActivityGoogleBooksBinding
import kotlinx.coroutines.launch
import android.content.Context

class GoogleBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleBooksBinding
    private lateinit var bookAdapter: BookAdapter
    private val googleBooksRepository = GoogleBooksRepository()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        loadGoogleBooks("")
    }


    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            books = emptyList(),
            onBookClick = { book ->
                // ✅ Open BookDetailActivity for Google Books too
                val intent = Intent(this, BookDetailActivity::class.java).apply {
                    putExtra("BOOK", book)
                }
                startActivity(intent)
            },
            useGridLayout = true
        )

        binding.googleBooksRecyclerView.apply {
            layoutManager = GridLayoutManager(this@GoogleBooksActivity, 2)
            adapter = bookAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                loadGoogleBooks(query)
            }
            true
        }
    }

    private fun loadGoogleBooks(query: String) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = googleBooksRepository.searchBooks(query)) {
                is Result.Success -> {
                    displayBooks(result.data)
                    if (result.data.isEmpty()) {
                        Toast.makeText(
                            this@GoogleBooksActivity,
                            "No books found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Result.Error -> {
                    displayBooks(emptyList())
                    Toast.makeText(
                        this@GoogleBooksActivity,
                        "Failed to load books: ${result.exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun displayBooks(books: List<Book>) {
        bookAdapter.updateBooks(books)
        showEmptyView(books.isEmpty())
    }

    private fun showLoading(loading: Boolean) {
        binding.loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.googleBooksRecyclerView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showEmptyView(show: Boolean) {
        binding.emptyView.visibility = if (show) View.VISIBLE else View.GONE
        binding.googleBooksRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}