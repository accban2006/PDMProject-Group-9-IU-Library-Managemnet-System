/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
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
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author Name
 */
public class SignUpPage extends javax.swing.JFrame {

    private static final Color FORM_BACKGROUND = new Color(51, 153, 255);
    private static final Color PRIMARY_TEXT = new Color(40, 40, 40);
    private static final Logger LOGGER = Logger.getLogger(SignUpPage.class.getName());

    private Connection con;
    private int id;
    private String uname;
    private String userrole;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField displayeNameField;
    private JTextField readerIdField;
    private JTextField staffIdField;
    private JLabel readerIdLabel;
    private JLabel staffIdLabel;
    private JComboBox<String> roleCombo;
    private JLabel roleHeaderLabel;
    private JButton addUserButton;
    private JButton backButton;

    public SignUpPage() {
        initComponents();
        Connect();
        setIconImage();
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    public SignUpPage(int id, String username, String urole) {
        this();
        this.id = id;
        this.uname = username;
        this.userrole = urole;
        if (urole != null && !urole.trim().isEmpty()) {
            roleHeaderLabel.setText(urole);
            roleCombo.setSelectedItem(urole);
        }
    }

    private void setIconImage() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo.png")));
    }

    public final void Connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://YOUR_SERVER_NAME:1433;databaseName=YOUR_DB_NAME;user=YOUR_USERNAME;password=YOUR_PASSWORD;trustServerCertificate=true");
        } catch (SQLException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database", ex);
        }
    }

    private void attemptSignUp() {
        if (validateUser()) {
            insertUserDetails();
        }
    }

    private void insertUserDetails() {
        Connect();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String displayName = displayeNameField.getText().trim();
        String role = roleCombo.getSelectedItem().toString();

        // These two fields are shown/hidden depending on role
        String readerID = readerIdField.getText().trim();
        String staffID = staffIdField.getText().trim();

        // Validate role
        if (role.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please select Reader or Staff.");
            return;
        }

        String sql = "INSERT INTO [auth].[Authentication] " +
                "(Username, Password, DisplayName, ReaderID, StaffID, Role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, displayName);

            try {
                if (role.equalsIgnoreCase("Reader")) {
                    int readerIdValue = Integer.parseInt(readerID);
                    stmt.setInt(4, readerIdValue);      // ReaderID
                    stmt.setNull(5, Types.INTEGER);     // StaffID = NULL
                } else { // Staff
                    int staffIdValue = Integer.parseInt(staffID);
                    stmt.setNull(4, Types.INTEGER);     // ReaderID = NULL
                    stmt.setInt(5, staffIdValue);       // StaffID
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID must be numeric.");
                return;
            }

            stmt.setString(6, role);

            int updatedRowCount = stmt.executeUpdate();
            if (updatedRowCount > 0) {
                JOptionPane.showMessageDialog(this, "Account created successfully.");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Account insertion failed.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to insert user", ex);
            JOptionPane.showMessageDialog(this, "Failed to create account. Please try again.");
        }
    }


    private boolean validateUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String displayName = displayeNameField.getText().trim();
        String readerID = readerIdField.getText().trim();
        String staffID = staffIdField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "please enter your username");
            return false;
        }

        if (checkDuplicateUser(username)) {
            JOptionPane.showMessageDialog(this, "username already exists");
            return false;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "please enter your password");
            return false;
        }

        if (displayName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "please enter a display name");
            return false;
        }

        if (role == null || "Select".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(this, "please select a user role");
            return false;
        }

        if ("Reader".equalsIgnoreCase(role)) {
            if (readerID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "please enter a reader ID");
                return false;
            }
            if (!readerID.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "reader ID must be numeric");
                return false;
            }
            int readerNumeric = Integer.parseInt(readerID);
            if (!readerExists(readerNumeric)) {
                JOptionPane.showMessageDialog(this, "reader ID does not exist. Please register the reader first.");
                return false;
            }
        } else if ("Staff".equalsIgnoreCase(role)) {
            if (staffID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "please enter a staff ID");
                return false;
            }
            if (!staffID.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "staff ID must be numeric");
                return false;
            }
            int staffNumeric = Integer.parseInt(staffID);
            if (!staffExists(staffNumeric)) {
                JOptionPane.showMessageDialog(this, "staff ID does not exist. Please register the staff member first.");
                return false;
            }
        }

        return true;
    }

    private boolean checkDuplicateUser(String username) {
        Connect();
        if (con == null) {
            return false;
        }
        try (PreparedStatement stmt = con.prepareStatement("select ReaderID from [auth].[Authentication] where Username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to check duplicate user", ex);
            return false;
        }
    }

    private boolean readerExists(int readerId) {
        return recordExists("select ReaderID from [auth].[Reader] where ReaderID = ?", readerId, "reader");
    }

    private boolean staffExists(int staffId) {
        return recordExists("select StaffID from [auth].[Staff] where StaffID = ?", staffId, "staff");
    }

    private boolean recordExists(String query, int id, String entityName) {
        Connect();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.");
            return false;
        }
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to verify " + entityName + " ID", ex);
            return false;
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        displayeNameField.setText("");
        readerIdField.setText("");
        staffIdField.setText("");
        roleCombo.setSelectedIndex(0);
        updateUserRoleHeader();
        updateIdFieldVisibility();
        usernameField.requestFocusInWindow();
    }

    private void updateIdFieldVisibility() {
        if (roleCombo == null) {
            return;
        }

        String selected = (String) roleCombo.getSelectedItem();
        boolean readerSelected = "Reader".equalsIgnoreCase(selected);
        boolean staffSelected = "Staff".equalsIgnoreCase(selected);

        if (readerIdLabel != null && readerIdField != null) {
            readerIdLabel.setVisible(readerSelected);
            readerIdField.setVisible(readerSelected);
            if (!readerSelected) {
                readerIdField.setText("");
            }
        }

        if (staffIdLabel != null && staffIdField != null) {
            staffIdLabel.setVisible(staffSelected);
            staffIdField.setVisible(staffSelected);
            if (!staffSelected) {
                staffIdField.setText("");
            }
        }

        java.awt.Container parent = readerIdField != null ? readerIdField.getParent()
                : (staffIdField != null ? staffIdField.getParent() : null);
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private void handleDuplicateCheck() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty() && checkDuplicateUser(username)) {
            JOptionPane.showMessageDialog(this, "username already exists");
            usernameField.requestFocusInWindow();
        }
    }

    private void navigateBackToHome() {
        if (userrole != null && uname != null) {
            new HomePage(id, uname, userrole).setVisible(true);
        }
        dispose();
    }

    private void updateUserRoleHeader() {
        String selection = (String) roleCombo.getSelectedItem();
        if (selection == null || "Select".equalsIgnoreCase(selection)) {
            roleHeaderLabel.setText("User");
        } else {
            roleHeaderLabel.setText(selection);
        }
    }

    private JLabel createLinkLabel(String text, String url, Color color) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(color);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setFont(new Font("Calibri", Font.BOLD, 14));
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
            JOptionPane.showMessageDialog(this, "Unable to open links on this system.");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "Failed to open link: " + url, ex);
            JOptionPane.showMessageDialog(this, "Failed to open link.");
        }
    }

    private <T extends JTextField> T styleTextField(T field) {
        field.setFont(new Font("Tahoma", Font.PLAIN, 16));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        field.setBackground(FORM_BACKGROUND);
        field.setForeground(PRIMARY_TEXT);
        field.setCaretColor(Color.WHITE);
        return field;
    }

    private JButton styleButton(JButton button, Color background, Color foreground, int fontSize) {
        button.setFont(new Font("Tahoma", Font.BOLD, fontSize));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        return button;
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Signup page");
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1100, 700));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(520, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcomeHeader = new JLabel("WELCOME TO");
        welcomeHeader.setFont(new Font("Calibri", Font.BOLD, 36));
        welcomeHeader.setForeground(new Color(255, 51, 51));
        welcomeHeader.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(welcomeHeader, BorderLayout.NORTH);

        JLabel heroImage = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/icons/signup-library-icon.png")));
        heroImage.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(heroImage, BorderLayout.CENTER);

        JPanel creditsPanel = new JPanel(new GridBagLayout());
        creditsPanel.setBackground(Color.WHITE);
        creditsPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        GridBagConstraints creditsGbc = new GridBagConstraints();
        creditsGbc.gridx = 0;
        creditsGbc.gridy = 0;
        creditsGbc.insets = new Insets(2, 5, 2, 5);
        creditsGbc.anchor = GridBagConstraints.CENTER;

        JLabel developedByLabel = new JLabel("Developed by:");
        developedByLabel.setFont(new Font("Calibri", Font.PLAIN, 14));
        developedByLabel.setForeground(PRIMARY_TEXT);
        creditsPanel.add(developedByLabel, creditsGbc);

        creditsGbc.gridx = 1;
        creditsPanel.add(createLinkLabel("Naveenkumar J", "https://github.com/naveenkumar-j", new Color(0, 102, 204)), creditsGbc);

        creditsGbc.gridx = 0;
        creditsGbc.gridy = 1;
        creditsGbc.gridwidth = 2;
        creditsPanel.add(createLinkLabel("EAGLE PROGRAMMER", "https://www.instagram.com/eagle_programming/?hl=en", new Color(255, 51, 51)), creditsGbc);

        leftPanel.add(creditsPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(FORM_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel welcomeLabel = new JLabel("Welcome,", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Tahoma", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        formPanel.add(welcomeLabel, gbc);

        gbc.gridy++;
        roleHeaderLabel = new JLabel("User", SwingConstants.CENTER);
        roleHeaderLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
        roleHeaderLabel.setForeground(Color.WHITE);
        formPanel.add(roleHeaderLabel, gbc);

        gbc.gridy++;
        JLabel subTitle = new JLabel("Create a new account below", SwingConstants.CENTER);
        subTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
        subTitle.setForeground(new Color(240, 240, 240));
        formPanel.add(subTitle, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        usernameLabel.setForeground(Color.WHITE);
        formPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        usernameField = styleTextField(new JTextField(25));
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                handleDuplicateCheck();
            }
        });
        formPanel.add(usernameField, gbc);

        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        passwordLabel.setForeground(Color.WHITE);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        passwordField = styleTextField(new JPasswordField(25));
        passwordField.addActionListener(e -> attemptSignUp());
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        JLabel emailLabel = new JLabel("DisplayName");
        emailLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        emailLabel.setForeground(Color.WHITE);
        formPanel.add(emailLabel, gbc);

        gbc.gridy++;
        displayeNameField = styleTextField(new JTextField(25));
        formPanel.add(displayeNameField, gbc);

        gbc.gridy++;
        JLabel userRoleLabel = new JLabel("User Role");
        userRoleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        userRoleLabel.setForeground(Color.WHITE);
        formPanel.add(userRoleLabel, gbc);

        gbc.gridy++;
        roleCombo = new JComboBox<>(new String[]{"Select", "Reader", "Staff"});
        roleCombo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        roleCombo.setBackground(Color.WHITE);
        roleCombo.setForeground(PRIMARY_TEXT);
        roleCombo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        roleCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        roleCombo.addActionListener(e -> {
            updateUserRoleHeader();
            updateIdFieldVisibility();
        });
        formPanel.add(roleCombo, gbc);

        // Add to panel
        gbc.gridy++;
        readerIdLabel = new JLabel("Reader ID");
        readerIdLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        readerIdLabel.setForeground(Color.WHITE);
        formPanel.add(readerIdLabel, gbc);

        gbc.gridy++;
        readerIdField = styleTextField(new JTextField(25));
        formPanel.add(readerIdField, gbc);

        gbc.gridy++;
        staffIdLabel = new JLabel("Staff ID");
        staffIdLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        staffIdLabel.setForeground(Color.WHITE);
        formPanel.add(staffIdLabel, gbc);

        gbc.gridy++;
        staffIdField = styleTextField(new JTextField(25));
        formPanel.add(staffIdField, gbc);

        updateIdFieldVisibility();

        gbc.gridy++;
        gbc.insets = new Insets(24, 0, 12, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        addUserButton = styleButton(new JButton("Add User"), new Color(255, 87, 51), Color.WHITE, 18);
        addUserButton.addActionListener(e -> attemptSignUp());
        formPanel.add(addUserButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 8, 0);
        backButton = styleButton(new JButton("Back to Home"), new Color(0, 102, 204), Color.WHITE, 16);
        backButton.addActionListener(e -> navigateBackToHome());
        formPanel.add(backButton, gbc);

        gbc.gridy++;
        JButton cancelButton = styleButton(new JButton("Cancel"), Color.WHITE, PRIMARY_TEXT, 14);
        cancelButton.addActionListener(e -> dispose());
        formPanel.add(cancelButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(addUserButton);
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new SignUpPage().setVisible(true));
    }
}

