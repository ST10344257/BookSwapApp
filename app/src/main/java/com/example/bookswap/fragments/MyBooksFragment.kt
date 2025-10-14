package com.example.bookswap.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookswap.BookDetailActivity // Assuming you have this
import com.example.bookswap.adapters.BookAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.databinding.FragmentMyBooksBinding
import com.example.bookswap.utils.Constants // Assuming you have this
import kotlinx.coroutines.launch

class MyBooksFragment : Fragment() {

    private var _binding: FragmentMyBooksBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter
    private val bookRepository = BookRepository() // Placeholder
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        userId = prefs.getString(Constants.KEY_USER_ID, null)

        setupRecyclerView()
        loadUserBooks()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(emptyList()) { book ->
            val intent = Intent(requireContext(), BookDetailActivity::class.java).apply {
                // You'll need to make your Book model Parcelable/Serializable
                // putExtra("BOOK", book)
            }
            startActivity(intent)
        }
        binding.userBooksRecyclerView.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadUserBooks() {
        if (userId == null) {
            showEmptyView(true)
            return
        }
        showLoading(true)

        lifecycleScope.launch {
            // This is a placeholder call. You'll need to implement your BookRepository.
            // val result = bookRepository.getUserBooks(userId!!)
            val result: Result<List<Any>> = Result.Success(emptyList()) // Faking an empty result for now

            when (result) {
                is Result.Success -> {
                    showLoading(false)
                    if (result.data.isEmpty()) {
                        showEmptyView(true)
                    } else {
                        showEmptyView(false)
                        // bookAdapter.updateBooks(result.data)
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showEmptyView(true)
                    Toast.makeText(requireContext(), "Failed to load books", Toast.LENGTH_SHORT).show()
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
        binding.userBooksRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

