Counselink: Streamlined Student Support with Advanced Analytics using Regression Analysis
Counselink is an Android application designed to enhance the workflow of the CATC Office of LPU Batangas by offering streamlined student support services. This app integrates advanced analytics, including regression analysis, to assist counselors in providing better service and insights. It features multiple user roles, dynamic content posting, and detailed analytics, all within an intuitive and easy-to-use interface.

Table of Contents:
About the App

Features

Technologies Used

Installation

Usage

App Structure

Screenshots

Important Notes

About the App
Counselink helps manage various tasks related to student-counselor interactions, with specific features tailored for counselors, students, and administrators. The app leverages analytics to allow counselors to track student data and view it through intuitive charts. It also provides them with the ability to filter the data based on dates, departments, and other criteria.

The app is designed using Android Studio, Firebase, and integrates Email OTP for secure authentication. The system supports three types of users:

Students: Can access their appointments, view announcements, and track referrals.

Counselors: Can view and manage appointments and referrals for students in their respective departments, along with advanced analytics for performance tracking.

Boss: Oversees the whole system, having access to all student data, appointments, and analytics.

Features
User Authentication: Secure login via email OTP (One-Time Password).

Roles:

Students: Can log in, view announcements, make appointments, and track referrals.

Counselors: View students from their department, manage appointments, view referrals, and access analytics.

Admin (Boss): Full access to all features and analytics across departments.

Announcement Board: Ability to create and view posts and announcements.

Appointment Management: Schedule, view, and manage appointments with counselors.

Referral System: Refer students to appropriate counselors and track progress.

Advanced Analytics:

View detailed reports and graphs.

Filter data by dates and other parameters.

Regression analysis of student-related data for better insights.

Generate downloadable reports/tally files for further analysis.

Technologies Used
Frontend:

Android Studio (Java/Kotlin)

Backend:

Firebase Authentication (for user management)

Firebase Firestore (for data storage)

Firebase Realtime Database (for real-time data updates)

Analytics:

Regression Analysis (via backend or app logic)

Google Analytics (for tracking app usage and events)

Email OTP Integration: Firebase OTP Authentication.

Installation
Since this is a private repository, the APIs required for the app to fully function may not be publicly available. However, the following steps can guide you through the basic setup process:

Clone the Repository:

bash
Copy
Edit
git clone https://github.com/yourusername/counselink.git
Open the Project:

Open Android Studio and import the project directory.

Firebase Setup:

Go to the Firebase Console, create a new project, and integrate the app with Firebase.

Add the necessary Firebase services, including Authentication, Firestore, and Realtime Database.

Replace the google-services.json file with your project’s configuration.

Dependencies:

Sync the project with Gradle files.

Ensure that the required dependencies for Firebase and any other external libraries are added.

Usage
Login:

Use your email and OTP (One-Time Password) to log into the app.

For Students:

View announcements posted by the department.

Set and manage appointments with counselors.

Track and view referrals assigned by counselors.

For Counselors:

View a list of students from your department.

Manage appointments and track referrals.

Use the Analytics section to generate insights about students’ data and generate downloadable reports.

For Admins:

Full access to all data and functionalities.

Oversee the entire app’s usage and analytics.

App Structure
css
Copy
Edit
Counselink/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/
│   │   │   │   │   ├── counselink/
│   │   │   │   │   │   ├── activities/
│   │   │   │   │   │   ├── adapters/
│   │   │   │   │   │   ├── models/
│   │   │   │   │   │   ├── utils/
│   │   │   │   │   │   ├── analytics/
│   │   │   │   │   │   ├── auth/
│   │   │   │   │   │   └── notifications/
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       ├── drawable/
│   │   │       └── values/
│   └── build.gradle
├── google-services.json
└── README.md
Screenshots
![image](https://github.com/user-attachments/assets/52748233-811a-4af1-a8a6-bdc646673eb3)
![Screenshot 2025-05-21 112342](https://github.com/user-attachments/assets/93f23756-9ebf-43e6-8b44-920ecf253ed5)
![Screenshot 2025-05-21 112602](https://github.com/user-attachments/assets/16b58cac-348f-4763-8896-1b048d4693bd)
![Screenshot 2025-05-21 112400](https://github.com/user-attachments/assets/59e3100c-c698-4667-a797-eb7e85e72745)
Important Notes![Screenshot 2025-05-21 112656](https://github.com/user-attachments/assets/34c1cef1-196b-4fce-8428-915672baad5d)


API Keys: The API keys and secrets used in this project are private and are not included in the repository. You will need to replace them with your own keys for the app to function correctly.

Firebase Integration: Ensure that you have Firebase services set up properly on your Firebase console for this app to work as expected.

Limited Access: The version of this app on this repository may not be fully functional due to missing API access and private configurations.
