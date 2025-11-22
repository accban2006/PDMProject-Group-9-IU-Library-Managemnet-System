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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Pure Swing implementation of the Issue Book screen.
 */
public class IssueBook extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(IssueBook.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color CARD_BACKGROUND = new Color(33, 150, 243);
    private static final Color ERROR_COLOR = new Color(255, 215, 0);

    private Connection con;

    private int id;
    private String uname;
    private String usertype;

    private JTextField bookIdField;
    private JTextField readerIdField;
    private JLabel bookIdValueLabel;
    private JLabel bookNameValueLabel;
    private JLabel bookAuthorValueLabel;
    private JLabel bookQuantityValueLabel;
    private JLabel bookErrorLabel;

    private JLabel readerIdValueLabel;
    private JLabel readerNameValueLabel;
    private JLabel readerErrorLabel;

    private JSpinner issueDateSpinner;
    private JSpinner dueDateSpinner;
    private JButton issueButton;
    private JButton clearButton;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public IssueBook() {
        this(0, null, null);
    }

    public IssueBook(int id, String username, String utype) {
        this.id = id;
        this.uname = username;
        this.usertype = utype;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            bookIdField.requestFocusInWindow();
            applyRolePermissions();
            updateUserContext();
        });
    }

    private void initializeUi() {
        setTitle("Issue Book");
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
        backButton.addActionListener(e -> navigateTo(new HomePage(id, uname, usertype)));

        header.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Issue Book", SwingConstants.CENTER);
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
        content.add(buildReaderDetailsCard(), BorderLayout.CENTER);
        content.add(buildIssueFormCard(), BorderLayout.EAST);

        return content;
    }

    private JPanel buildBookDetailsCard() {
        JPanel card = createCardPanel("Book Details");

        bookIdValueLabel = createValueLabel("-");
        bookNameValueLabel = createValueLabel("-");
//        bookAuthorValueLabel = createValueLabel("-");
//        bookQuantityValueLabel = createValueLabel("-");

        GridBagConstraints gbc = createGbc();
        JPanel body = (JPanel) card.getComponent(1);
        addRow(body, gbc, "Book ID (ISBN):", bookIdValueLabel);
        addRow(body, gbc, "Book Name:", bookNameValueLabel);
//        addRow(body, gbc, "Author:", bookAuthorValueLabel);
//        addRow(body, gbc, "Quantity:", bookQuantityValueLabel);

        bookErrorLabel = createErrorLabel();
        card.add(bookErrorLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildReaderDetailsCard() {
        JPanel card = createCardPanel("Reader Details");

        readerIdValueLabel = createValueLabel("-");
        readerNameValueLabel = createValueLabel("-");
//        courseValueLabel = createValueLabel("-");
//        branchValueLabel = createValueLabel("-");

        GridBagConstraints gbc = createGbc();
        JPanel body = (JPanel) card.getComponent(1);
        addRow(body, gbc, "Reader ID:", readerIdValueLabel);
        addRow(body, gbc, "Reader Name:", readerNameValueLabel);
//        addRow(body, gbc, "Course:", courseValueLabel);
//        addRow(body, gbc, "Branch:", branchValueLabel);

        readerErrorLabel = createErrorLabel();
        card.add(readerErrorLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildIssueFormCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(24, 24, 24, 24)
        ));
        card.setPreferredSize(new Dimension(360, 0));

        JLabel title = new JLabel("Issue Details", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 20));
        title.setForeground(new Color(70, 70, 70));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        bookIdField = new JTextField();
        configureInputField(bookIdField, "Enter Book ID (ISBN)");
        bookIdField.addActionListener(e -> fetchBookDetails());
        bookIdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fetchBookDetails();
            }
        });

        readerIdField = new JTextField();
        configureInputField(readerIdField, "Enter Reader ID");
        readerIdField.addActionListener(e -> fetchReaderDetails());
        readerIdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fetchReaderDetails();
            }
        });

        issueDateSpinner = createDateSpinner(new Date());
        dueDateSpinner = createDateSpinner(addDays(new Date(), 7));

        addLabeledField(form, gbc, "Book ID (ISBN)", bookIdField);
        addLabeledField(form, gbc, "Reader ID", readerIdField);
        addLabeledField(form, gbc, "Issue Date", issueDateSpinner);
        addLabeledField(form, gbc, "Due Date", dueDateSpinner);

        issueButton = new JButton("Issue Book");
        stylePrimaryButton(issueButton);
        issueButton.addActionListener(e -> issueBook());

        clearButton = new JButton("Clear Form");
        styleSecondaryButton(clearButton);
        clearButton.addActionListener(e -> clearForm());

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(20, 0, 0, 8);
        form.add(issueButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(20, 8, 0, 0);
        form.add(clearButton, gbc);

        card.add(form, BorderLayout.CENTER);

        JButton developerLink = new JButton("Developed by Naveenkumar J");
        developerLink.setBorder(BorderFactory.createEmptyBorder());
        developerLink.setContentAreaFilled(false);
        developerLink.setForeground(new Color(0, 102, 204));
        developerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        developerLink.addActionListener(e -> openLink("https://github.com/naveenkumar-j"));
        card.add(developerLink, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 24, 8, 24));
        JLabel note = new JLabel("Tip: Enter the IDs to load details automatically before issuing a book.");
        note.setFont(new Font("Tahoma", Font.ITALIC, 12));
        footer.add(note);
        return footer;
    }

    private JPanel createCardPanel(String titleText) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(320, 0));

        JLabel title = new JLabel(titleText, SwingConstants.LEFT);
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String labelText, JLabel valueLabel) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Tahoma", Font.BOLD, 16));
        label.setForeground(new Color(230, 230, 230));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(label, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);

        panel.add(row, gbc);
        gbc.gridy++;
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

    private JSpinner createDateSpinner(Date value) {
        SpinnerDateModel model = new SpinnerDateModel(value, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Tahoma", Font.PLAIN, 16));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
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
        button.setBackground(new Color(0, 123, 255));
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
        boolean canIssue = usertype != null && (usertype.equalsIgnoreCase("Staff"));
        issueButton.setEnabled(canIssue);
        clearButton.setEnabled(canIssue);
    }

    private void updateUserContext() {
        String displayName = (uname == null || uname.trim().isEmpty()) ? "Guest" : uname;
        String displayRole = (usertype == null || usertype.trim().isEmpty()) ? "Guest" : usertype;
        welcomeValueLabel.setText(displayName);
        roleValueLabel.setText(displayRole);
    }

    public final void Connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=LibraryManagementSystemGroup9;user=sa;password=Daigia_minhphuc1511;trustServerCertificate=true");
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database connection failed", ex);
            JOptionPane.showMessageDialog(this, "Unable to connect to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchBookDetails() {
        String isbn = bookIdField.getText().trim();
        if (isbn.isEmpty()) {
            clearBookDetails();
            return;
        }

//        if (isbn.length() >= 13) {
//            bookErrorLabel.setText("ISBN must be 13 characters or more.");
//            clearBookDetails();
//            return;
//        }

        Connect();
        if (con == null) {
            return;
        }
        String sql = "select ISBN, title from [lib].[Book] where ISBN = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                    bookIdValueLabel.setText(rs.getString("ISBN"));
                    bookNameValueLabel.setText(rs.getString("title"));
//                    bookAuthorValueLabel.setText(rs.getString("author"));
//                    bookQuantityValueLabel.setText(rs.getString("quantity"));
                    bookErrorLabel.setText(" ");
            } else {
                    bookErrorLabel.setText("Book ISBN not found.");
                    clearBookDetails();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load book details", ex);
            bookErrorLabel.setText("Error loading book details.");
            clearBookDetails();
        }
    }

    private void fetchReaderDetails() {
        String text = readerIdField.getText().trim();
        if (text.isEmpty()) {
            clearStudentDetails();
            return;
        }

        int readerId;
        try {
            readerId = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            readerErrorLabel.setText("Reader ID must be numeric.");
            clearStudentDetails();
            return;
        }

        Connect();
        if (con == null) {
            return;
        }

        // Query from [auth].[Reader] table which has FirstName, MiddleName, LastName columns
        String sql = "select ReaderID, FirstName, MiddleName, LastName from [auth].[Reader] where ReaderID = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, readerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String fname = rs.getString("FirstName");
                    String mname = rs.getString("MiddleName");
                    String lname = rs.getString("LastName");

                    // Build full name safely - concatenate with proper spacing
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

                    // Set UI labels
                    readerIdValueLabel.setText(String.valueOf(rs.getInt("ReaderID")));
                    readerNameValueLabel.setText(fullNameStr);

                    readerErrorLabel.setText(" ");

                } else {
                    readerErrorLabel.setText("Reader ID not found.");
                    clearStudentDetails();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load reader details", ex);
            readerErrorLabel.setText("Error loading reader details.");
            clearStudentDetails();
        }
    }

    public boolean isAlreadyIssued() {
        String isbn = bookIdField.getText().trim();
        String readerText = readerIdField.getText().trim();
        if (isbn.isEmpty() || readerText.isEmpty()) {
            return false;
        }

        int readerId;
        try {
            readerId = Integer.parseInt(readerText);
        } catch (NumberFormatException ex) {
            return false;
        }

        Connect();
        if (con == null) {
            return false;
        }

        // Check if there's an active loan (where ReturnDate is NULL or in the future)
        String sql = "select * from [loan].[Loan] where ISBN = ? and ReaderID = ? and (DueDate IS NULL OR DueDate >= CURDATE())";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.setInt(2, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to check issue status", ex);
            return false;
        }
    }

    private void issueBook() {
        String bookIdText = bookIdField.getText().trim();
        String readerIdText = readerIdField.getText().trim();

        if (bookIdText.isEmpty() || readerIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Book ID (ISBN) and Reader ID.");
            return;
        }

        if ("-".equals(bookNameValueLabel.getText()) || "-".equals(readerNameValueLabel.getText())) {
            JOptionPane.showMessageDialog(this, "Load both book and reader details before issuing.");
            return;
        }

        if (isAlreadyIssued()) {
            JOptionPane.showMessageDialog(this, "This reader already has this book issued.");
            return;
        }

        Date issueDate = (Date) issueDateSpinner.getValue();
        Date returnDate = (Date) dueDateSpinner.getValue();
        if (returnDate.before(issueDate)) {
            JOptionPane.showMessageDialog(this, "Return date cannot be earlier than issue date.");
            return;
        }

        Connect();
        if (con == null) {
            return;
        }

        String isbn = bookIdText.trim();
        int readerId;
        try {
            readerId = Integer.parseInt(readerIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Reader ID must be numeric.");
            return;
        }

        // Validate ISBN length
        if (isbn.length() > 50) {
            JOptionPane.showMessageDialog(this, "ISBN must be 50 characters or less.");
            return;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String borrowDateString = df.format(issueDate);
        String returnDateString = df.format(returnDate);
        
        // Fix SQL: use BorrowDate and ReturnDate (not DueDate), remove extra commas
        String sql = "insert into [loan].[Loan](ISBN, ReaderID, BorrowDate, DueDate) values (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            stmt.setInt(2, readerId);
            stmt.setString(3, borrowDateString);
            stmt.setString(4, returnDateString);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book issued successfully.");
//            updateBookCount();
            clearForm();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to issue book", ex);
            JOptionPane.showMessageDialog(this, "Failed to issue book. Please try again.");
        }
    }

//    public void updateBookCount() {
//        String bookIdText = bookIdField.getText().trim();
//        if (bookIdText.isEmpty()) {
//            return;
//        }
//
//        int bookId;
//        try {
//            bookId = Integer.parseInt(bookIdText);
//        } catch (NumberFormatException ex) {
//            return;
//        }
//
//        Connect();
//        if (con == null) {
//            return;
//        }
//
//        String sql = "update book_details set quantity = quantity - 1 where book_id = ? and quantity > 0";
//        try (PreparedStatement stmt = con.prepareStatement(sql)) {
//            stmt.setInt(1, bookId);
//            int updated = stmt.executeUpdate();
//            if (updated > 0) {
//                fetchBookDetails();
//            } else {
//                JOptionPane.showMessageDialog(this, "Unable to decrement quantity (already zero).");
//            }
//        } catch (SQLException ex) {
//            LOGGER.log(Level.SEVERE, "Failed to update book count", ex);
//        }
//    }

    private void clearForm() {
        bookIdField.setText("");
        readerIdField.setText("");

        issueDateSpinner.setValue(new Date());
        dueDateSpinner.setValue(addDays(new Date(), 7));

        clearBookDetails();
        clearStudentDetails();
        bookErrorLabel.setText(" ");
        readerErrorLabel.setText(" ");
        bookIdField.requestFocusInWindow();
    }

    private void clearBookDetails() {
        bookIdValueLabel.setText("-");
        bookNameValueLabel.setText("-");
//        bookAuthorValueLabel.setText("-");
//        bookQuantityValueLabel.setText("0");
    }

    private void clearStudentDetails() {
        readerIdValueLabel.setText("-");
        readerNameValueLabel.setText("-");
//        courseValueLabel.setText("-");
//        branchValueLabel.setText("-");
    }

    private void navigateTo(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setVisible(true);
        dispose();
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
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo.png")));
        } catch (Exception ex) {
            LOGGER.fine("Unable to set window icon.");
        }
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
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
            LOGGER.log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new IssueBook().setVisible(true));
    }
}




