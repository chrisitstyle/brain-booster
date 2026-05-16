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
- [Roles and Authorization](#-roles-and-authorization)
- [API Overview](#-api-overview)
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
├── brain-booster-backend/
│   ├── src/
│   ├── gradle/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   └── .env_example
│
└── brain-booster-frontend/
    ├── public/
    ├── src/
    ├── package.json
    ├── pnpm-lock.yaml
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
- Node.js v22 or newer
- pnpm
- PostgreSQL
- Git

You also need a configured local PostgreSQL database.

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

Example frontend environment variable:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

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

## 🗺 Roadmap

Planned features and improvements:

- **OpenAPI / Swagger Documentation**  
  Add automatically generated API documentation to make backend testing and integration easier.

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

- **Docker Support**  
  Add Docker or Docker Compose configuration for easier local development and deployment.

## 📄 License

No license has been specified for this project yet.

## 👤 Author

Created by [chrisitstyle](https://github.com/chrisitstyle).

Project repository: [brain-booster](https://github.com/chrisitstyle/brain-booster)
