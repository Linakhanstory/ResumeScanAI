package com.example.resumescanai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.resumescanai.databinding.ItemCandidateBinding
import com.google.firebase.firestore.FirebaseFirestore

class CandidateAdapter(
    private var candidates: List<Candidate>,
    private val onShortlistClick: (Candidate) -> Unit
) : RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder>() {

    class CandidateViewHolder(val binding: ItemCandidateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        val binding = ItemCandidateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CandidateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        val candidate = candidates[position]
        holder.binding.apply {
            tvCandidateName.text = candidate.name
            tvCandidateRole.text = candidate.role
            tvCandidateScore.text = "${candidate.score}%"
            progressScore.progress = candidate.score
            
            // Set color based on score
            val color = when {
                candidate.score >= 80 -> 0xFF48BB78.toInt() // Green
                candidate.score >= 50 -> 0xFFECC94B.toInt() // Yellow
                else -> 0xFFF56565.toInt() // Red
            }
            progressScore.setIndicatorColor(color)

            // Shortlist button state
            if (candidate.shortlisted) {
                btnShortlist.text = "Shortlisted"
                btnShortlist.isEnabled = false
                btnShortlist.alpha = 0.5f
            } else {
                btnShortlist.text = "Shortlist"
                btnShortlist.isEnabled = true
                btnShortlist.alpha = 1.0f
            }

            btnShortlist.setOnClickListener {
                onShortlistClick(candidate)
            }
        }
    }

    override fun getItemCount(): Int = candidates.size

    fun updateData(newCandidates: List<Candidate>) {
        candidates = newCandidates
        notifyDataSetChanged()
    }
}