package com.example.bookcycle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bookcycle.utilities.BookData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var searchBookName: TextInputEditText
    private lateinit var searchAuthor: TextInputEditText
    private lateinit var searchGenre: TextInputEditText

    private val db = FirebaseFirestore.getInstance()
    private var currentLocation: Location? = null
    private var currentSearchResults = mutableListOf<BookData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupSearchFields()
    }

    private fun setupSearchFields() {
        searchBookName = findViewById(R.id.search_book_name)
        searchAuthor = findViewById(R.id.search_author)
        searchGenre = findViewById(R.id.search_genre)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    performSearch()
                }
            }
        }

        searchBookName.addTextChangedListener(textWatcher)
        searchAuthor.addTextChangedListener(textWatcher)
        searchGenre.addTextChangedListener(textWatcher)
    }

    private fun performSearch() {
        currentLocation?.let { location ->
            val bookName = searchBookName.text.toString().trim().lowercase()
            val author = searchAuthor.text.toString().trim().lowercase()
            val genre = searchGenre.text.toString().trim().lowercase()

            mMap.clear()
            currentSearchResults.clear()

            db.collection("books")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val filteredBooks = documents
                        .mapNotNull { it.toObject(BookData::class.java) }
                        .filter { book ->
                            val matchesTitle = bookName.isEmpty() ||
                                    book.title.lowercase().contains(bookName)
                            val matchesAuthor = author.isEmpty() ||
                                    book.author.lowercase().contains(author)
                            val matchesGenre = genre.isEmpty() ||
                                    book.genre.lowercase() == genre

                            matchesTitle && matchesAuthor && matchesGenre
                        }

                    if (filteredBooks.isEmpty()) {
                        Toast.makeText(this, "No books found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    filteredBooks.forEach { book ->
                        currentSearchResults.add(book)
                        val bookLatLng = LatLng(book.latitude, book.longitude)
                        mMap.addMarker(MarkerOptions()
                            .position(bookLatLng)
                            .title(book.title)
                            .snippet("By: ${book.author}")
                        )
                    }
                    if (filteredBooks.isNotEmpty()) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            10f))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "Waiting for current location...", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (checkLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        mMap.setOnMarkerClickListener { marker ->
            val book = currentSearchResults.find {
                it.latitude == marker.position.latitude &&
                        it.longitude == marker.position.longitude
            }
            book?.let {
                val intent = Intent(this, ItemBookActivity::class.java).apply {
                    putExtra("book", book)
                    putExtra("view_type", "search_result")
                }
                startActivity(intent)
            }
            true
        }
    }

    private fun enableMyLocation() {
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f)) // שינינו את רמת הזום ל-10 במקום 13
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(this, "Location permission is required to show your position on the map", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
