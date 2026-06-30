package com.example.resumescanai

data class Candidate(
    val id: String = "",
    val name: String = "Unknown",
    val role: String = "",
    val score: Int = 0,
    val matchedKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val resumeUrl: String = "",
    val shortlisted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)