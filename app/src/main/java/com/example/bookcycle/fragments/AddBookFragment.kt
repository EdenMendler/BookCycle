package com.example.bookcycle.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bookcycle.R
import com.example.bookcycle.utilities.BookData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import java.util.UUID

class AddBookFragment : Fragment() {
    private lateinit var bookImage: ImageView
    private lateinit var bookTitle: TextInputEditText
    private lateinit var bookAuthor: TextInputEditText
    private lateinit var bookGenre: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUser: FirebaseUser
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private val selectedImages = mutableListOf<Uri>()

    companion object {
        private const val REQUEST_MAIN_IMAGE = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        initializeFirebase()
        initializeLocation()
        setupImagePickers()
        setupSubmitButton()
    }

    private fun initializeViews(view: View) {
        bookImage = view.findViewById(R.id.bookImage)
        bookTitle = view.findViewById(R.id.bookTitle)
        bookAuthor = view.findViewById(R.id.bookAuthor)
        bookGenre = view.findViewById(R.id.bookGenre)
        submitButton = view.findViewById(R.id.submitButton)
    }

    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkAndRequestLocationPermission()
    }

    private fun setupImagePickers() {
        bookImage.setOnClickListener {
            pickImage(REQUEST_MAIN_IMAGE)
        }
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateForm()) {
                uploadImages()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (bookTitle.text.isNullOrEmpty()) {
            bookTitle.error = "Please enter title"
            isValid = false
        }

        if (bookAuthor.text.isNullOrEmpty()) {
            bookAuthor.error = "Please enter author"
            isValid = false
        }

        if (bookGenre.text.isNullOrEmpty()) {
            bookGenre.error = "Please enter genre"
            isValid = false
        }

        return isValid
    }

    private fun pickImage(requestCode: Int) {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode)
    }

    private fun uploadImages() {
        val bookId = UUID.randomUUID().toString()
        var uploadedImages = 0
        val imageUrls = mutableListOf<String>()

        if (selectedImages.isEmpty()) {
            saveBookToFirestore(bookId, imageUrls)
            return
        }

        selectedImages.forEachIndexed { index, uri ->
            val imageRef = storage.reference.child("books/$bookId/image_$index.jpg")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        imageUrls.add(downloadUrl.toString())
                        uploadedImages++

                        if (uploadedImages == selectedImages.size) {
                            saveBookToFirestore(bookId, imageUrls)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Image upload error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveBookToFirestore(bookId: String, imageUrls: List<String>) {
        val book = BookData(
            id = bookId,
            title = bookTitle.text.toString(),
            author = bookAuthor.text.toString(),
            genre = bookGenre.text.toString(),
            imageUrl = imageUrls.firstOrNull() ?: "",
            ownerId = currentUser.uid,
            ownerName = currentUser.displayName ?: "",
            latitude = currentLocation?.latitude ?: 0.0,
            longitude = currentLocation?.longitude ?: 0.0,
            isAvailable = true,
            borrowerId = "",
            borrowerName = ""
        )

        firestore.collection("books").document(bookId)
            .set(book)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Book added successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error saving book: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkAndRequestLocationPermission() {
        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(),
                        "Error getting location: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(requireContext(),
                        "Location permission is required for sharing the book",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data

            when (requestCode) {
                REQUEST_MAIN_IMAGE -> {
                    bookImage.setImageURI(imageUri)
                    imageUri?.let { selectedImages.add(it) }
                }
            }
        }
    }
    @Override
    override fun onResume() {
        super.onResume()
        if (checkLocationPermission()) {
            getCurrentLocation()
        }
    }

    @Override
    override fun onPause() {
        super.onPause()
    }

}
