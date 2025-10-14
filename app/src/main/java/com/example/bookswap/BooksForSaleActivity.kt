package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.utils.Constants
import kotlinx.coroutines.launch

class BooksForSaleActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookAdapter: BookAdapter

    private val bookRepository = BookRepository()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadUserBooks()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnback)

        // Add these to you or create programmatically
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@BooksForSaleActivity)
        }

        emptyView = TextView(this).apply {
            text = "No books listed yet"
            textSize = 18f
            gravity = android.view.Gravity.CENTER
        }

        progressBar = ProgressBar(this)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList()) { book ->
            val intent = Intent(this, BookDetailActivity::class.java).apply {
                putExtra("BOOK", book)
            }
            startActivity(intent)
        }
        recyclerView.adapter = bookAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
    }

    private fun loadUserBooks() {
        if (userId == null) {
            showEmptyView(true)
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            when (val result = bookRepository.getUserBooks(userId!!)) {
                is Result.Success -> {
                    showLoading(false)
                    if (result.data.isEmpty()) {
                        showEmptyView(true)
                    } else {
                        showEmptyView(false)
                        bookAdapter.updateBooks(result.data)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmptyView(true)
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
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showEmptyView(show: Boolean) {
        emptyView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Refresh list when returning to this activity
        loadUserBooks()
    }
}