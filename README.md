# 🧠 Brain Booster

Brain Booster is a full-stack learning platform for creating, managing, sharing, and studying flashcards.  
In addition to classic flashcard review, the application provides multiple learning games, detailed attempt history, question-level results, progress analytics, and focused weak-card review sessions.

## 📌 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Environment Variables](#️-environment-variables)
- [Running Locally](#-running-locally)
- [Running with Docker](#-running-with-docker)
- [Running with Makefile](#-running-with-makefile)
- [Roles and Authorization](#-roles-and-authorization)
- [Learning Games and Analytics](#-learning-games-and-analytics)
- [API Overview](#-api-overview)
- [OpenAPI / Swagger](#-openapi--swagger)
- [Roadmap](#-roadmap)
- [License](#-license)
- [Author](#-author)

## ✨ Features

- **Authentication and Registration**  
  Secure user registration and login using JWT tokens.

- **Flashcard Set Management**  
  Users can create, edit, delete, and share their own flashcard sets.

- **Flashcard Management**  
  Users can add, update, remove, and study flashcards inside their sets.

- **Folder Organization**  
  Flashcard sets can be grouped into user-owned folders.

- **Classic Study Mode**  
  Interactive flashcard review with card flipping, navigation, text-to-speech, shuffling, and local known/unknown progress tracking.

- **Learning Games**  
  Four supported game modes: Multiple Choice, Written, Matching, and Custom Test.

- **Custom Test Questions**  
  Custom tests can combine multiple-choice, written, matching, and true/false questions.

- **Score and Time Tracking**  
  Completed games store the score, number of questions, duration, game mode, and completion time.

- **Attempt History**  
  Every completed game creates a separate attempt that can be reviewed later.

- **Question-Level Results**  
  Attempt details include prompts, user answers, correct answers, correctness, mistake counts, answer side, and question type.

- **Learning Analytics**  
  Per-set analytics include total attempts, accuracy, average and best scores, average duration, progress over time, and question-type performance.

- **Weak Flashcards**  
  The application identifies cards with low accuracy and provides a focused weak-card review session.

- **User Profiles and Public Sets**  
  Users can browse public profiles, folders, and public flashcard sets created by other users.

- **Role-Based Access Control**  
  The application supports regular user and administrator roles.

- **Light and Dark Theme**  
  The interface supports light and dark modes, including automatic system theme detection and manual theme switching.

- **Responsive UI**  
  Modern, clean, and responsive interface for desktop and mobile devices.

## 🛠 Tech Stack

### Backend

- Java 25 LTS
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Springdoc OpenAPI / Swagger UI
- PostgreSQL
- JWT
- Flyway
- Gradle
- Lombok

### Frontend

- TypeScript
- Next.js
- React
- Tailwind CSS
- Shadcn UI
- next-themes
- Recharts
- Lucide React
- pnpm

## 📂 Project Structure

The project is divided into two main modules:

```txt
brain-booster/
├── docker-compose.yml
├── Makefile
├── .env.example
├── brain-booster-backend/
│   ├── src/
│   ├── gradle/
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   └── .env_example
│
└── brain-booster-frontend/
    ├── public/
    ├── src/
    ├── Dockerfile
    ├── .dockerignore
    ├── package.json
    ├── pnpm-lock.yaml
    ├── pnpm-workspace.yaml
    ├── next.config.ts
    ├── tsconfig.json
    ├── components.json
    └── .env.local_example
```

### Backend

`brain-booster-backend/` contains the business logic, REST API, security configuration, database access layer, repositories, and Flyway migration scripts.

Main backend domains include:

- `auth`
- `user`
- `flashcard`
- `flashcardset`
- `folder`
- `gameresult`
  - `attempt`
  - `questionresult`
  - `analytics`

### Frontend

`brain-booster-frontend/` contains the client application built with the Next.js App Router.  
API services are separated inside `src/api/`, reusable data hooks are stored inside `src/hooks/`, and game-related TypeScript models are stored inside `src/types/`.

## ✅ Prerequisites

Before running the project locally, make sure you have installed:

- Java 25 LTS
- Node.js v24 or newer
- pnpm
- PostgreSQL
- Docker Engine/Desktop
- Make (optional, for shortcut commands from `Makefile`)
- Git

You also need a configured local PostgreSQL database when running the backend without Docker.  
When using Docker Compose, PostgreSQL is started automatically as a container.

## ⚙️ Environment Variables

### Backend

Go to the backend directory:

```bash
cd brain-booster-backend
```

Copy the example environment file:

```bash
cp .env_example .env
```

Example backend environment variables:

```env
POSTGRES_USERNAME=your_postgres_username
POSTGRES_PASSWORD=your_postgres_password
JWT_SECRET_KEY=your_secret_key
CLIENT_URL=http://localhost:3000
```

> **Note:** A `.env` file is not automatically loaded by Spring Boot unless additional configuration is added.  
> You can set environment variables in your terminal, IDE configuration, or deployment environment.

Example terminal setup:

```bash
export POSTGRES_USERNAME=postgres
export POSTGRES_PASSWORD=postgres
export JWT_SECRET_KEY=your_secret_key
export CLIENT_URL=http://localhost:3000
```

### Frontend

Go to the frontend directory:

```bash
cd brain-booster-frontend
```

Copy the example environment file:

```bash
cp .env.local_example .env.local
```

Example frontend environment variables for local development:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
API_INTERNAL_URL=http://localhost:8080/api/v1
```

`NEXT_PUBLIC_API_URL` is used by the browser.
`API_INTERNAL_URL` is used by Next.js when data is fetched on the server side, for example inside `page.tsx` server components.

When the frontend is started locally with `pnpm next dev` and the backend is exposed on port `8080`, both values can point to `localhost`.
When the frontend runs inside Docker Compose, `API_INTERNAL_URL` should use the backend service name instead: `http://backend:8080/api/v1`.

### Docker Compose

For Docker Compose, create a `.env` file in the project root based on `.env.example`:

```bash
cp .env.example .env
```

Example Docker environment variables:

```env
# PostgreSQL
POSTGRES_DB=brain-booster
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=postgres

# Backend
CLIENT_URL=http://localhost:3000
SPRING_PROFILES_ACTIVE=dev
JWT_EXPIRATION_HOURS=24
JWT_SECRET_KEY=change-me-to-a-long-random-secret-key

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
API_INTERNAL_URL=http://backend:8080/api/v1
```

`NEXT_PUBLIC_API_URL` should point to `localhost` because this value is used by the browser on the host machine.  
`API_INTERNAL_URL` should point to the backend service name because this value is used by Next.js server-side code running inside the frontend container.  
Inside Docker Compose, the backend connects to PostgreSQL using the `database` service name, and the frontend connects to the backend using the `backend` service name.

## 🚀 Running Locally

### 1. Clone the Repository

```bash
git clone https://github.com/chrisitstyle/brain-booster.git
cd brain-booster
```

### 2. Run the Backend

Go to the backend directory:

```bash
cd brain-booster-backend
```

Make sure your PostgreSQL database is running and environment variables are configured.

Then run the application:

```bash
./gradlew bootRun
```

To expose OpenAPI documentation and Swagger UI locally, run the backend with the `dev` profile enabled:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

Alternative:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> **Note:** Remember to configure the required environment variables based on `.env_example` before starting the backend.  
> Spring Boot does not automatically load `.env` files unless additional configuration is added.

Flyway migrations should run automatically during application startup.

The backend will be available at:

```txt
http://localhost:8080/api/v1
```

### 3. Run the Frontend

Open a second terminal and go to the frontend directory:

```bash
cd brain-booster-frontend
```

Install dependencies:

```bash
pnpm install
```

Run the development server:

```bash
pnpm next dev
```

The frontend will be available at:

```txt
http://localhost:3000
```

## 🐳 Running with Docker

The project can also be started with Docker Compose directly or through the included `Makefile`. This starts three services:

- PostgreSQL database
- Spring Boot backend
- Next.js frontend

### 1. Prepare Environment Variables

From the project root, copy the Docker environment example:

```bash
cp .env.example .env
```

Then update the values if needed, especially:

```env
JWT_SECRET_KEY=change-me-to-a-long-random-secret-key
SPRING_PROFILES_ACTIVE=dev
JWT_EXPIRATION_HOURS=24
```

### 2. Build and Start Containers

Run the application from the project root:

```bash
docker compose up --build
```

After startup, the services will be available at:

```txt
Frontend: http://localhost:3000
Backend:  http://localhost:8080/api/v1
Database: localhost:5432
```

### 3. Stop Containers

```bash
docker compose down
```

To remove the database volume as well, run:

```bash
docker compose down -v
```

### Useful Docker Commands

Rebuild only the frontend:

```bash
docker compose build --no-cache frontend
```

Rebuild only the backend:

```bash
docker compose build --no-cache backend
```

View logs:

```bash
docker compose logs -f
```

View backend logs only:

```bash
docker compose logs -f backend
```

## 🧾 Running with Makefile

The project includes a `Makefile` with shortcut commands for common Docker Compose workflows.

Available commands:

| Command      | Description                                                |
| ------------ | ---------------------------------------------------------- |
| `make up`    | Recreates containers and starts the application with build |
| `make up-nc` | Rebuilds images without cache and starts the application   |
| `make down`  | Stops containers and removes Docker volumes                |

### Start the Application

```bash
make up
```

This command runs:

```bash
docker compose down -v && docker compose up --build
```

### Rebuild Without Cache

```bash
make up-nc
```

This is useful when Docker cache causes outdated dependencies, images, or build artifacts to be reused.

### Stop the Application

```bash
make down
```

> **Note:** The current Makefile commands use `docker compose down -v`, which removes Docker volumes.  
> This means the PostgreSQL database data is reset after running these commands.

## 🔐 Roles and Authorization

The application supports two user roles:

### `USER`

A regular user can:

- browse public profiles, folders, flashcard sets, and flashcards,
- manage their own profile,
- create, edit, and delete their own flashcard sets,
- create, edit, and delete flashcards inside their own sets,
- organize sets into folders,
- study accessible flashcard sets,
- complete learning games,
- view their own results and attempt history,
- review question-level attempt details,
- view their own learning analytics,
- review weak flashcards.

### `ADMIN`

An admin has all user permissions and additional access to administrative API endpoints for managing users and viewing system-wide game results.

Authorization is based on JWT tokens sent in the request header:

```txt
Authorization: Bearer <token>
```

## 🎮 Learning Games and Analytics

Brain Booster supports four game modes:

| Game Mode       | API Value         | Description                                      |
| --------------- | ----------------- | ------------------------------------------------ |
| Multiple Choice | `multiple-choice` | Select the correct answer from available options |
| Written         | `written`         | Type the answer manually                         |
| Matching        | `matching`        | Match related terms and definitions              |
| Custom Test     | `custom-test`     | Build a mixed test from supported question types |

Custom Test supports the following question types:

```txt
multiple-choice
written
matching
true-false
```

### Result Persistence

Submitting a completed game to `POST /game-results` performs two related operations:

1. The latest result for the authenticated user, flashcard set, and game mode is created or updated.
2. A new immutable game attempt is saved for the attempt history.

When question results are included in the request, each answer is also stored with its question type, prompt, user answer, correct answer, correctness, mistake count, and related flashcard.

The backend stores learning data in three layers:

- `game_results` — the latest result for each user, set, and game mode,
- `game_attempts` — every completed game attempt,
- `game_question_results` — individual answers belonging to an attempt.

### Learning Analytics

Analytics are calculated only from the authenticated user's own attempts and include:

- total attempts,
- overall accuracy,
- average score,
- best score,
- average duration,
- date of the latest attempt,
- score percentage progress over time,
- weak flashcards,
- accuracy grouped by question type.

Weak-flashcard accuracy is calculated from the user's correct and total answers for a particular flashcard. The focused review page uses these results to create a practice session containing only cards that need additional work.

### Frontend Learning Routes

| Route                                        | Description                                                |
| -------------------------------------------- | ---------------------------------------------------------- |
| `/profile/settings`                          | Manage the authenticated user's nickname and email address |
| `/profile/stats`                             | Entry page for selecting a flashcard set                   |
| `/profile/sets/{setId}/stats`                | Detailed learning analytics for a set                      |
| `/profile/sets/{setId}/attempts`             | Attempt history for a set                                  |
| `/profile/sets/{setId}/attempts/{attemptId}` | Summary and question-level details of an attempt           |
| `/profile/sets/{setId}/weak-cards`           | Focused weak-card review session                           |

## 📡 API Overview

The backend exposes a REST API under the following base path:

```txt
/api/v1
```

### Authentication

| Method | Endpoint             | Description                             | Access |
| ------ | -------------------- | --------------------------------------- | ------ |
| POST   | `/auth/authenticate` | Authenticate user and receive JWT token | Public |
| POST   | `/auth/register`     | Register a new user                     | Public |

### Users

| Method | Endpoint                                    | Description                          | Access             |
| ------ | ------------------------------------------- | ------------------------------------ | ------------------ |
| GET    | `/users/me`                                 | Get authenticated user's information | Authenticated user |
| GET    | `/users`                                    | Get all users                        | Admin              |
| POST   | `/users`                                    | Create a new user                    | Admin              |
| GET    | `/users/nickname/{nickname}/flashcard-sets` | Get flashcard sets by user nickname  | Public             |
| GET    | `/users/{nickname}/folders`                 | Get folders by user nickname         | Public             |
| GET    | `/users/{userId}`                           | Get user by ID                       | Admin              |
| PUT    | `/users/{userId}`                           | Update user by ID                    | Admin              |
| DELETE | `/users/{userId}`                           | Delete user by ID                    | Admin              |
| GET    | `/users/{userId}/flashcard-sets`            | Get flashcard sets by user ID        | Public             |

### Profile Settings

| Method | Endpoint                     | Description          | Access             |
| ------ | ---------------------------- | -------------------- | ------------------ |
| PATCH  | `/profile/settings/nickname` | Update user nickname | Authenticated user |
| PATCH  | `/profile/settings/email`    | Update user email    | Authenticated user |

> **Note:** After a successful email update, the endpoint returns a new JWT token because the email address is used as the authentication username.

### Flashcards

| Method | Endpoint                    | Description            | Access             |
| ------ | --------------------------- | ---------------------- | ------------------ |
| GET    | `/flashcards`               | Get all flashcards     | Public             |
| POST   | `/flashcards`               | Create a new flashcard | Authenticated user |
| GET    | `/flashcards/{flashcardId}` | Get flashcard by ID    | Public             |
| PATCH  | `/flashcards/{flashcardId}` | Update flashcard by ID | Authenticated user |
| DELETE | `/flashcards/{flashcardId}` | Delete flashcard by ID | Authenticated user |

### Flashcard Sets

| Method | Endpoint                             | Description                         | Access             |
| ------ | ------------------------------------ | ----------------------------------- | ------------------ |
| GET    | `/flashcard-sets`                    | Get all flashcard sets              | Public             |
| POST   | `/flashcard-sets`                    | Create a new flashcard set          | Authenticated user |
| GET    | `/flashcard-sets/{setId}`            | Get flashcard set by ID             | Public             |
| PATCH  | `/flashcard-sets/{setId}`            | Update flashcard set by ID          | Authenticated user |
| DELETE | `/flashcard-sets/{setId}`            | Delete flashcard set by ID          | Authenticated user |
| GET    | `/flashcard-sets/{setId}/flashcards` | Get flashcards from a flashcard set | Public             |

### Folders

| Method | Endpoint                           | Description                        | Access             |
| ------ | ---------------------------------- | ---------------------------------- | ------------------ |
| GET    | `/folders`                         | Get all folders                    | Admin              |
| POST   | `/folders`                         | Create a new folder                | Authenticated user |
| GET    | `/folders/me`                      | Get folders of authenticated user  | Authenticated user |
| GET    | `/folders/{folderId}`              | Get folder details by ID           | Public             |
| PATCH  | `/folders/{folderId}`              | Update folder name and description | Authenticated user |
| DELETE | `/folders/{folderId}`              | Delete folder by ID                | Authenticated user |
| POST   | `/folders/{folderId}/sets/{setId}` | Add flashcard set to folder        | Authenticated user |
| DELETE | `/folders/{folderId}/sets/{setId}` | Remove flashcard set from folder   | Authenticated user |

### Game Results

`POST /game-results` updates the latest result for a user, set, and mode while also creating a new game attempt. If the request contains question results, they are persisted together with the attempt.

| Method | Endpoint                         | Description                                                                 | Access                |
| ------ | -------------------------------- | --------------------------------------------------------------------------- | --------------------- |
| POST   | `/game-results`                  | Save the latest result and create a new attempt with optional question data | Authenticated user    |
| GET    | `/game-results/me`               | Get the authenticated user's latest results                                 | Authenticated user    |
| GET    | `/game-results/me?setId={setId}` | Get the authenticated user's latest results for a specific flashcard set    | Authenticated user    |
| GET    | `/game-results`                  | Get all latest game results                                                 | Admin                 |
| GET    | `/game-results?setId={setId}`    | Get all latest game results for a specific flashcard set                    | Admin                 |
| GET    | `/game-results/{resultId}`       | Get a latest game result by ID                                              | Result owner or admin |
| DELETE | `/game-results/{resultId}`       | Delete a latest game result by ID                                           | Result owner or admin |

### Game Attempts

| Method | Endpoint                                      | Description                                          | Access                 |
| ------ | --------------------------------------------- | ---------------------------------------------------- | ---------------------- |
| GET    | `/game-attempts/me`                           | Get paginated attempts of the authenticated user     | Authenticated user     |
| GET    | `/game-attempts/me/sets/{setId}`              | Get paginated attempts for a specific flashcard set  | Authenticated user     |
| GET    | `/game-attempts/{attemptId}`                  | Get the summary of a single game attempt             | Attempt owner or admin |
| GET    | `/game-attempts/{attemptId}/question-results` | Get question-level results for a single game attempt | Attempt owner or admin |

#### Game Attempt Query Parameters

`GET /game-attempts/me` supports filtering, pagination, and sorting:

| Query Parameter | Description                                                              | Example                 |
| --------------- | ------------------------------------------------------------------------ | ----------------------- |
| `page`          | Zero-based page number                                                   | `page=0`                |
| `size`          | Number of attempts returned per page. The default is `20`                | `size=20`               |
| `sort`          | Spring Data sort expression                                              | `sort=completedAt,desc` |
| `setId`         | Filter attempts by flashcard set ID                                      | `setId=12`              |
| `mode`          | Filter attempts by game mode                                             | `mode=written`          |
| `from`          | Include attempts completed on or after this date in `YYYY-MM-DD` format  | `from=2026-01-01`       |
| `to`            | Include attempts completed on or before this date in `YYYY-MM-DD` format | `to=2026-01-31`         |

`GET /game-attempts/me/sets/{setId}` supports the same parameters except for `setId`, which is already part of the path.

The default attempt ordering is:

```txt
completedAt,DESC
```

Supported game mode values:

```txt
multiple-choice
written
matching
custom-test
```

Examples:

```http
GET /game-attempts/me?page=0&size=20
GET /game-attempts/me?setId=12&mode=written
GET /game-attempts/me?from=2026-01-01&to=2026-01-31
GET /game-attempts/me?setId=12&sort=completedAt,desc
GET /game-attempts/me/sets/12?page=0&size=20
GET /game-attempts/me/sets/12?mode=custom-test&from=2026-01-01&to=2026-01-31
```

### Game Analytics

| Method | Endpoint                                          | Description                                                          | Access             |
| ------ | ------------------------------------------------- | -------------------------------------------------------------------- | ------------------ |
| GET    | `/game-analytics/me/sets/{setId}/summary`         | Get summary analytics for the authenticated user's attempts in a set | Authenticated user |
| GET    | `/game-analytics/me/sets/{setId}/progress`        | Get score percentage progress for attempts in a set                  | Authenticated user |
| GET    | `/game-analytics/me/sets/{setId}/weak-flashcards` | Get weak flashcards calculated from question-level results           | Authenticated user |
| GET    | `/game-analytics/me/sets/{setId}/question-types`  | Get accuracy grouped by question type                                | Authenticated user |

> **Note:** Game attempt and analytics endpoints only expose learning data that belongs to the currently authenticated user, unless an endpoint explicitly allows administrator access.

## 📖 OpenAPI / Swagger

The backend provides automatically generated OpenAPI documentation using Springdoc OpenAPI and Swagger UI.

OpenAPI and Swagger UI are intended for local development and API testing. In this project, they should be enabled when the backend runs with the `dev` profile. The default application configuration may keep them disabled, so make sure `SPRING_PROFILES_ACTIVE=dev` is set when you want to use Swagger.

When the backend is running locally, the API documentation is available at:

```txt
Swagger UI:   http://localhost:8080/api/v1/swagger-ui.html
OpenAPI JSON: http://localhost:8080/api/v1/v3/api-docs
```

When running the application with Docker Compose, the `.env` file should include:

```env
SPRING_PROFILES_ACTIVE=dev
```

### Using Protected Endpoints in Swagger UI

Some endpoints require JWT authentication. To test protected endpoints in Swagger UI:

1. Authenticate with the login endpoint:

   ```txt
   POST /auth/authenticate
   ```

2. Copy the returned JWT token.

3. Click **Authorize** in Swagger UI.

4. Enter the token using the Bearer format:

   ```txt
   Bearer <token>
   ```

After authorization, Swagger UI can be used to call endpoints that require an authenticated user or admin role.

### Troubleshooting

If Swagger UI is not available, check that:

- the backend is running with the `dev` profile,
- `springdoc.api-docs.enabled=true` is configured for the active profile,
- `springdoc.swagger-ui.enabled=true` is configured for the active profile,
- the URL includes the backend context path: `/api/v1`.

## 🗺 Roadmap

Planned features and improvements:

- **Spaced Repetition**  
  Schedule future reviews based on previous answers and card difficulty.

- **Retry Incorrect Answers**  
  Start a new practice session directly from incorrect answers in a selected attempt.

- **Daily Goals and Learning Streaks**  
  Add daily card targets, completion indicators, and consecutive-day streaks.

- **Configurable Weak-Card Thresholds**  
  Allow users to choose the accuracy threshold used to classify a flashcard as weak.

- **Favorites and Saved Sets**  
  Allow users to save public flashcard sets to their own library.

- **Search, Filtering, and Sorting**  
  Improve browsing public flashcard sets with search, filters, and sorting options.

- **Frontend Route Guard Improvements**  
  Improve authentication-based and role-based route protection on the frontend.

- **Automated Tests**  
  Expand backend and frontend test coverage for games, attempts, analytics, and authorization.

- **Deployment Improvements**  
  Extend the Docker setup with production deployment configuration and CI/CD automation.

## 📄 License

No license has been specified for this project yet.

## 👤 Author

Created by [chrisitstyle](https://github.com/chrisitstyle).

Project repository: [brain-booster](https://github.com/chrisitstyle/brain-booster)
