package com.example.resumescanai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeAnalyzerTest {

    private val analyzer = ResumeAnalyzer()

    @Test
    fun testAndroidDeveloperAnalysis() {
        val resumeText = """
            John Doe
            Experience: 3 years as an Android Developer.
            Skills: Kotlin, Java, Retrofit, MVVM, Coroutines.
            Developed multiple apps and led a team of developers.
            Increased app performance by 20%.
        """.trimIndent()

        val result = analyzer.analyze(resumeText)

        assertEquals("Android Developer", result.detectedRole)
        assertTrue(result.matchedKeywords.contains("Kotlin"))
        assertTrue(result.matchedKeywords.contains("MVVM"))
        assertTrue(result.fitPercentage > 50)
        assertTrue(result.sentimentScore > 0)
    }

    @Test
    fun testWebDeveloperAnalysis() {
        val resumeText = """
            Jane Smith
            Full Stack Web Developer
            Expert in HTML, CSS, JavaScript, and React.
            Worked with Node.js and MongoDB for backend.
            Responsible for implementing responsive designs.
        """.trimIndent()

        val result = analyzer.analyze(resumeText)

        assertEquals("Web Developer", result.detectedRole)
        assertTrue(result.matchedKeywords.contains("React"))
        assertTrue(result.matchedKeywords.contains("MongoDB"))
    }

    @Test
    fun testScoringConsistency() {
        val lowSkillResume = "I am looking for a job. I know some computer things."
        val highSkillResume = "Expert Android Developer with Kotlin, Java, Retrofit, MVVM, Coroutines, and Hilt. Led major projects."
        
        val lowResult = analyzer.analyze(lowSkillResume)
        val highResult = analyzer.analyze(highSkillResume)
        
        assertTrue("High skill should score higher than low skill", highResult.fitPercentage > lowResult.fitPercentage)
    }
}