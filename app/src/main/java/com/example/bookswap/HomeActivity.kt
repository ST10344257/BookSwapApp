package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookCategory
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.databinding.HomePageBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    // Use View Binding for safer, cleaner code
    private lateinit var binding: HomePageBinding
    private lateinit var bookAdapter: BookAdapter
    private val bookRepository = BookRepository()

    private var allBooks = listOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload user data and books every time the user returns to this activity
        loadUserData()
        loadBooks()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList()) { book ->
            openBookDetail(book)
        }
        binding.booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = bookAdapter
            isNestedScrollingEnabled = false // Important for performance in a ScrollView
        }
    }

    private fun setupClickListeners() {
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                searchBooks(query)
            }
            true
        }

        binding.notificationIcon.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.avatarIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.fabAddBook.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

        binding.viewAllBooks.setOnClickListener {
            startActivity(Intent(this, AllBooksActivity::class.java))
        }

        setupCategoryFilters()
    }

    private fun setupCategoryFilters() {
        binding.categoryAll.setOnClickListener {
            filterBooksByCategory(null)
            highlightCategory(it) // 'it' refers to the clicked view
        }
        binding.categoryTech.setOnClickListener {
            filterBooksByCategory(BookCategory.TECH)
            highlightCategory(it)
        }
        binding.categoryLaw.setOnClickListener {
            filterBooksByCategory(BookCategory.LAW)
            highlightCategory(it)
        }
        binding.categoryBusiness.setOnClickListener {
            filterBooksByCategory(BookCategory.BUSINESS)
            highlightCategory(it)
        }
        binding.categoryScience.setOnClickListener {
            filterBooksByCategory(BookCategory.SCIENCE)
            highlightCategory(it)
        }
        binding.categoryGoogleBooks.setOnClickListener {
            Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Highlight "All" by default
        highlightCategory(binding.categoryAll)
    }

    private fun highlightCategory(selectedView: View) {
        val categories = listOf(
            binding.categoryAll,
            binding.categoryTech,
            binding.categoryLaw,
            binding.categoryBusiness,
            binding.categoryScience
        )

        categories.forEach { categoryLayout ->
            // Safely cast child views to prevent crashes
            val imageView = categoryLayout.getChildAt(0) as? ImageView
            val textView = categoryLayout.getChildAt(1) as? TextView

            if (categoryLayout.id == selectedView.id) {
                // Highlight selected category
                categoryLayout.backgroundTintList = ColorStateList.valueOf(getColor(R.color.orange_primary))
                imageView?.setColorFilter(getColor(R.color.white))
                textView?.setTextColor(getColor(R.color.white))
            } else {
                // Unhighlight other categories
                categoryLayout.backgroundTintList = ColorStateList.valueOf(getColor(R.color.white))
                imageView?.clearColorFilter()
                textView?.setTextColor(getColor(android.R.color.black))
            }
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)

        // Load and display name directly from Firebase Auth as the source of truth
        val user = Firebase.auth.currentUser
        val displayName = user?.displayName
        binding.greetingText.text = if (!displayName.isNullOrEmpty()) "Hi, $displayName" else "Hi, User"

        // Load and display profile picture from the permanent URL using Glide
        val urlString = prefs.getString("profile_image_url", null)
        if (urlString != null) {
            Glide.with(this)
                .load(urlString)
                .placeholder(R.drawable.baseline_person_3_24)
                .error(R.drawable.baseline_person_3_24)
                .into(binding.avatarIcon)
        } else {
            binding.avatarIcon.setImageResource(R.drawable.baseline_person_3_24) // Default icon
        }
    }


    private fun loadBooks() {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = bookRepository.getAllBooks(limit = 50)) {
                is Result.Success -> {
                    allBooks = result.data
                    displayBooks(allBooks)
                }
                is Result.Error -> {
                    allBooks = emptyList()
                    displayBooks(emptyList()) // Show empty state
                    Toast.makeText(this@HomeActivity, "Failed to load books.", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun searchBooks(query: String) {
        val filteredBooks = allBooks.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.author.contains(query, ignoreCase = true)
        }
        displayBooks(filteredBooks)
        if (filteredBooks.isEmpty()) {
            Toast.makeText(this, "No books found for '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterBooksByCategory(category: BookCategory?) {
        val filtered = if (category == null) {
            allBooks
        } else {
            allBooks.filter { it.category == category }
        }
        displayBooks(filtered)
    }

    private fun displayBooks(books: List<Book>) {
        bookAdapter.updateBooks(books)
        showEmptyView(books.isEmpty())
    }

    private fun openBookDetail(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("BOOK", book)
        }
        startActivity(intent)
    }

    private fun showLoading(loading: Boolean) {
        binding.loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.booksRecyclerView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showEmptyView(show: Boolean) {
        binding.emptyBooksView.visibility = if (show) View.VISIBLE else View.GONE
        binding.booksRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}

