package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatapp.databinding.ActivityVerifyCodeBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class VerifyCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyCodeBinding
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@VerifyCodeActivity)
        progressDialog?.setMessage("Please wait, the process is in progress")
        progressDialog?.show()

        mAuth = FirebaseAuth.getInstance()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        if (!phoneNumber.isNullOrEmpty()) {
            sendVerificationCode(phoneNumber)
        }
        binding.apply {
            verifyCode.setOnClickListener {
                val code = code.text.toString().trim()
                if ((code != "")) {
                    verifyGivenCode(code)
                } else {
                    Toast.makeText(this@VerifyCodeActivity, "Kodni kiriting", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        mAuth = FirebaseAuth.getInstance()
    }

    private fun verifyGivenCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val phoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).build()
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(p0)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            progressDialog?.dismiss()
            Toast.makeText(this@VerifyCodeActivity, p0.message, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String, token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = task.result.user
                if (user != null) {
                    setValue(user.displayName, user.photoUrl)
                } else {
                    progressDialog?.dismiss()
                    Toast.makeText(this, "User object is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                progressDialog?.dismiss()
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setValue(displayName: String?, photoUrl: Uri?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val profileUpdates =
            UserProfileChangeRequest.Builder().setDisplayName(displayName).setPhotoUri(photoUrl)
                .build()

        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseUserId = mAuth.currentUser?.uid.toString()
                refUsers =
                    FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)

                val userHashMap = HashMap<String, Any>()
                userHashMap["uid"] = firebaseUserId
                userHashMap["username"] = firebaseUser.displayName.toString()
                userHashMap["status"] = "offline"
                userHashMap["profile"] = firebaseUser.photoUrl.toString()
                userHashMap["cover"] =
                    "https://firebasestorage.googleapis.com/v0/b/companyexcangedocument.appspot.com/o/company.jpg?alt=media&token=66707636-0bc5-4bbe-a545-b400f265d02d"

                refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                    progressDialog?.dismiss()
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                progressDialog?.dismiss()
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}