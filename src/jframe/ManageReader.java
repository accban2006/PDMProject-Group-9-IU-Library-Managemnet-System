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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Pure Swing implementation of the Manage Readers screen.
 */
public class ManageReader extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ManageReader.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color ERROR_COLOR = new Color(255, 51, 51);

    private Connection con;

    private int id;
    private String uname;
    private String userrole;

    private JTextField txtReaderId;
    private JTextField txtReaderName;
    private JTextField txtAddress;
    private JTextField txtEmail;
    private JTextField txtContact;
    private JComboBox<String> txtSex;
    private JSpinner DateOfBirthSpinner;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JTable readersTable;
    private DefaultTableModel tableModel;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;


    public ManageReader() {
        this(0, null, null);
    }

    public ManageReader(int id, String username, String urole) {
        this.id = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            updateUserContext();
            applyRolePermissions();
            if (con != null) {
                loadReader();
            }
        });
    }

    private void initializeUi() {
        setTitle("Manage Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 800));
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

        content.add(buildFormPanel(), BorderLayout.WEST);
        content.add(buildTableAndChartPanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(PRIMARY_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(24, 24, 24, 24)
        ));
        formPanel.setPreferredSize(new Dimension(450, 0));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Add/Edit Reader");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        fieldsPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel readerNameLabel = new JLabel("Reader Name:");
        readerNameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        readerNameLabel.setForeground(Color.WHITE);
        fieldsPanel.add(readerNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtReaderName = new JTextField();
        styleTextField(txtReaderName, "Enter Reader Name");
        fieldsPanel.add(txtReaderName, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        addressLabel.setForeground(Color.WHITE);
        fieldsPanel.add(addressLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtAddress = new JTextField();
        styleTextField(txtAddress, "Enter Address");
        fieldsPanel.add(txtAddress, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel DOBLabel = new JLabel("Date Of Birth:");
        DOBLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        DOBLabel.setForeground(Color.WHITE);
        fieldsPanel.add(DOBLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerDateModel dobModel = new SpinnerDateModel();
        DateOfBirthSpinner = new JSpinner(dobModel);
        JSpinner.DateEditor dobEditor = new JSpinner.DateEditor(DateOfBirthSpinner, "yyyy-MM-dd");
        DateOfBirthSpinner.setEditor(dobEditor);
        DateOfBirthSpinner.setFont(new Font("Tahoma", Font.PLAIN, 14));
        DateOfBirthSpinner.setBackground(PRIMARY_COLOR);
        ((JSpinner.DefaultEditor) DateOfBirthSpinner.getEditor()).getTextField().setBackground(PRIMARY_COLOR);
        ((JSpinner.DefaultEditor) DateOfBirthSpinner.getEditor()).getTextField().setForeground(Color.BLACK);
        fieldsPanel.add(DateOfBirthSpinner, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel courseLabel = new JLabel("Sex:");
        courseLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        courseLabel.setForeground(Color.WHITE);
        fieldsPanel.add(courseLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtSex = new JComboBox<>(new String[]{"Select", "M", "F"});
        txtSex.setFont(new Font("Tahoma", Font.PLAIN, 14));
        txtSex.setBackground(PRIMARY_COLOR);
        txtSex.setForeground(Color.WHITE);
        fieldsPanel.add(txtSex, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        emailLabel.setForeground(Color.WHITE);
        fieldsPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtEmail = new JTextField();
        styleTextField(txtEmail, "Enter Email");
        fieldsPanel.add(txtEmail, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel contactLabel = new JLabel("Phone Number:");
        contactLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        contactLabel.setForeground(Color.WHITE);
        fieldsPanel.add(contactLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtContact = new JTextField();
        styleTextField(txtContact, "Enter Phone Number");
        fieldsPanel.add(txtContact, gbc);

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        buttonsPanel.setOpaque(false);
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(8, 8, 8, 8);
        btnGbc.fill = GridBagConstraints.BOTH;
        btnGbc.weightx = 1.0;

        btnGbc.gridx = 0;
        btnGbc.gridy = 0;
        addButton = new JButton("Add");
        styleButton(addButton, ERROR_COLOR);
        addButton.addActionListener(e -> addReader());

        buttonsPanel.add(addButton, btnGbc);

        btnGbc.gridx = 1;
        editButton = new JButton("Edit");
        styleButton(editButton, ERROR_COLOR);
        editButton.addActionListener(e -> editReader());
        buttonsPanel.add(editButton, btnGbc);

        btnGbc.gridx = 0;
        btnGbc.gridy = 1;
        deleteButton = new JButton("Delete");
        styleButton(deleteButton, ERROR_COLOR);
        deleteButton.addActionListener(e -> deleteReader());
        buttonsPanel.add(deleteButton, btnGbc);

        btnGbc.gridx = 1;
        clearButton = new JButton("Clear");
        styleButton(clearButton, ERROR_COLOR);
        clearButton.addActionListener(e -> clearFields());
        buttonsPanel.add(clearButton, btnGbc);

        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return formPanel;
    }

    private JPanel buildTableAndChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        panel.add(buildTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Reader"
        ));

        String[] columnNames = {"Reader ID", "Reader Name", "Address", "Date Of Birth", "Sex", "Email", "Phone Number"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        readersTable = new JTable(tableModel);
        readersTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        readersTable.setRowHeight(25);
        readersTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        readersTable.setFillsViewportHeight(true);
        readersTable.setSelectionBackground(new Color(220, 220, 255));
        readersTable.setSelectionForeground(Color.BLACK);
        readersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadSelectedReaderToForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(readersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
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

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(4, 4, 4, 4)
        ));
        field.setBackground(PRIMARY_COLOR);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
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

    private void applyRolePermissions() {
        boolean canEdit = "Staff".equals(userrole);
        addButton.setEnabled(canEdit);
        editButton.setEnabled(canEdit);
        deleteButton.setEnabled(canEdit);
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

    public void loadReader() {
        if (con == null) {
            return;
        }

        tableModel.setRowCount(0);

        String sql = "select ReaderID, FirstName, MiddleName, LastName, Address, DateOfBirth, Sex, Email, PhoneNumber from [auth].[Reader]";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                // Build full name from FirstName, MiddleName, LastName
                String firstName = rs.getString("FirstName") != null ? rs.getString("FirstName") : "";
                String middleName = rs.getString("MiddleName") != null ? rs.getString("MiddleName") : "";
                String lastName = rs.getString("LastName") != null ? rs.getString("LastName") : "";

                StringBuilder fullName = new StringBuilder();
                if (!firstName.isEmpty()) fullName.append(firstName);
                if (!middleName.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(middleName);
                }
                if (!lastName.isEmpty()) {
                    if (fullName.length() > 0) fullName.append(" ");
                    fullName.append(lastName);
                }

                // Format DateOfBirth
                String dobStr = "";
                java.sql.Date dobDate = rs.getDate("DateOfBirth");
                if (dobDate != null) {
                    dobStr = dateFormat.format(dobDate);
                }

                tableModel.addRow(new Object[]{
                        rs.getString("ReaderID"),
                        fullName.toString(),
                        rs.getString("Address"),
                        dobStr,
                        rs.getString("Sex"),
                        rs.getString("Email"),
                        rs.getString("PhoneNumber")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load readers", ex);
            JOptionPane.showMessageDialog(this, "Failed to load readers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addReader() {
        String readerName = txtReaderName.getText().trim();
        String address = txtAddress.getText().trim();
        String sex = (String) txtSex.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtContact.getText().trim();

        // Get DateOfBirth from spinner
        Date dobDate = (Date) DateOfBirthSpinner.getValue();

        // Validate
        if (readerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!");
            return;
        }
        if (con == null) return;

        // --------- Split Reader Name ---------
        String[] parts = readerName.split("\\s+");

        String fname = "";
        String mname = "";
        String lname = "";

        if (parts.length == 1) {
            fname = parts[0];
        }
        else if (parts.length == 2) {
            fname = parts[0];
            lname = parts[1];
        }
        else {
            fname = parts[0];
            lname = parts[parts.length - 1];

            StringBuilder middle = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                middle.append(parts[i]).append(" ");
            }
            mname = middle.toString().trim();
        }

        // --------- SQL Insert ---------
        String sql = "INSERT INTO [auth].[Reader](FirstName, MiddleName, LastName, Address, DateOfBirth, Sex, Email, PhoneNumber) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, fname);        // FirstName
            stmt.setString(2, mname.isEmpty() ? null : mname);        // MiddleName
            stmt.setString(3, lname);        // LastName
            stmt.setString(4, address);      // Address
            java.sql.Date sqlDate = dobDate != null ? new java.sql.Date(dobDate.getTime()) : null;
            stmt.setDate(5, sqlDate);          // DateOfBirth
            stmt.setString(6, "Select".equals(sex) ? null : sex);          // Sex
            stmt.setString(7, email.isEmpty() ? null : email);        // Email
            stmt.setString(8, phone.isEmpty() ? null : phone);        // PhoneNumber

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Reader added successfully!");

            clearFields();
            loadReader();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to add reader", ex);
            JOptionPane.showMessageDialog(this, "Failed to add reader.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void editReader() {
        int selectedRow = readersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reader from the table!");
            return;
        }

        String readerId = txtReaderId.getText().trim();
        String readerName = txtReaderName.getText().trim();
        String address = txtAddress.getText().trim();
        String sex = (String) txtSex.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtContact.getText().trim();

        // Get DateOfBirth from spinner
        Date dobDate = (Date) DateOfBirthSpinner.getValue();

        // Validate
        if (readerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields!");
            return;
        }
        if (con == null) return;

        // --------- Split Reader Name ---------
        String[] parts = readerName.split("\\s+");

        String fname = "";
        String mname = "";
        String lname = "";

        if (parts.length == 1) {
            fname = parts[0];
        }
        else if (parts.length == 2) {
            fname = parts[0];
            lname = parts[1];
        }
        else {
            fname = parts[0];
            lname = parts[parts.length - 1];

            StringBuilder middle = new StringBuilder();
            for (int i = 1; i < parts.length - 1; i++) {
                middle.append(parts[i]).append(" ");
            }
            mname = middle.toString().trim();
        }

        if (readerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if (con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("update [auth].[Reader] set FirstName = ?, MiddleName = ?, LastName = ?, Address = ?, DateOfBirth = ?, Sex = ?, Email = ?, PhoneNumber = ? where ReaderID = ?")) {
            stmt.setString(1, fname);        // FirstName
            stmt.setString(2, mname.isEmpty() ? null : mname);        // MiddleName
            stmt.setString(3, lname);        // LastName
            stmt.setString(4, address);      // Address
            java.sql.Date sqlDate = dobDate != null ? new java.sql.Date(dobDate.getTime()) : null;
            stmt.setDate(5, sqlDate);          // DateOfBirth
            stmt.setString(6, "Select".equals(sex) ? null : sex);          // Sex
            stmt.setString(7, email.isEmpty() ? null : email);        // Email
            stmt.setString(8, phone.isEmpty() ? null : phone);        // PhoneNumber
            stmt.setString(9, readerId);     // ReaderID (WHERE clause)

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Reader updated successfully!");
            clearFields();
            loadReader();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to update reader", ex);
            JOptionPane.showMessageDialog(this, "Failed to update reader.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteReader() {
        int selectedRow = readersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reader from the table!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this reader?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String readerId = (String) tableModel.getValueAt(selectedRow, 0);

        if (con == null) {
            return;
        }

        try {
            con.setAutoCommit(false); // Start transaction

            // Delete Authentication
            try (PreparedStatement stmt1 = con.prepareStatement(
                    "DELETE FROM auth.Authentication WHERE ReaderID = ?")) {
                stmt1.setString(1, readerId);
                stmt1.executeUpdate();
            }

            // Delete Reader
            try (PreparedStatement stmt2 = con.prepareStatement(
                    "DELETE FROM auth.Reader WHERE ReaderID = ?")) {
                stmt2.setString(1, readerId);
                stmt2.executeUpdate();
            }

            con.commit(); // Both succeed
            JOptionPane.showMessageDialog(this, "Reader deleted successfully!");
            clearFields();
            loadReader();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to delete reader", ex);
            JOptionPane.showMessageDialog(this, "Failed to delete reader.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtReaderName.setText("");
        txtAddress.setText("");
        txtEmail.setText("");
        txtContact.setText("");
        txtSex.setSelectedIndex(0);
        // Reset DateOfBirth spinner to current date
        DateOfBirthSpinner.setValue(new Date());
        readersTable.clearSelection();
        addButton.setEnabled(true);
    }

    private void loadSelectedReaderToForm() {
        int selectedRow = readersTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        txtReaderName.setText((String) tableModel.getValueAt(selectedRow, 1)); // Full name
        txtAddress.setText((String) tableModel.getValueAt(selectedRow, 2));

        // Load DateOfBirth
        String dobStr = (String) tableModel.getValueAt(selectedRow, 3);
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dobDate = sdf.parse(dobStr);
                DateOfBirthSpinner.setValue(dobDate);
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, "Failed to parse date: " + dobStr, ex);
            }
        } else {
            // Set to current date if no date is available
            DateOfBirthSpinner.setValue(new Date());
        }

        String sex = (String) tableModel.getValueAt(selectedRow, 4);
        if (sex != null) {
            txtSex.setSelectedItem(sex);
        } else {
            txtSex.setSelectedIndex(0);
        }

        txtEmail.setText((String) tableModel.getValueAt(selectedRow, 5));
        txtContact.setText((String) tableModel.getValueAt(selectedRow, 6));
        addButton.setEnabled(false);
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
            Logger.getLogger(ManageReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ManageReader().setVisible(true));
    }
}

