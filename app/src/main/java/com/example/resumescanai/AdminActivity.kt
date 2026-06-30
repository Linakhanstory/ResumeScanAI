package com.example.resumescanai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resumescanai.databinding.ActivityAdminBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: CandidateAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.adminRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = CandidateAdapter(emptyList()) { candidate ->
            shortlistCandidate(candidate)
        }
        binding.rvCandidates.layoutManager = LinearLayoutManager(this)
        binding.rvCandidates.adapter = adapter

        fetchCandidates()
    }

    private fun fetchCandidates() {
        db.collection("candidates")
            .orderBy("score", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val candidateList = value?.toObjects(Candidate::class.java) ?: emptyList()
                adapter.updateData(candidateList)
            }
    }

    private fun shortlistCandidate(candidate: Candidate) {
        db.collection("candidates").document(candidate.id)
            .update("shortlisted", true)
            .addOnSuccessListener {
                Toast.makeText(this, "${candidate.name} shortlisted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to shortlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}