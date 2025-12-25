# ✈️ Sky Airways - Airline Reservation System

A **Java Swing-based Airline Reservation System** with MySQL backend, allowing users to search flights, book seats, and manage bookings. The system demonstrates real-world concepts like GUI development, database integration, seat selection, and payment simulation.

---

## 🖥️ Features

### User Management
- Register or login using **Name, Email, and Phone**.
- Basic validation for email and mandatory fields.

### Flight Search
- Search for flights by **origin, destination, and date**.
- Display upcoming flights in a table.
- View flight details including departure, arrival, price, and available seats.

### Seat Selection
- Visual **seat map** with color codes:
  - Green: Available
  - Red: Booked
  - Blue: Selected
- Prevents booking already taken seats.
- Update seat selection in real-time.

### Payment Simulation
- Payment via **Credit Card, Debit Card, PayPal, or UPI**.
- Validate card number (16 digits) and CVV (3 digits).
- Shows booking summary before confirmation.
- Update bookings table after successful payment.

### Booking Management
- View **all your bookings**.
- Cancel confirmed bookings with refund simulation.
- Refresh bookings to reflect current status.

---

## 📂 Project Structure

AirlineReservationSystem/
│
├─ src/
│ ├─ AirlineReservationSystem.java # Main GUI application
│ ├─ DatabaseHelper.java # Handles database operations
│ └─ UserData.java # Stores user session data
│
├─ README.md # Project documentation
└─ lib/ # Optional libraries (JDBC driver)



---

## 💾 Database

**Database:** MySQL  
**Database Name:** `airline_db`  

**Tables:**
- `users` – stores user information.
- `flights` – stores flight details including total and available seats.
- `bookings` – stores bookings with flight, seat, payment, and status.

**Sample Flight Data:**
| Flight | Airline           | Origin       | Destination  | Departure       | Arrival        | Price | Seats |
|--------|-----------------|-------------|-------------|----------------|----------------|-------|-------|
| AA101  | American Airlines| New York    | Los Angeles | 2024-12-25 08:00| 2024-12-25 11:30| 299.99| 60    |
| UA202  | United Airlines  | New York    | Los Angeles | 2024-12-25 14:00| 2024-12-25 17:30| 349.99| 60    |

> The database is automatically initialized by the `DatabaseHelper` class with sample data.

---

## ⚙️ Setup Instructions

1. **Install MySQL** and create a database:
```sql
CREATE DATABASE airline_db;
Update credentials in DatabaseHelper.java:

java

private static final String DB_USER = "root";
private static final String DB_PASS = "your_password";
Add MySQL JDBC Driver to your project library path.

Compile and Run:

bash

javac -cp .;mysql-connector-java-8.0.33.jar AirlineReservationSystem.java
java -cp .;mysql-connector-java-8.0.33.jar AirlineReservationSystem
🎨 Technologies Used
Java SE (Swing for GUI)

MySQL (Database)

JDBC (Database connectivity)

GridLayout, CardLayout, BorderLayout for GUI management
