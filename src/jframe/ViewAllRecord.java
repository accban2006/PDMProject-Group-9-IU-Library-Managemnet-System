package jframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Pure Swing implementation of the View All Records screen.
 */
public class ViewAllRecord extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ViewAllRecord.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color ERROR_COLOR = new Color(255, 51, 51);

    private Connection con;

    private int id;
    private String uname;
    private String userrole;

    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private JButton searchButton;
    private JButton allRecordsButton;
    private JTable recordsTable;
    private DefaultTableModel tableModel;
    private JTable messagesTable;
    private DefaultTableModel messagesTableModel;
    private JTable reportsTable;
    private DefaultTableModel reportsTableModel;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public ViewAllRecord() {
        this(0, null, null);
    }

    public ViewAllRecord(int id, String username, String urole) {
        this.id = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            updateUserContext();
            if (con != null) {
                loadRecords();
            }
        });
    }

    private void initializeUi() {
        setTitle("View All Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 700));
        setIconImageSafe();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JButton backButton = new JButton("Back to Home");
        backButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        backButton.setBackground(new Color(0, 102, 204));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> navigateTo(new HomePage(id, uname, userrole)));

        header.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("View All Records", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.CENTER);

        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome, ");
        welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.WHITE);

        welcomeValueLabel = new JLabel("-");
        welcomeValueLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        welcomeValueLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("   Role: ");
        roleLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
        roleLabel.setForeground(Color.WHITE);

        roleValueLabel = new JLabel("-");
        roleValueLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        roleValueLabel.setForeground(Color.WHITE);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> System.exit(0));

        userPanel.add(welcomeLabel);
        userPanel.add(welcomeValueLabel);
        userPanel.add(Box.createHorizontalStrut(12));
        userPanel.add(roleLabel);
        userPanel.add(roleValueLabel);
        userPanel.add(Box.createHorizontalStrut(12));
        userPanel.add(exitButton);

        header.add(userPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(new EmptyBorder(24, 24, 24, 24));
        content.setBackground(new Color(245, 245, 245));

        content.add(buildSearchPanel(), BorderLayout.NORTH);
        content.add(buildTabbedPanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildTabbedPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Tahoma", Font.BOLD, 14));

        tabbedPane.addTab("Loan Records", buildTablePanel());
        tabbedPane.addTab("Messages", buildMessagesTablePanel());
        tabbedPane.addTab("Reports", buildReportsTablePanel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(PRIMARY_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel fromDateLabel = new JLabel("From Date:");
        fromDateLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        fromDateLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(fromDateLabel, gbc);

        fromDateSpinner = createDateSpinner();
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        searchPanel.add(fromDateSpinner, gbc);

        JLabel toDateLabel = new JLabel("To Date:");
        toDateLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        toDateLabel.setForeground(Color.WHITE);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        searchPanel.add(toDateLabel, gbc);

        toDateSpinner = createDateSpinner();
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.3;
        searchPanel.add(toDateSpinner, gbc);

        searchButton = new JButton("Search");
        styleButton(searchButton, ERROR_COLOR);
        searchButton.addActionListener(e -> performSearch());
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        searchPanel.add(searchButton, gbc);

        allRecordsButton = new JButton("All Records");
        styleButton(allRecordsButton, ERROR_COLOR);
        allRecordsButton.addActionListener(e -> loadRecords());
        gbc.gridx = 5;
        searchPanel.add(allRecordsButton, gbc);

        return searchPanel;
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Tahoma", Font.PLAIN, 14));
        spinner.setPreferredSize(new Dimension(150, 30));
        return spinner;
    }

    private JPanel buildTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "All Issue Book Records"
        ));

        String[] columnNames = {"Book ID", "Book Name", "Reader ID", "Reader Name", "Issue Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recordsTable = new JTable(tableModel);
        recordsTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        recordsTable.setRowHeight(25);
        recordsTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        recordsTable.setFillsViewportHeight(true);
        recordsTable.setSelectionBackground(new Color(220, 220, 255));
        recordsTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(recordsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel buildMessagesTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "All Messages"
        ));

        String[] columnNames = {"Message ID", "Message No", "Reader ID", "Reader Name", "Message Info"};
        messagesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        messagesTable = new JTable(messagesTableModel);
        messagesTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        messagesTable.setRowHeight(25);
        messagesTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        messagesTable.setFillsViewportHeight(true);
        messagesTable.setSelectionBackground(new Color(220, 220, 255));
        messagesTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(messagesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load messages
        SwingUtilities.invokeLater(() -> {
            if (con != null) {
                loadMessages();
            }
        });

        return tablePanel;
    }

    private JPanel buildReportsTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "All Reports"
        ));

        String[] columnNames = {"Report ID", "Report No", "Staff ID", "Staff Name", "Issue"};
        reportsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportsTable = new JTable(reportsTableModel);
        reportsTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        reportsTable.setRowHeight(25);
        reportsTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        reportsTable.setFillsViewportHeight(true);
        reportsTable.setSelectionBackground(new Color(220, 220, 255));
        reportsTable.setSelectionForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(reportsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Load reports
        SwingUtilities.invokeLater(() -> {
            if (con != null) {
                loadReports();
            }
        });

        return tablePanel;
    }

    private void loadMessages() {
        if (con == null) {
            return;
        }

        messagesTableModel.setRowCount(0);

        // Join [operate].[Message] with auth_reader to get reader name
        String sql = "select m.MessID, m.MessNo, m.ReaderID, " +
                "r.FirstName, r.MiddleName, r.LastName, m.MessInfo " +
                "from [operate].[Message] m " +
                "inner join [auth].[Reader] r on m.ReaderID = r.ReaderID " +
                "order by m.MessID DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Build reader full name
                String fname = rs.getString("FirstName");
                String mname = rs.getString("MiddleName");
                String lname = rs.getString("LastName");

                StringBuilder fullName = new StringBuilder();
                if (fname != null && !fname.isEmpty()) {
                    fullName.append(fname);
                }
                if (mname != null && !mname.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(mname);
                }
                if (lname != null && !lname.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(lname);
                }
                String readerName = fullName.toString().trim();
                if (readerName.isEmpty()) {
                    readerName = "-";
                }

                messagesTableModel.addRow(new Object[]{
                        rs.getInt("MessID"),
                        rs.getInt("MessNo"),
                        rs.getInt("ReaderID"),
                        readerName,
                        rs.getString("MessInfo")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load messages", ex);
            JOptionPane.showMessageDialog(this, "Failed to load messages.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReports() {
        if (con == null) {
            return;
        }

        reportsTableModel.setRowCount(0);

        // Join operate_report with auth_staff to get staff name
        String sql = "select r.ReportID, r.ReportNo, r.StaffID, s.StaffName, r.Issue " +
                "from [operate].[Report] r " +
                "inner join [auth].[Staff] s on r.StaffID = s.StaffID " +
                "order by r.ReportID DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reportsTableModel.addRow(new Object[]{
                        rs.getInt("ReportID"),
                        rs.getInt("ReportNo"),
                        rs.getInt("StaffID"),
                        rs.getString("StaffName") != null ? rs.getString("StaffName") : "-",
                        rs.getString("Issue") != null ? rs.getString("Issue") : "-"
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load reports", ex);
            JOptionPane.showMessageDialog(this, "Failed to load reports.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(245, 245, 245));
        footer.setBorder(new EmptyBorder(12, 24, 12, 24));

        JPanel creditsPanel = new JPanel();
        creditsPanel.setOpaque(false);
        creditsPanel.setLayout(new BoxLayout(creditsPanel, BoxLayout.X_AXIS));

        JLabel developedByLabel = new JLabel("Developed by: ");
        developedByLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        developedByLabel.setForeground(new Color(100, 100, 100));

        JLabel developerLabel = createLinkLabel("Naveenkumar J", "https://github.com/naveenkumar-j", new Color(0, 102, 204));
        developerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        creditsPanel.add(developedByLabel);
        creditsPanel.add(developerLabel);

        footer.add(creditsPanel, BorderLayout.CENTER);

        return footer;
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    private void updateUserContext() {
        String displayName = (uname == null || uname.trim().isEmpty()) ? "Guest" : uname;
        String displayRole = (userrole == null || userrole.trim().isEmpty()) ? "Guest" : userrole;
        welcomeValueLabel.setText(displayName);
        roleValueLabel.setText(displayRole);
    }

    public final void Connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://YOUR_SERVER_NAME:1433;databaseName=YOUR_DB_NAME;user=YOUR_USERNAME;password=YOUR_PASSWORD;trustServerCertificate=true");
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database connection failed", ex);
            JOptionPane.showMessageDialog(this, "Unable to connect to database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadRecords() {
        if (con == null) {
            return;
        }

        tableModel.setRowCount(0);

        // Join [loan].[Loan] with auth_reader and lib_book to get all required information
        String sql = "select l.ISBN, b.title, l.ReaderID, " +
                "r.FirstName, r.MiddleName, r.LastName, " +
                "l.BorrowDate, l.ReturnDate, l.Status " +
                "from [loan].[Loan] l " +
                "inner join [auth].[Reader] r on l.ReaderID = r.ReaderID " +
                "inner join [lib].[Book] b on l.ISBN = b.ISBN " +
                "order by l.BorrowDate DESC";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Build reader full name
                String fname = rs.getString("FirstName");
                String mname = rs.getString("MiddleName");
                String lname = rs.getString("LastName");

                StringBuilder fullName = new StringBuilder();
                if (fname != null && !fname.isEmpty()) {
                    fullName.append(fname);
                }
                if (mname != null && !mname.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(mname);
                }
                if (lname != null && !lname.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(lname);
                }
                String readerName = fullName.toString().trim();
                if (readerName.isEmpty()) {
                    readerName = "-";
                }

                // Format dates
                java.sql.Date borrowDate = rs.getDate("BorrowDate");
                java.sql.Date returnDate = rs.getDate("ReturnDate");
                String issueDateStr = borrowDate != null ? dateFormat.format(borrowDate) : "-";
                String returnDateStr = returnDate != null ? dateFormat.format(returnDate) : "-";

                // Get status
                String status = rs.getString("Status");
                if (status == null || status.isEmpty()) {
                    status = "pending";
                }

                tableModel.addRow(new Object[]{
                        rs.getString("ISBN"),                    // Book ID
                        rs.getString("title") != null ? rs.getString("title") : "-",  // Book Name
                        String.valueOf(rs.getInt("ReaderID")),   // Reader ID
                        readerName,                              // Reader Name
                        issueDateStr,                            // Issue Date
                        returnDateStr,                              // Due Date
                        status                                   // Status
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load records", ex);
            JOptionPane.showMessageDialog(this, "Failed to load records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSearch() {
        Date fromDate = (Date) fromDateSpinner.getValue();
        Date toDate = (Date) toDateSpinner.getValue();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Please select both dates!");
            return;
        }

        if (fromDate.after(toDate)) {
            JOptionPane.showMessageDialog(this, "From date must be before or equal to To date!");
            return;
        }

        if (con == null) {
            return;
        }

        tableModel.setRowCount(0);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String fromDateStr = df.format(fromDate);
        String toDateStr = df.format(toDate);

        // Join [loan].[Loan] with auth_reader and lib_book to get all required information
        String sql = "select l.ISBN, b.title, l.ReaderID, " +
                "r.FirstName, r.MiddleName, r.LastName, " +
                "l.BorrowDate, l.ReturnDate, l.Status " +
                "from [loan].[Loan] l " +
                "inner join [auth].[Reader] r on l.ReaderID = r.ReaderID " +
                "inner join [lib].[Book] b on l.ISBN = b.ISBN " +
                "where l.BorrowDate BETWEEN ? and ? " +
                "order by l.BorrowDate DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, fromDateStr);
            stmt.setString(2, toDateStr);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;

                    // Build reader full name
                    String fname = rs.getString("FirstName");
                    String mname = rs.getString("MiddleName");
                    String lname = rs.getString("LastName");

                    StringBuilder fullName = new StringBuilder();
                    if (fname != null && !fname.isEmpty()) {
                        fullName.append(fname);
                    }
                    if (mname != null && !mname.isEmpty()) {
                        if (fullName.length() > 0) fullName.append(" ");
                        fullName.append(mname);
                    }
                    if (lname != null && !lname.isEmpty()) {
                        if (fullName.length() > 0) fullName.append(" ");
                        fullName.append(lname);
                    }
                    String readerName = fullName.toString().trim();
                    if (readerName.isEmpty()) {
                        readerName = "-";
                    }

                    // Format dates
                    java.sql.Date borrowDate = rs.getDate("BorrowDate");
                    java.sql.Date returnDate = rs.getDate("Date");
                    String issueDateStr = borrowDate != null ? df.format(borrowDate) : "-";
                    String returnDateStr = returnDate != null ? df.format(returnDate) : "-";

                    // Get status
                    String status = rs.getString("Status");
                    if (status == null || status.isEmpty()) {
                        status = "pending";
                    }

                    tableModel.addRow(new Object[]{
                            rs.getString("ISBN"),                    // Book ID
                            rs.getString("title") != null ? rs.getString("title") : "-",  // Book Name
                            String.valueOf(rs.getInt("ReaderID")),   // Reader ID
                            readerName,                              // Reader Name
                            issueDateStr,                            // Issue Date
                            returnDateStr,                              // Due Date
                            status                                   // Status
                    });
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "No records found for the selected date range.");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to search records", ex);
            JOptionPane.showMessageDialog(this, "Failed to search records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navigateTo(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setVisible(true);
        dispose();
    }

    private JLabel createLinkLabel(String text, String url, Color color) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(color);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openLink(url);
            }
        });
        return label;
    }

    private void openLink(String url) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Opening links is not supported on this system.");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Failed to open link", ex);
            JOptionPane.showMessageDialog(this, "Failed to open link.");
        }
    }

    private void setIconImageSafe() {
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo.png")));
        } catch (Exception ex) {
            LOGGER.fine("Unable to set window icon.");
        }
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ViewAllRecord.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ViewAllRecord().setVisible(true));
    }
}

