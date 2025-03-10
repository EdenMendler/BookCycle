package com.example.bookcycle

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bookcycle.fragments.AddBookFragment
import com.example.bookcycle.fragments.NotificationsFragment
import com.example.bookcycle.fragments.PersonalInfoFragment
import com.example.bookcycle.utilities.BooksAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupViewPagerWithTabs()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener { showMenu(it) }
        findViewById<MaterialButton>(R.id.btn_add_book).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddBookFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupViewPagerWithTabs() {
        val adapter = BooksAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "My Books"
                1 -> "Lent Books"
                2 -> "Borrowed Books"
                else -> ""
            }
        }.attach()
    }

    private fun showMenu(view: View) {
        PopupMenu(this, view).apply {
            menu.add(Menu.NONE, 1, Menu.NONE, "Personal Info")
            menu.add(Menu.NONE, 2, Menu.NONE, "Search Book")
            menu.add(Menu.NONE, 3, Menu.NONE, "Notifications")
            menu.add(Menu.NONE, 4, Menu.NONE, "Log Out")

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, PersonalInfoFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    2 -> {
                        val intent = Intent(this@MainActivity, SearchActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    3 -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, NotificationsFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    4 -> {
                        signOut()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    override fun onResume() {
        super.onResume()
        setupViewPagerWithTabs()
    }
    @Override
    override fun onPause() {
        super.onPause()
    }
}