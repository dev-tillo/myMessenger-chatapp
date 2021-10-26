package com.example.mymessenger.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.R
import com.example.mymessenger.adapters.GalarryAdapter
import com.example.mymessenger.adapters.MessageAdapter
import com.example.mymessenger.databinding.DeleteMessgeBinding
import com.example.mymessenger.databinding.FragmentSelectedUserBinding
import com.example.mymessenger.databinding.FromitemBinding
import com.example.mymessenger.databinding.ToitemBinding
import com.example.mymessenger.myClasses.Messegs
import com.example.mymessenger.myClasses.Account
import com.example.mymessenger.myClasses.MessagesCount
import com.example.mymessenger.keraksiz.utils.GlideImageLoader
import com.example.mymessenger.keraksiz.utils.SaveImage
import com.example.mymessenger.servvv.Notification
import com.example.mymessenger.servvv.NotificationData
import com.example.mymessenger.servvv.ResNotificationData
import com.example.mymessenger.utilmessnger.ApiClient
import com.example.mymessenger.utilmessnger.NetworkHelper
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import lv.chi.photopicker.ChiliPhotoPicker
import lv.chi.photopicker.PhotoPickerFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SelectedUser : Fragment() {

    lateinit var fragment: FragmentSelectedUserBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var list: ArrayList<Messegs>
    private lateinit var messageAdapter: MessageAdapter
    lateinit var user: Account
    var countChat = 0
    var isBoolean: Boolean = false
    private lateinit var galarryAdapter: GalarryAdapter
    private lateinit var listChecked: ArrayList<SaveImage>

    @SuppressLint("SimpleDateFormat", "WrongConstant")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragment = FragmentSelectedUserBinding.inflate(inflater, container, false)
        fragment.apply {

            firebaseAuth = FirebaseAuth.getInstance()
            list = ArrayList()
            firebaseDatabase = FirebaseDatabase.getInstance()
            var referensOnline = firebaseDatabase.getReference("isOnline")
            databaseReference = firebaseDatabase.getReference("users")
            user = arguments?.getSerializable("user") as Account
            Picasso.get().load(user.photoUrl).into(fragment.userImage)
            fragment.nameUser.text = user.displayName

//            referensOnline.child(user.uid!!).addValueEventListener(object : ValueEventListener {
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val n = snapshot.getValue(Int::class.java)
//                    if (n == 1) {
//                        fragment.message.text = "online"
//                    } else {
//                        fragment.message.text = "last seen recently"
//                    }
//                }
//            })

            ChiliPhotoPicker.init(
                loader = GlideImageLoader(),
                authority = "lv.chi.sample.fileprovider"
            )

            chanel.setOnClickListener {
                listChecked = ArrayList()
                askPermission(android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                    ScrollingMovementMethod()
                    openPicker()
                }.onDeclined { e ->
                    if (e.hasDenied()) {
                        AlertDialog.Builder(requireContext())
                            .setMessage("Please accept our permissions")
                            .setPositiveButton("yes") { dialog, which ->
                                e.askAgain();
                            } //ask again
                            .setNegativeButton("no") { dialog, which ->
                                dialog.dismiss();
                            }.show();
                    }

                    if (e.hasForeverDenied()) {
                        // you need to open setting manually if you really need it
                        e.goToSettings();
                    }
                }
            }

            fragment.left.setOnClickListener {
                closeKerBoard()
                findNavController().popBackStack()
            }

            send.setOnClickListener {
                val messageChat = fragment.edittext.text.toString()
                if (messageChat.isNotBlank() && messageChat.isNotEmpty()) {
                    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                    val format = simpleDateFormat.format(Date())
                    var key = databaseReference.push().key!!
                    var message = Messegs(
                        messageChat,
                        format,
                        firebaseAuth.currentUser!!.uid,
                        user.uid,
                        key
                    )
                    databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}/$key")
                        .setValue(message)
                    databaseReference.child("${user.uid}/messages/${firebaseAuth.currentUser!!.uid}/$key")
                        .setValue(message)

//                    val networkHelper = NetworkHelper(requireContext())
//                    if (networkHelper.isNetworkConnected()) {
//                        val notify =
//                            NotificationData(Notification("user", messageChat), user.token!!)
//                        ApiClient.apiService.sendNotification(notify)
//                            .enqueue(object : Callback<ResNotificationData> {
//                                override fun onResponse(
//                                    call: Call<ResNotificationData>,
//                                    response: Response<ResNotificationData>,
//                                ) {
//                                    if (response.isSuccessful) {
//                                        Toast.makeText(requireContext(),
//                                            "notification",
//                                            Toast.LENGTH_SHORT)
//                                            .show()
//                                    }
//                                }
//
//                                override fun onFailure(
//                                    call: Call<ResNotificationData>,
//                                    t: Throwable,
//                                ) {
//
//                                }
//
//                            })
//                    }
                    if (list.size == 0) {
                        var messageCount = MessagesCount(list.size + 1)
                        firebaseDatabase.getReference("messages_count")
                            .child("${firebaseAuth.currentUser!!.uid}/${user.uid}")
                            .setValue(messageCount)
                    } else {
                        var messageCount = MessagesCount(list.size + 1)
                        firebaseDatabase.getReference("messages_count")
                            .child("${firebaseAuth.currentUser!!.uid}/${user.uid}")
                            .setValue(messageCount)
                    }
                    fragment.edittext.setText("")
                } else {
                }
            }

            databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        list.clear()
                        for (child in children) {
                            list.add(child.getValue(Messegs::class.java)!!)
                        }
                        for (message in list) {
                            countChat++
                        }

                        messageAdapter = MessageAdapter(user,
                            list,
                            firebaseAuth.currentUser!!.uid,
                            object : MessageAdapter.FromMessageClick {
                                override fun fromMessageMenu(
                                    message: Messegs,
                                    position: Int,
                                    item: FromitemBinding,
                                ) {
                                    var popupMenu = PopupMenu(requireContext(), item.cons1)
                                    popupMenu.inflate(R.menu.menu_item)
                                    popupMenu.setOnMenuItemClickListener {
                                        when (it.itemId) {
                                            R.id.copy -> {
                                                var clipBoard =
                                                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                var clip =
                                                    ClipData.newPlainText("Text", message.message)
                                                clipBoard.setPrimaryClip(clip)
                                            }
                                            R.id.copy_past -> {
                                                var clipBoard =
                                                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                var clip =
                                                    ClipData.newPlainText("Text", message.message)
                                                clipBoard.setPrimaryClip(clip)
                                                fragment.edittext.setText(message.message)
                                            }
                                            R.id.delete -> {
                                                var alertDialog = AlertDialog.Builder(
                                                    requireContext(),
                                                    R.style.BottomSheetDialogThem
                                                )
                                                val create = alertDialog.create()
                                                var deleteMessgeBinding =
                                                    DeleteMessgeBinding.inflate(
                                                        LayoutInflater.from(requireContext()),
                                                        null,
                                                        false
                                                    )
                                                deleteMessgeBinding.deleteCheck.text =
                                                    "${user.displayName} dan ham o`chsinmi"
                                                deleteMessgeBinding.deleteBtn.setOnClickListener {
                                                    if (deleteMessgeBinding.deleteCheck.isChecked) {
                                                        val uid = firebaseAuth.currentUser!!.uid
                                                        databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .value!! == uid && dataSnapshot.child(
                                                                                "message"
                                                                            ).value!! == message.message && dataSnapshot.child(
                                                                                "key"
                                                                            ).value!! == message.key
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                        databaseReference.child("${user.uid}/messages/${firebaseAuth.currentUser!!.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .value!! == message.fromUid && dataSnapshot.child(
                                                                                "message"
                                                                            ).value!! == message.message && dataSnapshot.child(
                                                                                "key"
                                                                            ).value!! == message.key
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                    } else {
                                                        val uid = firebaseAuth.currentUser!!.uid
                                                        databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .getValue()!!
                                                                                .equals(uid) && dataSnapshot.child(
                                                                                "message"
                                                                            ).getValue()!!
                                                                                .equals(message.message) && dataSnapshot.child(
                                                                                "key"
                                                                            ).getValue()!!
                                                                                .equals(message.key)
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                    }
                                                    create.dismiss()
                                                }
                                                deleteMessgeBinding.nodeleteBtn.setOnClickListener {
                                                    create.dismiss()
                                                }
                                                create.setView(deleteMessgeBinding.root)
                                                create.show()
                                            }
                                            R.id.edite -> {
                                                fragment.edittext.setText(message.message)
                                                val uid = firebaseAuth.currentUser!!.uid
                                                fragment.edittext.visibility = View.VISIBLE
                                                isBoolean = true
                                                fragment.send.visibility = View.VISIBLE
                                                isBoolean = false
                                                var message_m =
                                                    fragment.edittext.text.toString()
                                                databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}/${message.key}")
                                                    .setValue(
                                                        Messegs(
                                                            message_m,
                                                            message.date,
                                                            message.fromUid,
                                                            message.toUid,
                                                            message.key!!
                                                        )
                                                    )
                                                databaseReference.child("${user.uid}/messages/${firebaseAuth.currentUser!!.uid}/${message.key}")
                                                    .setValue(
                                                        Messegs(
                                                            message_m,
                                                            message.date,
                                                            message.fromUid,
                                                            message.toUid,
                                                            message.key!!
                                                        )
                                                    )
                                                fragment.edittext.setText("")
                                            }
                                        }
                                        true
                                    }
                                    try {
                                        var fieldMPopup =
                                            PopupMenu::class.java.getDeclaredField("mPopup")
                                        fieldMPopup.isAccessible = true
                                        var mPopup = fieldMPopup.get(popupMenu)
                                        mPopup.javaClass.getDeclaredMethod(
                                            "setForceShowIcon",
                                            Boolean::class.java
                                        )
                                            .invoke(mPopup, true)
                                    } catch (e: Exception) {
                                        Toast.makeText(root.context, "Error", Toast.LENGTH_SHORT)
                                            .show()
                                    } finally {
                                        popupMenu.show()
                                    }
                                    popupMenu.show()
                                }
                            },
                            object : MessageAdapter.ToMessageClick {
                                override fun ToMessageMenu(
                                    message: Messegs,
                                    position: Int,
                                    item: ToitemBinding,
                                ) {
                                    var popupMenu = PopupMenu(root.context, item.cons1)
                                    popupMenu.inflate(R.menu.menu_to)
                                    popupMenu.setOnMenuItemClickListener {
                                        when (it.itemId) {
                                            R.id.copy_ -> {
                                                var clipBoard =
                                                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                var clip =
                                                    ClipData.newPlainText("Text", message.message)
                                                clipBoard.setPrimaryClip(clip)
                                            }
                                            R.id.copy12 -> {
                                                var clipBoard =
                                                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                var clip =
                                                    ClipData.newPlainText("Text", message.message)
                                                clipBoard.setPrimaryClip(clip)
                                                fragment.edittext.setText(message.message)
                                            }
                                            R.id.delete1 -> {
                                                var alertDialog = AlertDialog.Builder(
                                                    root.context,
                                                    R.style.BottomSheetDialogThem
                                                )
                                                val create = alertDialog.create()
                                                var deleteMessgeBinding =
                                                    DeleteMessgeBinding.inflate(
                                                        LayoutInflater.from(root.context),
                                                        null,
                                                        false
                                                    )
                                                deleteMessgeBinding.deleteCheck.visibility =
                                                    View.INVISIBLE
                                                deleteMessgeBinding.messageDeleteTo.visibility =
                                                    View.VISIBLE
                                                deleteMessgeBinding.messageDeleteTo.text =
                                                    "Malumot faqat sizdan o`chadi"
                                                deleteMessgeBinding.deleteBtn.setOnClickListener {
                                                    if (deleteMessgeBinding.deleteCheck.isChecked) {
                                                        val uid = firebaseAuth.currentUser!!.uid
                                                        databaseReference.child("${user.uid}/messages/${firebaseAuth.currentUser!!.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .getValue()!!
                                                                                .equals(uid) && dataSnapshot.child(
                                                                                "message"
                                                                            ).getValue()!!
                                                                                .equals(message.message) && dataSnapshot.child(
                                                                                "key"
                                                                            ).getValue()!!
                                                                                .equals(message.key)
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                        databaseReference.child("${firebaseAuth.currentUser!!.uid}/messages/${user.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .getValue()!!
                                                                                .equals(message.fromUid) && dataSnapshot.child(
                                                                                "message"
                                                                            ).getValue()!!
                                                                                .equals(message.message) && dataSnapshot.child(
                                                                                "key"
                                                                            ).getValue()!!
                                                                                .equals(message.key)
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                    } else {
                                                        val uid = firebaseAuth.currentUser!!.uid
                                                        databaseReference.child("${user.uid}/messages/${firebaseAuth.currentUser!!.uid}")
                                                            .addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onCancelled(error: DatabaseError) {

                                                                }

                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    val children1 =
                                                                        snapshot.children
                                                                    for (dataSnapshot in children1) {
                                                                        if (dataSnapshot.child("fromUid")
                                                                                .getValue()!!
                                                                                .equals(uid) && dataSnapshot.child(
                                                                                "message"
                                                                            ).getValue()!!
                                                                                .equals(message.message) && dataSnapshot.child(
                                                                                "key"
                                                                            ).getValue()!!
                                                                                .equals(message.key)
                                                                        ) {
                                                                            dataSnapshot.ref.removeValue()
                                                                            messageAdapter.notifyDataSetChanged()
                                                                            break
                                                                        }
                                                                    }
                                                                }

                                                            })
                                                    }
                                                    create.dismiss()
                                                }
                                                deleteMessgeBinding.nodeleteBtn.setOnClickListener {
                                                    create.dismiss()
                                                }
                                                create.setView(deleteMessgeBinding.root)
                                                create.show()
                                            }
                                        }
                                        true
                                    }
                                    try {
                                        var fieldMPopup =
                                            PopupMenu::class.java.getDeclaredField("mPopup")
                                        fieldMPopup.isAccessible = true
                                        var mPopup = fieldMPopup.get(popupMenu)
                                        mPopup.javaClass.getDeclaredMethod(
                                            "setForceShowIcon",
                                            Boolean::class.java
                                        )
                                            .invoke(mPopup, true)
                                    } catch (e: Exception) {
                                        Toast.makeText(root.context, "Error", Toast.LENGTH_SHORT)
                                            .show()
                                    } finally {
                                        popupMenu.show()
                                    }
                                    popupMenu.show()

                                }
                            })
                        fragment.rvc.adapter = messageAdapter
                        messageAdapter.notifyDataSetChanged()
                        fragment.rvc.smoothScrollToPosition(messageAdapter.itemCount)
                    }

                })

            if (isBoolean) {
                fragment.edittext.addTextChangedListener {

                }
            } else {
                fragment.edittext.visibility = View.VISIBLE
                fragment.send.visibility = View.VISIBLE
            }
            var calback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, calback)
        }
        return fragment.root
    }

    private fun openPicker() {
        PhotoPickerFragment.newInstance(
            multiple = true,
            allowCamera = false,
            maxSelection = 5,
            theme = R.style.ChiliPhotoPicker_Light
        ).show(childFragmentManager, "picker")
    }

//    override fun onImagesPicked(photos: ArrayList<Uri>) {
//        Toast.makeText(requireContext(),
//            photos.joinToString(separator = "\n") { it.toString() },
//            Toast.LENGTH_SHORT).show()
//    }


    override fun onStop() {
        super.onStop()
        if (list.size >= 1) {
            var messageCount = MessagesCount(list.size)
            firebaseDatabase.getReference("messages_count")
                .child("${firebaseAuth.currentUser!!.uid}/${user.uid}").setValue(messageCount)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (list.size >= 1) {
            var messageCount = MessagesCount(list.size)
            firebaseDatabase.getReference("messages_count")
                .child("${firebaseAuth.currentUser!!.uid}/${user.uid}").setValue(messageCount)
        }
    }

    private fun closeKerBoard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}