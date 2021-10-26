package com.example.mymessenger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mymessenger.databinding.FragmentSignInBinding
import com.example.mymessenger.myClasses.Account
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class SignIn : Fragment() {

    lateinit var fragment: FragmentSignInBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val TAG = "MainFragment"
    lateinit var listColors :ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragment = FragmentSignInBinding.inflate(inflater, container, false)
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("users")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        auth = FirebaseAuth.getInstance()


        fragment.google.setOnClickListener {
            signIn()
            Toast.makeText(requireContext(), "Xush kelibsiz", Toast.LENGTH_SHORT).show()
        }

        fragment.signout.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            Toast.makeText(requireContext(), "Obuna bekor qilindi", Toast.LENGTH_LONG).show()
        }

        return fragment.root
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed: ", e)
                }
            } else {
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    var account = Account(
                        user?.email,
                        user?.displayName,
                        user?.photoUrl.toString(),
                        user?.uid, colorMessage = randomColor()
                    )
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var isHave = false
                            val children = snapshot.children
                            for (child in children) {
                                val value = child.getValue(Account::class.java)
                                if (value?.uid == user?.uid) {
                                    isHave = true
                                    break
                                }
                            }
                            if (isHave) {
                                findNavController().navigate(R.id.action_signIn_to_viewPager)
                            } else {
                                reference.child(user?.uid ?: "").setValue(account)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            findNavController().navigate(R.id.action_signIn_to_viewPager)
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
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
}