package com.example.chatapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chatapp.adapters.ChatsAdapter
import com.example.chatapp.databinding.ActivityMessageChatBinding
import com.example.chatapp.model_classes.Chat
import com.example.chatapp.model_classes.Users
import com.example.chatapp.utils.handleFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MessageChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageChatBinding
    var userIdVisit: String = ""
    var user: Users? = null
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var seenListener: ValueEventListener
    var reference: DatabaseReference? = null
    var chatsReference: DatabaseReference? = null
    private val TAG = "MessageChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMessageChat)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMessageChat.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        user = intent.getSerializableExtra("user") as Users
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.usernameMchat.text = user?.getUsername()
            Picasso.get().load(user?.getProfile()).placeholder(R.drawable.ic_profile)
                .into(binding.profileImageMessageChat)
        }

        reference = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit)
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                retrieveMessages(firebaseUser?.uid, userIdVisit, user?.getProfile())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        binding.apply {
            sendMessageBtn.setOnClickListener {
                val message = textMessage.text.toString().trim()
                if (message == "") {
                    Toast.makeText(
                        this@MessageChatActivity, "Please write a message", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    firebaseUser?.uid?.let { it1 ->
                        sendMessageToUser(it1, userIdVisit, message)
                        textMessage.setText("")
                    }
                }
            }
            attachImageFile.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "*/*"
                intent.putExtra(
                    Intent.EXTRA_MIME_TYPES, arrayOf(
                        "image/*",
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
                startActivityForResult(Intent.createChooser(intent, "Pick image"), 2)
            }
        }
        seenMessage(userIdVisit)
    }

    private fun retrieveMessages(senderId: String?, receiverId: String, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in snapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat?.getReceiver().equals(senderId) && chat?.getSender()
                            .equals(receiverId) || chat?.getReceiver()
                            .equals(receiverId) && chat?.getSender().equals(senderId)
                    ) {
                        if (chat != null) {
                            (mChatList as ArrayList).add(chat)
                            Log.d(TAG, "onDataChange: ${chat.getUrl()}")
                        }
                    }
                    chatsAdapter = ChatsAdapter(
                        this@MessageChatActivity,
                        (mChatList as ArrayList<Chat>),
                        receiverImageUrl.toString()
                    )
                    binding.recyclerViewChats.adapter = chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats").child(messageKey ?: "").setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatsListReference = firebaseUser?.uid?.let {
                        FirebaseDatabase.getInstance().reference.child("ChatList").child(it)
                            .child(userIdVisit)
                    }

                    chatsListReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatsListReference.child("id").setValue(userIdVisit)
                            }
                            val chatsListReceiverReference = userIdVisit.let {
                                firebaseUser?.uid?.let { it1 ->
                                    FirebaseDatabase.getInstance().reference.child("ChatList")
                                        .child(it).child(it1)
                                }
                            }

                            chatsListReceiverReference?.child("id")?.setValue(firebaseUser?.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                    val reference = firebaseUser?.uid?.let {
                        FirebaseDatabase.getInstance().reference.child("Users").child(it)
                    }

                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {/*

                        val loadingBar = ProgressDialog(applicationContext)
                        loadingBar.setMessage("Please wait, image is sending...")
                        loadingBar.show()*/

            val fileUri = data.data
            handleFile(fileUri)
            /* val storageReference = getInstance().reference.child("Chat Images")
             val ref = FirebaseDatabase.getInstance().reference
             val messageId = ref.push().key
             val filePath = storageReference.child("$messageId.jpg")

             var uploadTask: StorageTask<*>
             uploadTask = filePath.putFile(fileUri!!)

             uploadTask.addOnSuccessListener {


             }
             uploadTask.addOnFailureListener {
                 Toast.makeText(this, uploadTask.exception?.message.toString(), Toast.LENGTH_SHORT)
                     .show()
             }*/
        }
    }

    /*
        private fun uploadImageToFirebaseStorage(imageUri: Uri, onComplete: (String?) -> Unit) {
            val storageReference = getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val key = ref.push().key
            val uploadTask = storageReference.child("$key.jpg").putFile(imageUri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageReference.child("$key.jpg").downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "onActivityResult: File uri is $$$$$$$$$$$$$$$$$$$$$$ $uri")
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failure on DownloadUrl in FirebaseStorage",
                        Toast.LENGTH_SHORT
                    ).show()
                    onComplete(null)
                }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failure on FirebaseDatabase",
                    Toast.LENGTH_SHORT
                ).show()
                onComplete(null)
            }
        }
    */
    private fun seenMessage(userId: String) {
        chatsReference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = chatsReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Chat::class.java)
                        if (chat?.getReceiver().equals(firebaseUser?.uid) && chat?.getSender()
                                .equals(userId)
                        ) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["isseen"] = true
                            dataSnapshot.ref.updateChildren(hashMap)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })!!
    }

    override fun onPause() {
        super.onPause()
        chatsReference?.removeEventListener(seenListener)
    }
}