package com.example.bookcycle.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookcycle.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class RequestsAdapter(
    private val requests: List<BorrowRequest>,
    private val onRequestHandled: (BorrowRequest, Boolean) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: MaterialTextView = view.findViewById(R.id.requestTitle)
        val message: MaterialTextView = view.findViewById(R.id.requestMessage)
        val approveButton: MaterialButton = view.findViewById(R.id.approveButton)
        val rejectButton: MaterialButton = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_borrow_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.title.text = request.bookTitle

        holder.message.text = when (request.requestType) {
            "return" -> "Return request from ${request.requesterName}"
            else -> "Borrow request from ${request.requesterName}"
        }

        holder.approveButton.setOnClickListener { onRequestHandled(request, true) }
        holder.rejectButton.setOnClickListener { onRequestHandled(request, false) }
    }

    override fun getItemCount() = requests.size
}