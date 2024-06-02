package com.example.chatapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.chatapp.databinding.ActivityInputPhoneBinding

class InputPhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputPhoneBinding
    private var phoneBoolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#5A73F3")
        }
        val toolBar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolBar)
        supportActionBar?.title = "Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar?.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.apply {
            ccp.setCcpClickable(false)
            ccp.registerCarrierNumberEditText(number)
            ccp.setPhoneNumberValidityChangeListener {
                phoneBoolean = it
            }
            number.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    if (phoneBoolean) {

                    } else {

                    }
                }
            })
        }
        binding.apply {
            getCode.setOnClickListener {
                if (phoneBoolean) {
                    val fullNumber = ccp.fullNumberWithPlus
                    val intent = Intent(this@InputPhoneActivity, VerifyCodeActivity::class.java)
                    intent.putExtra("phoneNumber", fullNumber)
                    startActivity(intent)
//                    sendVerificationCode(fullNumber)
                } else {
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        super.onBackPressed()
    }
}