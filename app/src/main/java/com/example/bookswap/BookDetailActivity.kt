package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.bookswap.data.repository.WishlistRepository
import com.example.bookswap.databinding.ActivityBookDetailBinding
import com.example.bookswap.utils.ConnectivityObserver
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore // Added import
import kotlinx.coroutines.launch

class BookDetailActivity : AppCompatActivity() {

    private var isOnline = true
    private lateinit var binding: ActivityBookDetailBinding
    private val cartRepository = CartRepository()
    private val wishlistRepository = WishlistRepository()
    private lateinit var book: Book

    companion object {
        private const val TAG = "BookDetail"
        const val EXTRA_BOOK_ID = "EXTRA_BOOK_ID" // For notifications
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- UPDATED LOGIC TO HANDLE BOTH NAVIGATION AND NOTIFICATIONS ---

        // Check if we received a full Book object (from within the app)
        val bookFromParcel: Book? = intent.getParcelableExtra("BOOK")

        // Check if we received a bookId (from a notification)
        val bookIdFromNotification = intent.getStringExtra(EXTRA_BOOK_ID)

        if (bookFromParcel != null) {
            // Case 1: Navigating from within the app. The Book object is already here.
            Log.d(TAG, "Loading book from Parcelable: ${bookFromParcel.id}")
            this.book = bookFromParcel
            initializeView() // Setup the UI with the book data
        } else if (bookIdFromNotification != null) {
            // Case 2: Opened from a notification. We need to fetch the book data.
            Log.d(TAG, "Loading book from notification ID: $bookIdFromNotification")
            loadBookFromFirestore(bookIdFromNotification)
        } else {
            // Error case: No book data was provided.
            Log.e(TAG, "BookDetailActivity started without BOOK parcelable or EXTRA_BOOK_ID.")
            Toast.makeText(this, "Error: Could not load book details.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    /**
     * New function to fetch a single book's details from Firestore using its ID.
     */
    private fun loadBookFromFirestore(bookId: String) {
        // Make the main content invisible and show a loading indicator
        binding.contentGroup.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("books").document(bookId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Convert the Firestore document to your Book data class
                    val fetchedBook = document.toObject(Book::class.java)
                    if (fetchedBook != null) {
                        this.book = fetchedBook
                        Log.d(TAG, "Successfully fetched book from Firestore: ${this.book.title}")
                        initializeView() // Now setup the UI
                    } else {
                        Log.e(TAG, "Failed to parse Firestore document to Book object.")
                        showErrorAndFinish()
                    }
                } else {
                    Log.e(TAG, "No document found with ID: $bookId")
                    showErrorAndFinish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting book details from Firestore", exception)
                showErrorAndFinish()
            }
    }

    /**
     * A helper function to contain all the UI setup logic.
     * This ensures the UI is only set up *after* we have the book data.
     */
    private fun initializeView() {
        // Hide loading indicator and show the content
        binding.progressBar.visibility = View.GONE
        binding.contentGroup.visibility = View.VISIBLE
        setupClickListeners()
        displayBookDetails()
    }

    /**
     * Shows a generic error message and closes the activity.
     */
    private fun showErrorAndFinish() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Could not load book. It may have been removed.", Toast.LENGTH_LONG).show()
        finish()
    }


    private fun observeConnectivity() {
        lifecycleScope.launch {
            ConnectivityObserver.observeConnectivity(this@BookDetailActivity).collect { online ->
                isOnline = online
                Log.d(TAG, "Network status: ${if (online) "Online" else "Offline"}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // ✅ Cart/Wishlist Icon (ImageView in header)
        binding.btnAddToCart.setOnClickListener {
            Log.d(TAG, "Cart/Wishlist icon clicked. isGoogleBook: ${book.isGoogleBook}")
            if (book.isGoogleBook) {
                addToWishlist()
            } else {
                addToCart()
            }
        }

        // ✅ Buy Now Button
        binding.btnBuyNow.setOnClickListener {
            Log.d(TAG, "Buy Now button clicked. isGoogleBook: ${book.isGoogleBook}")
            if (book.isGoogleBook) {
                // Open Google Books
                if (book.buyLink.isNotEmpty()) {
                    Log.d(TAG, "Opening Google Books link: ${book.buyLink}")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(book.buyLink))
                    startActivity(intent)
                } else {
                    Log.w(TAG, "No buyLink available for book: ${book.title}")
                    Toast.makeText(this, "No purchase link available", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Add user book to cart
                addToCart()
            }
        }

        // ✅ Message Seller Button
        binding.btnMessageSeller.setOnClickListener {
            Toast.makeText(this, "Messaging feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayBookDetails() {
        Log.d(TAG, "displayBookDetails() called")
        Log.d(TAG, "  isGoogleBook: ${book.isGoogleBook}")

        binding.bookTitle.text = book.title
        binding.bookAuthor.text = "by ${book.author}"

        // ✅ Format price properly
        if (book.isGoogleBook) {
            if (book.price <= 0.0) {
                binding.bookPrice.text = "View on Google"
            } else {
                binding.bookPrice.text = "$%.2f".format(book.price)
            }
        } else {
            binding.bookPrice.text = "R%.2f".format(book.price)
        }

        binding.bookDescription.text = if (book.description.isNotEmpty()) {
            book.description
        } else {
            "No description available"
        }

        // --- Logic to show/hide UI based on book type ---
        if (book.isGoogleBook) {
            Log.d(TAG, "Setting up Google Book UI")

            // Hide user-specific fields
            binding.bookCondition.visibility = View.GONE
            binding.bookISBN.visibility = View.GONE
            binding.bookCourseCode.visibility = View.GONE
            binding.sellerInfoGroup.visibility = View.GONE
            binding.btnMessageSeller.visibility = View.GONE

            Log.d(TAG, "  Hidden: condition, ISBN, courseCode, sellerInfo, messageButton")

            // Show Google Book fields
            binding.bookPublishedDate.visibility = View.VISIBLE
            binding.bookPublishedDate.text = "Published: ${book.publishedDate}"

            // Change button text
            binding.btnBuyNow.text = "View on Google Books"
            Log.d(TAG, "  Button text set to: View on Google Books")

            // ✅ Change cart icon to heart (wishlist)
            binding.btnAddToCart.setImageResource(android.R.drawable.btn_star_big_off)
            binding.btnAddToCart.setColorFilter(getColor(R.color.orange_primary))
            Log.d(TAG, "  Icon changed to: star (wishlist)")

            // Handle Google Book image
            if (book.imageUrl.isNotEmpty()) {
                val images = listOf(book.imageUrl)
                val imageAdapter = BookImageAdapter(images)
                binding.imageViewPager.adapter = imageAdapter
                setupDotsIndicator(images.size)
            }

        } else {
            Log.d(TAG, "Setting up User Book UI")

            // Show user-specific fields
            binding.bookCondition.visibility = View.VISIBLE
            binding.bookISBN.visibility = View.VISIBLE
            binding.bookCourseCode.visibility = View.VISIBLE
            binding.sellerInfoGroup.visibility = View.VISIBLE
            binding.btnMessageSeller.visibility = View.VISIBLE

            Log.d(TAG, "  Shown: condition, ISBN, courseCode, sellerInfo, messageButton")

            // Hide Google Book fields
            binding.bookPublishedDate.visibility = View.GONE

            // Set user-specific text
            binding.bookCondition.text = book.condition.displayName
            binding.bookISBN.text = "ISBN: ${book.isbn}"
            binding.bookCourseCode.text = "Course: ${book.courseCode}"
            binding.sellerName.text = book.sellerName
            binding.sellerInstitution.text = book.institution

            // Keep button text as is
            binding.btnBuyNow.text = "Buy Now"
            Log.d(TAG, "  Button text set to: Buy Now")

            // ✅ Keep cart icon as cart
            binding.btnAddToCart.setImageResource(R.drawable.shopping_cart_svgrepo_com)
            binding.btnAddToCart.clearColorFilter()
            Log.d(TAG, "  Icon set to: cart")

            // Handle user-uploaded images
            if (book.photoUrls.isNotEmpty()) {
                val imageAdapter = BookImageAdapter(book.photoUrls)
                binding.imageViewPager.adapter = imageAdapter
                setupDotsIndicator(book.photoUrls.size)
            }
        }

        Log.d(TAG, "displayBookDetails() completed")
    }

    private fun setupDotsIndicator(count: Int) {
        if (count <= 1) {
            binding.dotsIndicator.visibility = View.GONE
            return
        }

        binding.dotsIndicator.visibility = View.VISIBLE
        val dots = Array(count) { ImageView(this) }
        binding.dotsIndicator.removeAllViews()

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

    private fun addToWishlist() {
        Log.d(TAG, "addToWishlist() called")
        Log.d(TAG, "  Book ID: ${book.id}")
        Log.d(TAG, "  Book Title: ${book.title}")
        Log.d(TAG, "  isGoogleBook: ${book.isGoogleBook}")

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)

        if (userId == null) {
            Log.w(TAG, "User not logged in")
            Toast.makeText(this, "Please login to add to wishlist", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "User ID: $userId")
        binding.btnAddToCart.isEnabled = false

        lifecycleScope.launch {
            when (val checkResult = wishlistRepository.isInWishlist(userId, book.id)) {
                is Result.Success -> {
                    if (checkResult.data) {
                        Log.d(TAG, "Book already in wishlist")
                        Toast.makeText(
                            this@BookDetailActivity,
                            "Already in wishlist",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.d(TAG, "Adding book to wishlist...")
                        when (wishlistRepository.addToWishlist(userId, book)) {
                            is Result.Success -> {
                                Log.d(TAG, "Successfully added to wishlist")
                                // ✅ Change icon to filled star
                                binding.btnAddToCart.setImageResource(android.R.drawable.btn_star_big_on)
                                // OFFLINE MESSAGE
                                val message = if (isOnline) {
                                    "Added to wishlist ❤️"
                                } else {
                                    "Added to wishlist (will sync when online) 📵❤️"
                                }
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is Result.Error -> {
                                Log.e(TAG, "Failed to add to wishlist")
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    "Failed to add to wishlist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {}
                        }
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error checking wishlist", checkResult.exception)
                    Toast.makeText(
                        this@BookDetailActivity,
                        "Error checking wishlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }

            binding.btnAddToCart.isEnabled = true
        }
    }

    private fun addToCart() {
        Log.d(TAG, "addToCart() called")

        if (book.isGoogleBook) {
            Log.w(TAG, "Attempted to add Google Book to cart - blocking")
            Toast.makeText(this, "This book can only be viewed on Google Books", Toast.LENGTH_SHORT).show()
            return
        }

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

        binding.btnAddToCart.isEnabled = false
        binding.btnBuyNow.isEnabled = false

        lifecycleScope.launch {
            when (val checkResult = cartRepository.isInCart(userId, book.bookId)) {
                is Result.Success -> {
                    if (checkResult.data) {
                        // OFFLINE MESSAGE
                        val message = if (isOnline) {
                            "Added to cart"
                        } else {
                            "Added to cart (will sync when online) 📵"
                        }
                        Toast.makeText(
                            this@BookDetailActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        when (cartRepository.addToCart(userId, book.bookId)) {
                            is Result.Success -> {
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    "Added to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is Result.Error -> {
                                Toast.makeText(
                                    this@BookDetailActivity,
                                    "Failed to add to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {}
                        }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(
                        this@BookDetailActivity,
                        "Error checking cart",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }

            binding.btnAddToCart.isEnabled = true
            binding.btnBuyNow.isEnabled = true
        }
    }
}
