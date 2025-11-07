package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.bookswap.data.repository.GoogleBooksRepository
import com.example.bookswap.databinding.HomePageBinding
import com.example.bookswap.utils.Constants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomePageBinding
    private lateinit var bookAdapter: BookAdapter
    private val bookRepository = BookRepository()
    private val googleBooksRepository = GoogleBooksRepository()

    private var allBooks = listOf<Book>()
    private var googleBooks = listOf<Book>()
    private var isGoogleBooksMode = false

    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()

        // Request notification permission for Android 13+
        NotificationPermissionHelper.requestNotificationPermission(this)

        // Initialize FCM if not already done
        FCMHelper.initializeFCM(this)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)
        if (userId != null) {
            // REGULAR startService() - NOT startForegroundService()
            val intent = Intent(this, NotificationListenerService::class.java)
            intent.putExtra("USER_ID", userId)
            startService(intent)  // Changed startForegroundService
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        if (!isGoogleBooksMode) {
            loadBooks()
        }
    }



    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList(), { book ->
            openBookDetail(book)
        }, useGridLayout = false)

        binding.booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = bookAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchBar.text.toString().trim()
            if (query.isNotEmpty()) {
                if (isGoogleBooksMode) {
                    searchGoogleBooks(query)
                } else {
                    searchBooks(query)
                }
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
            isGoogleBooksMode = false
            loadBooks()
            highlightCategory(it)
        }

        binding.categoryTech.setOnClickListener {
            isGoogleBooksMode = false
            filterBooksByCategory(BookCategory.TECH)
            highlightCategory(it)
        }

        binding.categoryLaw.setOnClickListener {
            isGoogleBooksMode = false
            filterBooksByCategory(BookCategory.LAW)
            highlightCategory(it)
        }

        binding.categoryBusiness.setOnClickListener {
            isGoogleBooksMode = false
            filterBooksByCategory(BookCategory.BUSINESS)
            highlightCategory(it)
        }

        binding.categoryScience.setOnClickListener {
            isGoogleBooksMode = false
            filterBooksByCategory(BookCategory.SCIENCE)
            highlightCategory(it)
        }

        binding.categoryGoogleBooks.setOnClickListener {
            startActivity(Intent(this, GoogleBooksActivity::class.java))
        }

        highlightCategory(binding.categoryAll)
    }

    private fun highlightCategory(selectedView: View) {
        val categories = listOf(
            binding.categoryAll,
            binding.categoryTech,
            binding.categoryLaw,
            binding.categoryBusiness,
            binding.categoryScience,
            binding.categoryGoogleBooks
        )

        categories.forEach { categoryLayout ->
            val imageView = categoryLayout.getChildAt(0) as? ImageView
            val textView = categoryLayout.getChildAt(1) as? TextView

            if (categoryLayout.id == selectedView.id) {
                categoryLayout.backgroundTintList = ColorStateList.valueOf(getColor(R.color.orange_primary))
                imageView?.setColorFilter(getColor(R.color.white))
                textView?.setTextColor(getColor(R.color.white))
            } else {
                categoryLayout.backgroundTintList = ColorStateList.valueOf(getColor(R.color.white))
                imageView?.clearColorFilter()
                textView?.setTextColor(getColor(android.R.color.black))
            }
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)

        val user = Firebase.auth.currentUser
        val displayName = user?.displayName

        // Use the localized greeting string
        val greetingFormat = getString(R.string.greetingText)
        val name = if (!displayName.isNullOrEmpty()) displayName else "User"
        binding.greetingText.text = String.format(greetingFormat, name)

        Log.d(TAG, "Greeting text set to: ${binding.greetingText.text}")

        val urlString = prefs.getString("profile_image_url", null)
        if (urlString != null) {
            Glide.with(this)
                .load(urlString)
                .placeholder(R.drawable.baseline_person_3_24)
                .error(R.drawable.baseline_person_3_24)
                .into(binding.avatarIcon)
        } else {
            binding.avatarIcon.setImageResource(R.drawable.baseline_person_3_24)
        }
    }

    private fun loadBooks() {
        showLoading(true)
        lifecycleScope.launch {
            Log.d(TAG, "Starting to load books...")

            when (val result = bookRepository.getAllBooks(limit = 50)) {
                is Result.Success -> {
                    Log.d(TAG, "Books loaded: ${result.data.size}")
                    result.data.forEach { book ->
                        Log.d(TAG, "Book: ${book.title}, Status: ${book.status}")
                    }
                    allBooks = result.data
                    displayBooks(allBooks)
                }
                is Result.Error -> {
                    Log.e(TAG, "Error loading books", result.exception)
                    allBooks = emptyList()
                    displayBooks(emptyList())
                    Toast.makeText(this@HomeActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun loadGoogleBooks() {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = googleBooksRepository.searchBooks("")) {
                is Result.Success -> {
                    googleBooks = result.data
                    displayBooks(googleBooks)
                }
                is Result.Error -> {
                    googleBooks = emptyList()
                    displayBooks(emptyList())
                    Toast.makeText(this@HomeActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
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

    private fun searchGoogleBooks(query: String) {
        showLoading(true)
        lifecycleScope.launch {
            when (val result = googleBooksRepository.searchBooks(query)) {
                is Result.Success -> {
                    googleBooks = result.data
                    displayBooks(googleBooks)
                    if (googleBooks.isEmpty()) {
                        Toast.makeText(this@HomeActivity, "No Google Books found for '$query'", Toast.LENGTH_SHORT).show()
                    }
                }
                is Result.Error -> {
                    googleBooks = emptyList()
                    displayBooks(emptyList())
                    Toast.makeText(this@HomeActivity, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
            showLoading(false)
        }
    }

    private fun filterBooksByCategory(category: BookCategory) {
        val filtered = allBooks.filter { it.category == category }
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