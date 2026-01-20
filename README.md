# Genie Backend

A Java-based backend API for the Genie chat/messaging application.

## Overview

Genie Backend provides RESTful API endpoints for a real-time messaging application. It handles user authentication, friend management, and chat functionality.

## Tech Stack

- **Java** - Core programming language
- **Java Servlets** - REST API endpoints
- **Hibernate ORM** - Database persistence
- **MySQL** - Database
- **GlassFish** - Application server
- **Gson** - JSON processing
- **Apache Ant** - Build tool (NetBeans project)

## Features

- User registration and authentication (Sign Up / Sign In)
- Friend search and management
- Real-time chat messaging
- User avatar support
- Chat history loading

## Project Structure

```
src/
├── java/
│   ├── controller/     # Servlet controllers (API endpoints)
│   ├── entity/         # JPA/Hibernate entity classes
│   └── model/          # Utility classes
└── conf/               # Configuration files

web/
├── WEB-INF/           # Web application configuration
└── index.html         # Default page
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `/SignUp` | User registration |
| `/SignIn` | User authentication |
| `/SearchFriends` | Search for users |
| `/AddFriends` | Add friends |
| `/LoadChat` | Load chat messages |
| `/SendMessage` | Send a message |
| `/LoadHomeData` | Load home screen data |

## Database Configuration

The application connects to a MySQL database. Configuration can be found in `src/java/hibernate.cfg.xml`.

## Building

This project uses Apache Ant for building. To build the project:

```bash
ant build
```

## Requirements

- Java JDK 8 or higher
- MySQL Server
- GlassFish Server (or compatible Java EE server)

## License

This project is for educational purposes.
