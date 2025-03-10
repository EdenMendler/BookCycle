package com.example.bookcycle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcycle.R
import com.example.bookcycle.utilities.BorrowRequest
import com.example.bookcycle.utilities.RequestsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textview.MaterialTextView

class NotificationsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: MaterialTextView
    private lateinit var title: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            requireActivity().finish()
            return
        }
        db = FirebaseFirestore.getInstance()

        setupViews(view)
        loadBorrowRequests()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.requestsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        title = view.findViewById(R.id.title)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadBorrowRequests() {
        db.collection("borrow_requests")
            .whereEqualTo("ownerId", currentUser.uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    val requests = documents.mapNotNull { it.toObject(BorrowRequest::class.java).copy(id = it.id) }
                    recyclerView.adapter = RequestsAdapter(requests, ::handleRequest)
                }
            }
    }

    private fun handleRequest(request: BorrowRequest, isApproved: Boolean) {
        if (isApproved) {
            val batch = db.batch()
            val bookRef = db.collection("books").document(request.bookId)

            when (request.requestType) {
                "return" -> {
                    batch.update(bookRef, mapOf(
                        "isAvailable" to true,
                        "borrowerId" to "",
                        "borrowerName" to "",
                        "status" to "available"
                    ))
                }
                else -> {
                    batch.update(bookRef, mapOf(
                        "isAvailable" to false,
                        "borrowerId" to request.requesterId,
                        "borrowerName" to request.requesterName,
                        "status" to "lent"
                    ))
                }
            }

            val requestRef = db.collection("borrow_requests").document(request.id)
            batch.update(requestRef, "status", "approved")

            batch.commit().addOnSuccessListener {
                val message = if (request.requestType == "return")
                    "Return request approved"
                else
                    "Borrow request approved"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadBorrowRequests()
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to approve request: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            updateRequestStatus(request.id, "rejected")
        }
    }

    private fun updateRequestStatus(requestId: String, status: String) {
        db.collection("borrow_requests").document(requestId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(requireContext(),
                    if (status == "approved") "The request is approved" else "The request is rejected",
                    Toast.LENGTH_SHORT).show()
                loadBorrowRequests()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update request: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    @Override
    override fun onResume() {
        super.onResume()
        loadBorrowRequests()
    }

    @Override
    override fun onPause() {
        super.onPause()
    }
}
