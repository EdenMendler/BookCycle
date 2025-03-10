package com.example.bookcycle.fragments

import android.content.Intent
import com.google.android.material.button.MaterialButton
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bookcycle.MainActivity
import com.example.bookcycle.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailsFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var submitButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_details, container, false)

        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        ageEditText = view.findViewById(R.id.ageEditText)
        submitButton = view.findViewById(R.id.submitButton)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitButton.setOnClickListener {
            saveUserDetails()
        }
    }

    private fun saveUserDetails() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userDetails = hashMapOf(
                "name" to nameEditText.text.toString(),
                "email" to emailEditText.text.toString(),
                "phone" to phoneEditText.text.toString(),
                "age" to ageEditText.text.toString()
            )

            db.collection("users").document(user.uid)
                .set(userDetails)
                .addOnSuccessListener {
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error saving details: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}