# 📚 BookCycle - Book Sharing App

BookCycle is an Android application that allows users to share, lend, and borrow books from each other. The app is designed for a community of readers who want to share their book collection and gain access to new and diverse books.

## ✨ Key Features

- **Book Management**: Users can add books to their personal library with images, titles, authors, and genres.
- **Location-Based Search**: Ability to search for available books nearby, with a Google Maps-based display.
- **Loan Request System**: Users can send requests to borrow books, and book owners can approve or reject requests.
- **Loan Tracking**: Track books that you've lent out and books you've borrowed.
- **Personal Profile**: Manage user details and view activity.

## 🏗️ Project Structure

The application is based on a Fragment architecture with Firebase as a database and authentication mechanism.

### 📱 Activities
- **MainActivity**: The main screen with ViewPager for navigating between book lists
- **LoginActivity**: Login and registration screen for users
- **SearchActivity**: Book search screen with map display
- **ItemBookActivity**: Detailed view of a single book

### 🧩 Fragments
- **AddBookFragment**: Interface for adding a new book
- **BooksListFragment**: Displaying a list of books (mine, lent, borrowed)
- **NotificationsFragment**: Displaying notifications and loan requests
- **PersonalInfoFragment**: Displaying user details
- **UserDetailsFragment**: Editing user details

### 🔧 Utilities
- **BookData**: Book data model
- **BooksAdapter**: Adapter for managing navigation between types of book lists
- **BooksType**: Enum defining the types of book lists
- **BorrowRequest**: Loan request data model
- **RequestsAdapter**: Adapter for displaying a list of loan requests
- **BooksRecyclerAdapter**: Adapter for displaying a list of books

## 🔄 Working with the Book Lending Flow

### 📝 Adding a Book
1. Access the "Add Book" screen
2. Fill in book details (title, author, genre)
3. Choose a book image
4. Save the book - the book will be added to "My Books" list

### 🔍 Searching for a Book
1. Access the "Search Book" screen
2. Search by name, author, or genre
3. View results on the map
4. Click on a marker on the map to display book details

### 🤲 Requesting a Book Loan
1. View book details from search
2. Click on "Request Loan"
3. The book owner will receive a notification and can approve or reject

### ✅ Approving a Loan Request
1. Access the "Notifications" screen
2. View pending loan requests
3. Approve or reject a request
4. After approval, the book will move to the "Lent Books" list

### 🔄 Returning a Book
1. The borrower can request a return from the "Borrowed Books" screen
2. The book owner confirms the return
3. The book returns to the "My Books" list and is marked as available

## 🔒 Security and User Management

- The application uses Firebase Authentication for user security management
- Email verification is required during registration
- User details are secured in the Firebase database
- Only the book owner can delete a book from the list

## 🛠️ Architecture and Infrastructure

- **Development Language**: Kotlin
- **Architecture**: Fragment-based for managing different screens
- **User Interface**: Material Design with TabLayout, ViewPager2 for navigation between categories
- **Data Storage**: Firebase Firestore for storing user and book data
- **Image Storage**: Firebase Storage for storing book images
- **Authentication**: Firebase Authentication for user management and permissions
- **Location**: Google Location Services for finding books nearby

## 📋 System Requirements

- Android 7.0 (API level 24) and above
- Google Play services installed
- Internet access
- Location permissions (for finding books nearby)
- Camera and gallery access permissions (for taking and uploading book pictures)

## 📱 Permissions and API Keys

### Required Permissions
The application requires the following permissions which are declared in the AndroidManifest.xml:
- `android.permission.INTERNET` - For connecting to Firebase and Google Maps services
- `android.permission.ACCESS_NETWORK_STATE` - For checking network connectivity
- `android.permission.ACCESS_FINE_LOCATION` - For precise location when finding nearby books
- `android.permission.ACCESS_COARSE_LOCATION` - For approximate location services

### API Keys Setup
1. **Google Maps API Key**:
   - Create a project in the [Google Cloud Platform Console](https://console.cloud.google.com/)
   - Enable the Google Maps Android API
   - Generate an API key with appropriate restrictions
   - Add the API key to the AndroidManifest.xml:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_API_KEY" />
     ```
   - **Important Security Note**: Never commit API keys to public repositories. Use environment variables or a secure method to store and access API keys.

2. **Firebase Configuration**:
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/)
   - Add your Android app to the Firebase project
   - Download the `google-services.json` file and place it in the app directory
   - **Security Recommendation**: Add `google-services.json` to your `.gitignore` file to prevent it from being uploaded to public repositories

## 💻 Installation

1. Download the project files from the repository
2. Open the project in Android Studio
3. Configure Firebase details (add the google-services.json file to the app folder)
4. Add Google Maps API key in the manifest file or through gradle
5. Build and install the application

## 📚 External Libraries

- Firebase Auth UI for login screen management
- Google Maps SDK for Android for map display
- Glide for loading and displaying images
- Firebase Firestore and Storage for data management
