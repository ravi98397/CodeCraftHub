# CodeCraftHub

CodeCraftHub is a beginner-friendly REST API for keeping track of courses you want to learn. It is built with Java 21 and Spring Boot, and saves course data to `courses.json` in the application's working directory. It does not use a database, authentication, or user accounts.

## Features

- Create, list, view, update, and delete courses
- Automatically assigns course IDs and creation timestamps
- Validates required fields, ISO dates, and the allowed course statuses
- Creates `courses.json` automatically when the application starts
- Returns clear JSON errors for invalid requests, missing courses, and file errors

## Project structure

```text
src/main/java/com/api/CodeCraftHub/
├── CodeCraftHubApplication.java
├── controller/
│   ├── ApiExceptionHandler.java
│   └── CourseController.java
├── model/
│   └── Course.java
└── service/
    ├── CourseNotFoundException.java
    ├── CourseService.java
    ├── CourseStorageException.java
    └── InvalidCourseException.java
src/main/resources/
└── application.yaml
```

The controller handles HTTP requests, the service contains the file and course logic, and the model represents a course. Gradle configuration is in `build.gradle`; this project uses Gradle rather than Maven.

## Installation

1. Install a JDK (version 21 or newer).
2. Open a terminal in the project directory.
3. Check that Java is available:

   ```powershell
   java -version
   ```

The included Gradle wrapper downloads and uses the project's configured Gradle version, so a separate Gradle installation is not required.

## Run the application

In PowerShell, from the project directory:

```powershell
.\gradlew.bat bootRun
```

The API starts at `http://localhost:8080`. On first startup, the app creates `courses.json` in the current working directory with an empty JSON array (`[]`). Course changes are persisted there.

To run the tests:

```powershell
.\gradlew.bat test
```

## API documentation

All endpoints use the `/api/courses` path. Send and receive JSON. A course has these fields:

| Field | Required in requests? | Description |
|---|---|---|
| `id` | No | Assigned by the API; starts at 1 |
| `name` | Yes | Course name; cannot be blank |
| `description` | Yes | Course description; cannot be blank |
| `target_date` | Yes | Target date in `YYYY-MM-DD` format |
| `status` | Yes | Exactly `Not Started`, `In Progress`, or `Completed` |
| `created_at` | No | Assigned by the API and returned in responses |

### Endpoints

| Method | Path | Purpose | Success response |
|---|---|---|---|
| `POST` | `/api/courses` | Add a course | `201 Created` with the created course |
| `GET` | `/api/courses` | Get all courses | `200 OK` with an array |
| `GET` | `/api/courses/{id}` | Get one course | `200 OK` with the course |
| `PUT` | `/api/courses/{id}` | Replace a course's editable fields (send all required fields) | `200 OK` with the updated course |
| `PATCH` | `/api/courses/{id}` | Update only the provided fields | `200 OK` with the updated course |
| `DELETE` | `/api/courses/{id}` | Delete a course | `204 No Content` |

### Add a course

```powershell
curl.exe -X POST http://localhost:8080/api/courses `
  -H "Content-Type: application/json" `
  -d '{"name":"Spring Boot Basics","description":"Learn to build REST APIs","target_date":"2026-12-31","status":"Not Started"}'
```

Example response:

```json
{
  "id": 1,
  "name": "Spring Boot Basics",
  "description": "Learn to build REST APIs",
  "target_date": "2026-12-31",
  "status": "Not Started",
  "created_at": "2026-11-25T18:00:00Z"
}
```

### Get courses

```powershell
curl.exe http://localhost:8080/api/courses
curl.exe http://localhost:8080/api/courses/1
```

### Update a course

`PUT` requires all editable fields. The ID and original `created_at` remain managed by the API.

```powershell
curl.exe -X PUT http://localhost:8080/api/courses/1 `
  -H "Content-Type: application/json" `
  -d '{"name":"Spring Boot Basics","description":"Build and test REST APIs","target_date":"2027-01-15","status":"In Progress"}'
```

For a partial update, use `PATCH`. Omitted fields keep their current values; any provided status must still be one of the allowed values.

```powershell
curl.exe -X PATCH http://localhost:8080/api/courses/1 `
  -H "Content-Type: application/json" `
  -d '{"status":"In Progress"}'
```

### Delete a course

```powershell
curl.exe -X DELETE http://localhost:8080/api/courses/1
```

### Errors

Errors are JSON objects with an `error` message. Invalid or missing request fields, invalid dates, and invalid status values return `400 Bad Request`; a course ID that does not exist returns `404 Not Found`; file read or write errors return `500 Internal Server Error`.

For example:

```json
{
  "error": "Invalid status. Use exactly: Not Started, In Progress, or Completed."
}
```

## Troubleshooting

- **`java` is not recognized or the Java version is too old:** Install JDK 21 or newer and ensure `java` is on your `PATH`.
- **Port 8080 is already in use:** Stop the other process using that port, or run with another port, for example `.\gradlew.bat bootRun --args='--server.port=8081'`.
- **The app cannot create or update `courses.json`:** Check that the application's working directory is writable. The default file path is relative to that directory. You can choose another path with `--app.courses-file="C:\data\courses.json"` (ensure that directory exists or can be created).
- **A request returns `400 Bad Request`:** Check that `name`, `description`, `target_date`, and `status` are present and non-blank. Use a valid date such as `2026-12-31` and one of the three exact status strings.
- **The app reports invalid JSON in `courses.json`:** Stop the app and restore a valid JSON array. For an empty course list, the file contents can be `[]`. Back up the file before editing it.
- **A request returns `404 Not Found`:** Confirm the course ID exists by requesting `GET /api/courses`.
