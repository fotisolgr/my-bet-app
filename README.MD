
---

## 🚀 Technologies Used

| Layer      | Technology  |
|------------|-------------|
| Frontend   | Angular     |
| Backend    | Spring Boot |
| Auth       | Keycloak    |
| Database   | PostgreSQL  |

---

## 🔐 Authentication Flow

1. The user accesses the frontend and is redirected to **Keycloak** to log in or register with an email.
2. Upon successful login, **Keycloak** issues a **JWT**.
3. The frontend stores this token and includes it in API requests.
4. The **Spring Boot** backend validates the token against Keycloak and processes the request.
5. The backend communicates with **PostgreSQL** to fetch or store data.

---
## API Endpoints

### `GET /bet/matches`

Fetches a paginated list of matches.

**Query Parameters:**

| Name      | Type      | Default   | Description                                                |
|-----------|-----------|-----------|------------------------------------------------------------|
| page      | int       | 0         | The page number to retrieve                                |
| size      | int       | 10        | Number of items per page                                   |
| sortBy    | string    | matchDate | Field to sort by (e.g., `owner`, `sport`, `dateTime`)      |
| direction | string    | desc      | Sort direction (`asc` or `desc`)                           |
| owner     | string    | -         | Filtering option (optional)                                |
| sport     | string    | -         | Filtering option (optional)                                |
| matchDate | LocalDate | -         | Filtering option (optional)                                |

**Responses:**

| Status Code               | Description                                                               |
|---------------------------|---------------------------------------------------------------------------|
| 200 OK                    | Request was successful (e.g. fetching matches, updating a match).         |
| 500 Internal Server Error | A server-side error occurred while processing the request.                |

### `POST /bet/saveMatch  (SECURED)`

Saves a match.

**Request body Parameters:**

| Name        | Type   | Default   | Description                                |
|-------------|--------|-----------|--------------------------------------------|
| description | String | 0         | The match description                      |
| matchDate   | String | 10        | Date of match                              |
| matchTime   | String | dateTime  | Time of match                              |
| teamA       | String | desc      | First team                                 |
| teamB       | String | desc      | Second team                                |
| Sport       | string | desc      | FOOTBALL OR BASKETBALL                     |
| odds        | string | desc      | List of 3 exactly odds (WIN,DRAW AND LOSE) |


**Responses:**

| Status Code               | Description                                                              |
|---------------------------|--------------------------------------------------------------------------|
| 201 Created               | A new resource (e.g. match) was successfully created.                    |
| 400 Bad Request           | The request body or parameters were invalid.                             |
| 401 Unauthorized          | The user is not authenticated (requires a valid token).                  |
| 409 Conflict              | There was a conflict with the current state (e.g. duplicate match).      |
| 500 Internal Server Error | A server-side error occurred while processing the request.               |


### `PUT /bet/updateMatch/{matchId}  (SECURED)`

**Request body Parameters:**

| Name        | Type   | Default   | Description                                |
|-------------|--------|-----------|--------------------------------------------|
| matchId     | String | 0         | The match id to update                     |
| description | String | 0         | The match description                      |
| matchDate   | String | 10        | Date of match                              |
| matchTime   | String | dateTime  | Time of match                              |
| teamA       | String | desc      | First team                                 |
| teamB       | String | desc      | Second team                                |
| Sport       | string | desc      | FOOTBALL OR BASKETBALL                     |
| odds        | string | desc      | List of 3 exactly odds (WIN,DRAW AND LOSE) |


**Responses:**

| Status Code               | Description                                                             |
|---------------------------|-------------------------------------------------------------------------|
| 200 OK                    | Request was successful (e.g. fetching matches, updating a match).       |
| 400 Bad Request           | The request body or parameters were invalid.                            |
| 401 Unauthorized          | The user is not authenticated (requires a valid token).                 |
| 403 Forbidden             | The user is authenticated but not authorized to perform the action.     |
| 404 Not Found             | The requested resource was not found (e.g. match ID does not exist).    |
| 500 Internal Server Error | A server-side error occurred while processing the request.              |


### `DELETE /bet/matches/{matchId} (SECURED)`

**Request body Parameters:**

| Name        | Type   | Default | Description            |
|-------------|--------|---------|------------------------|
| matchId     | String | 0       | The match id to delete |

**Responses:**

| Status Code               | Description                                                          |
|---------------------------|----------------------------------------------------------------------|
| 204 No Content            | Resource was successfully deleted (e.g. match deletion).             |
| 401 Unauthorized          | The user is not authenticated (requires a valid token).              |
| 403 Forbidden             | The user is authenticated but not authorized to perform the action.  |
| 404 Not Found             | The requested resource was not found (e.g. match ID does not exist). |
| 500 Internal Server Error | A server-side error occurred while processing the request.           |


---
## 🛠️ Development Setup

### Prerequisites

- Java 21
- Node.js (for Angular)
- Docker (to run Keycloak and PostgreSQL)

### Run Services locally

From root directory:

`docker network create my-bet-app-network`

**POSTGRES**

```
docker build -t my-postgres -f docker/postgres .
docker run -d --name my-postgres -p 5432:5432 --network my-bet-app-network my-postgres
```

**BACKEND**

```
mvn cean install
docker build -t my-bet-app -f docker/backend .
docker run -d --name my-bet-app -p 8888:8888 --network my-bet-app-network my-bet-app
```

**KEYCLOAK**

```
docker build -t my-keycloak -f docker/keycloak docker
docker run -d --name my-keycloak -p 8188:8080 --network my-bet-app-network my-keycloak
```

**FRONTEND**

```
docker build -t my-frontend -f docker/frontend .
docker run -d --name my-frontend -p 4200:4200 --network my-bet-app-network my-frontend
```


### Run services by just using docker

`docker network create my-bet-app-network`
`docker run -d --name my-postgres -p 5432:5432 --network my-bet-app-network fotisolgr/my-postgres:latest`
`docker run -d --name my-keycloak -p 8188:8080 --network my-bet-app-network fotisolgr/my-keycloak:latest`
`docker run -d --name my-bet-app -p 8888:8888 --network my-bet-app-network fotisolgr/my-bet-app:latest`
`docker run -d --name my-frontend -p 4200:4200 --network my-bet-app-network fotisolgr/my-frontend:latest`

### Access application

- UI: `http://localhost:4200`
- Keycloak `http://localhost:8188/` (username: admin, password: admin)
- Swagger: `http://localhost:8888/swagger-ui/index.html`
