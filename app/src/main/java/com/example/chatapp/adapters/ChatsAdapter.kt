package com.example.chatapp.adapters

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.model_classes.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.lang.IllegalArgumentException

class ChatsAdapter(
    mContext: Context, mChatList: List<Chat>, imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.Vh>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var progressBar: ProgressDialog? = null

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }

    inner class Vh(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileImage: CircleImageView? = null
        var showTextMessage: TextView? = null
        var leftImageView: ImageView? = null
        var textSeen: TextView? = null
        var rightImageView: ImageView? = null
        var layout1: RelativeLayout? = null
        var layout2: RelativeLayout? = null
        var fileName: TextView? = null
        var fileSize: TextView? = null

        init {
            profileImage = itemView.findViewById(R.id.profile_image)
            showTextMessage = itemView.findViewById(R.id.show_text_message)
            leftImageView = itemView.findViewById(R.id.left_image_view)
            textSeen = itemView.findViewById(R.id.text_seen)
            rightImageView = itemView.findViewById(R.id.right_image_view)
            layout1 = itemView.findViewById(R.id.layout1)
            layout2 = itemView.findViewById(R.id.layout2)
            fileName = itemView.findViewById(R.id.name)
            fileSize = itemView.findViewById(R.id.b_type)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return if (viewType == 1) {
            Vh(LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false))
        } else {
            Vh(LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false))
        }
    }

    override fun getItemCount(): Int = mChatList.size

    override fun onBindViewHolder(holder: Vh, position: Int) {
        val chat: Chat = mChatList[position]
        try {
            if (imageUrl != null) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_profile)
                    .into(holder.profileImage)
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(mContext, "Exception is ${e.message}", Toast.LENGTH_SHORT).show()
        }

        //images Message
        val message = chat.getMessage()
        if (message != "") {
            when (message) {
                "sent you an image" -> {
                    holder.layout1?.visibility = View.VISIBLE
                    holder.layout2?.visibility = View.GONE
                    if (chat.getSender().equals(firebaseUser?.uid)) {
                        holder.showTextMessage?.visibility = View.GONE
                        holder.rightImageView?.visibility = View.VISIBLE
                        Picasso.get().load(chat.getUrl()).placeholder(R.drawable.placeholder)
                            .into(holder.rightImageView)
                    }
                    //image message left side
                    else if (!chat.getSender().equals(firebaseUser?.uid)) {
                        holder.showTextMessage?.visibility = View.GONE
                        holder.leftImageView?.visibility = View.VISIBLE
                        Picasso.get().load(chat.getUrl()).placeholder(R.drawable.placeholder)
                            .into(holder.leftImageView)
                    }
                }

                "pdf_876148732846982682" -> {
                    holder.layout2?.visibility = View.VISIBLE
                    holder.layout1?.visibility = View.GONE
                    if (chat.getUrl() != null) {
                        var storageReference =
                            FirebaseStorage.getInstance().getReferenceFromUrl(chat.getUrl()!!)
                        storageReference.metadata.addOnSuccessListener { metadata ->
                            val name = metadata.name
                            val sizeBytes = metadata.sizeBytes
                            holder.fileName?.text = name
                            holder.fileSize?.text = sizeBytes.toString()
                        }
                    }
                    holder.itemView.setOnClickListener {
                        if (chat.getUrl() != null) {
                            downloadAndOpenFile(chat.getUrl()!!)
                        }
                    }
                }

                "word_184713749174987123904812" -> {
                    holder.layout2?.visibility = View.VISIBLE
                    holder.layout1?.visibility = View.GONE
                    if (chat.getUrl() != null) {
                        var storageReference =
                            FirebaseStorage.getInstance().getReferenceFromUrl(chat.getUrl()!!)
                        storageReference.metadata.addOnSuccessListener { metadata ->
                            val name = metadata.name
                            val sizeBytes = metadata.sizeBytes
                            holder.fileName?.text = name
                            holder.fileSize?.text = sizeBytes.div(1000).toString() + ".0 KB"
                        }
                    }
                    holder.itemView.setOnClickListener {
                        if (chat.getUrl() != null) {
                            downloadAndOpenFile(chat.getUrl()!!)
                        }
                    }
                }

                "unknown_01983418324098" -> {

                }

                else -> {
                    holder.layout1?.visibility = View.VISIBLE
                    holder.layout2?.visibility = View.GONE
                    holder.showTextMessage?.text = chat.getMessage()
                }
            }
        }

        if (position == mChatList.size - 1) {
            if (chat.getIsSeen()) {
                holder.textSeen?.text = "Seen"

                if (chat.getMessage().equals("sent you an image") && !chat.getUrl().equals("")) {
                    val lp: RelativeLayout.LayoutParams? =
                        holder.textSeen?.layoutParams as RelativeLayout.LayoutParams
                    lp?.setMargins(0, 245, 10, 0)
                    holder.textSeen?.layoutParams = lp
                }
            } else {
                holder.textSeen?.text = "Sent"

                if (chat.getMessage().equals("sent you an image") && !chat.getUrl().equals("")) {
                    val lp: RelativeLayout.LayoutParams? =
                        holder.textSeen?.layoutParams as RelativeLayout.LayoutParams
                    lp?.setMargins(0, 245, 10, 0)
                    holder.textSeen?.layoutParams = lp
                }
            }
        } else {
            holder.textSeen?.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender().equals(firebaseUser?.uid)) {
            1
        } else {
            0
        }
    }

    private fun downloadAndOpenFile(fileUrl: String) {
        progressBar = ProgressDialog(mContext)
        progressBar?.setMessage("File is opening, please wait...")
        progressBar?.show()
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
        val localFile = File.createTempFile("tempFile", null, mContext.cacheDir)
        storageReference.getFile(localFile).addOnSuccessListener {
            openFile(mContext, localFile)
        }.addOnFailureListener { exception ->
            Toast.makeText(
                mContext, "File download failed: ${exception.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openFile(context: Context, file: File) {
        val uri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mimeType = when (file.extension) {
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "*/*"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
            if (progressBar != null) {
                progressBar?.dismiss()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, "No application found to open this file type", Toast.LENGTH_SHORT
            ).show()
        }
    }
}