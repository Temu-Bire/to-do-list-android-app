# 📋 To-Do List Android App — Project Review

## Overview

A native **Android To-Do List app** written in **Java**, following the **MVVM architecture** pattern with a **Room local database**. It is a clean, well-structured single-user task management app.

- **Package:** `com.example.to_dolist`
- **Min SDK:** 23 (Android 6.0 Marshmallow)
- **Target SDK:** 36
- **Language:** Java
- **Architecture:** MVVM (Model-View-ViewModel)

---

## Architecture Layers

```
UI Layer          →  MainActivity, AddEditTaskActivity, TaskAdapter
ViewModel Layer   →  TaskViewModel
Repository Layer  →  TaskRepository
Data Layer        →  TaskDao (Room DAO), TaskDatabase (Room DB), Task (Entity)
```

---

## Current Features

### 1. 📝 Add a Task
- Tapping the **Floating Action Button (FAB)** opens the `AddEditTaskActivity`.
- User can fill in:
  - **Title** (required — shows a Toast if empty)
  - **Description** (optional)
  - **Priority** — Spinner with 3 options: `High`, `Medium`, `Low`
  - **Due Date** — Opens a `DatePickerDialog` to pick a date (defaults to today)
- Saving inserts the task into the Room database on a background thread.

### 2. ✏️ Edit a Task
- Tapping any task card in the list opens the **same `AddEditTaskActivity`** pre-populated with the task's existing data (title, description, priority, due date, completion status).
- Saving updates the existing record in the database.

### 3. 🗑️ Delete a Task (Swipe Gesture)
- Swiping a task card **left or right** deletes it immediately.
- A **Toast** notification confirms deletion (`"Task deleted"`).

### 4. ✅ Mark Task as Complete
- Each task card has a **CheckBox**.
- Checking/unchecking instantly updates the `isCompleted` field in the database via the ViewModel.

### 5. 🔽 Filter Tasks
- A **Filter Spinner** on the main screen with two options:
  - **All** — Shows all tasks regardless of completion status.
  - **Completed** — Shows only tasks where `isCompleted = true`.

### 6. 🔃 Sort Tasks
- A **Sort Spinner** on the main screen with two options:
  - **Sort: Date** — Orders tasks by `dueDate ASC`.
  - **Sort: Priority** — Orders tasks `High → Medium → Low`, then by `dueDate ASC`.

### 7. 🎨 Priority Color Chips
- Each task card shows a color-coded priority chip/badge:
  - 🔴 **High** → `priority_high` color
  - 🟡 **Medium** → `priority_medium` color
  - 🟢 **Low** → `priority_low` color
- Uses a drawable background (`bg_priority_chip.xml`) with dynamic tint.

### 8. 📭 Empty State
- When the task list is empty (or no tasks match the active filter), a centered `"No tasks yet"` message is shown.

---

## Data Model — `Task` Entity

| Field | Type | Description |
|---|---|---|
| `id` | `int` (auto) | Primary key, auto-generated |
| `title` | `String` | Task name (required) |
| `description` | `String` | Optional notes |
| `priority` | `String` | `"High"`, `"Medium"`, or `"Low"` |
| `dueDate` | `long` | Unix timestamp in milliseconds |
| `isCompleted` | `boolean` | Completion status |

---

## Database Queries (`TaskDao`)

| Method | SQL Summary |
|---|---|
| `getAllTasks()` | All tasks, ordered by `dueDate ASC` |
| `getCompletedTasks()` | Only `isCompleted = 1`, by `dueDate ASC` |
| `getAllTasksSortedByPriority()` | All tasks, High→Medium→Low, then by date |
| `getAllTasksSortedByDate()` | All tasks, ordered by `dueDate ASC` |
| `insert()` | Add new task |
| `update()` | Update existing task |
| `delete()` | Remove a task |

All write operations run on a **background thread** via `ExecutorService`.

---

## Key Libraries Used

| Library | Purpose |
|---|---|
| **Room** | Local SQLite database ORM |
| **LiveData** | Reactive data observation (auto UI updates) |
| **ViewModel** | Lifecycle-aware state holder |
| **RecyclerView** | Efficient task list rendering |
| **Material Components** | FAB, MaterialTextView, theming |
| **ItemTouchHelper** | Swipe-to-delete gesture |
| **ListAdapter + DiffUtil** | Efficient list diffing & updates |

---

## What's NOT Yet Implemented (Potential Enhancements)

| Feature | Notes |
|---|---|
| Search / keyword filter | No search bar currently |
| Notifications / reminders | No alarm or notification for due dates |
| Task categories / tags | No grouping beyond priority |
| Undo delete (Snackbar) | Swipe delete is permanent — no undo |
| Dark mode theming | `values-night/themes.xml` exists but may not be fully styled |
| Due date overdue highlight | No visual indicator if a task is past due |
| Drag to reorder | `ItemTouchHelper` only handles swipe, not drag |
