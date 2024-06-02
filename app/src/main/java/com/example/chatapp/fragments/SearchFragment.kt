package com.example.chatapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.FragmentSearchBinding
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: ArrayList<Users>? = null
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        mUsers = ArrayList()

        binding.searchList.setHasFixedSize(true)
        retrieveAllUsers()

        binding.searchUserSet.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim() != "") {
                    searchForUsers(p0.toString().trim())
                } else {
                    (mUsers as ArrayList<Users>).clear()
                    userAdapter?.notifyItemRangeRemoved(0, (mUsers as ArrayList<Users>).size)
                }
            }
        })
        return binding.root
    }

    private fun retrieveAllUsers() {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
        val refUsers = firebaseUserId?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(
                it
            )
        }

        refUsers?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                if (binding.searchUserSet.text.toString() == "") {
                    for (child in snapshot.children) {
                        val user: Users = snapshot.getValue(Users::class.java) as Users
                        if (!user.getUID().equals(firebaseUserId)) {
                            if (user != null) {
                                (mUsers as ArrayList<Users>).add(user)
                            }
                        }
                    }
                }
                userAdapter = context?.let { UserAdapter(it, mUsers ?: emptyList(), false) }
                binding.searchList.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun searchForUsers(str: String) {
        var firebaseUserId = FirebaseAuth.getInstance().currentUser?.uid
        val queryUsers =
            FirebaseDatabase.getInstance().reference.child("users").orderByChild("username")
                .startAt(str).endAt(str + "\uf8ff")
        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (mUsers as ArrayList<Users>).clear()
                    for (child in snapshot.children) {
                        val user: Users? = child.getValue(Users::class.java)
                        if (!user?.getUID().equals(firebaseUserId)) {
                            if (user != null) {
                                (mUsers as ArrayList<Users>).add(user)
                            }
                        }
                    }
                    userAdapter =
                        context?.let { UserAdapter(it, mUsers as ArrayList<Users>, false) }
                    binding.searchList.adapter = userAdapter
                } else {
                    Toast.makeText(
                        context,
                        "${snapshot.hasChildren()}   Snapshot not exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}