package com.example.bccl.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.bccl.PdfGenerationActivity
import com.example.bccl.databinding.FragmentEducationDetailsBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class EducationDetails : Fragment() {

    private lateinit var binding: FragmentEducationDetailsBinding
    private var oldUserId: String? = null
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEducationDetailsBinding.inflate(inflater, container, false)

        prefill()
        inputvalidate()
        setupEducationSectionListeners()

        return binding.root
    }

    private fun prefill() {
        val oldUserId = Firebase.auth.currentUser?.uid
        oldUserId?.let { id ->
            db.collection("user").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val data = document.data

                        // Accessing nested data
                        val education = data?.get("education") as? Map<*, *>
                        val matriculation = education?.get("matriculation") as? Map<*, *>
                        val intermediate = education?.get("intermediate") as? Map<*, *>
                        val graduation = education?.get("graduation") as? Map<*, *>

                        matriculation?.let {
                            val year = it["year"] as? String
                            val percentage = it["percentage"] as? String
                            val school = it["school"] as? String

                            year?.let { binding.matriculationYearEditText.setText(it) }
                            percentage?.let { binding.matriculationPercentageEditText.setText(it) }
                            school?.let { binding.matriculationSchoolEditText.setText(it) }
                        }

                        intermediate?.let {
                            val year = it["year"] as? String
                            val percentage = it["percentage"] as? String
                            val school = it["school"] as? String

                            year?.let { binding.intermediateYearEditText.setText(it) }
                            percentage?.let { binding.intermediatePercentageEditText.setText(it) }
                            school?.let { binding.intermediateSchoolEditText.setText(it) }
                        }

                        graduation?.let {
                            val year = it["year"] as? String
                            val cgpa = it["cgpa"] as? String
                            val college = it["college"] as? String

                            year?.let { binding.GraduationYearEditText.setText(it) }
                            cgpa?.let { binding.cgpaEditText.setText(it) }
                            college?.let { binding.graduationSchoolEditText.setText(it) }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the error
                    println("Error getting document: $exception")
                }
        }
    }


    private fun setupEducationSectionListeners() {
        binding.nextbutton.setOnClickListener {
            val matriculationYear = binding.matriculationYearEditText.text.toString()
            val matriculationPercentage = binding.matriculationPercentageEditText.text.toString()
            val matriculationSchool = binding.matriculationSchoolEditText.text.toString()
            val intermediateYear = binding.intermediateYearEditText.text.toString()
            val intermediatePercentage = binding.intermediatePercentageEditText.text.toString()
            val intermediateSchool = binding.intermediateSchoolEditText.text.toString()
            val graduationYear = binding.GraduationYearEditText.text.toString()
            val cgpa = binding.cgpaEditText.text.toString()
            val graduationSchool = binding.graduationSchoolEditText.text.toString()



            if (matriculationYear.isEmpty() || matriculationPercentage.isEmpty() || matriculationSchool.isEmpty() ||
                intermediateYear.isEmpty() || intermediatePercentage.isEmpty() || intermediateSchool.isEmpty() ||
                graduationYear.isEmpty() || cgpa.isEmpty() || graduationSchool.isEmpty()) {
                Toast.makeText(context, "Please fill in all the input fields", Toast.LENGTH_SHORT).show()
            } else {
                val educationUpdates = hashMapOf(
                    "education" to hashMapOf(
                        "matriculation" to hashMapOf(
                            "year" to matriculationYear,
                            "percentage" to matriculationPercentage,
                            "school" to matriculationSchool
                        ),
                        "intermediate" to hashMapOf(
                            "year" to intermediateYear,
                            "percentage" to intermediatePercentage,
                            "school" to intermediateSchool
                        ),
                        "graduation" to hashMapOf(
                            "year" to graduationYear,
                            "cgpa" to cgpa,
                            "college" to graduationSchool
                        )
                    )
                )
                oldUserId = Firebase.auth.currentUser?.uid
                oldUserId?.let { userId ->
                    db.collection("user").document(userId)
                        .update(educationUpdates as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Education details updated successfully", Toast.LENGTH_SHORT).show()
                            /*parentFragmentManager.beginTransaction().apply {
                                replace(R.id.frame, UploadDetails()) // Replace with the actual fragment you want to navigate to
                                addToBackStack(null)
                                commit()*/
                            val intent = Intent(context, PdfGenerationActivity::class.java)
                            startActivity(intent)


                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error updating education details: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "Error updating document for user ID: $userId", e)
                        }
                }
            }
        }
    }

    private fun inputvalidate() {

        binding.matriculationYearEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutMatriculationYear.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutMatriculationYear.error = "cannot be empty"
            } else
                binding.textInputLayoutMatriculationYear.error = null
        }

        binding.matriculationPercentageEditText.doOnTextChanged { text,_,_,_->
            binding.textInputLayoutMatriculationPercentage.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutMatriculationPercentage.error = "cannot be empty"
            } else
                binding.textInputLayoutMatriculationPercentage.error = null
        }

        binding.matriculationSchoolEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutMatriculationSchool.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutMatriculationSchool.error = "cannot be empty"
            } else
                binding.textInputLayoutMatriculationSchool.error = null
        }

        binding.intermediateYearEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutIntermediateYear.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutIntermediateYear.error = "cannot be empty"
            } else
                binding.textInputLayoutIntermediateYear.error = null
        }

        binding.intermediatePercentageEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutIntermediatePercentage.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutIntermediatePercentage.error = "cannot be empty"
            } else
                binding.textInputLayoutIntermediatePercentage.error = null
        }

        binding.intermediateSchoolEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutIntermediateSchool.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutIntermediateSchool.error = "cannot be empty"
            } else
                binding.textInputLayoutIntermediateSchool.error = null
        }

        binding.GraduationYearEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutGraduationYear.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutGraduationYear.error = "cannot be empty"
            } else
                binding.textInputLayoutGraduationYear.error = null
        }

        binding.cgpaEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutCGPA.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutCGPA.error = "cannot be empty"
            } else
                binding.textInputLayoutCGPA.error = null
        }

        binding.graduationSchoolEditText.doOnTextChanged { text, start, before, count ->
            binding.textInputLayoutGraduationSchool.helperText = null
            if (text.toString().isEmpty()) {
                binding.textInputLayoutGraduationSchool.error = "cannot be empty"
            } else
                binding.textInputLayoutGraduationSchool.error = null
        }
    }


    companion object {

    }
}