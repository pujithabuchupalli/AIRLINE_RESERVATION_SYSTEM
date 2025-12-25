import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AirlineReservationSystem extends JFrame {
    private DatabaseHelper db;
    private UserData user;
    private CardLayout cards;
    private JPanel main;
    
    // Components
    private JTable flightTable, bookingTable;
    private DefaultTableModel flightModel, bookingModel;
    private JPanel seatMap;
    private JLabel seatLabel;
    private JTextArea summary;
    private JTextField nameF, emailF, phoneF, fromF, toF, dateF, cardF, cvvF;
    private JComboBox<String> methodBox;
    
    public AirlineReservationSystem() {
        db = new DatabaseHelper();
        user = new UserData();
        setTitle("✈️ Sky Airways");
        setSize(1200, 900); // Increased from 800 to 900 for more space
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cards = new CardLayout();
        main = new JPanel(cards);
        main.add(createLogin(), "LOGIN");
        main.add(createSearch(), "SEARCH");
        main.add(createSeats(), "SEATS");
        main.add(createPayment(), "PAYMENT");
        main.add(createManage(), "MANAGE");
        add(main);
        show("LOGIN");
    }
    
    void show(String name) {
        if (name.equals("SEATS")) updateSeats();
        if (name.equals("PAYMENT")) updateSummary();
        if (name.equals("MANAGE")) db.loadBookings(user.email, bookingModel);
        cards.show(main, name);
    }
    
    // ============= LOGIN =============
    JPanel createLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("✈️ SKY AIRWAYS");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(new Color(25, 25, 112));
        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        form.add(title, g);
        
        g.gridwidth = 1;
        nameF = field("", 20);
        emailF = field("", 20);
        phoneF = field("", 20);
        
        addRow(form, g, "Name:", nameF);
        addRow(form, g, "Email:", emailF);
        addRow(form, g, "Phone:", phoneF);
        
        JButton btn = btn("🔐 Login / Register", new Color(0, 123, 255));
        btn.setPreferredSize(new Dimension(280, 50));
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        g.gridwidth = 2; g.gridx = 0; g.gridy++;
        form.add(btn, g);
        
        btn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty() || !emailF.getText().contains("@")) {
                msg("⚠️ Fill all fields!");
                return;
            }
            db.registerUser(nameF.getText().trim(), emailF.getText().trim(), phoneF.getText().trim());
            user.name = nameF.getText().trim();
            user.email = emailF.getText().trim();
            msg("Welcome, " + user.name + "!");
            show("SEARCH");
        });
        
        p.add(form);
        return p;
    }
    
    // ============= SEARCH =============
    JPanel createSearch() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(new Color(248, 249, 250));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton manageBtn = new JButton("📋 My Bookings");
        manageBtn.setFont(new Font("Arial", Font.BOLD, 16));
        manageBtn.setBackground(new Color(255, 193, 7)); // Orange/Yellow
        manageBtn.setForeground(Color.BLACK);
        manageBtn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        manageBtn.setFocusPainted(false);
        manageBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageBtn.setOpaque(true);
        
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        logoutBtn.setBackground(new Color(220, 53, 69)); // Red
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setOpaque(true);
        
        manageBtn.addActionListener(e -> show("MANAGE"));
        logoutBtn.addActionListener(e -> { user.clear(); show("LOGIN"); });
        p.add(header("✈️ Available Flights", manageBtn, logoutBtn), BorderLayout.NORTH);
        
        // Main content area with two sections
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20)); // Increased spacing from 15 to 20
        contentPanel.setOpaque(false);
        
        // Upcoming flights panel (always visible)
        JPanel upcomingPanel = new JPanel(new BorderLayout(10, 10));
        upcomingPanel.setBackground(Color.WHITE);
        upcomingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
                "🌟 Upcoming Flights - Book Now!",
                0, 0,
                new Font("Arial", Font.BOLD, 18),
                new Color(0, 123, 255)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        String[] upcomingCols = {"Flight", "Airline", "Route", "Date", "Time", "Price", "Seats"};
        DefaultTableModel upcomingModel = new DefaultTableModel(upcomingCols, 0) { 
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable upcomingTable = new JTable(upcomingModel);
        styleTable(upcomingTable);
        upcomingTable.setRowHeight(40);
        
        JScrollPane upcomingScroll = new JScrollPane(upcomingTable);
        upcomingScroll.setPreferredSize(new Dimension(0, 180));
        upcomingPanel.add(upcomingScroll, BorderLayout.CENTER);
        
        // Load all upcoming flights on panel creation
        db.loadAllUpcomingFlights(upcomingModel);
        
        // Search form
        JPanel searchForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        searchForm.setBackground(Color.WHITE);
        searchForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)));
        
        JLabel searchLabel = new JLabel("🔍 Search Specific Flight:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        searchLabel.setForeground(new Color(25, 25, 112));
        
        fromF = field("New York", 15);
        toF = field("Los Angeles", 15);
        dateF = field("2024-12-25", 12);
        JButton searchBtn = btn("🔍 Search", new Color(0, 123, 255));
        JButton showAllBtn = btn("📋 Show All Flights", new Color(108, 117, 125));
        
        searchForm.add(searchLabel);
        searchForm.add(new JLabel("From:"));
        searchForm.add(fromF);
        searchForm.add(new JLabel("To:"));
        searchForm.add(toF);
        searchForm.add(new JLabel("Date:"));
        searchForm.add(dateF);
        searchForm.add(searchBtn);
        searchForm.add(showAllBtn);
        
        // Search results table
        String[] cols = {"Flight", "Airline", "Route", "Depart", "Arrive", "Price", "Seats"};
        flightModel = new DefaultTableModel(cols, 0) { 
            public boolean isCellEditable(int r, int c) { return false; }
        };
        flightTable = new JTable(flightModel);
        styleTable(flightTable);
        
        JScrollPane scroll = new JScrollPane(flightTable);
        scroll.setPreferredSize(new Dimension(0, 250)); // Ensure minimum height
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                "Search Results",
                0, 0,
                new Font("Arial", Font.BOLD, 16),
                new Color(25, 25, 112)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JButton selectBtn = btn("✈️ Select Flight & Choose Seat", new Color(40, 167, 69));
        selectBtn.setPreferredSize(new Dimension(350, 50));
        selectBtn.setFont(new Font("Arial", Font.BOLD, 18));
        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        bottom.add(selectBtn);
        
        // Actions
        searchBtn.addActionListener(e -> {
            if (fromF.getText().isEmpty()) { msg("⚠️ Fill all fields!"); return; }
            db.searchFlights(fromF.getText(), toF.getText(), dateF.getText(), flightModel);
        });
        
        showAllBtn.addActionListener(e -> {
            db.loadAllUpcomingFlights(flightModel);
        });
        
        selectBtn.addActionListener(e -> {
            int row = flightTable.getSelectedRow();
            if (row == -1) {
                // Try selecting from upcoming table
                row = upcomingTable.getSelectedRow();
                if (row == -1) {
                    msg("⚠️ Select a flight from either table!");
                    return;
                }
                // Get data from upcoming table
                user.flight = (String) upcomingModel.getValueAt(row, 0);
                user.amount = Double.parseDouble(((String) upcomingModel.getValueAt(row, 5)).replace("$", ""));
            } else {
                // Get data from search results table
                user.flight = (String) flightModel.getValueAt(row, 0);
                user.amount = Double.parseDouble(((String) flightModel.getValueAt(row, 5)).replace("$", ""));
            }
            show("SEATS");
        });
        
        // Layout
        contentPanel.add(upcomingPanel, BorderLayout.NORTH);
        
        JPanel searchSection = new JPanel(new BorderLayout(0, 15)); // Increased from 10 to 15
        searchSection.setOpaque(false);
        searchSection.add(searchForm, BorderLayout.NORTH);
        searchSection.add(scroll, BorderLayout.CENTER);
        searchSection.add(bottom, BorderLayout.SOUTH);
        
        contentPanel.add(searchSection, BorderLayout.CENTER);
        
        p.add(contentPanel, BorderLayout.CENTER);
        return p;
    }
    
    // ============= SEATS =============
    JPanel createSeats() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(new Color(248, 249, 250));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(header("💺 Choose Seat"), BorderLayout.NORTH);
        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        legend.setBackground(new Color(248, 249, 250));
        legend(legend, "Available", new Color(76, 175, 80));
        legend(legend, "Booked", new Color(244, 67, 54));
        legend(legend, "Selected", new Color(33, 150, 243));
        
        seatMap = new JPanel(new GridLayout(10, 6, 15, 15));
        seatMap.setBackground(Color.WHITE);
        seatMap.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel mapWrap = new JPanel(new GridBagLayout());
        mapWrap.setBackground(Color.WHITE);
        mapWrap.add(seatMap);
        
        content.add(legend, BorderLayout.NORTH);
        content.add(mapWrap, BorderLayout.CENTER);
        
        seatLabel = new JLabel("No seat selected");
        seatLabel.setFont(new Font("Arial", Font.BOLD, 18));
        seatLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton backBtn = btn("← Back", new Color(108, 117, 125));
        JButton nextBtn = btn("Continue to Payment →", new Color(40, 167, 69));
        backBtn.setPreferredSize(new Dimension(150, 50));
        nextBtn.setPreferredSize(new Dimension(250, 50));
        
        backBtn.addActionListener(e -> show("SEARCH"));
        nextBtn.addActionListener(e -> {
            if (user.seat == null) { msg("⚠️ Select a seat!"); return; }
            show("PAYMENT");
        });
        
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        btns.setOpaque(false);
        btns.add(backBtn);
        btns.add(nextBtn);
        
        JPanel bottom = new JPanel(new BorderLayout(0, 15));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        bottom.add(seatLabel, BorderLayout.NORTH);
        bottom.add(btns, BorderLayout.SOUTH);
        
        p.add(content, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }
    
    void updateSeats() {
        seatMap.removeAll();
        user.seat = null;
        seatLabel.setText("No seat selected");
        
        List<String> booked = db.getBookedSeats(user.flight);
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        
        for (int r = 0; r < 10; r++) {
            for (int c = 1; c <= 6; c++) {
                String seat = rows[r] + c;
                JButton b = new JButton(seat);
                b.setFont(new Font("Arial", Font.BOLD, 16));
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(70, 50));
                b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                
                if (booked.contains(seat)) {
                    b.setBackground(new Color(244, 67, 54));
                    b.setForeground(Color.WHITE);
                    b.setEnabled(false);
                } else {
                    b.setBackground(new Color(76, 175, 80));
                    b.setForeground(Color.WHITE);
                    b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    b.addActionListener(e -> {
                        for (Component comp : seatMap.getComponents()) {
                            if (comp instanceof JButton && ((JButton)comp).isEnabled()) {
                                comp.setBackground(new Color(76, 175, 80));
                            }
                        }
                        b.setBackground(new Color(33, 150, 243));
                        user.seat = seat;
                        seatLabel.setText("✓ Selected: " + seat);
                    });
                }
                seatMap.add(b);
            }
        }
        seatMap.revalidate();
        seatMap.repaint();
    }
    
    // ============= PAYMENT =============
    JPanel createPayment() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(248, 249, 250));
        
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("💳 Payment");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 112));
        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        form.add(title, g);
        
        summary = new JTextArea(5, 30);
        summary.setEditable(false);
        summary.setFont(new Font("Monospaced", Font.PLAIN, 13));
        summary.setBackground(new Color(240, 248, 255));
        g.gridy = 1;
        form.add(new JScrollPane(summary), g);
        
        g.gridwidth = 1;
        String[] methods = {"Credit Card", "Debit Card", "PayPal", "UPI"};
        methodBox = new JComboBox<>(methods);
        methodBox.setFont(new Font("Arial", Font.PLAIN, 14));
        cardF = field("", 16);
        cvvF = field("", 4);
        
        addRow(form, g, "Method:", methodBox);
        addRow(form, g, "Card:", cardF);
        addRow(form, g, "CVV:", cvvF);
        
        JButton backBtn = btn("← Back to Seats", new Color(108, 117, 125));
        JButton payBtn = btn("💳 Pay Now", new Color(40, 167, 69));
        backBtn.setPreferredSize(new Dimension(180, 50));
        payBtn.setPreferredSize(new Dimension(200, 50));
        payBtn.setFont(new Font("Arial", Font.BOLD, 18));
        
        backBtn.addActionListener(e -> show("SEATS"));
        payBtn.addActionListener(e -> {
            String card = cardF.getText().trim();
            String cvv = cvvF.getText().trim();
            if (card.length() != 16 || !card.matches("\\d+")) { msg("⚠️ 16-digit card!"); return; }
            if (cvv.length() != 3 || !cvv.matches("\\d+")) { msg("⚠️ 3-digit CVV!"); return; }
            
            if (db.bookFlight(user.flight, user.name, user.email, user.seat, user.amount, 
                (String)methodBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(this,
                    "✅ Payment Successful!\n\n" +
                    "Booking Confirmed\n" +
                    "Flight: " + user.flight + "\n" +
                    "Seat: " + user.seat + "\n" +
                    "Amount: $" + String.format("%.2f", user.amount) + "\n\n" +
                    "Have a great flight! ✈️",
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);
                cardF.setText("");
                cvvF.setText("");
                show("SEARCH");
            }
        });
        
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btns.setOpaque(false);
        btns.add(backBtn);
        btns.add(payBtn);
        
        g.gridx = 0; g.gridy++; g.gridwidth = 2;
        g.insets = new Insets(20, 10, 10, 10);
        form.add(btns, g);
        
        p.add(form);
        return p;
    }
    
    void updateSummary() {
        summary.setText(String.format(
            "╔═══════════════════════════╗\n" +
            "║    BOOKING SUMMARY        ║\n" +
            "╠═══════════════════════════╣\n" +
            "  Flight:    %s\n" +
            "  Seat:      %s\n" +
            "  Passenger: %s\n" +
            "  Amount:    $%.2f\n" +
            "╚═══════════════════════════╝",
            user.flight, user.seat, user.name, user.amount));
    }
    
    // ============= MANAGE =============
    JPanel createManage() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setBackground(new Color(248, 249, 250));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton backBtn = new JButton("← Back to Search");
        backBtn.setFont(new Font("Arial", Font.BOLD, 16));
        backBtn.setBackground(new Color(108, 117, 125));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setOpaque(true);
        backBtn.addActionListener(e -> show("SEARCH"));
        p.add(header("📋 My Bookings", backBtn), BorderLayout.NORTH);
        
        String[] cols = {"ID", "Flight", "Seat", "Date", "Status", "Amount"};
        bookingModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        bookingTable = new JTable(bookingModel);
        styleTable(bookingTable);
        
        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2));
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(25, 0, 25, 0)));
        
        JButton refreshBtn = btn("🔄 Refresh Bookings", new Color(0, 123, 255));
        refreshBtn.setPreferredSize(new Dimension(220, 50));
        
        JButton cancelBtn = btn("❌ CANCEL BOOKING", new Color(220, 53, 69));
        cancelBtn.setPreferredSize(new Dimension(250, 50));
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 18));
        
        refreshBtn.addActionListener(e -> db.loadBookings(user.email, bookingModel));
        cancelBtn.addActionListener(e -> {
            int r = bookingTable.getSelectedRow();
            if (r == -1) { 
                JOptionPane.showMessageDialog(this, 
                    "⚠️ Please select a booking from the table first!", 
                    "No Booking Selected",
                    JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            String status = (String) bookingModel.getValueAt(r, 4);
            if (status.contains("CANCELLED")) { 
                JOptionPane.showMessageDialog(this,
                    "⚠️ This booking is already cancelled!",
                    "Already Cancelled",
                    JOptionPane.WARNING_MESSAGE);
                return; 
            }
            
            int id = (int) bookingModel.getValueAt(r, 0);
            String flight = (String) bookingModel.getValueAt(r, 1);
            String seat = (String) bookingModel.getValueAt(r, 2);
            String amount = (String) bookingModel.getValueAt(r, 5);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "⚠️ Are you sure you want to cancel this booking?\n\n" +
                "Booking ID: #" + id + "\n" +
                "Flight: " + flight + "\n" +
                "Seat: " + seat + "\n" +
                "Amount: " + amount + "\n\n" +
                "Refund will be processed in 7-10 business days.", 
                "Confirm Cancellation", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (db.cancelBooking(id, flight, seat)) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Booking Cancelled Successfully!\n\n" +
                        "Booking ID: #" + id + "\n" +
                        "Flight: " + flight + "\n" +
                        "Seat: " + seat + "\n\n" +
                        "Your refund of " + amount + " will be processed\n" +
                        "within 7-10 business days.",
                        "Cancellation Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
                    db.loadBookings(user.email, bookingModel);
                }
            }
        });
        
        bottom.add(refreshBtn);
        bottom.add(cancelBtn);
        
        p.add(scroll, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }
    
    // ============= HELPERS =============
    JPanel header(String title, JButton... btns) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(25, 25, 112));
        h.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 28));
        lbl.setForeground(Color.WHITE);
        h.add(lbl, BorderLayout.WEST);
        
        if (btns.length > 0) {
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            bp.setOpaque(false);
            for (JButton b : btns) {
                // Don't override button styles - they're already set
                bp.add(b);
            }
            h.add(bp, BorderLayout.EAST);
        }
        return h;
    }
    
    JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(true);
        
        // Add hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(bg);
            }
        });
        
        return b;
    }
    
    JTextField field(String text, int cols) {
        JTextField f = new JTextField(text, cols);
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return f;
    }
    
    void styleTable(JTable t) {
        t.setRowHeight(35);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.getTableHeader().setBackground(new Color(52, 58, 64));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        t.getTableHeader().setPreferredSize(new Dimension(0, 40));
        t.setSelectionBackground(new Color(0, 123, 255, 150));
    }
    
    void legend(JPanel p, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        item.setOpaque(false);
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(40, 40));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        item.add(box);
        item.add(lbl);
        p.add(item);
    }
    
    void addRow(JPanel p, GridBagConstraints g, String label, JComponent comp) {
        g.gridx = 0; g.gridy++;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(lbl, g);
        g.gridx = 1;
        p.add(comp, g);
    }
    
    void msg(String text) {
        JOptionPane.showMessageDialog(this, text);
    }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new AirlineReservationSystem().setVisible(true));
    }
}

class UserData {
    String name, email, flight, seat;
    double amount;
    void clear() { name = email = flight = seat = null; amount = 0; }
}