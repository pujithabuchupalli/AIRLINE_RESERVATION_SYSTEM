import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/airline_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "BangBang@1234";
    
    public DatabaseHelper() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            
            // Drop old tables if they exist (to recreate with new schema)
            try {
                stmt.execute("DROP TABLE IF EXISTS bookings");
                System.out.println("✓ Dropped old bookings table");
            } catch (Exception e) {
                // Table might not exist, ignore
            }
            
            try {
                stmt.execute("DROP TABLE IF EXISTS flights");
                System.out.println("✓ Dropped old flights table");
            } catch (Exception e) {
                // Table might not exist, ignore
            }
            
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "phone VARCHAR(20) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("✓ Created users table");
            
            // Create flights table
            stmt.execute("CREATE TABLE flights (" +
                "flight_number VARCHAR(10) PRIMARY KEY," +
                "airline VARCHAR(50) NOT NULL," +
                "origin VARCHAR(50) NOT NULL," +
                "destination VARCHAR(50) NOT NULL," +
                "departure_time DATETIME NOT NULL," +
                "arrival_time DATETIME NOT NULL," +
                "price DECIMAL(8,2) NOT NULL," +
                "total_seats INT NOT NULL," +
                "available_seats INT NOT NULL)");
            System.out.println("✓ Created flights table");
            
            // Create bookings table with seat number
            stmt.execute("CREATE TABLE bookings (" +
                "booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                "flight_number VARCHAR(10)," +
                "passenger_name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL," +
                "seat_number VARCHAR(5) NOT NULL," +
                "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                "amount DECIMAL(8,2)," +
                "payment_method VARCHAR(30)," +
                "FOREIGN KEY (flight_number) REFERENCES flights(flight_number)," +
                "UNIQUE KEY unique_flight_seat (flight_number, seat_number))");
            System.out.println("✓ Created bookings table with seat_number column");
            
            // Insert sample flights
            stmt.execute("INSERT INTO flights VALUES " +
                "('AA101', 'American Airlines', 'New York', 'Los Angeles', " +
                "'2024-12-25 08:00:00', '2024-12-25 11:30:00', 299.99, 60, 60)," +
                "('UA202', 'United Airlines', 'New York', 'Los Angeles', " +
                "'2024-12-25 14:00:00', '2024-12-25 17:30:00', 349.99, 60, 60)," +
                "('DL303', 'Delta Airlines', 'Los Angeles', 'New York', " +
                "'2024-12-26 09:00:00', '2024-12-26 17:00:00', 279.99, 60, 60)," +
                "('SW404', 'Southwest', 'Chicago', 'Miami', " +
                "'2024-12-25 10:30:00', '2024-12-25 14:45:00', 199.99, 60, 60)," +
                "('JB505', 'JetBlue', 'Boston', 'Seattle', " +
                "'2024-12-25 16:00:00', '2024-12-25 20:30:00', 389.99, 60, 60)");
            System.out.println("✓ Inserted sample flight data");
            
            System.out.println("\n✅ Database initialized successfully!");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("\n❌ Database setup error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database initialization failed!\n\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Please check:\n" +
                "1. MySQL server is running\n" +
                "2. Database 'airline_db' exists\n" +
                "3. Username and password are correct",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Register or get existing user
    public boolean registerUser(String name, String email, String phone) {
        String checkSql = "SELECT user_id FROM users WHERE email = ?";
        String insertSql = "INSERT INTO users (name, email, phone) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Check if user exists
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // User already exists
                return true;
            }
            
            // Register new user
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, name);
            insertStmt.setString(2, email);
            insertStmt.setString(3, phone);
            
            int result = insertStmt.executeUpdate();
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("User registration error: " + e.getMessage());
            return false;
        }
    }
    
    // Search for flights
    public void searchFlights(String from, String to, String date, DefaultTableModel model) {
        model.setRowCount(0);
        
        String sql = "SELECT * FROM flights WHERE origin = ? AND destination = ? " +
                    "AND DATE(departure_time) = ? AND available_seats > 0";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setDate(3, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            
            while (rs.next()) {
                String route = rs.getString("origin") + " → " + rs.getString("destination");
                String departure = rs.getTimestamp("departure_time")
                    .toLocalDateTime().format(timeFormat);
                String arrival = rs.getTimestamp("arrival_time")
                    .toLocalDateTime().format(timeFormat);
                String price = "$" + String.format("%.0f", rs.getDouble("price"));
                String seats = rs.getInt("available_seats") + " available";
                
                model.addRow(new Object[]{
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    route,
                    departure,
                    arrival,
                    price,
                    seats
                });
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, 
                    "No flights found for:\n" +
                    "Route: " + from + " → " + to + "\n" +
                    "Date: " + date + "\n\n" +
                    "Please try different search criteria.",
                    "No Flights Available", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Search error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // Get already booked seats for a flight
    public List<String> getBookedSeats(String flightNumber) {
        List<String> bookedSeats = new ArrayList<>();
        
        String sql = "SELECT seat_number FROM bookings WHERE flight_number = ? AND status = 'CONFIRMED'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_number"));
            }
            
        } catch (Exception e) {
            System.err.println("Error getting booked seats: " + e.getMessage());
        }
        
        return bookedSeats;
    }
    
    // Book a flight with seat selection
    public boolean bookFlight(String flightNumber, String name, String email, 
                            String seatNumber, double amount, String paymentMethod) {
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            conn.setAutoCommit(false);
            
            // Check if seat is already booked
            String checkSql = "SELECT booking_id FROM bookings WHERE flight_number = ? " +
                            "AND seat_number = ? AND status = 'CONFIRMED'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, flightNumber);
            checkStmt.setString(2, seatNumber);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next()) {
                JOptionPane.showMessageDialog(null, 
                    "Seat " + seatNumber + " is already booked!\nPlease select another seat.",
                    "Seat Unavailable", JOptionPane.ERROR_MESSAGE);
                conn.rollback();
                return false;
            }
            
            // Insert booking
            String bookingSql = "INSERT INTO bookings (flight_number, passenger_name, email, " +
                              "seat_number, amount, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
            bookingStmt.setString(1, flightNumber);
            bookingStmt.setString(2, name);
            bookingStmt.setString(3, email);
            bookingStmt.setString(4, seatNumber);
            bookingStmt.setDouble(5, amount);
            bookingStmt.setString(6, paymentMethod);
            
            int result = bookingStmt.executeUpdate();
            
            if (result > 0) {
                // Update available seats
                String updateSql = "UPDATE flights SET available_seats = available_seats - 1 " +
                                 "WHERE flight_number = ? AND available_seats > 0";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, flightNumber);
                int updateResult = updateStmt.executeUpdate();
                
                if (updateResult > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, 
                        "No seats available on this flight!",
                        "Booking Failed", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            
            conn.rollback();
            return false;
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, 
                "Booking failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Load user bookings
    public void loadBookings(String email, DefaultTableModel model) {
        model.setRowCount(0);
        
        String sql = "SELECT b.*, f.airline, f.origin, f.destination " +
                    "FROM bookings b " +
                    "JOIN flights f ON b.flight_number = f.flight_number " +
                    "WHERE b.email = ? " +
                    "ORDER BY b.booking_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            while (rs.next()) {
                String bookingDate = rs.getTimestamp("booking_date")
                    .toLocalDateTime().format(dateFormat);
                String amount = "$" + String.format("%.2f", rs.getDouble("amount"));
                String status = rs.getString("status");
                
                // Color code status
                String displayStatus = status.equals("CONFIRMED") ? "✅ CONFIRMED" : "❌ CANCELLED";
                
                model.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("flight_number"),
                    rs.getString("seat_number"),
                    bookingDate,
                    displayStatus,
                    amount
                });
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, 
                    "No bookings found for " + email + "\n\n" +
                    "Book your first flight now!",
                    "No Bookings", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error loading bookings: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // Cancel a booking
    public boolean cancelBooking(int bookingId, String flightNumber, String seatNumber) {
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            conn.setAutoCommit(false);
            
            // Update booking status
            String updateSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, bookingId);
            
            int result = updateStmt.executeUpdate();
            
            if (result > 0) {
                // Increase available seats
                String seatSql = "UPDATE flights SET available_seats = available_seats + 1 " +
                               "WHERE flight_number = ?";
                PreparedStatement seatStmt = conn.prepareStatement(seatSql);
                seatStmt.setString(1, flightNumber);
                seatStmt.executeUpdate();
                
                conn.commit();
                return true;
            }
            
            conn.rollback();
            return false;
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, 
                "Cancellation failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Get flight details
    public String getFlightDetails(String flightNumber) {
        String sql = "SELECT * FROM flights WHERE flight_number = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String departure = rs.getTimestamp("departure_time")
                    .toLocalDateTime().format(format);
                
                return String.format(
                    "Flight: %s\nAirline: %s\nRoute: %s → %s\nDeparture: %s\nPrice: $%.2f",
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    departure,
                    rs.getDouble("price")
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error getting flight details: " + e.getMessage());
        }
        
        return "Flight not found";
    }
    
    // Load all upcoming flights for home page display
    public void loadAllUpcomingFlights(DefaultTableModel model) {
        model.setRowCount(0);
        
        String sql = "SELECT * FROM flights WHERE available_seats > 0 ORDER BY departure_time";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            
            while (rs.next()) {
                String route = rs.getString("origin") + " → " + rs.getString("destination");
                LocalDateTime departureDateTime = rs.getTimestamp("departure_time").toLocalDateTime();
                String date = departureDateTime.format(dateFormat);
                String time = departureDateTime.format(timeFormat);
                String price = "$" + String.format("%.0f", rs.getDouble("price"));
                String seats = rs.getInt("available_seats") + " available";
                
                model.addRow(new Object[]{
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    route,
                    date,
                    time,
                    price,
                    seats
                });
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, 
                    "No upcoming flights available at the moment.",
                    "No Flights", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error loading flights: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}