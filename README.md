# Authentication Service

Spring Boot microservice handling organizations, employees, admin accounts, and authentication (credentials + LinkedIn OAuth) for the recruitment management platform.

## Key Features

- Organization signup (with logo upload), login, refresh, logout
- Employee creation and login under an organization
- Admin login and organization approval workflow (accept/reject organization status)
- LinkedIn OAuth login and signup completion
- Password reset via email verification
- JWT-based authentication with Redis-backed sessions
- LDAP integration for organization directory data
- File uploads via Cloudinary

## Tech Stack

- Java 21, Spring Boot 4.1.0
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL + Flyway migrations
- Spring Data Redis
- Spring LDAP (OpenLDAP)
- Spring Mail
- JWT (jjwt 0.12.6)
- Cloudinary (file uploads)
- Lombok
- Maven

## Prerequisites

- Java 21
- Docker (for PostgreSQL, Redis, and OpenLDAP)

## Setup

Start the required infrastructure:

```bash
docker compose up -d
```

This starts:

| Service | Port |
|---|---|
| PostgreSQL | 5433 |
| Redis | 6379 |
| OpenLDAP | 389, 636 |

Run the application with the `dev` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The service starts on port **8081**.

## Environment Variables

Set these before running with the `dev` profile (see `src/main/resources/application-dev.yaml`):

| Variable | Purpose |
|---|---|
| `MAIL_USERNAME` | SMTP username for password reset emails |
| `MAIL_PASSWORD` | SMTP password |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `JWT_SECRET` | JWT signing secret |
| `LINKEDIN_CLIENT_ID` | LinkedIn OAuth client ID |
| `LINKEDIN_CLIENT_SECRET` | LinkedIn OAuth client secret |
| `LINKEDIN_REDIRECT_URI` | LinkedIn OAuth redirect URI |
| `ADMIN_ID` | Bootstrap admin ID |
| `ADMIN_EMAIL` | Bootstrap admin email |
| `ADMIN_PASSWORD` | Bootstrap admin password |
| `FRONTEND_URL` | Frontend origin, used for redirects/CORS (default `http://localhost:5173`) |
| `APP_COOKIE_CROSS_SITE` | Set `true` if frontend and backend are on different sites (default `false`) |

## API Documentation

Interactive Swagger UI is available at:

```
http://localhost:8081/docs
```

Raw OpenAPI spec: `http://localhost:8081/v3/api-docs`

## API Endpoints

Base path: `/api/v1`

### Organizations (`/organizations`)

| Method | Path | Description |
|---|---|---|
| POST | `/organizations` | Register organization (multipart, with logo) |
| POST | `/organizations/login` | Organization login |
| POST | `/organizations/refresh` | Refresh access token |
| POST | `/organizations/logout` | Logout |
| GET | `/organizations/lookup` | Look up organization |
| GET | `/organizations/profile` | Get organization profile |
| POST | `/organizations/forgot-password` | Request password reset |
| POST | `/organizations/reset-password/verify` | Verify reset token |
| POST | `/organizations/reset-password` | Reset password |
| GET | `/organizations/linkedin` | Start LinkedIn OAuth flow |
| GET | `/organizations/linkedin/callback` | LinkedIn OAuth callback |
| POST | `/organizations/linkedin/signup/complete` | Complete LinkedIn signup (multipart) |

### Employees (`/employees`)

| Method | Path | Description |
|---|---|---|
| POST | `/employees` | Create employee |
| POST | `/employees/login` | Employee login |
| GET | `/employees/profile` | Get employee profile |
| GET | `/employees` | List employees |

### Admin (`/admin`)

| Method | Path | Description |
|---|---|---|
| POST | `/admin/login` | Admin login |
| GET | `/admin/organizations` | List organizations |
| GET | `/admin/organizations/{organizationId}` | Get organization by ID |
| PATCH | `/admin/organizations/{organizationId}/status` | Update organization status |

## Database

PostgreSQL, managed with Flyway migrations in `src/main/resources/database/migration`:

- `V1__create_organizations_table.sql`
- `V2__add_linkedin_auth.sql`
- `V3__create_employees.sql`
