package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.utils.Constants
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


class BooksForSaleActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var availableCount: TextView
    private lateinit var soldCount: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var btnAddBook: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var bookAdapter: BookAdapter
    private val bookRepository = BookRepository()

    private var allBooks = listOf<Book>()
    private var userId: String? = null
    private var currentFilter = BookStatus.AVAILABLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.booksale_page)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadUserBooks()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        availableCount = findViewById(R.id.availableCount)
        soldCount = findViewById(R.id.soldCount)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.booksRecyclerView)
        emptyView = findViewById(R.id.emptyBooksView)
        btnAddBook = findViewById(R.id.btnAddBook)
        progressBar = findViewById(R.id.loadingProgress)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList()) { book ->
            showBookOptions(book)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BooksForSaleActivity)
            adapter = bookAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnAddBook.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // All books
                        currentFilter = BookStatus.AVAILABLE
                        displayBooks(allBooks)
                    }
                    1 -> {
                        // Available only
                        currentFilter = BookStatus.AVAILABLE
                        displayBooks(allBooks.filter { it.status == BookStatus.AVAILABLE })
                    }
                    2 -> {
                        // Sold only
                        currentFilter = BookStatus.SOLD
                        displayBooks(allBooks.filter { it.status == BookStatus.SOLD })
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadUserBooks() {
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            when (val result = bookRepository.getBooksBySeller(userId!!)) {
                is Result.Success -> {
                    allBooks = result.data
                    updateStats()
                    displayBooks(allBooks)
                    showLoading(false)
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this@BooksForSaleActivity,
                        "Failed to load books: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyView(true)
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun updateStats() {
        val available = allBooks.count { it.status == BookStatus.AVAILABLE }
        val sold = allBooks.count { it.status == BookStatus.SOLD }

        availableCount.text = available.toString()
        soldCount.text = sold.toString()
    }

    private fun displayBooks(books: List<Book>) {
        if (books.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
            bookAdapter.updateBooks(books)
        }
    }

    private fun showBookOptions(book: Book) {
        val options = if (book.status == BookStatus.AVAILABLE) {
            arrayOf("View Details", "Edit", "Mark as Sold", "Delete")
        } else {
            arrayOf("View Details", "Delete")
        }

        AlertDialog.Builder(this)
            .setTitle(book.title)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "View Details" -> openBookDetail(book)
                    "Edit" -> editBook(book)
                    "Mark as Sold" -> markAsSold(book)
                    "Delete" -> confirmDelete(book)
                }
            }
            .show()
    }

    private fun openBookDetail(book: Book) {
        val intent = Intent(this, BookDetailActivity::class.java)
        intent.putExtra("BOOK", book)
        startActivity(intent)
    }

    private fun editBook(book: Book) {
        Toast.makeText(this, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to edit book activity
    }

    private fun markAsSold(book: Book) {
        AlertDialog.Builder(this)
            .setTitle("Mark as Sold")
            .setMessage("Mark ${book.title} as sold?")
            .setPositiveButton("Mark Sold") { _, _ ->
                lifecycleScope.launch {
                    when (bookRepository.updateBookStatus(book.bookId, BookStatus.SOLD)) {
                        is Result.Success -> {
                            Toast.makeText(
                                this@BooksForSaleActivity,
                                "Book marked as sold",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadUserBooks()
                        }
                        is Result.Error -> {
                            Toast.makeText(
                                this@BooksForSaleActivity,
                                "Failed to update book",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Loading -> {}
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(book: Book) {
        AlertDialog.Builder(this)
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete ${book.title}? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteBook(book: Book) {
        showLoading(true)

        lifecycleScope.launch {
            when (bookRepository.deleteBook(book.bookId)) {
                is Result.Success -> {
                    Toast.makeText(
                        this@BooksForSaleActivity,
                        "Book deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUserBooks()
                }
                is Result.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        this@BooksForSaleActivity,
                        "Failed to delete book",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showEmptyView(show: Boolean) {
        emptyView.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadUserBooks()  // Reload when returning to this activity
    }
}