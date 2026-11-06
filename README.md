# Event Planner Android Application

## Project Description

Event Planner is an Android application developed using Kotlin and Jetpack Compose. It allows users to organize and manage personal events in one convenient place.

The application was designed with a simple, user-friendly interface and local data storage so users can keep track of their upcoming events and reminders.

## Features

- Create, edit, and delete events.
- Store event names, dates, times, locations, and descriptions.
- Set reminder preferences for scheduled events.
- Save event information locally using Room Database.
- Restore scheduled reminders after device restarts.
- Validate required event information.
- Display event information in an organized interface.

## Technologies Used

- Kotlin
- Android Studio
- Jetpack Compose
- Material Design 3
- Room Database
- MVVM Architecture
- Kotlin Coroutines and Flow
- AlarmManager
- Git and GitHub

## Application Architecture

The application follows the Model-View-ViewModel (MVVM) architecture.

- **Model:** Room entities and database components manage event information.
- **View:** Jetpack Compose displays the application's user interface.
- **ViewModel:** Manages application data, UI state, and communication between the interface and repository.
- **Repository:** Handles communication between the ViewModel and Room database.
- **DAO:** Defines database operations for creating, retrieving, updating, and deleting events.

## Database

Room Database provides local storage for event information.

The event records include:
- Event ID
- Event name
- Date
- Time
- Location
- Description
- Reminder preference

The database includes a migration from version 1 to version 2 to add the reminder preference field.

## Reminder Functionality

The application uses Android AlarmManager to schedule event reminders. Users can select reminder intervals, including 5, 15, 30, 60, or 1,440 minutes before an event, or choose no reminder.

A boot receiver restores scheduled reminders after device restarts.

## Build Information

- Version Name: 1.1
- Version Code: 2
- Minimum SDK: 24
- Target SDK: 37
- Application ID: `com.example.eventplanningapp`

## Testing

The project includes seven Room DAO tests and one instrumented package-context test.

All eight automated tests completed successfully on a physical Samsung device with zero failures, errors, or skipped tests.

Manual testing included:
- Creating
