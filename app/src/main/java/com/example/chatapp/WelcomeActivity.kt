package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.chatapp.databinding.ActivityWelcomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    var firebaseUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""
    private lateinit var googleSignInClient: GoogleSignInClient
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.apply {
            signInWithGoogleCard.setOnClickListener {
                googleSignIn()
            }
            signInWithPhoneNumberCard.setOnClickListener {
                val intent = Intent(this@WelcomeActivity, InputPhoneActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 9001)
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Please wait, the process is in progress")
        progressDialog?.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: Exception) {
                progressDialog?.dismiss()
                Toast.makeText(this, "89 " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                firebaseUserId = auth.currentUser?.uid.toString()
                refUsers =
                    FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)

                val userHashMap = HashMap<String, Any>()
                userHashMap["uid"] = firebaseUserId
                userHashMap["username"] = user?.displayName.toString()
                userHashMap["status"] = "offline"
                userHashMap["profile"] = user?.photoUrl.toString()
                userHashMap["cover"] =
                    "https://firebasestorage.googleapis.com/v0/b/chatapp-256fd.appspot.com/o/Chat%20Images%2Fcover.jpg?alt=media&token=0e071eb0-0170-414a-91a3-f68b21c7f842"

                refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        progressDialog?.dismiss()
                    } else {
                        progressDialog?.dismiss()
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                progressDialog?.dismiss()
                Toast.makeText(this, "133" + task.exception?.message, Toast.LENGTH_SHORT).show()
                Log.w("MainActivity", "signInWithCredential:failure", task.exception)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "User is null", Toast.LENGTH_SHORT).show()
        }
    }
}