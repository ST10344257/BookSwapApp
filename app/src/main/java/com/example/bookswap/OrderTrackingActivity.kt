package com.example.bookswap

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookswap.adapters.OrderAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Transaction
import com.example.bookswap.databinding.OrderPageBinding
import com.example.bookswap.utils.Constants
import kotlinx.coroutines.launch

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var binding: OrderPageBinding
    private lateinit var orderAdapter: OrderAdapter

    private val transactionRepository = FirebaseModule.transactionRepository
    private var userId: String? = null

    companion object {
        private const val TAG = "OrderTrackingActivity"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = OrderPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)

        Log.d(TAG, "onCreate - userId: $userId")

        setupRecyclerView()
        setupClickListeners()
        loadOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { transaction ->
            showOrderDetails(transaction)
        }
        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderTrackingActivity)
            adapter = orderAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnback.setOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        if (userId == null) {
            Log.w(TAG, "User ID is null, cannot load orders")
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            showEmptyView(true)
            return
        }

        showLoading(true)
        Log.d(TAG, "Loading orders for user: $userId")

        lifecycleScope.launch {
            when (val result = transactionRepository.getUserTransactions(userId!!, asBuyer = true)) {
                is Result.Success -> {
                    showLoading(false)
                    Log.d(TAG, "Successfully loaded ${result.data.size} orders")

                    if (result.data.isEmpty()) {
                        Log.d(TAG, "No orders found for user")
                        showEmptyView(true)
                    } else {
                        Log.d(TAG, "Displaying ${result.data.size} orders")
                        result.data.forEach { transaction ->
                            Log.d(TAG, "Order: ${transaction.bookTitle} - ${transaction.status.name}")
                        }
                        showEmptyView(false)
                        orderAdapter.updateOrders(result.data)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmptyView(true)
                    Log.e(TAG, "Error loading orders: ${result.exception.message}", result.exception)
                    Toast.makeText(
                        this@OrderTrackingActivity,
                        "Failed to load orders: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {
                    Log.d(TAG, "Loading state")
                }
            }
        }
    }

    private fun showOrderDetails(transaction: Transaction) {
        Toast.makeText(
            this,
            "Order: ${transaction.bookTitle}\nStatus: ${transaction.status.name}\nAmount: R ${String.format("%.2f", transaction.amount)}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.ordersRecyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun showEmptyView(show: Boolean) {
        binding.emptyView.visibility = if (show) View.VISIBLE else View.GONE
        binding.ordersRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - reloading orders")
        loadOrders()
    }
}