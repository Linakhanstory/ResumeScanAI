Resume Scan AI
​Project Overview
​Resume Scan AI is an Android-based application designed to streamline the recruitment process by automatically screening and analyzing candidate resumes. The application allows users to upload resumes, which are then processed to extract key information and evaluate candidate suitability based on job requirements.
​Key Features
​Secure File Uploads: Integrated with Cloudinary for efficient, secure, and optimized media/document handling.
​Cloud Database: Utilizes Firebase Firestore to store candidate metadata, scores, and application history securely.
​Authentication: Leverages Firebase Authentication to ensure a secure user experience.
​AI-Powered Analysis: Implements natural language processing logic to extract keywords and calculate candidate fit percentages.
​Admin Dashboard: A dedicated interface for recruiters to view, manage, and shortlist candidate applications.
​Tech Stack
​Language: Kotlin
​Backend/Storage: Firebase (Firestore, Auth)
​Media Management: Cloudinary Android SDK
​NLP/AI: Google ML Kit
​Architecture: MVVM (Model-View-ViewModel)
​Setup Instructions
​Clone this repository.
​Add your google-services.json file to the app/ directory (ensure it is configured in your Firebase Console).
​Add your Cloudinary credentials (cloud_name and upload_preset) in your initialization class.
​Sync your project with Gradle and build.
