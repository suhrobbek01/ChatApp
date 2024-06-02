package com.example.chatapp.utils

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.example.chatapp.MessageChatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

private const val TAG = "Extensions"
fun MessageChatActivity.handleFile(uri: Uri?) {
    if (uri != null) {
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("File is uploading, please wait...")
        progressBar.show()
        val mimeType = contentResolver.getType(uri)
        var messageType = ""

        when {
            mimeType?.startsWith("image/") == true -> {
                messageType = "sent you an image"
            }

            mimeType == "application/pdf" -> {
                var url = ""
                uploadImageToFirebaseStorage(uri) { downloadUrl, fileName ->
                    messageType = "pdf_876148732846982682"
                    if (downloadUrl != null) {
                        url = downloadUrl
                        if (fileName != null) {
                            downloadFileFromFirebase(downloadUrl, fileName) { uri ->
                                if (uri != null) {
                                    Toast.makeText(
                                        this,
                                        "Successfully downloaded from firebase",
                                        Toast.LENGTH_SHORT
                                    ).show()
//                                    openFile(this, uri, "application/pdf")
                                }
                            }
                        }
                    }
                }
            }

            mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                uploadImageToFirebaseStorage(uri) { downloadUrl, fileName ->
                    messageType = "word_184713749174987123904812"
                    if (downloadUrl != null) {
                        if (fileName != null) {
                            downloadFileFromFirebase(downloadUrl, fileName) { uri ->
                                if (uri != null) {
//                                    openFile(this, uri, contentResolver.getType(uri).toString())
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                uploadImageToFirebaseStorage(uri) { downloadUrl, fileName ->
                    messageType = "unknown_01983418324098"
                }
            }
        }

        uploadImageToFirebaseStorage(uri) { downloadUrl, key ->
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            uri.let {
                uploadImageToFirebaseStorage(it) { downloadUrl, key ->
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = this.firebaseUser?.uid
                    messageHashMap["message"] = messageType
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = downloadUrl
                    Log.d(
                        TAG,
                        "onActivityResult: Url of image #############################${downloadUrl}"
                    )
                    messageHashMap["messageId"] = messageId
                    ref.child("Chats").child(messageId ?: "").setValue(messageHashMap)
                    progressBar.dismiss()
                }
            }
        }
    }
}

fun MessageChatActivity.openFile(context: Context, uri: Uri, mimeType: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, mimeType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No application found to open this file type", Toast.LENGTH_SHORT)
            .show()
    }
}

fun MessageChatActivity.downloadFileFromFirebase(
    url: String, fileName: String, callback: (Uri?) -> Unit
) {
    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
    val localFile = File.createTempFile(fileName, null)
    storageReference.getFile(localFile).addOnSuccessListener {
        callback(Uri.fromFile(localFile))
    }.addOnFailureListener { exception ->
        Log.d(TAG, "downloadFileFromFirebase: Exception is ..................... $exception")
        callback(null)
    }


}

fun MessageChatActivity.uploadImageToFirebaseStorage(
    imageUri: Uri, onComplete: (String?, String?) -> Unit
): String {
    val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
    val ref = FirebaseDatabase.getInstance().reference
    val fileNameFromUri = getFileNameFromUri(this, uri = imageUri)
    val key = ref.push().key
    val uploadTask = fileNameFromUri?.let { storageReference.child(it).putFile(imageUri) }

    uploadTask?.addOnSuccessListener { taskSnapshot ->
        storageReference.child(fileNameFromUri).downloadUrl.addOnSuccessListener { uri ->
            Log.d(TAG, "onActivityResult: File uri is $$$$$$$$$$$$$$$$$$$$$$ $uri")
            onComplete(uri.toString(), "$key.jpg")
        }.addOnFailureListener {
            Toast.makeText(
                this, "Failure on DownloadUrl in FirebaseStorage", Toast.LENGTH_SHORT
            ).show()
            onComplete(null, null)
        }
    }?.addOnFailureListener {
        Toast.makeText(
            this, "Failure on FirebaseDatabase", Toast.LENGTH_SHORT
        ).show()
        onComplete(null, null)
    }
    return "$key.jpg"
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        } finally {
            cursor?.close()
        }
    } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
        fileName = uri.lastPathSegment
    }
    return fileName
}