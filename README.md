# IU Library Management System
*A Project for the PDM Course – Taught by Assoc. Prof. Nguyễn Thị Thúy Loan*

This project is a Java-based **Library Management System** developed as part of the PDM course. It provides essential functionalities for managing books, members, borrowing records, and administrative tasks. The system uses **Java Swing** for the user interface, **Java (JDBC)** for backend communication, and **SQL Server** as the database.

---

## Features

- Manage Books (add, edit, delete, search)
- Manage Members
- Borrow and Return Books
- Track due dates & borrowing history
- Login system with user authentication
- SQL Server database integration
- User-friendly Java Swing interface

---

## Download & Installation

### 1. Clone the Repository
```
git clone https://github.com/accban2006/PDMProject-Group-9-IU-Library-Managemnet-System.git
```
## Setup Instructions

### 2. Install JDBC Driver
The required Microsoft SQL Server JDBC driver is already included in the project: `dist/lib/`

Make sure to add the `.jar` file to your project libraries in your IDE (IntelliJ, NetBeans, or Eclipse).

### 3. Configure the Database
The SQL Server database file/script is located in: `src`

### 4. Update JDBC Connection in the Code
Inside the project, locate the database connection configuration and update it with **your server details**:

```
"jdbc:sqlserver://YOUR_SERVER_NAME:1433;databaseName=YOUR_DB_NAME;user="YOUR_USERNAME";password="YOUR_PASSWORD";trustServerCertificate=true");"
```

---

## Run the Project
Open the project in your preferred Java IDE.

Add the JDBC driver from dist/lib to your classpath.

Build and run the application.

credit: [naveenkumar-j](https://github.com/naveenkumar-j/Library-Management-System-using-Java/tree/main)
