package com.example.mymessenger.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mymessenger.fragment.Listf
import com.example.mymessenger.myClasses.Category

class PagerViewAdapter(var listCategory: ArrayList<Category>, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return listCategory.size
    }

    override fun createFragment(position: Int): Fragment {
        return Listf.newInstance(listCategory[position].nameCategory!!,
            listCategory[position].positionCategory!!
        )
    }
}