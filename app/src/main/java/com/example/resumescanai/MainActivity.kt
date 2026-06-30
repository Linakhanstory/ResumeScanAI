package com.example.resumescanai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.resumescanai.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val analyzer = ResumeAnalyzer()
    private val db = FirebaseFirestore.getInstance()

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadResumeToCloudinary(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        PDFBoxResourceLoader.init(applicationContext)

        binding.btnUpload.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }

        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun uploadResumeToCloudinary(fileUri: Uri) {
        setLoadingState(true)
        binding.tvResults.text = "Uploading to Cloudinary..."

        MediaManager.get().upload(fileUri)
            .unsigned("resume_upload_preset")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = if (totalBytes > 0) (100.0 * bytes / totalBytes).toInt() else 0
                    runOnUiThread {
                        binding.uploadProgress.progress = progress
                        binding.tvResults.text = "Uploading: $progress%"
                    }
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as String
                    runOnUiThread {
                        binding.tvResults.text = "Upload successful. Analyzing..."
                        processFile(fileUri, secureUrl)
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        handleError("Cloudinary Error: ${error.description}")
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnUpload.isEnabled = !isLoading
        binding.uploadProgress.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }

    private fun handleError(message: String) {
        setLoadingState(false)
        binding.tvResults.text = message
        Log.e("ResumeScanAI", message)
    }

    private fun processFile(uri: Uri, downloadUrl: String) {
        try {
            val contentResolver = contentResolver
            val mimeType = contentResolver.getType(uri)
            val extractedText = when {
                mimeType == "application/pdf" -> extractTextFromPdf(uri)
                mimeType?.startsWith("text/") == true -> extractTextFromTxt(uri)
                else -> {
                    val fileName = uri.path ?: ""
                    if (fileName.endsWith(".pdf", true)) extractTextFromPdf(uri)
                    else if (fileName.endsWith(".txt", true)) extractTextFromTxt(uri)
                    else null
                }
            }

            if (!extractedText.isNullOrBlank()) {
                analyzeResume(extractedText, downloadUrl)
            } else {
                handleError("Could not read file.")
            }
        } catch (e: Exception) {
            handleError("Error: ${e.message}")
        }
    }

    private fun analyzeResume(text: String, downloadUrl: String) {
        analyzer.analyze(text) { result ->
            val candidate = Candidate(
                id = UUID.randomUUID().toString(),
                name = "Candidate ${System.currentTimeMillis() % 1000}",
                role = result.detectedRole,
                score = result.fitPercentage,
                matchedKeywords = result.matchedKeywords,
                missingKeywords = result.missingKeywords,
                resumeUrl = downloadUrl,
                shortlisted = false
            )
            saveCandidateToFirestore(candidate, result)
        }
    }

    private fun saveCandidateToFirestore(candidate: Candidate, result: AnalysisResult) {
        db.collection("candidates").document(candidate.id)
            .set(candidate)
            .addOnSuccessListener {
                setLoadingState(false)
                displayResults(result)
            }
            .addOnFailureListener { e -> handleError("DB Error: ${e.message}") }
    }

    private fun displayResults(result: AnalysisResult) {
        val displayBuilder = StringBuilder()
        displayBuilder.append("Analysis Results\n")
        displayBuilder.append("━━━━━━━━━━━━━━━━━━━━\n\n")
        displayBuilder.append("Role: ${result.detectedRole}\n")
        displayBuilder.append("Score: ${result.fitPercentage}%\n\n")
        displayBuilder.append("Matched Skills:\n")
        result.matchedKeywords.forEach { displayBuilder.append("✓ $it\n") }
        binding.tvResults.text = displayBuilder.toString()
    }

    private fun extractTextFromPdf(uri: Uri): String {
        return contentResolver.openInputStream(uri).use { inputStream ->
            val document = PDDocument.load(inputStream)
            val text = PDFTextStripper().getText(document)
            document.close()
            text
        }
    }

    private fun extractTextFromTxt(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
    }
}