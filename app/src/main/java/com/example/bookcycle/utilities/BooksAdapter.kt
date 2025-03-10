package com.example.bookcycle.utilities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bookcycle.fragments.BooksListFragment

class BooksAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val type = when(position) {
            0 -> BooksType.MY_BOOKS
            1 -> BooksType.LENT_BOOKS
            2 -> BooksType.BORROWED_BOOKS
            else -> throw IllegalArgumentException("Invalid position")
        }
        return BooksListFragment.newInstance(type)
    }
}
