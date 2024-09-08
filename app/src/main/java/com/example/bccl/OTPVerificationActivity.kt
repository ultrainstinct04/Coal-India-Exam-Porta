package com.example.bccl

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bccl.databinding.ActivityOtpverificationBinding
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OTPVerificationActivity : AppCompatActivity() {
    private val binding: ActivityOtpverificationBinding by lazy {
        ActivityOtpverificationBinding.inflate(layoutInflater)
    }

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var realUser: FirebaseUser? = null
    private var hero: FirebaseUser? = null

    private var email: String? = null
    private var password: String? = null
    private var emailCredential: AuthCredential? = null
    private var otp: String? = null
    private var otpBackend : String? = null

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() method")
        hero = auth.currentUser
        hero?.let {
            fetchUserCredentials(it.uid)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fetchPhoneNumber()

         otpBackend = intent.getStringExtra("verificationId")

        binding.verifybutton.setOnClickListener {
            Log.d(TAG, "User ID :${auth.currentUser?.uid}")
            realUser = auth.currentUser
            verifyOtp(otpBackend)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupOtpInputs()
    }

    private fun fetchPhoneNumber() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val ref = db.collection("user").document(uid)
            ref.get()
                .addOnSuccessListener {
                    it?.data?.get("phone")?.toString()?.let { phoneNumber ->
                        binding.phnnumber.text = String.format("+91-%s", phoneNumber)
                        Toast.makeText(this, "Phone Number Fetched Successfully", Toast.LENGTH_SHORT).show()
                        Log.d("Firebase", "DocumentSnapshot data: ${it.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firebase", "Error fetching phone number", exception)
                }
        }
    }

    private fun fetchUserCredentials(userId: String) {
        db.collection("user").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "Email and password fetched from database")
                    email = document.data?.get("email")?.toString()
                    password = document.data?.get("password")?.toString()
                    emailCredential = email?.let { email ->
                        password?.let { password ->
                            EmailAuthProvider.getCredential(email, password)
                        }
                    }
                    Log.d(TAG, "emailCredential ready")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user credentials", e)
            }
    }

    private fun verifyOtp(otpBackend: String?) {
         otp = binding.editTextText1.text.toString() +
                binding.editTextText2.text.toString() +
                binding.editTextText3.text.toString() +
                binding.editTextText4.text.toString() +
                binding.editTextText5.text.toString() +
                binding.editTextText6.text.toString()

        if (otp!!.length != 6) {
            Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            return
        }

        if (otpBackend == null) {
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.verifybutton.visibility = View.GONE



        val credential = PhoneAuthProvider.getCredential(otpBackend, otp!!)
        signInWithPhoneAuthCredential(credential)
    }



    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.verifybutton.visibility = View.VISIBLE

                if (task.isSuccessful) {

                    binding.progressBar.visibility = View.GONE
                    binding.verifybutton.visibility = View.VISIBLE

                    Log.d(TAG, "signInWithCredential:success, user id: ${auth.currentUser?.uid}")

                    val emailPasswordUserId = intent.getStringExtra("emailPasswordUserId")

                    if (emailPasswordUserId != null) {
                        val db = Firebase.firestore
                        val user = db.collection("user").document(emailPasswordUserId)
                        user.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val userId = documentSnapshot.getString("uid")
                                    if (userId != null) {
                                        if (userId != auth.currentUser?.uid) {
                                            Log.d(
                                                TAG,
                                                "The user ids are different. Proceeding to link them"
                                            )
                                            signInAndUnlinkPhoneNumber(credential)
                                        } else {
                                            Log.d(TAG, "Already Linked")
                                            markOTPVerification(userId)
                                            navigateToMainActivity()
                                        }
                                    } else {
                                        Log.w(TAG, "Missing user ID in Firestore document")
                                    }
                                } else {
                                    Log.w(TAG, "User document not found for $emailPasswordUserId")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error fetching user document", exception)
                            }
                    } else {
                        Log.e(TAG, "Missing emailPasswordUserId from intent extra")
                    }
                } else {
                    //handleSignInFailure(task.exception)
                }
            }
    }


    private fun linkPhoneNumberToCurrentUser(credential: PhoneAuthCredential) {
        Log.d(TAG,"Entered linkPhoneNumberToCurrentUser")
        realUser?.linkWithCredential(credential)
            ?.addOnCompleteListener(this) { linkTask ->
                if (linkTask.isSuccessful) {
                    Log.d(TAG, "Phone credential linked successfully")
                    markOTPVerification(realUser?.uid ?: "")
                    navigateToMainActivity()
                } else {
                    if (linkTask.exception is FirebaseAuthUserCollisionException) {
                        Log.d(TAG,"Inside FirebaseAuthUserCollisionException")
                        val collisionException = linkTask.exception as FirebaseAuthUserCollisionException
                        collisionException.updatedCredential?.let { updatedCredential ->
                            Log.d(TAG,"Proceeding to unlink the phone number to its user id")
                            //signInAndUnlinkPhoneNumber(updatedCredential as PhoneAuthCredential, credential)
                        }
                    }

                    else {
                        handleSignInFailure(linkTask.exception)
                    }
                }
            }
    }

    private fun signInAndUnlinkPhoneNumber(existingCredential: PhoneAuthCredential) {
        Log.d(TAG,"Entered signInAndUnlinkPhoneNumber")
        auth.signInWithCredential(existingCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.unlink(PhoneAuthProvider.PROVIDER_ID)
                        ?.addOnCompleteListener { unlinkTask ->
                            if (unlinkTask.isSuccessful) {
                                Log.d(TAG, "Phone number unlinked successfully.Proceeding to link with desired uid")
                                linkPhoneNumberToCurrentUser(existingCredential)
                            } else {
                                Log.e(TAG, "Error unlinking phone number", unlinkTask.exception)
                            }
                        }
                } else {
                    Log.e(TAG, "Error signing in with existing credential", task.exception)
                }
            }
    }

    private fun handleSignInFailure(exception: Exception?) {
        Log.w(TAG, "signInWithCredential:failure", exception)
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid OTP.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Log-in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        Toast.makeText(this, "OTP Verification Successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("olduserid", hero?.uid)
        startActivity(intent)
        finish()
    }

    private fun markOTPVerification(uid: String) {
        db.collection("user").document(uid)
            .update("otpverified", true)
            .addOnSuccessListener {
                Log.d(TAG, "uid is $uid")
                Log.d(TAG, "OTP Verification marked in database successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error marking OTP Verification", e)
                Toast.makeText(this, "Failed to mark OTP Verification", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupOtpInputs() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    when (currentFocus?.id) {
                        R.id.editTextText1 -> binding.editTextText2.requestFocus()
                        R.id.editTextText2 -> binding.editTextText3.requestFocus()
                        R.id.editTextText3 -> binding.editTextText4.requestFocus()
                        R.id.editTextText4 -> binding.editTextText5.requestFocus()
                        R.id.editTextText5 -> binding.editTextText6.requestFocus()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editTextText1.addTextChangedListener(textWatcher)
        binding.editTextText2.addTextChangedListener(textWatcher)
        binding.editTextText3.addTextChangedListener(textWatcher)
        binding.editTextText4.addTextChangedListener(textWatcher)
        binding.editTextText5.addTextChangedListener(textWatcher)
        binding.editTextText6.addTextChangedListener(textWatcher)
    }
}