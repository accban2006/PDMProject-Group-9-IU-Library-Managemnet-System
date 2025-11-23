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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Pure Swing implementation of the Manage Books screen.
 */
public class ManageBooks extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(ManageBooks.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color ERROR_COLOR = new Color(255, 51, 51);

    private Connection con;

    private int id;
    private String uname;
    private String userrole;

    private JTextField txtBookId;
    private JTextField txtBookName;
    private JTextField txtGerne;
    private JTextField txtEdition;
    private JTextField txtPlot;
    private JTextField txtRatings;
    private JTextField txtPublisher;
    private JTextField txtStaffID;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public ManageBooks() {
        this(0, null, null);
    }

    public ManageBooks(int id, String username, String urole) {
        this.id = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            updateUserContext();
            applyRolePermissions();
            if (con != null) {
                loadBooks();
            }
        });
    }

    private void initializeUi() {
        setTitle("Manage Books");
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
        JLabel titleLabel = new JLabel("Add/Edit Book");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        fieldsPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel bookIdLabel = new JLabel("ISBN:");
        bookIdLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        bookIdLabel.setForeground(Color.WHITE);
        fieldsPanel.add(bookIdLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtBookId = new JTextField();
        styleTextField(txtBookId, "Enter ISBN");
        txtBookId.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkDuplicateBookId();
            }
        });
        fieldsPanel.add(txtBookId, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel bookNameLabel = new JLabel("Book Name:");
        bookNameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        bookNameLabel.setForeground(Color.WHITE);
        fieldsPanel.add(bookNameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtBookName = new JTextField();
        styleTextField(txtBookName, "Enter Book Name");
        txtBookName.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkDuplicateBookName();
            }
        });
        fieldsPanel.add(txtBookName, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel gerneLabel = new JLabel("Genre:");
        gerneLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        gerneLabel.setForeground(Color.WHITE);
        fieldsPanel.add(gerneLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtGerne = new JTextField();
        styleTextField(txtGerne, "Enter Genre");
        fieldsPanel.add(txtGerne, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel editionLabel = new JLabel("Edition:");
        editionLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        editionLabel.setForeground(Color.WHITE);
        fieldsPanel.add(editionLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtEdition = new JTextField();
        styleTextField(txtEdition, "Enter Edition");
        fieldsPanel.add(txtEdition, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel plotLabel = new JLabel("Plot:");
        plotLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        plotLabel.setForeground(Color.WHITE);
        fieldsPanel.add(plotLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPlot = new JTextField();
        styleTextField(txtPlot, "Enter Plot");
        fieldsPanel.add(txtPlot, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel ratingsLabel = new JLabel("Ratings:");
        ratingsLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        ratingsLabel.setForeground(Color.WHITE);
        fieldsPanel.add(ratingsLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtRatings = new JTextField();
        styleTextField(txtRatings, "Enter Ratings");
        fieldsPanel.add(txtRatings, gbc);

        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel stfIDLabel = new JLabel("StaffID:");
        stfIDLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        stfIDLabel.setForeground(Color.WHITE);
        fieldsPanel.add(stfIDLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtStaffID = new JTextField();
        styleTextField(txtStaffID, "Enter StaffID");
        fieldsPanel.add(txtStaffID, gbc);

        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        publisherLabel.setForeground(Color.WHITE);
        fieldsPanel.add(publisherLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPublisher = new JTextField();
        styleTextField(txtPublisher, "Enter Publisher");
        fieldsPanel.add(txtPublisher, gbc);


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
        addButton.addActionListener(e -> addBook());
        buttonsPanel.add(addButton, btnGbc);

        btnGbc.gridx = 1;
        editButton = new JButton("Edit");
        styleButton(editButton, ERROR_COLOR);
        editButton.addActionListener(e -> editBook());
        buttonsPanel.add(editButton, btnGbc);

        btnGbc.gridx = 0;
        btnGbc.gridy = 1;
        deleteButton = new JButton("Delete");
        styleButton(deleteButton, ERROR_COLOR);
        deleteButton.addActionListener(e -> deleteBook());
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
                "All Books"
        ));

        String[] columnNames = {"ISBN", "Title", "Genre", "Edition", "Plot", "Rating", "StaffID", "PublisherID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        booksTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        booksTable.setRowHeight(25);
        booksTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        booksTable.setFillsViewportHeight(true);
        booksTable.setSelectionBackground(new Color(220, 220, 255));
        booksTable.setSelectionForeground(Color.BLACK);
        booksTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadSelectedBookToForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(booksTable);
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

    public void loadBooks() {
        if (con == null) {
            return;
        }

        tableModel.setRowCount(0);

        String sql = "select ISBN, title, genre, edition, plot, ratings, StaffID, PublisherID from [lib].[Book]";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("ISBN"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("edition"),
                        rs.getString("plot"),
                        rs.getString("ratings"),
                        rs.getString("StaffID"),
                        rs.getString("PublisherID")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load books", ex);
            JOptionPane.showMessageDialog(this, "Failed to load books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkDuplicateBookId() {
        String bookId = txtBookId.getText().trim();
        if (bookId.isEmpty() || con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("select * from [lib].[Book] where ISBN = ?")) {
            stmt.setString(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Book ID already exists!");
                    txtBookId.setText("");
                    txtBookId.requestFocus();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to check duplicate book ID", ex);
        }
    }

    private void checkDuplicateBookName() {
        String bookName = txtBookName.getText().trim();
        if (bookName.isEmpty() || con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("select * from [lib].[Book] where Title = ?")) {
            stmt.setString(1, bookName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Book name already exists!");
                    txtBookName.setText("");
                    txtBookName.requestFocus();
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to check duplicate book name", ex);
        }
    }

    private void addBook() {
        String ISBN = txtBookId.getText().trim();
        String Title = txtBookName.getText().trim();
        String Gerne = txtGerne.getText().trim();
        String Edition = txtEdition.getText().trim();
        String Plot = txtPlot.getText().trim();
        String Ratings = txtRatings.getText().trim();
        String StaffID = txtStaffID.getText().trim();
        String Publisher = txtPublisher.getText().trim();


        if (ISBN.isEmpty() || Title.isEmpty() || Gerne.isEmpty() || Edition.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if (con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("insert into [lib].[Book](ISBN, title, genre, edition, plot, ratings, StaffID, PublisherID) values(?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, ISBN);
            stmt.setString(2, Title);
            stmt.setString(3, Gerne);
            stmt.setString(4, Edition);
            stmt.setString(5, Plot);
            stmt.setString(6, Ratings);
            stmt.setString(7, StaffID);
            stmt.setString(8, Publisher);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearFields();
            loadBooks();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to add book", ex);
            JOptionPane.showMessageDialog(this, "Failed to add book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table!");
            return;
        }

        String ISBN = txtBookId.getText().trim();
        String Title = txtBookName.getText().trim();
        String Genre = txtGerne.getText().trim();
        String Edition = txtEdition.getText().trim();
        String Plot = txtPlot.getText().trim();
        String Ratings = txtRatings.getText().trim();
        String StaffID = txtStaffID.getText().trim();
        String Publisher = txtPublisher.getText().trim();

        if (ISBN.isEmpty() || Title.isEmpty() || Genre.isEmpty() || Edition.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        if (con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("update [lib].[Book] set Title = ?, genre = ?, Edition = ?, Plot = ?, Ratings = ?, StaffID = ?, PublisherID = ? where ISBN = ?")) {
            stmt.setString(1, ISBN);
            stmt.setString(2, Title);
            stmt.setString(3, Genre);
            stmt.setString(4, Edition);
            stmt.setString(5, Plot);
            stmt.setString(6, Ratings);
            stmt.setString(7, StaffID);
            stmt.setString(8, Publisher);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book updated successfully!");
            clearFields();
            loadBooks();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to update book", ex);
            JOptionPane.showMessageDialog(this, "Failed to update book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String ISBN = txtBookId.getText().trim();

        if (con == null) {
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement("delete from [lib].[Book] where ISBN = ?")) {
            stmt.setString(1, ISBN);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book deleted successfully!");
            clearFields();
            loadBooks();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to delete book", ex);
            JOptionPane.showMessageDialog(this, "Failed to delete book.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtBookId.setText("");
        txtBookName.setText("");
        txtGerne.setText("");
        txtEdition.setText("");
        txtBookId.requestFocus();
        booksTable.clearSelection();
        addButton.setEnabled(true);
    }

    private void loadSelectedBookToForm() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        txtBookId.setText((String) tableModel.getValueAt(selectedRow, 0));
        txtBookName.setText((String) tableModel.getValueAt(selectedRow, 1));
        txtGerne.setText((String) tableModel.getValueAt(selectedRow, 2));
        txtEdition.setText((String) tableModel.getValueAt(selectedRow, 3));
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
            Logger.getLogger(ManageBooks.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ManageBooks().setVisible(true));
    }
}

