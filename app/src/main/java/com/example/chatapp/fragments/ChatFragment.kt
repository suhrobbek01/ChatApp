package com.example.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.model_classes.ChatList
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private var userAdapter: UserAdapter? = null
    private var mUsers: ArrayList<Users>? = null
    private var userChatList: List<ChatList>? = null
    private var firebaseUser: FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.recyclerViewChatList.setHasFixedSize(true)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userChatList = ArrayList()
        val ref =
            firebaseUser?.uid?.let {
                FirebaseDatabase.getInstance().reference.child("ChatList").child(
                    it
                )
            }
        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userChatList as ArrayList).clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val chatList = dataSnapshot.getValue(ChatList::class.java)
                        if (chatList != null) {
                            (userChatList as ArrayList).add(chatList)
                        }
                    }
                    retrieveCHatList()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return binding.root
    }

    private fun retrieveCHatList() {
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("users")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(Users::class.java)
                        if (userChatList != null) {
                            for (eachChatList in userChatList!!) {
                                if (user?.getUID()?.equals(eachChatList.getId()) == true) {
                                    (mUsers as ArrayList).add(user)
                                }
                            }
                        }
                    }
                    userAdapter =
                        context?.let { UserAdapter(it, (mUsers as ArrayList<Users>), true) }
                    binding.recyclerViewChatList.adapter = userAdapter
                } else {
                    Toast.makeText(context, "Snapshot not exists", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}