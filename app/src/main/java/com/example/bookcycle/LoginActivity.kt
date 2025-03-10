package com.example.bookcycle

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookcycle.fragments.UserDetailsFragment
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var loginFields: LinearLayoutCompat
    private lateinit var authButtons: LinearLayoutCompat
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var signInButton: MaterialButton
    private lateinit var appTitle: MaterialTextView
    private lateinit var appSubtitle: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        initializeViews()
        setupWindowInsets()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            setupAuthButtons()
        } else {
            checkUserDetails(currentUser.uid)
        }
    }

    private fun initializeViews() {
        loginFields = findViewById(R.id.loginFields)
        authButtons = findViewById(R.id.authButtons)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        signInButton = findViewById(R.id.signInButton)
        appTitle = findViewById(R.id.appTitle)
        appSubtitle = findViewById(R.id.appSubtitle)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupAuthButtons() {
        signUpButton.setOnClickListener {
            signUp()
        }

        signInButton.setOnClickListener {
            loginFields.visibility = View.VISIBLE
            authButtons.visibility = View.GONE
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                showMessage("All fields must be filled")
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        checkUserDetails(user.uid)
                    }
                }
                .addOnFailureListener {
                    showMessage("Invalid login credentials")
                }
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_LONG).show()
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun signUp() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder()
                .setRequireName(true)
                .setAllowNewAccounts(true)
                .build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.FirebaseUI)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            checkUserDetails(user?.uid)
        } else {
            showMessage("Invalid login credentials")
            setupAuthButtons()
        }
    }

    private fun checkUserDetails(userId: String?) {
        if (userId == null) {
            return
        }

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    transactToNextScreen()
                } else {
                    showUserDetailsFragment()
                }
            }
            .addOnFailureListener { e ->
                showMessage("Failed to load user details")
            }
    }

    private fun transactToNextScreen() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun showUserDetailsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserDetailsFragment())
            .commit()
    }
}