package com.example.mymessenger.fragment

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.R
import com.example.mymessenger.adapters.AdapterGroups
import com.example.mymessenger.adapters.HomeAdapter
import com.example.mymessenger.databinding.AddGroupBinding
import com.example.mymessenger.databinding.FragmentListBinding
import com.example.mymessenger.myClasses.Account
import com.example.mymessenger.myClasses.Group
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Listf : Fragment() {

    private var param1: String? = null
    private var param2: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getInt(ARG_PARAM2)
        }
    }

    lateinit var fragment: FragmentListBinding
    lateinit var adapter: HomeAdapter
    lateinit var stringlist: ArrayList<Account>
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var listGroups: ArrayList<Group>
    lateinit var listColors: ArrayList<String>
    lateinit var adapterGroups: AdapterGroups
    lateinit var handler: Handler

    private val open: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.roatate_anim_open)
    }
    private val close: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.rotate_close_anim)
    }
    private val frombottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.from_bottom_anim)
    }
    private val tobottom: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(),
            R.anim.to_bottomt_anim)
    }
    private var clicked = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragment = FragmentListBinding.inflate(inflater, container, false)
        fragment.apply {
            firebaseAuth = FirebaseAuth.getInstance()
            listGroups = ArrayList()
            handler = Handler(Looper.getMainLooper())
            firebaseDatabase = FirebaseDatabase.getInstance()
            stringlist = ArrayList()
            reference = firebaseDatabase.getReference("users")
            var referenceGroup = firebaseDatabase.getReference("groups")

//            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
//                    return@OnCompleteListener
//                }
//                val token = task.result
//                reference.child("token")
//                    .setValue("$token")
//                    .addOnCompleteListener {
//                    }
//                Log.d(TAG, token)
//                Toast.makeText(requireContext(), token, Toast.LENGTH_SHORT).show()
//            })

            when (param2) {
                0 -> {
                    var currentUser = firebaseAuth.currentUser
                    val email = currentUser!!.email
                    val displayName = currentUser.displayName
                    val photoUrl = currentUser.photoUrl
                    val uid = currentUser.uid
                    var user = Account(email, displayName, photoUrl.toString(), uid, randomColor())
                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            var listFilter = ArrayList<Account>()
                            val children = snapshot.children
                            stringlist.clear()
                            for (i in children) {
                                val value = i.getValue(Account::class.java)
                                if (value != null && value.uid != uid) {
                                    stringlist.add(value)
                                }
                                if (value != null && value.uid == uid) {
                                    listFilter.add(value)
                                }
                            }
                            if (listFilter.isEmpty()) {
                                reference.child(uid).setValue(user)
                            }
                            adapter = HomeAdapter(
                                root.context,
                                stringlist,
                                object : HomeAdapter.OnItemClickLitener {
                                    override fun onItemClick(user: Account, position: Int) {
                                        var bundle = Bundle()
                                        bundle.putSerializable("user", user)
                                        findNavController().navigate(R.id.selectedUser,
                                            bundle
                                        )
                                    }
                                })
                            fragment.rvc.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                    })
                }
                1 -> {
                    fragment.floatingActionButton.visibility = View.VISIBLE
                    floatingActionButton.setOnClickListener {
                        onFunctionButton()
                    }
                    fragment.chip1.setOnClickListener {
                        var alertDialog =
                            AlertDialog.Builder(root.context, R.style.BottomSheetDialogThem)
                        val create = alertDialog.create()
                        var addGroupBinding =
                            AddGroupBinding.inflate(LayoutInflater.from(root.context), null, false)
                        create.setView(addGroupBinding.root)
                        addGroupBinding.save.setOnClickListener {
                            var key = referenceGroup.push().key!!
                            val name = addGroupBinding.nameGroup.text.toString()
                            val info = addGroupBinding.info.text.toString()
                            if (name.isNotBlank() && info.isNotBlank()) {
                                var group = Group(name,
                                    info,
                                    key, R.drawable.group)
                                referenceGroup.child("$key").setValue(group)
                                create.dismiss()
                            } else {
                                Toast.makeText(
                                    root.context,
                                    "Iltimos malumotlarni to`liq qiling",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        addGroupBinding.cancle.setOnClickListener {
                            create.dismiss()
                        }
                        create.setCancelable(false)
                        create.show()
                    }

                    referenceGroup.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            listGroups.clear()
                            for (child in children) {
                                var a = child.getValue(Group::class.java)
                                if (a != null)
                                    listGroups.add(a)
                            }
                            adapterGroups.notifyDataSetChanged()
                        }

                    })
                    adapterGroups =
                        AdapterGroups(listGroups, object : AdapterGroups.OnItemCklickListener {
                            override fun onItemClick(group: Group, position: Int) {
                                var bundle = Bundle()
                                bundle.putSerializable("group", group)
                                findNavController().navigate(R.id.chatGroup,
                                    bundle
                                )
                            }
                        })
                    fragment.rvc.adapter = adapterGroups
                }
            }
            return fragment.root
        }
    }

    fun randomColor(): String {
        listColors = ArrayList()
        listColors.add("#B61D1D")
        listColors.add("#EF281A")
        listColors.add("#757170")
        listColors.add("#6016E3")
        listColors.add("#3F51B5")
        listColors.add("#048BF6")
        listColors.add("#00BCD4")
        listColors.add("#009688")
        listColors.add("#4CAF50")
        listColors.add("#7FD619")
        listColors.add("#CDDC39")
        listColors.add("#ECD404")
        listColors.add("#FF9800")
        listColors.add("#FF5722")
        listColors.add("#D55F3A")
        listColors.add("#9C27B0")
        listColors.add("#465295")
        listColors.add("#60C3CF")
        var rendom = Random()
        val nextInt = rendom.nextInt(listColors.size)
        return listColors[nextInt]
    }

    private fun onFunctionButton() {
        setvisibility(clicked)
        setanimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setvisibility(cliked: Boolean) {
        if (!cliked) {
            fragment.chip1.visibility = View.VISIBLE
        } else {
            fragment.chip1.visibility = View.INVISIBLE
        }
    }

    private fun setanimation(cliked: Boolean) {
        if (!cliked) {
            fragment.chip1.startAnimation(frombottom)
            fragment.floatingActionButton.startAnimation(open)
        } else {
            fragment.chip1.startAnimation(tobottom)
            fragment.floatingActionButton.startAnimation(close)
        }
    }

    private fun setClickable(cliked: Boolean) {
        fragment.chip1.isClickable = !cliked
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: Int) =
            Listf().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }
    }
}