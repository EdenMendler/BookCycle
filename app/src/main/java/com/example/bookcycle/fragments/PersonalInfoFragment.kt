package com.example.bookcycle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bookcycle.R
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var nameText: MaterialTextView
    private lateinit var emailText: MaterialTextView
    private lateinit var phoneText: MaterialTextView
    private lateinit var ageText: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_info, container, false)

        nameText = view.findViewById(R.id.nameText)
        emailText = view.findViewById(R.id.emailText)
        phoneText = view.findViewById(R.id.phoneText)
        ageText = view.findViewById(R.id.ageText)

        loadUserData()

        return view
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameText.text = document.getString("name") ?: ""
                        emailText.text = document.getString("email") ?: ""
                        phoneText.text = document.getString("phone") ?: ""
                        ageText.text = document.getString("age") ?: ""
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading data", Toast.LENGTH_LONG).show()
                }
        }
    }
}