package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookswap.adapters.SelectedImageAdapter
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookCategory
import com.example.bookswap.data.models.BookCondition
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.data.repository.UserRepository
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.ValidationUtils
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.launch

class AddBookActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSelectImages: Button
    private lateinit var selectedImagesRecyclerView: RecyclerView
    private lateinit var edtTitle: EditText
    private lateinit var edtAuthor: EditText
    private lateinit var edtISBN: EditText
    private lateinit var edtEdition: EditText
    private lateinit var edtCourseCode: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var edtPrice: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var edtDescription: EditText
    private lateinit var btnListBook: Button

    private val selectedImages = mutableListOf<Uri>()
    private lateinit var imageAdapter: SelectedImageAdapter
    private val bookRepository = BookRepository()
    private val userRepository = UserRepository()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null && selectedImages.size < Constants.MAX_IMAGES_PER_BOOK) {
                selectedImages.add(uri)
                imageAdapter.notifyItemInserted(selectedImages.size - 1)
            } else if (selectedImages.size >= Constants.MAX_IMAGES_PER_BOOK) {
                Toast.makeText(
                    this,
                    "Maximum ${Constants.MAX_IMAGES_PER_BOOK} images allowed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        initViews()
        setupRecyclerView()
        setupSpinners()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSelectImages = findViewById(R.id.btnSelectImages)
        selectedImagesRecyclerView = findViewById(R.id.selectedImagesRecyclerView)
        edtTitle = findViewById(R.id.edtTitle)
        edtAuthor = findViewById(R.id.edtAuthor)
        edtISBN = findViewById(R.id.edtISBN)
        edtEdition = findViewById(R.id.edtEdition)
        edtCourseCode = findViewById(R.id.edtCourseCode)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        edtPrice = findViewById(R.id.edtPrice)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        edtDescription = findViewById(R.id.edtDescription)
        btnListBook = findViewById(R.id.btnListBook)
    }

    private fun setupRecyclerView() {
        imageAdapter = SelectedImageAdapter(selectedImages) { position ->
            selectedImages.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
        }
        selectedImagesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedImagesRecyclerView.adapter = imageAdapter
    }

    private fun setupSpinners() {
        // Category Spinner
        val categories = BookCategory.values().map { it.name }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Condition Spinner
        val conditions = BookCondition.values().map { it.displayName }
        val conditionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, conditions)
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCondition.adapter = conditionAdapter
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnSelectImages.setOnClickListener {
            if (selectedImages.size < Constants.MAX_IMAGES_PER_BOOK) {
                pickImage()
            } else {
                Toast.makeText(
                    this,
                    "Maximum ${Constants.MAX_IMAGES_PER_BOOK} images allowed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ISBN Help Icon - ADD THIS
        findViewById<ImageView>(R.id.btnIsbnHelp)?.setOnClickListener {
            showIsbnHelpDialog()
        }

        btnListBook.setOnClickListener {
            if (validateInput()) {
                listBook()
            }
        }
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                imagePickerLauncher.launch(intent)
            }
    }

    private fun showIsbnHelpDialog() {
        val message = """
        📚 What is ISBN?
        
        ISBN (International Standard Book Number) is a unique identifier for books.
        
        📖 Where to Find It:
        • Look on the back cover of your textbook
        • Usually above or near the barcode
        • Also on the copyright page inside
        
        🔢 Two Formats:
        
        ISBN-13 (Current Standard):
        • 13 digits long
        • Usually starts with 978 or 979
        • Example: 978-0-13-468599-1
        
        ISBN-10 (Older Format):
        • 10 digits long
        • Example: 0-13-468599-2
        
        ✏️ How to Enter:
        You can type it WITH or WITHOUT hyphens:
        • 9780134685991 ✓
        • 978-0-13-468599-1 ✓
        • 978 0 13 468599 1 ✓
        
        All these formats work!
        
        💡 Tip: The ISBN helps other students find the exact edition of your textbook.
    """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📖 About ISBN")
            .setMessage(message)
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Where's my ISBN?") { _, _ ->
                showWhereToFindIsbn()
            }
            .show()
    }

    private fun showWhereToFindIsbn() {
        val message = """
        🔍 Finding Your ISBN:
        
        Step 1: Flip your book over to the BACK COVER
        
        Step 2: Look for the barcode (black and white lines)
        
        Step 3: The ISBN is printed:
        • Above the barcode, OR
        • Below the barcode, OR
        • Next to it
        
        Step 4: Look for text that says:
        • "ISBN-13:" followed by 13 numbers
        • "ISBN-10:" followed by 10 numbers
        • "ISBN:" followed by numbers
        
        Example:
        ISBN-13: 978-0-13-468599-1
                 ↑ This is what you type
        
        📄 Alternative Location:
        If not on back cover, check the copyright page (usually page 2 or 3 inside the book).
        
        ❓ Still can't find it?
        Very old books might not have an ISBN. In that case, you can leave this field empty and describe your book well in the title and description.
    """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Where to Find ISBN")
            .setMessage(message)
            .setPositiveButton("Thanks!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validateInput(): Boolean {
        val title = edtTitle.text.toString().trim()
        val author = edtAuthor.text.toString().trim()
        val isbn = edtISBN.text.toString().trim()
        val price = edtPrice.text.toString().trim()

        return when {
            selectedImages.isEmpty() -> {
                Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show()
                false
            }
            title.isEmpty() -> {
                edtTitle.error = "Title is required"
                edtTitle.requestFocus()
                false
            }
            author.isEmpty() -> {
                edtAuthor.error = "Author is required"
                edtAuthor.requestFocus()
                false
            }
            isbn.isNotEmpty() && !ValidationUtils.isValidISBN(isbn) -> {
                // Only validate if ISBN is provided
                edtISBN.error = "Invalid ISBN format (must be 10 or 13 digits)"
                edtISBN.requestFocus()
                false
            }
            price.isEmpty() -> {
                edtPrice.error = "Price is required"
                edtPrice.requestFocus()
                false
            }
            !ValidationUtils.isValidPrice(price) -> {
                edtPrice.error = "Invalid price"
                edtPrice.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun listBook() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(Constants.KEY_USER_ID, null)

        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            // Get user info first
            when (val userResult = userRepository.getUser(userId)) {
                is Result.Success -> {
                    val user = userResult.data

                    val book = Book(
                        sellerId = userId,
                        sellerName = "${user.name} ${user.surname}",
                        title = edtTitle.text.toString().trim(),
                        author = edtAuthor.text.toString().trim(),
                        isbn = edtISBN.text.toString().trim(),
                        edition = edtEdition.text.toString().trim(),
                        courseCode = edtCourseCode.text.toString().trim(),
                        category = BookCategory.values()[spinnerCategory.selectedItemPosition],
                        price = edtPrice.text.toString().toDouble(),
                        condition = BookCondition.values()[spinnerCondition.selectedItemPosition],
                        description = edtDescription.text.toString().trim(),
                        institution = user.institution,
                        location = user.institution
                    )

                    when (val result = bookRepository.addBook(book, selectedImages)) {
                        is Result.Success -> {
                            // Update user's book count
                            userRepository.incrementBookStats(userId, sold = false)

                            Toast.makeText(
                                this@AddBookActivity,
                                "Book listed successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        is Result.Error -> {
                            setLoading(false)
                            Toast.makeText(
                                this@AddBookActivity,
                                "Failed to list book: ${result.exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is Result.Loading -> {}
                    }
                }
                is Result.Error -> {
                    setLoading(false)
                    Toast.makeText(
                        this@AddBookActivity,
                        "Failed to load user info",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnListBook.isEnabled = !loading
        btnSelectImages.isEnabled = !loading
        btnListBook.text = if (loading) "Listing Book..." else "List Book"
    }
}