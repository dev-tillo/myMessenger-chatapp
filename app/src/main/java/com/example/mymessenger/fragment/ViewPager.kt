package com.example.mymessenger.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.example.mymessenger.R
import com.example.mymessenger.adapters.PagerViewAdapter
import com.example.mymessenger.databinding.FragmentViewPagerBinding
import com.example.mymessenger.databinding.ItemCategoryBinding
import com.example.mymessenger.databinding.TabItemBinding
import com.example.mymessenger.myClasses.Account
import com.example.mymessenger.myClasses.Category
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewPager : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var fragment: FragmentViewPagerBinding
    lateinit var pagerViewAdapter: PagerViewAdapter
    lateinit var listCategory: ArrayList<Category>
    lateinit var listUser: ArrayList<Account>
    private val referenceOnline = FirebaseDatabase.getInstance().getReference("isOnline")
    private val currentUser = FirebaseAuth.getInstance().currentUser
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firbaseDatabse: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var auth: FirebaseAuth

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragment = FragmentViewPagerBinding.inflate(inflater, container, false)
        fragment.apply {

            auth = FirebaseAuth.getInstance()
            loadCategory()

            pagerViewAdapter = PagerViewAdapter(listCategory, requireActivity())
            viewpager2.adapter = pagerViewAdapter
            listUser = ArrayList()

            firebaseAuth = FirebaseAuth.getInstance()
            firbaseDatabse = FirebaseDatabase.getInstance()
            reference = firbaseDatabse.getReference("users")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            reference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.children
                    for (i in value) {
                        val value1 = i.getValue(Account::class.java)
                        if (value1 != null && value1.uid == firebaseAuth.currentUser!!.uid) {

                        }
                    }
                }

            })

            TabLayoutMediator(
                tablayout,
                viewpager2
            ) { tab, position ->
                tab.text = listCategory[position].nameCategory
            }.attach()
            statTab()
            fragment.tablayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val customView = tab!!.customView
                    val itemBinding = ItemCategoryBinding.bind(customView!!)
                    itemBinding.container.setBackgroundResource(R.drawable.item_category1)
                    itemBinding.text.setTextColor(Color.BLACK)
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val customView = tab!!.customView
                    val itemBinding = ItemCategoryBinding.bind(customView!!)
                    itemBinding.container.setBackgroundResource(R.drawable.item)
                    itemBinding.text.setTextColor(Color.WHITE)
                }
            })

            var calback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as AppCompatActivity).finish()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, calback)
            return fragment.root
        }
    }

    private fun statTab() {
        val tabCount = fragment.tablayout.tabCount
        for (i in 0 until tabCount) {
            val inflate1 =
                ItemCategoryBinding.inflate(LayoutInflater.from(requireContext()), null, false)
            val tabAt = fragment.tablayout.getTabAt(i)
            tabAt!!.customView = inflate1.root
            inflate1.text.text = listCategory[i].nameCategory
            if (i == 0) {
                inflate1.container.setBackgroundResource(R.drawable.item)
                inflate1.text.setTextColor(Color.WHITE)
            } else {
                inflate1.container.setBackgroundResource(R.drawable.item_category1)
                inflate1.text.setTextColor(Color.BLACK)
            }
        }
    }

    private fun loadCategory() {
        listCategory = ArrayList()
        listCategory.add(Category("Chats", 0))
        listCategory.add(Category("Groups", 1))
    }

    override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            referenceOnline.child(currentUser.uid).setValue(1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (currentUser != null) {
            referenceOnline.child(currentUser.uid).setValue(0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewPager().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}