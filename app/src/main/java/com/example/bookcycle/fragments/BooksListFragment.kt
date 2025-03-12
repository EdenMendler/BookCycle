package com.example.bookcycle.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcycle.utilities.BookData
import com.example.bookcycle.utilities.BooksRecyclerAdapter
import com.example.bookcycle.utilities.BooksType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.bookcycle.R
import com.google.android.material.snackbar.Snackbar

class BooksListFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var booksType: BooksType
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val ARG_TYPE = "books_type"

        fun newInstance(type: BooksType): BooksListFragment {
            return BooksListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TYPE, type)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booksType = arguments?.getSerializable(ARG_TYPE) as? BooksType
            ?: throw IllegalArgumentException("BooksType must be provided")

        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.books_recycler_view)
        loadBooks()
    }

    private fun loadBooks() {
        when (booksType) {
            BooksType.MY_BOOKS -> {
                firestore.collection("books")
                    .whereEqualTo("ownerId", currentUser.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        val books = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(BookData::class.java)
                        } ?: listOf()

                        updateRecyclerView(books)
                    }
            }
            BooksType.LENT_BOOKS -> {
                firestore.collection("books")
                    .whereEqualTo("ownerId", currentUser.uid)
                    .whereEqualTo("isAvailable", false)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        val books = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(BookData::class.java)
                        } ?: listOf()

                        updateRecyclerView(books)
                    }
            }
            BooksType.BORROWED_BOOKS -> {
                firestore.collection("books")
                    .whereEqualTo("borrowerId", currentUser.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        val books = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(BookData::class.java)
                        } ?: listOf()

                        updateRecyclerView(books)
                    }
            }
        }
    }

    private fun updateRecyclerView(books: List<BookData>) {
        val adapter = BooksRecyclerAdapter(
            books = books,
            onReturnRequested = { book -> createReturnRequest(book) },
            onDeleteRequested = { book -> showDeleteConfirmation(book) }
        )
        recyclerView.adapter = adapter
    }

    private fun createReturnRequest(book: BookData) {
        val requestData = hashMapOf(
            "bookId" to book.id,
            "bookTitle" to book.title,
            "requesterId" to currentUser.uid,
            "requesterName" to (currentUser.displayName ?: ""),
            "ownerId" to book.ownerId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis(),
            "requestType" to "return"
        )

        firestore.collection("borrow_requests").add(requestData)
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Return request sent successfully", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), "Error sending return request: ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
            }
    }

    private fun showDeleteConfirmation(book: BookData) {
        if (book.ownerId != currentUser.uid) {
            Snackbar.make(requireView(), "You can only delete your own books", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                .setTextColor(Color.WHITE)
                .show()
            return
        }

        if (!book.isAvailable) {
            Snackbar.make(requireView(), "Cannot delete a borrowed book", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                .setTextColor(Color.WHITE)
                .show()
            return
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Book")
            .setMessage("Are you sure you want to delete '${book.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }

        dialog.show()
    }

    private fun deleteBook(book: BookData) {
        firestore.collection("books").document(book.id)
            .delete()
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Book deleted successfully", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), "Error deleting book: ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                    .setTextColor(Color.WHITE)
                    .show()
            }
    }
}
