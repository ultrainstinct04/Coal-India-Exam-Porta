package com.example.bccl

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.bccl.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve data from Firestore
        val userId = Firebase.auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document != null) {
                    val personalDetails = mutableMapOf(
                        "Full Name" to "${document.getString("firstName") ?: ""} ${document.getString("middleName") ?: ""} ${document.getString("lastName") ?: ""}",
                        "Father's Name" to "${document.getString("fatherFirstName") ?: ""} ${document.getString("fatherMiddleName") ?: ""} ${document.getString("fatherLastName") ?: ""}",
                        "Government Id No" to (document.getString("governmentId") ?: ""),
                        "Email" to (document.getString("email") ?: ""),
                        "Mobile No" to (document.getString("phone") ?: ""),
                        "Date of Birth" to (document.getString("dob") ?: ""),
                        "Gender" to (document.getString("gender") ?: ""),
                        "Category" to (document.getString("category") ?: "")
                    )

                    val educationalDetails = mutableMapOf(
                        "Matriculation School" to (document.getString("education.matriculation.school") ?: ""),
                        "Matriculation Percentage" to (document.getString("education.matriculation.percentage") ?: ""),
                        "Matriculation Passing Year" to (document.getString("education.matriculation.year") ?: ""),
                        "Intermediate School" to (document.getString("education.intermediate.school") ?: ""),
                        "Intermediate Percentage" to (document.getString("education.intermediate.percentage") ?: ""),
                        "Intermediate Passing Year" to (document.getString("education.intermediate.year") ?: ""),
                        "Graduation College" to (document.getString("education.graduation.college") ?: ""),
                        "Graduation CGPA" to (document.getString("education.graduation.cgpa") ?: ""),
                        "Graduation Passing Year" to (document.getString("education.graduation.year") ?: "")
                    )


                    createPdf(personalDetails, educationalDetails)
                }
            }
        }
    }

    private fun createPdf(
        personalDetails: Map<String, String?>,
        educationalDetails: Map<String, String?>
    ) {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val margin = 40f
        var yPosition = margin

        // Header
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Student Application Form", (canvas.width / 2).toFloat(), yPosition, paint)

        // Load logo
        val logo = BitmapFactory.decodeResource(resources, R.drawable.logo) // Replace with your logo drawable
        val logoScaled = Bitmap.createScaledBitmap(logo, 100, 100, false)
        yPosition += 40f
        canvas.drawBitmap(logoScaled, (canvas.width / 2 - 50).toFloat(), yPosition, paint)
        yPosition += 120f

        // Section Headers
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 16f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true

        // Personal Information
        yPosition = drawSectionHeader(canvas, "Personal Information", yPosition, paint)
        paint.textSize = 14f
        paint.isFakeBoldText = false
        yPosition = drawData(canvas, personalDetails, yPosition, paint)

        // Educational Details
        paint.isFakeBoldText = true
        yPosition = drawSectionHeader(canvas, "Educational Details", yPosition, paint)
        paint.textSize = 14f
        paint.isFakeBoldText = false
        yPosition = drawData(canvas, educationalDetails, yPosition, paint)

        pdfDocument.finishPage(page)

        // Save the document
        val filePath = getExternalFilesDir(null)?.absolutePath + "/user_data.pdf"
        val file = File(filePath)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF generated successfully", Toast.LENGTH_SHORT).show()
            // Open the PDF
            openPdf(file)
        } catch (e: IOException) {
            Toast.makeText(this, "Error writing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawSectionHeader(canvas: Canvas, header: String, yPosition: Float, paint: Paint): Float {
        canvas.drawText(header, 40f, yPosition, paint)
        return yPosition + 25f // Adjust space after header
    }

    private fun drawData(canvas: Canvas, data: Map<String, String?>, yPosition: Float, paint: Paint): Float {
        var currentPosition = yPosition
        data.forEach { (key, value) ->
            canvas.drawText("$key: $value", 40f, currentPosition, paint)
            currentPosition += 20f // Adjust space between lines
        }
        return currentPosition + 20f // Adjust space after data section
    }

    private fun openPdf(file: File) {
        val path = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(path, "application/pdf")
        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(pdfIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No application available to view PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
