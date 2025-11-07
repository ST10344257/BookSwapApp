package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.WishlistRepository
import com.example.bookswap.databinding.ActivityWishlistBinding
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.ConnectivityObserver
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private lateinit var bookAdapter: BookAdapter
    private val wishlistRepository = WishlistRepository()
    private var userId: String? = null
    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)

        setupRecyclerView()
        setupClickListeners()
        observeConnectivity()
        loadWishlist()
    }

    // Observe connectivity
    private fun observeConnectivity() {
        lifecycleScope.launch {
            ConnectivityObserver.observeConnectivity(this@WishlistActivity).collect { online ->
                isOnline = online
                android.util.Log.d("WishlistActivity", "Network status: ${if (online) "Online" else "Offline"}")
            }
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            books = emptyList(),
            onBookClick = { book ->
                val intent = Intent(this, BookDetailActivity::class.java).apply {
                    putExtra("BOOK", book)
                }
                startActivity(intent)
            },
            useGridLayout = true
        )

        binding.wishlistRecyclerView.apply {
            layoutManager = GridLayoutManager(this@WishlistActivity, 2)
            adapter = bookAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnBrowseGoogleBooks.setOnClickListener {
            val intent = Intent(this, GoogleBooksActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadWishlist() {
        if (userId == null) {
            showEmptyView(true)
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            when (val result = wishlistRepository.getWishlist(userId!!)) {
                is Result.Success -> {
                    showLoading(false)

                    android.util.Log.d("WishlistActivity", "Loaded ${result.data.size} books")

                    if (result.data.isEmpty()) {
                        showEmptyView(true)
                    } else {
                        showEmptyView(false)
                        bookAdapter.updateBooks(result.data)

                        // Show message if loaded from cache
                        if (!isOnline) {
                            Toast.makeText(
                                this@WishlistActivity,
                                "📵 Loaded from offline cache",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmptyView(true)

                    if (!isOnline) {
                        Toast.makeText(
                            this@WishlistActivity,
                            "📵 Offline - No cached wishlist data",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@WishlistActivity,
                            "Failed to load wishlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showEmptyView(show: Boolean) {
        binding.emptyView.visibility = if (show) View.VISIBLE else View.GONE
        binding.wishlistRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }
}