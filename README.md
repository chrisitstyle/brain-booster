# 🧠 Brain Booster

Brain Booster is a full-stack web application for creating, managing, and studying with flashcards.  
The project allows users to learn effectively through interactive flashcard sets and share their collections with others.

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
- [API Overview](#-api-overview)
- [OpenAPI / Swagger](#-openapi--swagger)
- [Roadmap](#-roadmap)
- [License](#-license)
- [Author](#-author)

## ✨ Features

- **Authentication and Registration**  
  Secure user registration and login using JWT tokens.

- **Flashcard Set Management**  
  Users can create, edit, and delete their own flashcard sets.

- **Flashcard Management**  
  Users can add, update, and remove flashcards inside their sets.

- **Study Mode**  
  Interactive learning mode for reviewing flashcards and checking knowledge.

- **User Profiles**  
  Users can browse public profiles and view public flashcard sets created by other users.

- **Public Flashcard Sets**  
  Users can browse public learning materials created by others.

- **Role-Based Access Control**  
  The application supports user and admin roles.

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

### Frontend

`brain-booster-frontend/` contains the client application built with Next.js.  
Routing is handled by the Next.js routing system, and API services are separated inside the `src/api/` directory.

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

- browse public resources,
- manage their own profile,
- create, edit, and delete their own flashcard sets,
- create, edit, and delete flashcards inside their own sets.

### `ADMIN`

An admin has all user permissions and additional access to administrative API endpoints for managing users.

Authorization is based on JWT tokens sent in the request header:

```txt
Authorization: Bearer <token>
```

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

| Method | Endpoint                                    | Description                         | Access             |
| ------ | ------------------------------------------- | ----------------------------------- | ------------------ |
| GET    | `/users`                                    | Get all users                       | Admin              |
| POST   | `/users`                                    | Create a new user                   | Admin              |
| GET    | `/users/nickname/{nickname}/flashcard-sets` | Get flashcard sets by user nickname | Public             |
| GET    | `/users/{nickname}/folders`                 | Get folders by user nickname        | Public             |
| GET    | `/users/{userId}`                           | Get user by ID                      | Admin              |
| PUT    | `/users/{userId}`                           | Update user by ID                   | Admin              |
| DELETE | `/users/{userId}`                           | Delete user by ID                   | Admin              |
| GET    | `/users/{userId}/flashcard-sets`            | Get flashcard sets by user ID       | Public             |
| PATCH  | `/users/{userId}/nickname`                  | Update user nickname                | Authenticated user |

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

| Method | Endpoint                         | Description                                                        | Access                |
| ------ | -------------------------------- | ------------------------------------------------------------------ | --------------------- |
| POST   | `/game-results`                  | Save or update the authenticated user's latest game result         | Authenticated user    |
| GET    | `/game-results/me`               | Get all game results of the authenticated user                     | Authenticated user    |
| GET    | `/game-results/me?setId={setId}` | Get authenticated user's game results for a specific flashcard set | Authenticated user    |
| GET    | `/game-results`                  | Get all game results                                               | Admin                 |
| GET    | `/game-results?setId={setId}`    | Get all game results for a specific flashcard set                  | Admin                 |
| GET    | `/game-results/{resultId}`       | Get game result by ID                                              | Result owner or admin |
| DELETE | `/game-results/{resultId}`       | Delete game result by ID                                           | Result owner or admin |

### Game Attempts

| Method | Endpoint                         | Description                                                            | Access                 |
| ------ | -------------------------------- | ---------------------------------------------------------------------- | ---------------------- |
| GET    | `/game-attempts/me`              | Get all game attempts of the authenticated user                        | Authenticated user     |
| GET    | `/game-attempts/me/sets/{setId}` | Get authenticated user's game attempts for a specific flashcard set    | Authenticated user     |
| GET    | `/game-attempts/{attemptId}`     | Get details of a single game attempt, including question-level results | Attempt owner or admin |

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

- **Quiz / Test Mode**  
  Add a mode where users can test their knowledge using questions based on their flashcards.

- **Learning Games**  
  Introduce game-based learning modes to make studying more interactive and engaging.

- **Progress Tracking**  
  Add statistics for study sessions, completed flashcards, and learning progress over time.

- **Spaced Repetition**  
  Implement a review system based on spaced repetition to improve long-term memorization.

- **Favorites and Saved Sets**  
  Allow users to save public flashcard sets to their own library.

- **Search, Filtering, and Sorting**  
  Improve browsing public flashcard sets with search, filters, and sorting options.

- **Frontend Route Guards Improvements**  
  Improve authentication-based and role-based route protection on the frontend.

- **Automated Tests**  
  Expand backend and frontend test coverage.

- **Deployment Improvements**  
  Extend the Docker setup with production deployment configuration and CI/CD automation.

## 📄 License

No license has been specified for this project yet.

## 👤 Author

Created by [chrisitstyle](https://github.com/chrisitstyle).

Project repository: [brain-booster](https://github.com/chrisitstyle/brain-booster)
