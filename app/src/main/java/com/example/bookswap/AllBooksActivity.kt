package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.BookCategory
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.databinding.ActivityAllBooksBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class AllBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllBooksBinding
    private lateinit var bookAdapter: BookAdapter
    private val bookRepository = BookRepository()

    // Define the categories for the tabs
    private val categories = listOf(
        "All" to null,
        "Tech" to BookCategory.TECH,
        "Law" to BookCategory.LAW,
        "Business" to BookCategory.BUSINESS,
        "Science" to BookCategory.SCIENCE
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabs()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList(), { book ->
            val intent = Intent(this, BookDetailActivity::class.java).apply {
                putExtra("BOOK", book)
            }
            startActivity(intent)
        }, useGridLayout = true) // ✅ Use grid layout for AllBooksActivity

        binding.booksRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AllBooksActivity, 2) // 2 columns
            adapter = bookAdapter

            // Add spacing between grid items
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.left = 8
                    outRect.right = 8
                    outRect.top = 8
                    outRect.bottom = 8
                }
            })
        }
    }

    private fun setupTabs() {
        categories.forEach { (title, _) ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title))
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedCategory = categories[tab?.position ?: 0].second
                filterBooks(selectedCategory)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Load initial "All" category
        filterBooks(null)
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish() // Close this activity and go back to the homepage
        }
    }

    private fun filterBooks(category: BookCategory?) {
        showLoading(true)
        lifecycleScope.launch {
            val result = if (category == null) {
                bookRepository.getAllBooks() // Fetch all books
            } else {
                bookRepository.getBooksByCategory(category) // Fetch by category
            }

            when (result) {
                is Result.Success -> {
                    bookAdapter.updateBooks(result.data)

                    // ✅ Show message if no books found
                    if (result.data.isEmpty()) {
                        Toast.makeText(
                            this@AllBooksActivity,
                            "No books found in this category",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@AllBooksActivity,
                        "Failed to load books.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.booksRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}