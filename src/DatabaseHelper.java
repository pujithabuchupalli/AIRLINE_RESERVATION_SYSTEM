import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/airline_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "BangBang@1234";
    
    public DatabaseHelper() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // Create tables
            Statement stmt = conn.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS flights (" +
                "flight_number VARCHAR(10) PRIMARY KEY," +
                "airline VARCHAR(50) NOT NULL," +
                "origin VARCHAR(50) NOT NULL," +
                "destination VARCHAR(50) NOT NULL," +
                "departure_time DATETIME NOT NULL," +
                "price DECIMAL(8,2) NOT NULL," +
                "available_seats INT NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                "booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                "flight_number VARCHAR(10)," +
                "passenger_name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL," +
                "phone VARCHAR(20)," +
                "booking_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                "amount DECIMAL(8,2))");
            
            // Insert sample data
            stmt.execute("INSERT IGNORE INTO flights VALUES " +
                "('AA101', 'American Airlines', 'New York', 'Los Angeles', '2024-12-25 08:00:00', 299.99, 150)," +
                "('UA202', 'United Airlines', 'New York', 'Los Angeles', '2024-12-25 14:00:00', 349.99, 180)," +
                "('DL303', 'Delta Airlines', 'Los Angeles', 'New York', '2024-12-26 09:00:00', 279.99, 200)," +
                "('SW404', 'Southwest', 'Chicago', 'Miami', '2024-12-25 10:30:00', 199.99, 120)," +
                "('JB505', 'JetBlue', 'Boston', 'Seattle', '2024-12-25 16:00:00', 389.99, 140)");
            
        } catch (Exception e) {
            System.out.println("Database setup: " + e.getMessage());
        }
    }
    
    public void searchFlights(String from, String to, String date, DefaultTableModel model) {
        model.setRowCount(0);
        
        String sql = "SELECT * FROM flights WHERE origin = ? AND destination = ? AND DATE(departure_time) = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setDate(3, Date.valueOf(date));
            
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
            
            while (rs.next()) {
                String route = rs.getString("origin") + " → " + rs.getString("destination");
                String time = rs.getTimestamp("departure_time").toLocalDateTime().format(timeFormat);
                String price = "$" + String.format("%.0f", rs.getDouble("price"));
                String seats = rs.getInt("available_seats") + " left";
                
                model.addRow(new Object[]{
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    route,
                    time,
                    price,
                    seats
                });
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No flights found for " + from + " to " + to + " on " + date, 
                                            "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean bookFlight(String flightNumber, String name, String email, String phone, String priceStr) {
        double price = Double.parseDouble(priceStr.replace("$", ""));
        
        String sql = "INSERT INTO bookings (flight_number, passenger_name, email, phone, amount) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, flightNumber);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setDouble(5, price);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                // Update seats
                updateSeats(flightNumber);
                return true;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Booking failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private void updateSeats(String flightNumber) {
        String sql = "UPDATE flights SET available_seats = available_seats - 1 WHERE flight_number = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, flightNumber);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            System.out.println("Seat update error: " + e.getMessage());
        }
    }
    
    public void loadBookings(String email, DefaultTableModel model) {
        model.setRowCount(0);
        
        String sql = "SELECT * FROM bookings WHERE email = ? ORDER BY booking_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String date = rs.getTimestamp("booking_date").toString().substring(0, 16);
                String amount = "$" + String.format("%.0f", rs.getDouble("amount"));
                
                model.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("flight_number"),
                    rs.getString("passenger_name"),
                    date,
                    rs.getString("status"),
                    amount
                });
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No bookings found for " + email, 
                                            "No Bookings", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading bookings: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}