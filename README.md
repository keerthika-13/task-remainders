# Task Remainder

A full-stack starter project using React on the frontend and plain Java + MySQL on the backend.

## Stack
- Frontend: React + Vite + HTML/CSS/JS
- Backend: Java 17 + HttpServer
- Database: MySQL

## Project structure
- `frontend/` – React UI
- `backend/` – Java API server
- `database/` – MySQL schema and seed data

## 1) Database setup
1. Start MySQL.
2. Run the SQL file:

```bash
mysql -u root -p < database/schema.sql
```

If needed, update the connection values in `backend/src/main/resources/application.properties` or use environment variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/task_remainder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="password"
```

## 2) Start the Java backend

If Maven is installed:

```bash
cd backend
mvn clean compile exec:java
```

If Maven is not installed, compile and run directly with Java:

```bash
cd backend
javac -cp "lib\mysql-connector-j-8.4.0.jar" -d target\classes (Get-ChildItem -Recurse .\src\main\java -Filter *.java | ForEach-Object { $_.FullName })
java -cp "target\classes;lib\mysql-connector-j-8.4.0.jar" com.example.project.api.Main
```

The API will run on:
- `http://localhost:8080/api/health`
- `http://localhost:8080/api/customers`

## 3) Start the React frontend

```bash
cd frontend
npm install
npm run dev
```

Then open:
- `http://localhost:5173`

The React app is already configured to proxy `/api/*` requests to the Java backend.

## Notes
- The backend intentionally avoids frameworks and uses plain Java server classes.
- If MySQL is not available, the backend will return sample customer data automatically.
