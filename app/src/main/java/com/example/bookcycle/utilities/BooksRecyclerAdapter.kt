package com.example.bookcycle.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookcycle.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BooksRecyclerAdapter(
    private val books: List<BookData>,
    private val onReturnRequested: ((BookData) -> Unit)? = null,
    private val onDeleteRequested: ((BookData) -> Unit)? = null
) : RecyclerView.Adapter<BooksRecyclerAdapter.BookViewHolder>() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: MaterialTextView = itemView.findViewById(R.id.bookTitle)
        val author: MaterialTextView = itemView.findViewById(R.id.bookAuthor)
        val genre: MaterialTextView = itemView.findViewById(R.id.bookGenre)
        val image: AppCompatImageView = itemView.findViewById(R.id.bookImage)
        val ownerInfo: MaterialTextView = itemView.findViewById(R.id.ownerInfo)
        val borrowerInfo: MaterialTextView = itemView.findViewById(R.id.borrowerInfo)
        val returnButton: MaterialButton = itemView.findViewById(R.id.returnButton)
        val deleteMenuButton: MaterialButton = itemView.findViewById(R.id.deleteMenuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author
        holder.genre.text = book.genre

        if (book.borrowerId == currentUser?.uid) {
            db.collection("users").document(book.ownerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val ownerPhone = document.getString("phone") ?: ""
                        val ownerEmail = document.getString("email") ?: ""
                        holder.ownerInfo.apply {
                            visibility = View.VISIBLE
                            text = "Book Owner: ${book.ownerName}\n" +
                                    "Phone: $ownerPhone\n" +
                                    "Email: $ownerEmail"
                        }
                        holder.returnButton.apply {
                            visibility = View.VISIBLE
                            setOnClickListener { onReturnRequested?.invoke(book) }
                        }
                    }
                }
        } else {
            holder.ownerInfo.visibility = View.GONE
            holder.returnButton.visibility = View.GONE
        }

        if (book.ownerId == currentUser?.uid && book.borrowerId.isNotEmpty()) {
            db.collection("users").document(book.borrowerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val borrowerPhone = document.getString("phone") ?: ""
                        val borrowerEmail = document.getString("email") ?: ""
                        holder.borrowerInfo.apply {
                            visibility = View.VISIBLE
                            text = "Borrowed by: ${book.borrowerName}\n" +
                                    "Phone: $borrowerPhone\n" +
                                    "Email: $borrowerEmail"
                        }
                    }
                }
        } else {
            holder.borrowerInfo.visibility = View.GONE
        }

        if (book.ownerId == currentUser?.uid && book.isAvailable && book.borrowerId.isEmpty()) {
            holder.deleteMenuButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    onDeleteRequested?.invoke(book)
                }
            }
        } else {
            holder.deleteMenuButton.visibility = View.GONE
        }

        if (book.imageUrl.isNotEmpty()) {
            Glide.with(holder.image)
                .load(book.imageUrl)
                .into(holder.image)
        }
    }

    override fun getItemCount() = books.size
}