package com.example.bccl

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import androidx.core.view.WindowInsetsCompat
import com.example.bccl.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private var passwordShowing = false
    private val db = Firebase.firestore
    private var olduser : String? = null

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        olduser=intent.getStringExtra("realuser")
        Log.d(TAG,"olduser id is ${olduser}")
        olduser?.let {
            Log.d(TAG,"user id is ${currentUser?.uid}")
            db.collection("user").document(olduser!!)
                .get()
                .addOnSuccessListener {
                    if (it != null && it.data?.get("otpverified") == true) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }



        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth

        binding.eyepassword2.setOnClickListener {
            passwordShowing = !passwordShowing

            if (passwordShowing) {
                binding.editTextTextPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyepassword2.setImageResource(R.drawable.eye)
            } else {
                binding.editTextTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyepassword2.setImageResource(R.drawable.eye_off)
            }
            binding.editTextTextPassword.setSelection(binding.editTextTextPassword.text.length)
        }

        binding.SignUpText.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG,"user id is ${currentUser.uid}")
        }

        binding.loginbutton.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please Fill In All The Details", Toast.LENGTH_LONG).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please Enter a Valid Email", Toast.LENGTH_LONG).show()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.loginbutton.visibility = View.INVISIBLE
                binding.loginbutton.isEnabled = false

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.progressBar.visibility = View.GONE
                        binding.loginbutton.visibility = View.VISIBLE
                        binding.loginbutton.isEnabled = true

                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            val intent = Intent(this, OTPVerificationActivity::class.java)

                            val userId = auth.currentUser?.uid
                            userId?.let {
                                val ref = db.collection("user").document(it)
                                ref.get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            val phoneNumber = document.data?.get("phone").toString()

                                            val options = PhoneAuthOptions.newBuilder(auth)
                                                .setPhoneNumber("+91$phoneNumber")
                                                .setTimeout(120L, TimeUnit.SECONDS)
                                                .setActivity(this)
                                                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                        // Handle verification completion
                                                    }

                                                    override fun onVerificationFailed(e: FirebaseException) {
                                                        Toast.makeText(
                                                            this@LoginActivity,
                                                            e.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }

                                                    override fun onCodeSent(
                                                        verificationId: String,
                                                        token: PhoneAuthProvider.ForceResendingToken
                                                    ) {
                                                        intent.putExtra("verificationId", verificationId)
                                                        intent.putExtra("emailPasswordUserId", userId)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                    override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                                                        // Handle code auto-retrieval timeout
                                                    }
                                                })
                                                .build()

                                            PhoneAuthProvider.verifyPhoneNumber(options)
                                        }
                                    }
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Invalid Credentials.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            /*val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()*/
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}