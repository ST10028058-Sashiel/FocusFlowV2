🧠 FocusFlowV2

Smart Productivity App — Manage, Focus, Achieve.

📘 Overview

FocusFlowV2 is a mobile productivity application built using Android Studio, Firebase, and MongoDB, with the backend hosted on Render.
It enables users to create, manage, and track daily tasks while staying organized and motivated.

Developed with Jetpack Compose, the app offers a clean, responsive, and modern interface designed to enhance user experience through smooth performance and UX-driven design principles.

This repository contains the Android frontend, while the API backend is hosted separately.

🎯 Portfolio of Evidence (POE) Context

This project was developed as part of the PROG7315 (Programming 3D) Portfolio of Evidence (POE).
The POE demonstrates skills in mobile app development, API integration, and cloud-based database management.

The requirements from the POE were:

Implement Single Sign-On (SSO) for secure authentication.

Include a feature within the settings page that adds value to the user.

Allow users to delete their account, permanently removing data from both the authentication system and database.

Our team addressed these as follows:

POE Requirement	Implementation
SSO Authentication	Integrated Google Sign-In (OAuth 2.0) via Firebase for secure and seamless login.
Settings Feature	Added a Dark Mode / Light Mode Toggle to enhance usability and accessibility.
Account Management	Created a Delete Account button that removes user credentials and associated task data permanently from both Firebase Authentication and MongoDB.
💡 Additional Student-Implemented Features

In addition to POE requirements, the following features were designed and developed by the team to improve functionality and engagement:

# 3+ additional	Features found in POE PART 1
1. Add a Task	. Users can create tasks by entering a name, location, due date, and time.	
2. Task Filtering	Users can filter tasks based on priority levels for quick navigation.	
3. Sorting Options	Sort tasks by Oldest, Newest, or Priority (High → Low).	
4. Priority Setter	Tag tasks with urgency levels — Low, Medium, High, Critical — helping users focus on what matters most.	
5. Reminder Notifications	Set reminders minutes before tasks begin; notifications pop up automatically using Android alarms.	
6. Task Counter	Displays the total number of pending tasks to track user progress visually.	
7. Update Tasks	: Modify existing tasks (title, priority, etc.).	
8. Delete Tasks	: Remove tasks permanently at any time.	
9. Mark as Complete	: Tick off tasks as completed and move them to the “Completed” list instantly.	
🎨 Design & User Experience

FocusFlowV2 uses a purple gradient theme representing creativity, focus, and calmness.
The design was influenced by Material 3 guidelines, ensuring:

Adaptive light/dark color schemes

Consistent typography and spacing

Smooth animations for transitions and task updates

All pages were designed with clarity and usability in mind, especially for mobile-first interaction.

⚙️ Technology Stack
Layer	Technology	Description
Frontend	Kotlin (Jetpack Compose)	Declarative UI for Android, providing modern performance and flexibility.
Backend	Node.js + Express	API layer for CRUD operations and authentication routing.
Database	MongoDB (Cloud)	Stores user data and task details in collections for fast access.
Authentication	Firebase Authentication + Google SSO	Secure and user-friendly sign-in process using Google accounts.
Hosting	Render.com	Deployed Node.js API backend with continuous hosting.
IDE	Android Studio	Used for Kotlin development, Gradle management, and UI design.
🧠 Architecture

The app follows the MVVM (Model-View-ViewModel) pattern:

Model: Data entities (tasks, users) and network handling.

ViewModel: Business logic connecting the repository and UI.

View: Jetpack Compose UI elements displaying dynamic content reactively.

Data syncs between the RoomDB (local cache) and MongoDB (remote storage) through REST API endpoints.

🚀 Core Screens

Login / Register Screen — Firebase + Google SSO Authentication.

Home Dashboard — Quick view of tasks, statistics, and productivity insights.

Add Task Screen — Create and edit tasks with time, location, and priority fields.

Task Status Screen — Displays completed and outstanding tasks in scrollable lists.

Settings Screen — Toggle dark/light mode, delete account, and manage preferences.

🔗 Project Links
💻 Frontend (Android App)

👉 GitHub Repository – FocusFlowV2

⚙️ Backend API

👉 API Repository – focusflow-api

👉 Hosted API – Render

🌐 API Documentation

Base URL:

https://focusflow-api-ts06.onrender.com

Method	Endpoint	Description
POST	/users/register	Register new user account
POST	/users/login	Authenticate existing user
DELETE	/users/delete/:id	Delete user account
GET	/tasks	Retrieve all user tasks
POST	/tasks	Create a new task
PUT	/tasks/:id	Update task details
DELETE	/tasks/:id	Delete a task
🎥 YouTube Demonstration

📺 FocusFlowV2 – Final POE Demo Video
(Add your YouTube link here once uploaded)
🔗 YouTube Link – Coming Soon

📦 Installation & Setup
1️⃣ Clone the Frontend
git clone https://github.com/ST10028058-Sashiel/FocusFlowV2.git

2️⃣ Clone the Backend
git clone https://github.com/MoltenBog4/focusflow-api.git

3️⃣ Run the API
npm install
npm start

4️⃣ Open and Run the App

Open FocusFlowV2 in Android Studio.

Sync Gradle and connect your Firebase project.

Run on an emulator or physical device.

🧩 Dependencies

Frontend

Jetpack Compose (Material3)

Firebase Authentication

Google Sign-In SDK

Retrofit (API communication)

Room Database

Kotlin Coroutines

Android AlarmManager (Reminders)

Backend

Express.js

Mongoose (MongoDB ODM)

bcrypt.js (Password encryption)

jsonwebtoken (Authentication)

dotenv (Environment variables)

📚 References

Android Developers. (2024). Jetpack Compose Overview.
https://developer.android.com/jetpack/compose

Google. (2024). Firebase Authentication.
https://firebase.google.com/docs/auth

MongoDB. (2024). NoSQL Database Service.
https://www.mongodb.com/

Render. (2024). Deploying Node.js Apps.
https://render.com/docs

OWASP Foundation. (2024). Mobile Security Best Practices.
https://owasp.org/www-project-mobile-top-10/

OpenAI. (2025). Project Documentation Support via ChatGPT.

🏁 Authors

Lead Developer: Sashiel Moonsamy (ST10028058) , ST10040092- NIKHIL SAROOP , ST10203525 -KIYASHAN NADASEN

Module: PROG7315 – Programming 3D
Institution: The IIE Varsity College
Year: 2025

🧾 License

This project is licensed under the MIT License.
You may modify, distribute, and reuse it with proper attribution.
