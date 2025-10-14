package com.example.bookswap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.R
import com.example.bookswap.data.models.Transaction
import com.example.bookswap.utils.DateUtils
import com.example.bookswap.utils.PriceUtils

class OrderAdapter(
    private var orders: List<Transaction>,
    private val onOrderClick: (Transaction) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookTitle: TextView = itemView.findViewById(R.id.orderBookTitle)
        private val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        private val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        private val orderAmount: TextView = itemView.findViewById(R.id.orderAmount)

        fun bind(transaction: Transaction) {
            bookTitle.text = transaction.bookTitle
            orderStatus.text = transaction.status.name
            orderDate.text = DateUtils.formatDate(transaction.createdAt)
            orderAmount.text = PriceUtils.formatPrice(transaction.amount)

            // Color code by status
            val statusColor = when (transaction.status) {
                com.example.bookswap.data.models.TransactionStatus.PENDING ->
                    android.graphics.Color.parseColor("#FFA500")
                com.example.bookswap.data.models.TransactionStatus.COMPLETED ->
                    android.graphics.Color.parseColor("#4CAF50")
                com.example.bookswap.data.models.TransactionStatus.CANCELLED ->
                    android.graphics.Color.parseColor("#F44336")
                else -> android.graphics.Color.parseColor("#2196F3")
            }
            orderStatus.setTextColor(statusColor)

            itemView.setOnClickListener {
                onOrderClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Transaction>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}