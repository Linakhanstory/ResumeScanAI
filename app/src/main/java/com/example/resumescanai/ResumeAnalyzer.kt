package com.example.resumescanai

import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import java.util.Locale

data class AnalysisResult(
    val fitPercentage: Int,
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>,
    val sentimentScore: Int, // 0-100
    val detectedRole: String
)

class ResumeAnalyzer {

    private val jobRoles = mapOf(
        "Android Developer" to listOf("Kotlin", "Java", "Android SDK", "Retrofit", "MVVM", "Coroutines", "Dagger", "Hilt", "Jetpack Compose"),
        "Web Developer" to listOf("HTML", "CSS", "JavaScript", "React", "Node.js", "Express", "MongoDB", "SQL", "TypeScript"),
        "Data Scientist" to listOf("Python", "R", "Machine Learning", "Pandas", "NumPy", "TensorFlow", "PyTorch", "SQL", "Data Visualization")
    )

    private val professionalKeywords = listOf("achieved", "managed", "developed", "led", "increased", "coordinated", "implemented", "responsible", "experience", "skills")

    fun analyze(text: String, callback: (AnalysisResult) -> Unit) {
        val lowerText = text.lowercase(Locale.ROOT)
        
        // 1. ML Kit Entity Extraction (Optional enhancement for name/date/email extraction)
        // For simplicity and speed in this logic, we use the core keyword matching, 
        // but we keep the ML Kit dependency ready for advanced NLP tasks.

        // 2. Detect Role based on keyword density
        var bestRole = "General"
        var maxMatches = -1
        
        jobRoles.forEach { (role, keywords) ->
            val matches = keywords.count { lowerText.contains(it.lowercase(Locale.ROOT)) }
            if (matches > maxMatches) {
                maxMatches = matches
                bestRole = role
            }
        }

        // 3. Keyword Matching for the detected role
        val roleKeywords = jobRoles[bestRole] ?: emptyList()
        val matched = roleKeywords.filter { lowerText.contains(it.lowercase(Locale.ROOT)) }
        val missing = roleKeywords.filter { !lowerText.contains(it.lowercase(Locale.ROOT)) }

        // 4. Simple "Sentiment/Professional Tone" Scoring
        val profMatches = professionalKeywords.count { lowerText.contains(it) }
        val sentimentScore = (profMatches * 10).coerceAtMost(100)

        // 5. Calculate Fit Percentage
        val keywordScore = if (roleKeywords.isNotEmpty()) (matched.size.toFloat() / roleKeywords.size * 100).toInt() else 0
        val fitPercentage = ((keywordScore * 0.7) + (sentimentScore * 0.3)).toInt()

        callback(AnalysisResult(
            fitPercentage = fitPercentage,
            matchedKeywords = matched,
            missingKeywords = missing,
            sentimentScore = sentimentScore,
            detectedRole = bestRole
        ))
    }
}