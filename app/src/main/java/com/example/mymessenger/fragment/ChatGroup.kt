package com.example.mymessenger.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.R
import com.example.mymessenger.adapters.MessageGroupAdapter
import com.example.mymessenger.databinding.DeleteMessgeBinding
import com.example.mymessenger.databinding.FragmentChatGroupBinding
import com.example.mymessenger.databinding.FromitemBinding
import com.example.mymessenger.databinding.ToitemBinding
import com.example.mymessenger.myClasses.Group
import com.example.mymessenger.myClasses.MessageGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatGroup : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var fragment: FragmentChatGroupBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference_messageGroup: DatabaseReference
    lateinit var messageGroupAdapter: MessageGroupAdapter
    lateinit var reference: DatabaseReference
    lateinit var listMessage: ArrayList<MessageGroup>
    var isBoolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        fragment = FragmentChatGroupBinding.inflate(inflater, container, false)
        fragment.apply {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            listMessage = ArrayList()
            var group = arguments?.getSerializable("group") as Group
            reference = firebaseDatabase.getReference("groups")
            reference_messageGroup = firebaseDatabase.getReference("group_messages")

            fragment.cluseBtn.setOnClickListener {
                closeKerBoard()
                findNavController().popBackStack()
            }

            fragment.userImage.setImageResource(R.drawable.group)
            fragment.nameUser.text = group.groupName
            fragment.send.setOnClickListener {
                val messageChat = fragment.messageText.text.toString()
                if (messageChat.isNotBlank() && messageChat.isNotEmpty()) {
                    var key = reference_messageGroup.push().key
                    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                    val format = simpleDateFormat.format(Date())
                    var messageGroup = MessageGroup(
                        messageChat, firebaseAuth.currentUser!!.uid, format, group.key,
                        key!!
                    )
                    reference_messageGroup.child("${group.key}/${key}").setValue(messageGroup)
                    fragment.messageText.setText("")
                } else {
                    Toast.makeText(root.context, "No message", Toast.LENGTH_SHORT).show()
                }
            }

            reference_messageGroup.child("${group.key}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        listMessage.clear()
                        for (child in children) {
                            listMessage.add(child.getValue(MessageGroup::class.java)!!)
                        }
                        fragment.rvChat.smoothScrollToPosition(listMessage.size)
                        messageGroupAdapter.notifyDataSetChanged()
                    }

                })

            messageGroupAdapter = MessageGroupAdapter(
                root.context,
                listMessage,
                firebaseAuth.currentUser!!.uid,
                object : MessageGroupAdapter.FromMessageClick {
                    override fun fromMessageMenu(
                        group: MessageGroup,
                        position: Int,
                        item: FromitemBinding,
                    ) {
                        var popupMenu = PopupMenu(root.context, item.cons1)
                        popupMenu.inflate(R.menu.menu_item)
                        popupMenu.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.copy -> {
                                    var clipBoard =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    var clip = ClipData.newPlainText("Text", group.message)
                                    clipBoard.setPrimaryClip(clip)
                                }
                                R.id.copy_past -> {
                                    var clipBoard =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    var clip = ClipData.newPlainText("Text", group.message)
                                    clipBoard.setPrimaryClip(clip)
                                    fragment.messageText.setText(group.message)
                                }
                                R.id.delete -> {
                                    var alertDialog =
                                        AlertDialog.Builder(root.context,
                                            R.style.BottomSheetDialogThem)
                                    val create = alertDialog.create()
                                    var deleteMessgeBinding = DeleteMessgeBinding.inflate(
                                        LayoutInflater.from(root.context),
                                        null,
                                        false
                                    )
                                    deleteMessgeBinding.deleteCheck.visibility = View.GONE
                                    deleteMessgeBinding.messageDeleteTo.visibility = View.VISIBLE
                                    deleteMessgeBinding.messageDeleteTo.text =
                                        "Xabarni haqiqatdan o`chirmoqchimisiz"
                                    deleteMessgeBinding.deleteBtn.setOnClickListener {
                                        reference_messageGroup.child("${group.key}")
                                            .addValueEventListener(object : ValueEventListener {
                                                override fun onCancelled(error: DatabaseError) {

                                                }

                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val children = snapshot.children
                                                    for (child in children) {
                                                        if (child.child("message").value!!.equals(
                                                                group.message) && child.child(
                                                                "keyMessage"
                                                            ).value!!.equals(group.keyMessage)
                                                        ) {
                                                            child.ref.removeValue()
                                                            messageGroupAdapter.notifyDataSetChanged()
                                                            break
                                                        }
                                                    }
                                                }
                                            })
                                        create.dismiss()
                                    }
                                    deleteMessgeBinding.nodeleteBtn.setOnClickListener {
                                        create.dismiss()
                                    }
                                    create.setView(deleteMessgeBinding.root)
                                    create.setCancelable(false)
                                    create.show()

                                }
                                R.id.edite -> {
                                    fragment.messageText.setText(group.message)
                                    fragment.edite.visibility = View.VISIBLE
                                    fragment.send.visibility = View.INVISIBLE
                                    isBoolean = true
                                    fragment.edite.setOnClickListener {
                                        isBoolean = false
                                        val messageGroup =
                                            fragment.messageText.text.toString()
                                        reference_messageGroup.child("${group.key}/${group.keyMessage}")
                                            .setValue(
                                                MessageGroup(
                                                    messageGroup,
                                                    group.toUid,
                                                    group.date,
                                                    group.key,
                                                    group.keyMessage!!
                                                )
                                            )
                                        fragment.messageText.setText("")
                                    }
                                }

                            }

                            true
                        }
                        try {
                            var fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                            fieldMPopup.isAccessible = true
                            var mPopup = fieldMPopup.get(popupMenu)
                            mPopup.javaClass.getDeclaredMethod("setForceShowIcon",
                                Boolean::class.java)
                                .invoke(mPopup, true)
                        } catch (e: Exception) {
                            Toast.makeText(root.context, "Error", Toast.LENGTH_SHORT).show()
                        } finally {
                            popupMenu.show()
                        }
                        popupMenu.show()
                    }

                },
                object : MessageGroupAdapter.ToMessageClick {
                    override fun ToMessageMenu(
                        group: MessageGroup,
                        position: Int,
                        item: ToitemBinding,
                    ) {
                        var popupMenu = PopupMenu(root.context, item.cons1)
                        popupMenu.inflate(R.menu.menu_to1)
                        popupMenu.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.copy_to1 -> {
                                    var clipBoard =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    var clip = ClipData.newPlainText("Text", group.message)
                                    clipBoard.setPrimaryClip(clip)
                                }
                                R.id.copy1_to1 -> {
                                    var clipBoard =
                                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    var clip = ClipData.newPlainText("Text", group.message)
                                    clipBoard.setPrimaryClip(clip)
                                    fragment.messageText.setText(group.message)
                                }
                            }
                            true
                        }
                        try {
                            var fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                            fieldMPopup.isAccessible = true
                            var mPopup = fieldMPopup.get(popupMenu)
                            mPopup.javaClass.getDeclaredMethod("setForceShowIcon",
                                Boolean::class.java)
                                .invoke(mPopup, true)
                        } catch (e: Exception) {
                            Toast.makeText(root.context, "Error", Toast.LENGTH_SHORT).show()
                        } finally {
                            popupMenu.show()
                        }
                        popupMenu.show()
                    }
                })
            fragment.rvChat.adapter = messageGroupAdapter

            if (isBoolean) {
                fragment.messageText.addTextChangedListener {
                    fragment.edite.visibility = View.VISIBLE
                    fragment.send.visibility = View.INVISIBLE
                }
            } else {
                fragment.edite.visibility = View.INVISIBLE
                fragment.send.visibility = View.VISIBLE
            }
        }
        return fragment.root
    }

    private fun closeKerBoard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatGroup().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}