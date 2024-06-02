package com.example.chatapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.WelcomeActivity
import com.example.chatapp.databinding.FragmentSettingsBinding
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private var storageRef: StorageReference? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.apply {
            signOutBtn.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(
                it
            )
        }
        storageRef = FirebaseStorage.getInstance().reference.child("User images")
        userReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var user: Users? = snapshot.getValue(Users::class.java)
                    if (context != null) {
                        binding.usernameSettings.text = user?.getUsername()
                        Picasso.get().load(user?.getProfile()).into(binding.profileImage)
                        Picasso.get().load(user?.getCover()).into(binding.coverImage)
                    }
                } else {
                    Toast.makeText(
                        context, "Snapshot not exist", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return binding.root
    }
}