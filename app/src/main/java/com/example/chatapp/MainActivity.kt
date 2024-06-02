package com.example.chatapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.chatapp.R.id.toolbar_main
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.fragments.ChatFragment
import com.example.chatapp.fragments.SearchFragment
import com.example.chatapp.fragments.SettingsFragment
import com.example.chatapp.fragments.UsersFragment
import com.example.chatapp.model_classes.Chat
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    var refChats: DatabaseReference? = null
    var addChatListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#5A73F3")
        }
        val toolBar: androidx.appcompat.widget.Toolbar? = findViewById(toolbar_main)
        setSupportActionBar(toolBar)
        supportActionBar?.title = ""

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(
                it
            )
        }

        refChats = FirebaseDatabase.getInstance().reference.child("Chats")
        addChatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat?.getReceiver().equals(firebaseUser?.uid) && !chat?.getIsSeen()!!) {
                        countUnreadMessages += 1
                    }
                }
                viewPagerAdapter.addFragment(UsersFragment(), "All users")
                if (countUnreadMessages == 0) {
                    viewPagerAdapter.addFragment(ChatFragment(), "Chats")
                } else {
                    viewPagerAdapter.addFragment(ChatFragment(), "$countUnreadMessages Chats")
                }
                viewPagerAdapter.addFragment(SearchFragment(), "Search")
                viewPagerAdapter.addFragment(SettingsFragment(), "Settings")
                binding.viewPager.adapter = viewPagerAdapter
                binding.tabLayout.setupWithViewPager(binding.viewPager)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        if (addChatListener != null) {
            refChats?.addValueEventListener(addChatListener!!)
        }

        //display username and picture
        refUsers?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    binding.username.text = user?.getUsername()
                    Picasso.get().load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                        .placeholder(R.drawable.ic_profile).into(binding.profileImage)
                } else {
                    Toast.makeText(this@MainActivity, "Snapshot not exist", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }


        override fun getCount(): Int = fragments.size
        override fun getItem(position: Int): Fragment = fragments[position]

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }

    private fun updateStatus(status: String) {
        val ref = firebaseUser?.uid?.let {
            FirebaseDatabase.getInstance().reference.child("users").child(
                it
            )
        }
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        ref?.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onDestroy() {
        super.onDestroy()
        updateStatus("offline")
    }
}