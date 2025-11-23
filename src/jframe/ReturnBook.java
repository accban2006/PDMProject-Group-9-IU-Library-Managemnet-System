package jframe;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Pure Swing implementation of the Return Book screen.
 */
public class ReturnBook extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ReturnBook.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color CARD_BACKGROUND = new Color(255, 51, 51);
    private static final Color ERROR_COLOR = new Color(255, 215, 0);

    private Connection con;

    private int id;
    private String uname;
    private String userrole;

    private JTextField bookIdField;
    private JTextField readerIdField;

    private JLabel issueIdValueLabel;
    private JLabel bookIdValueLabel;
    private JLabel bookNameValueLabel;
    private JLabel readerIdValueLabel;
    private JLabel readerNameValueLabel;
    private JLabel issueDateValueLabel;
    private JLabel dueDateValueLabel;
    private JLabel errorLabel;

    private JButton findButton;
    private JButton returnButton;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public ReturnBook() {
        this(0, null, null);
    }

    public ReturnBook(int id, String username, String urole) {
        this.id = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            bookIdField.requestFocusInWindow();
            applyRolePermissions();
            updateUserContext();
        });
    }

    private void initializeUi() {
        setTitle("Return Book");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1100, 720));
        setIconImageSafe();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
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

        JLabel titleLabel = new JLabel("Return Book", SwingConstants.CENTER);
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

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.setBorder(new EmptyBorder(24, 24, 24, 24));
        content.setBackground(new Color(245, 245, 245));

        content.add(buildBookDetailsCard(), BorderLayout.WEST);
        content.add(buildReturnFormCard(), BorderLayout.EAST);

        return content;
    }

    private JPanel buildBookDetailsCard() {
        JPanel card = createCardPanel("Issued Book Details");
        // Expand the card to take more space
        card.setPreferredSize(new Dimension(600, 0));

        issueIdValueLabel = createValueLabel("-");
        bookIdValueLabel = createValueLabel("-");
        bookNameValueLabel = createValueLabel("-");
        readerIdValueLabel = createValueLabel("-");
        readerNameValueLabel = createValueLabel("-");
        issueDateValueLabel = createValueLabel("-");
        dueDateValueLabel = createValueLabel("-");

        GridBagConstraints gbc = createGbc();
        JPanel body = (JPanel) card.getComponent(1);
        addRow(body, gbc, "Issue ID:", issueIdValueLabel);
        addRow(body, gbc, "Book ID (ISBN):", bookIdValueLabel);
        addRow(body, gbc, "Book Name:", bookNameValueLabel);
        addRow(body, gbc, "Reader ID:", readerIdValueLabel);
        addRow(body, gbc, "Reader Name:", readerNameValueLabel);
        addRow(body, gbc, "Issue Date:", issueDateValueLabel);
        addRow(body, gbc, "Due Date:", dueDateValueLabel);

        errorLabel = createErrorLabel();
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 0, 0, 0);
        body.add(errorLabel, gbc);

        return card;
    }

    private JPanel buildReturnFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel titleLabel = new JLabel("Return Book", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(new Color(255, 51, 51));
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy++;

        addLabeledField(card, gbc, "Book ID (ISBN):", bookIdField = new JTextField(15));
        configureInputField(bookIdField, "Enter Book ID");

        addLabeledField(card, gbc, "Reader ID:", readerIdField = new JTextField(15));
        configureInputField(readerIdField, "Enter Reader ID");

        gbc.gridy++;
        gbc.insets = new Insets(16, 0, 8, 0);
        findButton = new JButton("Find");
        stylePrimaryButton(findButton);
        findButton.addActionListener(e -> findIssueDetails());
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(findButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        returnButton = new JButton("Return Book");
        styleSecondaryButton(returnButton);
        returnButton.addActionListener(e -> processReturn());
        card.add(returnButton, gbc);

        return card;
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

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 12);
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
    }

    private JLabel createValueLabel(String initialText) {
        JLabel label = new JLabel(initialText);
        label.setFont(new Font("Tahoma", Font.PLAIN, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(ERROR_COLOR);
        label.setBorder(new EmptyBorder(12, 0, 0, 0));
        return label;
    }

    private void configureInputField(JTextField field, String placeholder) {
        field.setFont(new Font("Tahoma", Font.PLAIN, 16));
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                new EmptyBorder(4, 4, 4, 4)
        ));
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, java.awt.Component component) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));

        gbc.gridwidth = 2;
        panel.add(label, gbc);
        gbc.gridy++;

        component.setPreferredSize(new Dimension(0, 32));
        panel.add(component, gbc);
        gbc.gridy++;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(220, 53, 69));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Tahoma", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
    }

    private void applyRolePermissions() {
        boolean canReturn = userrole != null && (userrole.equalsIgnoreCase("Staff"));
        findButton.setEnabled(true);
        returnButton.setEnabled(canReturn);
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
            JOptionPane.showMessageDialog(this, "Unable to connect to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setIconImageSafe() {
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo.png")));
        } catch (Exception ex) {
            LOGGER.fine("Unable to set window icon.");
        }
    }

    private void findIssueDetails() {
        String isbn = bookIdField.getText().trim();
        String readerIdText = readerIdField.getText().trim();

        if (isbn.isEmpty() || readerIdText.isEmpty()) {
            errorLabel.setText("Please enter both ISBN and Reader ID.");
            clearIssueDetails();
            return;
        }

        // Validate ISBN length (VARCHAR(50))
        if (isbn.length() > 50) {
            errorLabel.setText("ISBN must be 50 characters or less.");
            clearIssueDetails();
            return;
        }

        int readerId;
        try {
            readerId = Integer.parseInt(readerIdText);
        } catch (NumberFormatException ex) {
            errorLabel.setText("Reader ID must be numeric.");
            clearIssueDetails();
            return;
        }

        Connect();
        if (con == null) {
            return;
        }

        // Join with [auth].[Reader] to get reader name and [lib].[Book] to get book title
        String sql = "select l.ReaderID, l.ISBN, l.BorrowDate, l.DueDate, " +
                "r.FirstName, r.MiddleName, r.LastName, " +
                "b.title " +
                "from [loan].[Loan] l " +
                "inner join [auth].[Reader] r on l.ReaderID = r.ReaderID " +
                "inner join [lib].[Book] b on l.ISBN = b.ISBN " +
                "where l.ISBN = ? and l.ReaderID = ? and (l.Status = 'pending' OR l.DueDate IS NULL)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.setInt(2, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
                    String fullNameStr = fullName.toString().trim();
                    if (fullNameStr.isEmpty()) {
                        fullNameStr = "-";
                    }

                    // Format dates
                    java.sql.Date borrowDate = rs.getDate("BorrowDate");
                    java.sql.Date dueDate = rs.getDate("DueDate");

                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

                    // Set UI labels
                    issueIdValueLabel.setText("-"); // No separate issue ID in this schema
                    bookIdValueLabel.setText(rs.getString("ISBN"));
                    bookNameValueLabel.setText(rs.getString("title") != null ? rs.getString("title") : "-");
                    readerIdValueLabel.setText(String.valueOf(rs.getInt("ReaderID")));
                    readerNameValueLabel.setText(fullNameStr);
                    issueDateValueLabel.setText(borrowDate != null ? dateFormat.format(borrowDate) : "-");
                    dueDateValueLabel.setText(dueDate != null ? dateFormat.format(dueDate) : "-");

                    errorLabel.setText(" ");
                } else {
                    errorLabel.setText("No records found for this ISBN and reader combination.");
                    clearIssueDetails();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load issue details", ex);
            errorLabel.setText("Error loading issue details.");
            clearIssueDetails();
        }
    }

    private boolean returnBook() {
        String isbn = bookIdField.getText().trim();
        String readerIdText = readerIdField.getText().trim();

        if (isbn.isEmpty() || readerIdText.isEmpty()) {
            return false;
        }

        // Validate ISBN length
        if (isbn.length() > 50) {
            return false;
        }

        int readerId;
        try {
            readerId = Integer.parseInt(readerIdText);
        } catch (NumberFormatException ex) {
            return false;
        }

        Connect();
        if (con == null) {
            return false;
        }

        // Update ReturnDate to current date and set status to returned
        // Using CURDATE() for ReturnDate and updating status
        String sql = "update [loan].[Loan] set DueDate = CURDATE(), Status = ? where ISBN = ? and ReaderID = ? and (Status = ? OR DueDate IS NULL)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, "returned");
            stmt.setString(2, isbn);
            stmt.setInt(3, readerId);
            stmt.setString(4, "pending");
            int rowCount = stmt.executeUpdate();
            return rowCount > 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to return book", ex);
            return false;
        }
    }

    private void processReturn() {
        if (returnBook()) {
            JOptionPane.showMessageDialog(this, "Book returned successfully!");
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Book return failed. Please check the details and try again.");
        }
    }

    private void clearForm() {
        bookIdField.setText("");
        readerIdField.setText("");
        clearIssueDetails();
        errorLabel.setText(" ");
        bookIdField.requestFocusInWindow();
    }

    private void clearIssueDetails() {
        issueIdValueLabel.setText("-");
        bookIdValueLabel.setText("-");
        bookNameValueLabel.setText("-");
        readerIdValueLabel.setText("-");
        readerNameValueLabel.setText("-");
        issueDateValueLabel.setText("-");
        dueDateValueLabel.setText("-");
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

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ReturnBook.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ReturnBook().setVisible(true));
    }
}

