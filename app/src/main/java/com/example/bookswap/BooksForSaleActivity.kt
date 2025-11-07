package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.BookRepository
// Import new binding class
import com.example.bookswap.databinding.ActivityBooksForSaleBinding
import com.example.bookswap.utils.Constants
import kotlinx.coroutines.launch

class BooksForSaleActivity : AppCompatActivity() {

    // Use View Binding
    private lateinit var binding: ActivityBooksForSaleBinding
    private lateinit var bookAdapter: BookAdapter

    private val bookRepository = BookRepository()
    private var userId: String? = null
    private val TAG = "BooksForSaleActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityBooksForSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID from SharedPreferences
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)

        setupRecyclerView()
        setupClickListeners()
        loadUserBooks()
    }

    // initViews() is no longer needed, View Binding handles it.

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            books = emptyList(),
            onBookClick = { book ->
                val intent = Intent(this, BookDetailActivity::class.java).apply {
                    putExtra("BOOK", book)
                }
                startActivity(intent)
            },
            useGridLayout = false
        )
        // Use the binding object to find the RecyclerView
        binding.userBooksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userBooksRecyclerView.adapter = bookAdapter
    }

    private fun setupClickListeners() {
        // Use the binding object to find the button
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadUserBooks() {
        if (userId == null) {
            Log.w(TAG, "User ID is null. Cannot load books.")
            showEmptyView(true)
            return
        }

        showLoading(true)
        Log.d(TAG, "Loading books for user: $userId")

        lifecycleScope.launch {
            // Make sure you have a 'getUserBooks' function in your repository
            when (val result = bookRepository.getUserBooks(userId!!)) {
                is Result.Success -> {
                    showLoading(false)
                    if (result.data.isEmpty()) {
                        Log.d(TAG, "No books found for user.")
                        showEmptyView(true)
                    } else {
                        Log.d(TAG, "Successfully loaded ${result.data.size} books.")
                        showEmptyView(false)
                        bookAdapter.updateBooks(result.data)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmptyView(true)
                    Log.e(TAG, "Failed to load books", result.exception)
                    Toast.makeText(
                        this@BooksForSaleActivity,
                        "Failed to load books",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        // Use binding to find views
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.userBooksRecyclerView.visibility = if (loading) View.GONE else View.VISIBLE
        binding.emptyView.visibility = View.GONE // Hide empty view while loading
    }

    private fun showEmptyView(show: Boolean) {
        // Use binding to find views
        binding.emptyView.visibility = if (show) View.VISIBLE else View.GONE
        binding.userBooksRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Refresh list when returning to this activity
        loadUserBooks()
    }
}