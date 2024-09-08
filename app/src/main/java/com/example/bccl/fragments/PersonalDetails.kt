package com.example.bccl.fragments

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.bccl.LoginActivity
import com.example.bccl.MainActivity
import com.example.bccl.R
import com.example.bccl.databinding.FragmentPersonalDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar



class PersonalDetails : Fragment() {
    private lateinit var binding: FragmentPersonalDetailsBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var datePickerDialog: DatePickerDialog
    private var oldUserId: String? = null

    override fun onStart() {
        super.onStart()
        oldUserId = Firebase.auth.currentUser?.uid
        oldUserId?.let { id ->
            db.collection("user").document(id)
                .get()
                .addOnSuccessListener { document ->
                    document?.data?.let { data ->
                        binding.editTextTextEmailAddress.text = data["email"].toString()
                        binding.Phone.text = String.format("+91-%s", data["phone"].toString())
                        Log.d("Firebase", "email: ${data["email"]}")
                        Log.d("Firebase", "phone: ${data["phone"]}")
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Error fetching document")
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        prefill()
        inputvalidate()
        setupListeners()
        return binding.root
    }

    private fun prefill() {
        oldUserId = Firebase.auth.currentUser?.uid
        oldUserId?.let { id ->
            db.collection("user").document(id)
                .get()
                .addOnSuccessListener { document ->
                    document?.data?.let { data ->
                        if(data["firstName"] != null)
                        binding.fnametext.setText(data["firstName"].toString())
                        if(data["middleName"] != null)
                        binding.mnametext.setText(data["middleName"].toString())
                        if(data["lastName"] != null)
                        binding.lnametext.setText(data["lastName"].toString())
                        if(data["fatherFirstName"] != null)
                        binding.fatherfnametext.setText(data["fatherFirstName"].toString())
                        if(data["fatherMiddleName"] != null)
                        binding.fathermnametext.setText(data["fatherMiddleName"].toString())
                        if(data["fatherLastName"] != null)
                        binding.fatherlnametext.setText(data["fatherLastName"].toString())
                        if(data["dob"] != null)
                        binding.dobbutton.text = data["dob"].toString()
                        if(data["governmentId"] != null)
                        binding.Idno.setText(data["governmentId"].toString())


                    }
                }
        }
    }

    private fun inputvalidate() {
        binding.fnametext.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutFname.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutFname.error = "cannot be empty"
            } else
                binding.textInputLayoutFname.error = null
        }



        binding.lnametext.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutLname.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutLname.error = "cannot be empty"
            } else
                binding.textInputLayoutLname.error = null
        }

        binding.fatherfnametext.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutFatherfname.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutFatherfname.error = "cannot be empty"
            } else
                binding.textInputLayoutFatherfname.error = null
        }



        binding.fatherlnametext.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutFatherlname.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutFatherlname.error = "cannot be empty"
            } else
                binding.textInputLayoutFatherlname.error = null
        }

        binding.Idno.doOnTextChanged { text, start, before, count ->
            binding.govtId.helperText = null
            if (text.toString().isEmpty()) {
                binding.govtId.error = "cannot be empty"
            } else
                binding.govtId.error = null
        }
    }

    private fun setupListeners() {
        binding.signoutbutton.setOnClickListener {
            Log.d(TAG, "Sign-out button clicked")
            oldUserId?.let { id ->
                db.collection("user").document(id)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            Log.d(TAG, "Document exists for user ID: $id")
                            db.collection("user").document(id)
                                .update("otpverified", false)
                                .addOnSuccessListener {
                                    Log.d(TAG, "otpverified updated to false for user ID: $id")
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error updating document for user ID: $id", e)
                                }
                        } else {
                            Log.d(TAG, "Document does not exist for user ID: $id")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error fetching document for user ID: $id", e)
                    }
            }

            auth.signOut()
            Log.d(TAG, "Sign Out Successful")
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra("realuser", oldUserId)
            startActivity(intent)
        }

        initDatePicker()
        binding.dobbutton.text = getTodaysDate()
        binding.dobbutton.setOnClickListener {
            openDatePicker(it)
        }

        binding.NextButton.setOnClickListener {
            val id = binding.radioGroupGender.checkedRadioButtonId
            if (id == -1 || binding.Idno.text.toString()
                    .isEmpty() || binding.fnametext.text.toString().isEmpty() ||
                 binding.lnametext.text.toString()
                    .isEmpty() ||
                binding.fatherfnametext.text.toString()
                    .isEmpty() ||
                binding.fatherlnametext.text.toString().isEmpty()
            ) {
                Toast.makeText(context, "Please fill in all the input fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Handle next button action
                val userUpdates = hashMapOf(
                    "firstName" to binding.fnametext.text.toString(),
                    "middleName" to binding.mnametext.text.toString(),
                    "lastName" to binding.lnametext.text.toString(),
                    "fatherFirstName" to binding.fatherfnametext.text.toString(),
                    "fatherMiddleName" to binding.fathermnametext.text.toString(),
                    "fatherLastName" to binding.fatherlnametext.text.toString(),
                    "dob" to binding.dobbutton.text.toString(),
                    "category" to binding.spinnerCategory.selectedItem.toString(),
                    "gender" to when (id) {
                        R.id.radioButtonMale -> "Male"
                        R.id.radioButtonFemale -> "Female"
                        R.id.radioButtonOther -> "Other"
                        else -> ""
                    },
                    "governmentId" to binding.Idno.text.toString()
                )

                oldUserId?.let { userId ->
                    db.collection("user").document(userId)
                        .update(userUpdates as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Details saved successfully", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction().apply {
                                replace(R.id.frame, EducationDetails())
                                addToBackStack(null)
                                commit()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error saving details: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "Error saving document for user ID: $userId", e)
                        }
                }
                /*parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frame, EducationDetails())
                    addToBackStack(null)
                    commit()*/
            }
        }

        val categories = arrayOf("General", "OBC", "SC", "ST", "EWS")
        binding.spinnerCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle item selection
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }



    }

    private fun getTodaysDate(): CharSequence {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$day/${month + 1}/$year"
    }

    private fun initDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            binding.dobbutton.text = selectedDate
        }
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    }

    private fun openDatePicker(view: View) {
        datePickerDialog.show()
    }
}
