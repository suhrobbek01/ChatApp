package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private lateinit var userList: ArrayList<Users>
    private lateinit var userAdapter: UserAdapter
    private var addUserListener: ValueEventListener? = null
    private var userRef: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        binding.apply {

        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userList = ArrayList()
        userRef = FirebaseDatabase.getInstance().reference.child("users")
        addUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(Users::class.java)
                        if (user != null) {
                            if (user.getUID() != firebaseUser?.uid) {
                                userList.add(user)
                            }
                        }
                    }
                    userAdapter = UserAdapter(requireContext(), userList, true)
                    binding.recyclerViewUserList.adapter = userAdapter
                } else {
                    Toast.makeText(requireContext(), "Snapshot not exists", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        if (addUserListener != null) {
            userRef?.addValueEventListener(addUserListener!!)
        }
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        if (userRef != null) {
            addUserListener?.let { userRef?.removeEventListener(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        if (userRef != null) {
            addUserListener?.let { userRef?.removeEventListener(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (userRef != null) {
            addUserListener?.let { userRef?.removeEventListener(it) }
        }
    }
}