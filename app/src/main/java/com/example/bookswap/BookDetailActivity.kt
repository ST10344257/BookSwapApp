package com.example.bookswap

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.bookswap.adapters.BookImageAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.repository.CartRepository
import com.example.bookswap.databinding.ActivityBookDetailBinding
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.PriceUtils
import kotlinx.coroutines.launch

class BookDetailActivity : AppCompatActivity() {

    // Use View Binding
    private lateinit var binding: ActivityBookDetailBinding
    private val cartRepository = CartRepository()
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        book = intent.getParcelableExtra("BOOK") ?: run {
            Toast.makeText(this, "Error loading book details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        displayBookDetails()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // --- THIS IS THE FIX ---
        // "Buy Now" button now also adds the book to the cart.
        binding.btnBuyNow.setOnClickListener {
            addToCart()
        }

        binding.btnMessageSeller.setOnClickListener {
            Toast.makeText(this, "Messaging feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun displayBookDetails() {
        binding.bookTitle.text = book.title
        binding.bookAuthor.text = "by ${book.author}"
        binding.bookPrice.text = PriceUtils.formatPrice(book.price)
        binding.bookCondition.text = book.condition.displayName
        binding.bookISBN.text = book.isbn
        binding.bookCourseCode.text = book.courseCode
        binding.bookDescription.text = if (book.description.isNotEmpty()) book.description else "No description available"
        binding.sellerName.text = book.sellerName
        binding.sellerInstitution.text = book.institution

        if (book.photoUrls.isNotEmpty()) {
            val imageAdapter = BookImageAdapter(book.photoUrls)
            binding.imageViewPager.adapter = imageAdapter
            setupDotsIndicator(book.photoUrls.size)
        }
    }

    private fun setupDotsIndicator(count: Int) {
        val dots = Array(count) { ImageView(this) }
        binding.dotsIndicator.removeAllViews() // Clear old dots

        dots.forEach { dot ->
            dot.setImageResource(R.drawable.baseline_circle_24)
            dot.setColorFilter(getColor(android.R.color.darker_gray))
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            binding.dotsIndicator.addView(dot, params)
        }

        if (dots.isNotEmpty()) {
            dots[0].setColorFilter(getColor(android.R.color.black))
        }

        binding.imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dots.forEachIndexed { index, dot ->
                    val color = if (index == position) android.R.color.black else android.R.color.darker_gray
                    dot.setColorFilter(getColor(color))
                }
            }
        })
    }

    private fun addToCart() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)

        if (userId == null) {
            Toast.makeText(this, "Please login to add to cart", Toast.LENGTH_SHORT).show()
            return
        }
        if (userId == book.sellerId) {
            Toast.makeText(this, "You cannot add your own book to the cart", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable buttons to prevent multiple clicks
        binding.btnAddToCart.isEnabled = false
        binding.btnBuyNow.isEnabled = false

        lifecycleScope.launch {
            when (val checkResult = cartRepository.isInCart(userId, book.bookId)) {
                is Result.Success -> {
                    if (checkResult.data) {
                        Toast.makeText(this@BookDetailActivity, "Already in cart", Toast.LENGTH_SHORT).show()
                    } else {
                        when (cartRepository.addToCart(userId, book.bookId)) {
                            is Result.Success -> Toast.makeText(this@BookDetailActivity, "Added to cart", Toast.LENGTH_SHORT).show()
                            is Result.Error -> Toast.makeText(this@BookDetailActivity, "Failed to add to cart", Toast.LENGTH_SHORT).show()
                            else -> {}
                        }
                    }
                }
                is Result.Error -> Toast.makeText(this@BookDetailActivity, "Error checking cart", Toast.LENGTH_SHORT).show()
                else -> {}
            }
            // Re-enable buttons
            binding.btnAddToCart.isEnabled = true
            binding.btnBuyNow.isEnabled = true
        }
    }
}
