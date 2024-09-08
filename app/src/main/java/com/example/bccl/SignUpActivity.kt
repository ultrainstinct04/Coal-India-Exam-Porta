package com.example.bccl

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bccl.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {
    private val binding : ActivitySignUpBinding by lazy{
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore

    private var passwordShowing = false
    private var passwordShowing2 = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.eyepassword.setOnClickListener{
            if(passwordShowing){
                passwordShowing=false

                binding.editTextTextPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                binding.eyepassword.setImageResource(R.drawable.eye_off)

            }
            else{
                passwordShowing=true
                binding.editTextTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                binding.eyepassword.setImageResource(R.drawable.eye)
            }

        }



        binding.eyerepassword.setOnClickListener{
            if(passwordShowing2){
                passwordShowing2=false

                binding.editTextTextPassword2.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                binding.eyerepassword.setImageResource(R.drawable.eye_off)

            }
            else{
                passwordShowing2=true
                binding.editTextTextPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                binding.eyerepassword.setImageResource(R.drawable.eye)
            }

        }

        auth = Firebase.auth

        binding.signupbutton.setOnClickListener {

        val name = binding.Name.text.toString()
        val email = binding.editTextTextEmailAddress.text.toString()
        val password = binding.editTextTextPassword.text.toString()
        val password2 = binding.editTextTextPassword2.text.toString()
        val phone = binding.Phone.text.toString()
        val otpverified = false

        if(name.isEmpty()||email.isEmpty()||password.isEmpty()||password2.isEmpty()||phone.isEmpty())
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
        else if(!email.contains("@")||!email.contains("."))
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        else if(phone.length!=10)
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
        else if(password.length<6)
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        else if(password!=password2)
            Toast.makeText(this, "Please re-enter the same Password", Toast.LENGTH_SHORT).show()
        else if(!binding.checkBox.isChecked)
            Toast.makeText(this, "Please tick the checkbox", Toast.LENGTH_SHORT).show()
        else {
            binding.progressBar.visibility = View.VISIBLE
            binding.signupbutton.visibility = INVISIBLE
            binding.signupbutton.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.signupbutton.visibility = View.VISIBLE
                    binding.signupbutton.isEnabled = true
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val uid = auth.currentUser?.uid
                        val usermap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "password" to password,
                            "otpverified" to otpverified,
                            "uid" to uid
                        )







                        Log.d(TAG, "createUserWithEmail:success")
                        val userId = auth.currentUser!!.uid

                        db.collection("user").document(userId).set(usermap)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot successfully written!")
                                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Log.w(TAG, "Error writing document", it)
                                Toast.makeText(this, "User Registration Failed.Try again later.", Toast.LENGTH_SHORT).show()
                            }

                        //updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Email already exists",
                            Toast.LENGTH_SHORT,
                        ).show()
                        //updateUI(null)
                    }
                }




            }
        }
        binding.LoginText.setOnClickListener {



            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            //reload()
//        }
//    }
}