

# 🧠 **FocusFlowV2**

### *Smart Productivity App — Manage, Focus, Achieve*

---

## 📘 **Overview**

**FocusFlowV2** is a mobile productivity application built using **Android Studio**, **Firebase**, and **MongoDB**, with the backend hosted on **Render**.
It enables users to **create, manage, and track daily tasks** while staying organized and motivated.

Developed with **Jetpack Compose**, the app offers a clean, responsive, and modern interface designed to enhance user experience through **smooth performance** and **UX-driven design principles**.

This repository contains the **Android frontend**, while the API backend is hosted separately.

---

## 🎯 **Portfolio of Evidence (POE) Context**

This project was developed as part of the **PROG7314** Portfolio of Evidence (POE).
The POE demonstrates skills in **mobile app development**, **RESTful API integration**, and **cloud-based database management**.

### 🔍 **POE Requirements & Implementations**

| **POE Requirement**    | **Implementation**                                                                                                                           |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| **SSO Authentication** | Integrated **Google Sign-In (OAuth 2.0)** via **Firebase** for secure and seamless login.                                                    |
| **Settings Feature**   | Added a **Dark/Light Mode Toggle**, **Delete Account** (removes data from Firebase + MongoDB), and **Export CSV via email** for task backup. |

---

## 💡 **PART 1 – Student-Implemented Features**

| # | **Feature**                                 | **Description**                                                          |
| - | --------------------------------------------|------------------------------------------------------------------------- |
| 1 | **Add a Task**                              | Users can create tasks by entering a name, location, due date, and time. |
| 2 | **Task Filtering**                          | Filter tasks based on priority levels for quick navigation.              |
| 3 | **Sorting Options**                         | Sort tasks by **Oldest**, **Newest**, or **Priority (High → Low)**.      |
| 4 | **Priority Setter**                         | Assign urgency levels — *Low, Medium, High, Critical*.                   |
| 5 | **Real time Reminder Notifications**        | Notifications triggered before task start using AlarmManager.            |
| 6 | **Task Counter**                            | Displays total number of pending tasks.                                  |
| 7 | **Update Tasks**                            | Modify existing task information.                                        |
| 8 | **Delete Tasks**                            | Permanently removes a task.                                              |
| 9 | **Mark as Complete**                        | Moves tasks to the Completed list instantly.                             |


# 🚀 **PART 2 – SUMMATIVE FEATURES (Advanced Enhancements)**

FocusFlowV2 includes a set of **advanced summative features** designed to improve usability, security, accessibility, and real-world reliability.

## ✨ 1. **Share Task Functionality**

Users can instantly share task details via:

* WhatsApp
* Email
* SMS
* Social media apps

A formatted summary includes title, date, time, priority, and location.
This enables collaboration and real-world teamwork.

---

## 🔐 2. **Biometric Authentication for Task Status Changes**

To prevent accidental or unauthorized actions, biometric authentication is required for:

* Marking tasks as *Complete*
* Updating priority
* Deleting tasks

Supports:

* **Fingerprint**
* **Face Unlock**

---

## 🌍 3. **Multi-Language Support (Afrikaans + isiZulu)**

The app supports three languages:

* 🇬🇧 English
* 🇿🇦 Afrikaans
* 🇿🇦 isiZulu

All UI text automatically updates based on system language.

---

## 🔔 4. **Real-Time Notifications**

Two notification systems:

### ✔ Instant Task Creation Notification

Pops up immediately after adding a task.

### ✔ Scheduled Reminder Notification

Alerts users shortly before task start (customizable lead time).

---

## 📶 5. **Offline Mode + Automatic Cloud Sync**

Full functionality while offline:

* Add tasks
* Update tasks
* Delete tasks
* Mark complete

All changes store in **Locally and MongoDB with API** and sync automatically when connection is restored.

---


---

## 🧪 6. **Unit Testing (Video Included)**

Covers:

* ViewModel tests
* Business logic validation
* API call mocking
* Syncing reliability tests

📹 **Unit Test Video:**
[https://drive.google.com/file/d/145MXywRNL-MseXETwYiAUxR1qV6f6bGu/view?usp=sharing](https://drive.google.com/file/d/145MXywRNL-MseXETwYiAUxR1qV6f6bGu/view?usp=sharing)

---

## 🎨 **Design & User Experience**

FocusFlowV2 uses a vibrant **purple gradient theme** symbolizing creativity, focus, and calmness, following **Material 3** guidelines.

* Adaptive light/dark mode
* Smooth animations
* Clean typography and spacing
* Mobile-first layouts

---

## ⚙️ **Technology Stack**

| **Layer**          | **Technology**           | **Description**                |
| ------------------ | ------------------------ | ------------------------------ |
| **Frontend**       | Kotlin (Jetpack Compose) | Modern declarative Android UI. |
| **Backend**        | Node.js + Express        | REST API for tasks & auth.     |
| **Database**       | MongoDB (Cloud)          | Scalable NoSQL data storage.   |
| **Authentication** | Firebase + Google SSO    | Secure login.                  |
| **Hosting**        | Render.com               | Cloud deployment for backend.  |
| **IDE**            | Android Studio           | Development environment.       |

---

## 🧠 **Architecture**

Follows **MVVM** pattern:

* **Model** — Data entities + API communication
* **ViewModel** — Business logic, state handling
* **View** — Jetpack Compose UI

Data sync between **RoomDB** (offline) and **MongoDB** (online).

---

## 🚀 **Core Screens**

* 🔐 Login Screen
* 🏠 Home Dashboard
* 📝 Add Task Screen
* 📋 Task Status Screen
* ⚙️ Settings Screen

---

## 🔗 **Project Links**

### 💻 Frontend

👉 [https://github.com/ST10028058-Sashiel/FocusFlowV2.git](https://github.com/ST10028058-Sashiel/FocusFlowV2.git)

### ⚙️ Backend API

👉 [https://github.com/MoltenBog4/focusflow-api.git](https://github.com/MoltenBog4/focusflow-api.git)

### 🌍 Hosted API

👉 [https://focusflow-api-ts06.onrender.com](https://focusflow-api-ts06.onrender.com)

---

## 🌐 **API Documentation**

Base URL:

```
https://focusflow-api-ts06.onrender.com
```

| Method | Endpoint            | Description         |
| ------ | ------------------- | ------------------- |
| POST   | `/users/register`   | Register a new user |
| POST   | `/users/login`      | Log in a user       |
| DELETE | `/users/delete/:id` | Delete account      |
| GET    | `/tasks`            | Retrieve tasks      |
| POST   | `/tasks`            | Create task         |
| PUT    | `/tasks/:id`        | Update task         |
| DELETE | `/tasks/:id`        | Delete task         |

---

## 🎥 **YouTube Demonstration**

📺 **Final POE Demo Video:**
[https://youtu.be/Jz4rFbhzQGI?si=PkdkIOF2if381a1z](https://youtu.be/Jz4rFbhzQGI?si=PkdkIOF2if381a1z)

---

## 📦 **Installation & Setup**

### 1️⃣ Clone Frontend

```bash
git clone https://github.com/ST10028058-Sashiel/FocusFlowV2.git
```

### 2️⃣ Clone Backend

```bash
git clone https://github.com/MoltenBog4/focusflow-api.git
```

### 3️⃣ Run the API

```bash
npm install
npm start
```

### 4️⃣ Run the App

1. Open in Android Studio
2. Connect Firebase
3. Sync Gradle
4. Run on emulator/device

---

## 🧩 **Dependencies**

### Frontend

* Jetpack Compose
* Firebase Auth
* Google Sign-In
* Retrofit
* Coroutines
* AlarmManager
* STORES INFORMATION LOCALLY

### Backend

* Express.js
* Mongoose
* bcrypt.js
* JWT
* dotenv
* MongoDB with API

---

## 📚 **References**

* [Android Developers. (2024). *Jetpack Compose Overview.*](https://developer.android.com/jetpack/compose)
* [Google. (2024). *Firebase Authentication.*](https://firebase.google.com/docs/auth)
* [MongoDB. (2024). *NoSQL Database Service.*](https://www.mongodb.com/)
* [Render. (2024). *Deploying Node.js Apps.*](https://render.com/docs)
* [OWASP Foundation. (2024). *Mobile Security Best Practices.*](https://owasp.org/www-project-mobile-top-10/)
* [OpenAI. (2025). *Project Documentation Support via ChatGPT.*](https://chat.openai.com)


## 🏁 **Authors**

Sashiel Moonsamy – ST10028058
Nikhil Saroop – ST10040092
Kiyashan Nadasen – ST10203525

Module: PROG7314 — Programming 3D
Institution: Varsity College
Year: 2025

---

## 🧾 **License**

MIT License.

---

### ✨ *“Plan better. Focus deeper. Achieve more — with FocusFlowV2.”*

