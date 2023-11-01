# ProjectFoot

Welcome to ProjectFoot! This README provides developers with all the necessary information to understand, contribute to, and use this project effectively.

### Project Overview
This is a project in development to modernize amateur football. With this app, athletes will be able to schedule courts to play, mainly football, as well as consult the court details, such as value per hour, amenities, restrooms, and photos.
Football project to organize and schedule your court at the app.

Use Android Studio to emulate the app.

## Prerequisites

Android Studio
Java Development Kit (JDK) - 17+
Installation

### What's being developed

- Creation of two possible account types: player and court owner
- User location tracking
- Court listing
- Court registration with images and general data
- Court data editing as court owner
- Court scheduling by player
- Google Maps API to display courts in the region
- Monetary transfer between player and court owner
- Account creation and authentication

### Config Files

1. Go to https://console.firebase.google.com

   a. Register/Login to Firebase. (You can use a Google account.)

   b. Create a new project

   c. Creating a project will generate a `google-services.json` file during configuration. Download the `google-services.json` file and follow google firebase instructions

   d. Create API KEY for Google Maps and follow AndroidManifest instructions

2. If using Android Studio, click Sync Project with Gradle Files. Update Android Studio if it asks you to update. Run the project.

## Code Good Practices
Maintaining clean, readable, and maintainable code is crucial for the success of any project. Here are some code good practices that we follow in this project:

### 1. Meaningful Variable and Method Names
Use descriptive names for variables, methods, and classes that accurately convey their purpose. Avoid single-letter variable names or generic names like "temp," "data," etc.

### 2. Consistent Indentation and Formatting
Consistent indentation enhances code readability. Follow the established formatting conventions in the project and be consistent throughout your code.

### 3. Proper Commenting
Add comments to your code to explain complex logic, algorithm steps, or any non-obvious behavior. However, strive for code clarity, so use comments sparingly and only when necessary.

### 4. Modularization and Single Responsibility
Divide your code into smaller, modular components with clear responsibilities. Each method or class should have a single, well-defined purpose. This makes code easier to understand and maintain.

### 5. Avoid Magic Numbers and Strings
Avoid using hard-coded numbers or strings directly in your code. Instead, use constants or enums to give meaningful names to these values.

### 6. Error Handling
Always handle exceptions and errors appropriately. Use try-catch blocks to catch exceptions and provide meaningful error messages when necessary.

### 7. Version Control Best Practices
Follow proper version control practices when working with Git:

- Make small, meaningful commits.
- Write clear and concise commit messages.
- Keep your feature branches up to date with the main branch.

### 8. Testing
Write unit tests for your code whenever possible. Proper test coverage helps catch bugs and ensures the reliability of your codebase.

### 9. Avoid Nested Code Blocks
Avoid excessive nesting of if-else statements or loops. Deeply nested code blocks can become hard to read and lead to bugs.

### 10. Code Reviews
Encourage and participate in code reviews. Code reviews help catch issues early, ensure adherence to coding standards, and promote knowledge sharing among team members.
