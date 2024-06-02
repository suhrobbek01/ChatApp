package com.example.chatapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MessageChatActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.UserSearchItemLayoutBinding
import com.example.chatapp.model_classes.Chat
import com.example.chatapp.model_classes.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(
    mContext: Context, mUsers: List<Users>, isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.Vh>() {

    private val mContext: Context
    private val mUsers: List<Users>
    private val isChatCheck: Boolean
    var lastMsg: String = ""

    init {
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
        this.mContext = mContext
    }

    inner class Vh(var itemUserBinding: UserSearchItemLayoutBinding) :
        RecyclerView.ViewHolder(itemUserBinding.root) {

        fun onBind(user: Users) {
            itemUserBinding.apply {
                itemUserBinding.username.text = user.getUsername()
                Picasso.get().load(user.getProfile())
                    .placeholder(R.drawable.ic_profile).into(itemUserBinding.profileImage)
                if (isChatCheck) {
                    retrieveLastMessage(user.getUID(), itemUserBinding.messageLast)
                } else {
                    itemUserBinding.messageLast.visibility = View.GONE
                }
                if (isChatCheck) {
                    if (user.getStatus() == "online") {
                        itemUserBinding.imageOnline.visibility = View.VISIBLE
                        itemUserBinding.imageOffline.visibility = View.GONE
                    } else {
                        itemUserBinding.imageOnline.visibility = View.GONE
                        itemUserBinding.imageOffline.visibility = View.VISIBLE
                    }
                } else {
                    itemUserBinding.imageOnline.visibility = View.GONE
                    itemUserBinding.imageOffline.visibility = View.GONE
                }

                if ((user.getUnreadMessageCount() > 0)) {
                    unreadMessageCard.visibility = View.VISIBLE
                    countUnreadMessages.text = user.getUnreadMessageCount().toString()
                } else {
                    unreadMessageCard.visibility = View.GONE
                }
            }
            itemView.setOnClickListener {
                val intent = Intent(mContext, MessageChatActivity::class.java)
                intent.putExtra("visit_id", user.getUID())
                intent.putExtra("user", user)
                mContext.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(
            UserSearchItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = mUsers.size

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(mUsers[position])
    }

    private fun retrieveLastMessage(chatUserId: String?, lastMessageText: TextView) {
        lastMsg = "defaultMsg"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                        if ((firebaseUser != null && chat != null)) {
                            if (chat.getReceiver() == firebaseUser.uid && chat.getSender() == chatUserId || chat.getReceiver() == chatUserId && chat.getSender() == firebaseUser.uid) {
                                if ((chat.getMessage() != null)) {
                                    lastMsg = chat.getMessage()!!
                                }
                            }
                        }
                    }
                }
                when (lastMsg) {
                    "defaultMsg" -> {
                        lastMessageText.text = mContext.getString(R.string.no_message)
                    }

                    "sent you an image" -> {
                        lastMessageText.text = mContext.getString(R.string.image_sent)
                    }

                    "pdf_876148732846982682" -> {
                        lastMessageText.text = mContext.getString(R.string.file_sent)
                    }

                    "word_184713749174987123904812" -> {
                        lastMessageText.text = mContext.getString(R.string.file_sent)
                    }

                    else -> {
                        lastMessageText.text = lastMsg
                    }
                }
                lastMsg = "defaultMsg"
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}