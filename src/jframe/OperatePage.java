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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * OperatePage for Readers to send messages and view responses
 */
public class OperatePage extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(OperatePage.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color ERROR_COLOR = new Color(255, 51, 51);

    private Connection con;

    private int readerId;
    private String uname;
    private String userrole;

    private JTextField messNoField;
    private JTextArea messInfoArea;
    private JButton sendButton;
    private JButton clearButton;
    private JTable messagesTable;
    private DefaultTableModel messagesTableModel;
    private JTable handledTable;
    private DefaultTableModel handledTableModel;
    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public OperatePage() {
        this(0, null, null);
    }

    public OperatePage(int id, String username, String urole) {
        this.readerId = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            updateUserContext();
            if (con != null) {
                loadMessages();
                loadHandledMessages();
            }
        });

        // Auto-refresh handled messages every 5 seconds
        javax.swing.Timer refreshTimer = new javax.swing.Timer(5000, e -> {
            if (con != null) {
                loadHandledMessages();
            }
        });
        refreshTimer.start();
    }

    private void initializeUi() {
        setTitle("Operate - Send Messages");
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
        backButton.addActionListener(e -> navigateTo(new HomePage(readerId, uname, userrole)));

        header.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Send Messages", SwingConstants.CENTER);
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

        content.add(buildSendMessagePanel(), BorderLayout.WEST);
        content.add(buildMessagesPanel(), BorderLayout.CENTER);

        return content;
    }

    private JPanel buildSendMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(24, 24, 24, 24)
        ));
        panel.setPreferredSize(new Dimension(400, 0));

        JLabel titleLabel = new JLabel("Send New Message");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel messNoLabel = new JLabel("Message Number:");
        messNoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        messNoLabel.setForeground(Color.WHITE);
        formPanel.add(messNoLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 2;
        messNoField = new JTextField();
        styleTextField(messNoField);
        formPanel.add(messNoField, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel messInfoLabel = new JLabel("Message Info:");
        messInfoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        messInfoLabel.setForeground(Color.WHITE);
        formPanel.add(messInfoLabel, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        messInfoArea = new JTextArea(8, 20);
        messInfoArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
        messInfoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(4, 4, 4, 4)
        ));
        messInfoArea.setBackground(PRIMARY_COLOR);
        messInfoArea.setForeground(Color.WHITE);
        messInfoArea.setCaretColor(Color.WHITE);
        messInfoArea.setLineWrap(true);
        messInfoArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messInfoArea);
        formPanel.add(scrollPane, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        sendButton = new JButton("Send Message");
        styleButton(sendButton, ERROR_COLOR);
        sendButton.addActionListener(e -> sendMessage());
        formPanel.add(sendButton, gbc);

        gbc.gridx = 1;
        clearButton = new JButton("Clear");
        styleButton(clearButton, ERROR_COLOR);
        clearButton.addActionListener(e -> clearFields());
        formPanel.add(clearButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        JPanel sentPanel = buildSentMessagesTable();
        sentPanel.setPreferredSize(new Dimension(0, 350));

        JPanel handledPanel = buildHandledMessagesTable();
        handledPanel.setPreferredSize(new Dimension(0, 350));

        panel.add(sentPanel, BorderLayout.CENTER);
        panel.add(handledPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildSentMessagesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "My Sent Messages"
        ));

        String[] columnNames = {"Message ID", "Message No", "Message Info", "Date Sent"};
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

        JScrollPane scrollPane = new JScrollPane(messagesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel buildHandledMessagesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Handled Messages (Staff Responses) - Click Refresh to Update"
        ));

        String[] columnNames = {"Message ID", "Message Info", "Staff ID", "Receive Date", "Feedback"};
        handledTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        handledTable = new JTable(handledTableModel);
        handledTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        handledTable.setRowHeight(25);
        handledTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        handledTable.setFillsViewportHeight(true);
        handledTable.setSelectionBackground(new Color(220, 220, 255));

        // Add refresh button
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton refreshButton = new JButton("Refresh Handled Messages");
        styleButton(refreshButton, ERROR_COLOR);
        refreshButton.addActionListener(e -> loadHandledMessages());
        buttonPanel.add(refreshButton, BorderLayout.EAST);
        buttonPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(handledTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        tablePanel.add(contentPanel, BorderLayout.CENTER);

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

    private void styleTextField(JTextField field) {
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

    private void sendMessage() {
        String messNoText = messNoField.getText().trim();
        String messInfo = messInfoArea.getText().trim();

        if (messNoText.isEmpty() || messInfo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        int messNo;
        try {
            messNo = Integer.parseInt(messNoText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Message Number must be numeric!");
            return;
        }

        if (con == null) {
            return;
        }

        String sql = "INSERT INTO [operate].[Message](MessNo, MessInfo, ReaderID) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, messNo);
            stmt.setString(2, messInfo);
            stmt.setInt(3, readerId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Message sent successfully!");

            clearFields();
            loadMessages();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to send message", ex);
            JOptionPane.showMessageDialog(this, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMessages() {
        if (con == null) {
            return;
        }

        messagesTableModel.setRowCount(0);

        String sql = "select MessID, MessNo, MessInfo, ReaderID from [operate].[Message] where ReaderID = ? order by MessID DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                while (rs.next()) {
                    messagesTableModel.addRow(new Object[]{
                            rs.getInt("MessID"),
                            rs.getInt("MessNo"),
                            rs.getString("MessInfo"),
                            "Sent" // We don't have a date field, so just show "Sent"
                    });
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load messages", ex);
            JOptionPane.showMessageDialog(this, "Failed to load messages.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHandledMessages() {
        if (con == null) {
            return;
        }

        handledTableModel.setRowCount(0);

        // Join [operate].[Handle] with [operate].[Message] to get handled messages for this reader
        String sql = "select h.MessID, m.MessInfo, h.StaffID, h.ReceiveDate, h.Feedback " +
                "from [operate].[Handle] h " +
                "inner join [operate].[Message] m on h.MessID = m.MessID " +
                "where m.ReaderID = ? " +
                "order by h.ReceiveDate DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                while (rs.next()) {
                    java.sql.Date receiveDate = rs.getDate("ReceiveDate");
                    handledTableModel.addRow(new Object[]{
                            rs.getInt("MessID"),
                            rs.getString("MessInfo"),
                            rs.getInt("StaffID"),
                            receiveDate != null ? dateFormat.format(receiveDate) : "-",
                            rs.getString("Feedback") != null ? rs.getString("Feedback") : "-"
                    });
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load handled messages", ex);
            JOptionPane.showMessageDialog(this, "Failed to load handled messages.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        messNoField.setText("");
        messInfoArea.setText("");
        messNoField.requestFocus();
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
            Logger.getLogger(OperatePage.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new OperatePage().setVisible(true));
    }
}

