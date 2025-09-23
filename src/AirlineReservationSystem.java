import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AirlineReservationGUI extends JFrame {
    private DatabaseHelper dbHelper;
    private JTable flightTable;
    private DefaultTableModel tableModel;
    private JTextField fromField, toField, dateField;
    private JTextField nameField, emailField, phoneField;
    
    public AirlineReservationGUI() {
        dbHelper = new DatabaseHelper();
        setupGUI();
    }
    
    private void setupGUI() {
        setTitle("✈️ Sky Airways - Book Your Flight");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(135, 206, 250), 
                                                         0, getHeight(), new Color(255, 255, 255));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        
        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        
        // Content with tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("🔍 Search & Book", createSearchPanel());
        tabs.addTab("📋 My Bookings", createBookingsPanel());
        
        mainPanel.add(tabs, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel title = new JLabel("✈️ SKY AIRWAYS", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(new Color(25, 25, 112));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Your Journey Begins Here", JLabel.CENTER);
        subtitle.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitle.setForeground(new Color(70, 130, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        header.add(title);
        header.add(subtitle);
        return header;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Search Form
        JPanel searchForm = createTransparentPanel();
        searchForm.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        searchForm.setBorder(createStyledBorder("🔍 Search Flights"));
        
        fromField = createStyledTextField("New York", 12);
        toField = createStyledTextField("Los Angeles", 12);
        dateField = createStyledTextField("2024-12-25", 10);
        JButton searchBtn = createStyledButton("Search", new Color(0, 123, 255));
        
        searchForm.add(new JLabel("From:"));
        searchForm.add(fromField);
        searchForm.add(new JLabel("To:"));
        searchForm.add(toField);
        searchForm.add(new JLabel("Date:"));
        searchForm.add(dateField);
        searchForm.add(searchBtn);
        
        // Flight Table
        String[] columns = {"Flight", "Airline", "Route", "Time", "Price", "Seats"};
        tableModel = new DefaultTableModel(columns, 0);
        flightTable = new JTable(tableModel);
        styleTable(flightTable);
        
        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setBorder(createStyledBorder("Available Flights"));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Booking Form
        JPanel bookingForm = createBookingForm();
        
        panel.add(searchForm, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bookingForm, BorderLayout.SOUTH);
        
        // Search Action
        searchBtn.addActionListener(e -> {
            dbHelper.searchFlights(fromField.getText(), toField.getText(), dateField.getText(), tableModel);
        });
        
        return panel;
    }
    
    private JPanel createBookingForm() {
        JPanel form = createTransparentPanel();
        form.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        form.setBorder(createStyledBorder("👤 Book Your Flight"));
        
        nameField = createStyledTextField("", 15);
        emailField = createStyledTextField("", 15);
        phoneField = createStyledTextField("", 12);
        
        JButton bookBtn = createStyledButton("📝 Book Flight", new Color(40, 167, 69));
        JButton payBtn = createStyledButton("💳 Pay Now", new Color(255, 193, 7));
        
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(bookBtn);
        form.add(payBtn);
        
        // Book Action
        bookBtn.addActionListener(e -> {
            if (flightTable.getSelectedRow() == -1) {
                showMessage("Please select a flight!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                showMessage("Please fill all details!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String flight = (String) tableModel.getValueAt(flightTable.getSelectedRow(), 0);
            String price = (String) tableModel.getValueAt(flightTable.getSelectedRow(), 4);
            
            if (dbHelper.bookFlight(flight, nameField.getText(), emailField.getText(), phoneField.getText(), price)) {
                showMessage("✅ Flight booked successfully!\nFlight: " + flight + "\nPassenger: " + nameField.getText(), 
                           "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Payment Action
        payBtn.addActionListener(e -> {
            String[] methods = {"Credit Card", "Debit Card", "PayPal"};
            String method = (String) JOptionPane.showInputDialog(this, "Choose Payment Method:", 
                           "Payment", JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);
            if (method != null) {
                showMessage("💳 Payment Successful!\nMethod: " + method + "\n\nThank you for flying with Sky Airways! ✈️", 
                           "Payment Complete", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            }
        });
        
        return form;
    }
    
    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Search Panel
        JPanel searchPanel = createTransparentPanel();
        searchPanel.setLayout(new FlowLayout());
        
        JTextField emailSearch = createStyledTextField("john@example.com", 20);
        JButton viewBtn = createStyledButton("View My Bookings", new Color(0, 123, 255));
        
        searchPanel.add(new JLabel("📧 Email:"));
        searchPanel.add(emailSearch);
        searchPanel.add(viewBtn);
        
        // Bookings Table
        String[] columns = {"Booking ID", "Flight", "Passenger", "Date", "Status", "Amount"};
        DefaultTableModel bookingModel = new DefaultTableModel(columns, 0);
        JTable bookingTable = new JTable(bookingModel);
        styleTable(bookingTable);
        
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(createStyledBorder("My Flight Bookings"));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        viewBtn.addActionListener(e -> {
            dbHelper.loadBookings(emailSearch.getText(), bookingModel);
        });
        
        return panel;
    }
    
    // Helper Methods
    private JPanel createTransparentPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        panel.setOpaque(false);
        return panel;
    }
    
    private JTextField createStyledTextField(String text, int cols) {
        JTextField field = new JTextField(text, cols);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(color);
        btn.setForeground(color.equals(new Color(255, 193, 7)) ? Color.BLACK : Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setBackground(new Color(52, 58, 64));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setSelectionBackground(new Color(0, 123, 255, 100));
    }
    
    private javax.swing.border.Border createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), title, 0, 0, 
            new Font("Arial", Font.BOLD, 13), new Color(25, 25, 112));
    }
    
    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }
    
    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            new AirlineReservationGUI().setVisible(true);
        });
    }
}