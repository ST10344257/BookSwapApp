package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookswap.adapters.CartItemAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.data.models.CartItem
import com.example.bookswap.data.models.Transaction
import com.example.bookswap.data.models.TransactionStatus
import com.example.bookswap.data.models.PaymentMethod
import com.example.bookswap.data.repository.CartRepository
import com.example.bookswap.data.repository.TransactionRepository
import com.example.bookswap.data.repository.UserRepository
import com.example.bookswap.databinding.ActivityCartBinding
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.PriceUtils
import kotlinx.coroutines.launch

class CartActivity : AppCompatActivity() {

    // Use View Binding
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartItemAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var userId: String? = null

    private val cartRepository = FirebaseModule.cartRepository
    private val transactionRepository = FirebaseModule.transactionRepository
    private val userRepository = FirebaseModule.userRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)

        setupRecyclerView()
        setupClickListeners()
        loadCart()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(cartItems) { cartItem, position ->
            removeFromCart(cartItem, position)
        }
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnBrowseBooks.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.btnCheckout.setOnClickListener {
            // Initial listener, will be updated in updateTotals()
            handleCheckout()
        }
    }

    private fun handleCheckout(){
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val unavailableItems = cartItems.filter { it.book?.status != BookStatus.AVAILABLE }

        if (unavailableItems.isNotEmpty()) {
            showUnavailableItemsDialog(unavailableItems.size)
        } else {
            showCheckoutDialog()
        }
    }


    private fun loadCart() {
        if (userId == null) {
            showEmptyCart(true)
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            when (val result = cartRepository.getCartItems(userId!!)) {
                is Result.Success -> {
                    cartItems = result.data.toMutableList()
                    checkItemAvailability()
                    updateUI()
                }
                is Result.Error -> {
                    showEmptyCart(true)
                    Toast.makeText(this@CartActivity, "Failed to load cart", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun checkItemAvailability() {
        for (item in cartItems) {
            val book = item.book
            item.isAvailable = book != null && book.status == BookStatus.AVAILABLE
        }
    }

    private fun updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyCart(true)
        } else {
            showEmptyCart(false)
            cartAdapter.updateCart(cartItems)
            updateTotals()
        }
        binding.cartCount.text = cartItems.size.toString()
    }

    private fun updateTotals() {
        val availableItems = cartItems.filter { it.isAvailable }
        val subtotal = availableItems.sumOf { it.book?.price ?: 0.0 }
        val total = subtotal

        binding.subtotalAmount.text = PriceUtils.formatPrice(subtotal)
        binding.totalAmount.text = PriceUtils.formatPrice(total)

        if (availableItems.isEmpty() && cartItems.isNotEmpty()) {
            binding.btnCheckout.text = "Remove Unavailable Items"
            binding.btnCheckout.setOnClickListener { removeUnavailableItems() }
        } else {
            binding.btnCheckout.text = "Proceed to Checkout"
            binding.btnCheckout.setOnClickListener { handleCheckout() }
        }
    }

    private fun showEmptyCart(empty: Boolean) {
        binding.emptyCartView.visibility = if (empty) View.VISIBLE else View.GONE
        binding.cartRecyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        binding.summaryCard.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun removeFromCart(cartItem: CartItem, position: Int) {
        if (userId == null) return
        val bookTitle = cartItem.book?.title ?: "this item"
        AlertDialog.Builder(this)
            .setTitle("Remove from Cart")
            .setMessage("Remove $bookTitle from your cart?")
            .setPositiveButton("Remove") { _, _ ->
                lifecycleScope.launch {
                    when (cartRepository.removeFromCart(userId!!, cartItem.bookId)) {
                        is Result.Success -> {
                            cartItems.removeAt(position)
                            updateUI() // Recalculate everything
                            Toast.makeText(this@CartActivity, "Removed from cart", Toast.LENGTH_SHORT).show()
                        }
                        is Result.Error -> Toast.makeText(this@CartActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                        else -> {}
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUnavailableItemsDialog(count: Int) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Unavailable Items")
            .setMessage("Your cart contains $count item(s) that are no longer available. Would you like to remove them and proceed?")
            .setPositiveButton("Remove & Proceed") { _, _ ->
                removeUnavailableItems {
                    // This block will execute after items are removed
                    if (cartItems.any { it.isAvailable }) {
                        showCheckoutDialog()
                    }
                }
            }
            .setNegativeButton("Review Cart", null)
            .show()
    }

    private fun removeUnavailableItems(onComplete: (() -> Unit)? = null) {
        if (userId == null) return
        lifecycleScope.launch {
            val unavailableItems = cartItems.filter { !it.isAvailable }
            for (item in unavailableItems) {
                cartRepository.removeFromCart(userId!!, item.bookId)
            }
            loadCart() // Reload cart from Firestore
            Toast.makeText(this@CartActivity, "Removed ${unavailableItems.size} unavailable item(s)", Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
        }
    }

    private fun showCheckoutDialog() {
        val availableItems = cartItems.filter { it.isAvailable }
        val total = availableItems.sumOf { it.book?.price ?: 0.0 }
        AlertDialog.Builder(this)
            .setTitle("Confirm Purchase")
            .setMessage("Complete purchase of ${availableItems.size} book(s) for ${PriceUtils.formatPrice(total)}?")
            .setPositiveButton("Confirm") { _, _ -> processCheckout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun processCheckout() {
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnCheckout.isEnabled = false
        binding.btnCheckout.text = "Processing..."

        lifecycleScope.launch {
            when (val userResult = userRepository.getUser(userId!!)) {
                is Result.Success -> {
                    val buyer = userResult.data
                    val availableItems = cartItems.filter { it.isAvailable }
                    var successCount = 0

                    for (cartItem in availableItems) {
                        val book = cartItem.book ?: continue
                        val transaction = Transaction(
                            bookId = book.bookId,
                            bookTitle = book.title,
                            buyerId = userId!!,
                            buyerName = "${buyer.name} ${buyer.surname}",
                            sellerId = book.sellerId,
                            sellerName = book.sellerName,
                            amount = book.price,
                            status = TransactionStatus.PENDING,
                            paymentMethod = PaymentMethod.WALLET
                        )
                        if (transactionRepository.createTransaction(transaction) is Result.Success) {
                            successCount++
                            cartRepository.removeFromCart(userId!!, book.bookId)
                        }
                    }

                    if (successCount > 0) {
                        Toast.makeText(this@CartActivity, "Purchase successful! Track your orders in profile.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@CartActivity, "Purchase failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
                is Result.Error -> Toast.makeText(this@CartActivity, "Failed to load user info", Toast.LENGTH_SHORT).show()
                else -> {}
            }
            binding.btnCheckout.isEnabled = true
            binding.btnCheckout.text = "Proceed to Checkout"
        }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }
}

