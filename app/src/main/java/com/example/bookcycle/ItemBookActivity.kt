package com.example.bookcycle

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bookcycle.utilities.BookData
import com.google.android.material.textview.MaterialTextView
import com.example.bookcycle.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ItemBookActivity : AppCompatActivity() {
    private lateinit var currentUser: FirebaseUser
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_book)

        currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            finish()
            return
        }
        db = FirebaseFirestore.getInstance()

        val book = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("book", BookData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("book") as? BookData
        }

        val viewType = intent.getStringExtra("view_type") ?: "my_books"

        book?.let {
            findViewById<MaterialTextView>(R.id.bookTitle).text = it.title
            findViewById<MaterialTextView>(R.id.bookAuthor).text = it.author
            findViewById<MaterialTextView>(R.id.bookGenre).text = it.genre

            findViewById<MaterialTextView>(R.id.ownerInfo).apply {
                if (it.borrowerId == currentUser.uid && it.ownerName.isNotEmpty()) {
                    visibility = View.VISIBLE
                    text = "Book Owner: ${it.ownerName}"
                } else {
                    visibility = View.GONE
                }
            }

            findViewById<MaterialTextView>(R.id.borrowerInfo).apply {
                if (it.ownerId == currentUser.uid && !it.isAvailable && it.borrowerName.isNotEmpty()) {
                    visibility = View.VISIBLE
                    text = "Borrowed by: ${it.borrowerName}"
                } else {
                    visibility = View.GONE
                }
            }

            // Load book image
            if (it.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(it.imageUrl)
                    .into(findViewById(R.id.bookImage))
            }

            findViewById<MaterialButton>(R.id.actionButton).apply {
                when {
                    viewType == "search_result" && it.isAvailable -> {
                        visibility = View.VISIBLE
                        text = "Request Book Loan"
                        setOnClickListener { _ ->
                            showBorrowRequestDialog(it)
                        }
                    }
                    it.ownerId == currentUser.uid -> {
                        visibility = View.VISIBLE
                        text = if (it.isAvailable) "Lend Book" else "Mark as Returned"
                        setOnClickListener { _ ->
                            if (it.isAvailable) {
                                showLendBookDialog(it)
                            } else {
                                markBookAsReturned(it)
                            }
                        }
                    }
                    else -> {
                        visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showBorrowRequestDialog(book: BookData) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Book Loan Request")
            .setMessage("Would you like to send a request to borrow the book \"${book.title}\"?")
            .setPositiveButton("Send Request") { _, _ ->
                sendBorrowRequest(book)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primary))
        }

        dialog.show()
    }

    private fun showLendBookDialog(book: BookData) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Lend Book")
            .setMessage("Currently, books cannot be lent proactively. Please wait for a borrow request from another user.")
            .setPositiveButton("I Understand", null)
            .show()
    }

    private fun sendBorrowRequest(book: BookData) {
        val requestData = hashMapOf(
            "bookId" to book.id,
            "bookTitle" to book.title,
            "requesterId" to currentUser.uid,
            "requesterName" to (currentUser.displayName ?: ""),
            "ownerId" to book.ownerId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("borrow_requests").add(requestData)
            .addOnSuccessListener {
                Snackbar.make(findViewById(android.R.id.content),
                    "The loan request was sent successfully",
                    Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
                finish()
            }
            .addOnFailureListener { e ->
                Snackbar.make(findViewById(android.R.id.content),
                    "Error sending request: ${e.message}",
                    Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
            }
    }

    private fun markBookAsReturned(book: BookData) {
        val bookRef = db.collection("books").document(book.id)

        val updates = hashMapOf<String, Any>(
            "isAvailable" to true,
            "borrowerId" to "",
            "borrowerName" to ""
        )

        bookRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "The book was marked as returned", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating book: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
