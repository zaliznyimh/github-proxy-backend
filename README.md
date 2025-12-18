# GitHub Proxy API

A proxy application that fetches a user's GitHub repositories, filters out forks, and returns branch details (name & SHA).

`Written with Java 25` and `Spring Boot 4.0`

## Technology Stack

- **Java 25**
- **Spring Boot 4.0**
- **Spring Web**
- **RestClient**
- **WireMock**

## Requirements

* JDK 25 installed
* Internet connection for dependencies & GitHub API

## How to Run

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/zaliznyimh/atipera-test-backend.git
    cd github-proxy
    ```

2.  **Build and Run using Gradle wrapper:**
    ```bash
    ./gradlew bootRun
    ```
    *Note: The application starts on port `8080` by default. You can change it in `src/main/resources/application.yaml`* file

## API Usage

### 1. Get User Repositories
Retrieves a list of non-forked repositories with their branches

**Request:**
```http
GET /api/v1/users/{username}/repositories
Accept: application/json
```
**Response 200 (Ok):**
```json
[
  {
    "repositoryName": "atipera-test",
    "ownerLogin": "zaliznyimh",
    "branches": [
      {
        "name": "master",
        "lastCommitSha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d"
      }
    ]
  }
]
```

### 2. Error Handling
**Error Response 404 (NotFound):**
```json
{
  "status": 404,
  "message": "Not found account for user with username: {username}"
}
```

**Error Response 403 (Forbidden):**
```json
{
  "status": 403,
  "message": "GitHub API rate limit exceeded. Please try again later"
}
```

## TESTING
Uses WireMock for integration tests that mock GitHub API responses, verifying the full application flow without internal mocks

## To run tests use:
```bash 
  ./gradlew test
```

