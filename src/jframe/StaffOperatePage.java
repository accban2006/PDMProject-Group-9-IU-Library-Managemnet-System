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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * StaffOperatePage for Staff to handle messages and send reports
 */
public class StaffOperatePage extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(StaffOperatePage.class.getName());
    private static final Color PRIMARY_COLOR = new Color(51, 153, 255);
    private static final Color ERROR_COLOR = new Color(255, 51, 51);

    private Connection con;

    private int staffId;
    private String uname;
    private String userrole;

    private JTable messagesTable;
    private DefaultTableModel messagesTableModel;
    private JTextField messIdField;
    private JSpinner receiveDateSpinner;
    private JTextArea feedbackArea;
    private JButton handleButton;

    private JTextField reportNoField;
    private JTextArea issueArea;
    private JButton sendReportButton;
    private JTable reportsTable;
    private DefaultTableModel reportsTableModel;

    private JLabel roleValueLabel;
    private JLabel welcomeValueLabel;

    public StaffOperatePage() {
        this(0, null, null);
    }

    public StaffOperatePage(int id, String username, String urole) {
        this.staffId = id;
        this.uname = username;
        this.userrole = urole;
        initializeUi();
        Connect();
        SwingUtilities.invokeLater(() -> {
            updateUserContext();
            if (con != null) {
                loadMessages();
                loadReports();
            }
        });
    }

    private void initializeUi() {
        setTitle("Staff Operate - Handle Messages & Reports");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1600, 900)); // Make window wider
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
        backButton.addActionListener(e -> navigateTo(new HomePage(staffId, uname, userrole)));

        header.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Handle Messages & Send Reports", SwingConstants.CENTER);
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

        JPanel messagesPanel = buildMessagesPanel();
        messagesPanel.setPreferredSize(new Dimension(700, 0)); // Make messages panel wider

        JPanel reportsPanel = buildReportsPanel();
        reportsPanel.setPreferredSize(new Dimension(700, 0)); // Make reports panel wider

        content.add(messagesPanel, BorderLayout.WEST);
        content.add(reportsPanel, BorderLayout.EAST);

        return content;
    }

    private JPanel buildMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        panel.add(buildMessagesTable(), BorderLayout.CENTER);
        panel.add(buildHandleMessagePanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildMessagesTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.BLACK);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Pending Messages from Readers"
        ));

        String[] columnNames = {"Message ID", "Message No", "Reader ID", "Message Info"};
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
        messagesTable.setSelectionForeground(new Color(10,10,10));
        messagesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadSelectedMessageToForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(messagesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.setPreferredSize(new Dimension(0, 300));

        return tablePanel;
    }

    private JPanel buildHandleMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(16, 16, 16, 16)
        ));
        panel.setPreferredSize(new Dimension(0, 400)); // Make handle message panel taller

        JLabel titleLabel = new JLabel("Handle Message");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
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
        JLabel messIdLabel = new JLabel("Message ID:");
        messIdLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        messIdLabel.setForeground(Color.WHITE);
        formPanel.add(messIdLabel, gbc);

        gbc.gridx = 1;
        messIdField = new JTextField();
        messIdField.setEditable(false);
        styleTextField(messIdField);
        formPanel.add(messIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel dateLabel = new JLabel("Receive Date:");
        dateLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        dateLabel.setForeground(Color.WHITE);
        formPanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        receiveDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(receiveDateSpinner, "yyyy-MM-dd");
        receiveDateSpinner.setEditor(dateEditor);
        receiveDateSpinner.setFont(new Font("Tahoma", Font.PLAIN, 14));
        receiveDateSpinner.setBackground(PRIMARY_COLOR);
        ((JSpinner.DefaultEditor) receiveDateSpinner.getEditor()).getTextField().setBackground(PRIMARY_COLOR);
        ((JSpinner.DefaultEditor) receiveDateSpinner.getEditor()).getTextField().setForeground(Color.WHITE);
        formPanel.add(receiveDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel feedbackLabel = new JLabel("Feedback:");
        feedbackLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        feedbackLabel.setForeground(Color.WHITE);
        formPanel.add(feedbackLabel, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        feedbackArea = new JTextArea(6, 40); // Make feedback area wider and taller
        feedbackArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(4, 4, 4, 4)
        ));
        feedbackArea.setBackground(PRIMARY_COLOR);
        feedbackArea.setForeground(Color.WHITE);
        feedbackArea.setCaretColor(Color.WHITE);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(feedbackArea);
        formPanel.add(scrollPane, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.gridwidth = 2;
        handleButton = new JButton("Handle Message");
        styleButton(handleButton, ERROR_COLOR);
        handleButton.addActionListener(e -> handleMessage());
        formPanel.add(handleButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        panel.add(buildSendReportPanel(), BorderLayout.NORTH);
        panel.add(buildReportsTable(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSendReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ERROR_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
                new EmptyBorder(16, 16, 16, 16)
        ));
        panel.setPreferredSize(new Dimension(0, 400)); // Make send report panel taller

        JLabel titleLabel = new JLabel("Send Report");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
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
        JLabel reportNoLabel = new JLabel("Report Number:");
        reportNoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        reportNoLabel.setForeground(Color.WHITE);
        formPanel.add(reportNoLabel, gbc);

        gbc.gridy = 1;
        reportNoField = new JTextField();
        styleTextField(reportNoField, ERROR_COLOR);
        formPanel.add(reportNoField, gbc);

        gbc.gridy = 2;
        JLabel issueLabel = new JLabel("Issue:");
        issueLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        issueLabel.setForeground(Color.WHITE);
        formPanel.add(issueLabel, gbc);

        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        issueArea = new JTextArea(6, 40); // Make issue area wider and taller
        issueArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
        issueArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(4, 4, 4, 4)
        ));
        issueArea.setBackground(ERROR_COLOR);
        issueArea.setForeground(Color.WHITE);
        issueArea.setCaretColor(Color.WHITE);
        issueArea.setLineWrap(true);
        issueArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(issueArea);
        formPanel.add(scrollPane, gbc);

        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        sendReportButton = new JButton("Send Report");
        styleButton(sendReportButton, PRIMARY_COLOR);
        sendReportButton.addActionListener(e -> sendReport());
        formPanel.add(sendReportButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildReportsTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "My Reports"
        ));

        String[] columnNames = {"Report ID", "Report No", "Issue", "Date Sent"};
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

        JScrollPane scrollPane = new JScrollPane(reportsTable);
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

    private void styleTextField(JTextField field) {
        styleTextField(field, PRIMARY_COLOR);
    }

    private void styleTextField(JTextField field, Color bgColor) {
        field.setFont(new Font("Tahoma", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(4, 4, 4, 4)
        ));
        field.setBackground(bgColor);
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

    private void loadMessages() {
        if (con == null) {
            return;
        }

        messagesTableModel.setRowCount(0);

        // Get messages that haven't been handled by this staff yet
        String sql = "select m.MessID, m.MessNo, m.ReaderID, m.MessInfo " +
                "from [operate].[Message] m " +
                "where m.MessID NOT IN (select h.MessID from [operate].[Handle] h where h.StaffID = ?) " +
                "order by m.MessID DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messagesTableModel.addRow(new Object[]{
                            rs.getInt("MessID"),
                            rs.getInt("MessNo"),
                            rs.getInt("ReaderID"),
                            rs.getString("MessInfo")
                    });
                }
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

        String sql = "select ReportID, ReportNo, Issue, StaffID from [operate].[Report] where StaffID = ? order by ReportID DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportsTableModel.addRow(new Object[]{
                            rs.getInt("ReportID"),
                            rs.getInt("ReportNo"),
                            rs.getString("Issue"),
                            "Sent" // We don't have a date field, so just show "Sent"
                    });
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load reports", ex);
            JOptionPane.showMessageDialog(this, "Failed to load reports.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedMessageToForm() {
        int selectedRow = messagesTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        messIdField.setText(String.valueOf(messagesTableModel.getValueAt(selectedRow, 0)));
    }

    private void handleMessage() {
        String messIdText = messIdField.getText().trim();
        String feedback = feedbackArea.getText().trim();

        if (messIdText.isEmpty() || feedback.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a message and enter feedback!");
            return;
        }

        int messId;
        try {
            messId = Integer.parseInt(messIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Message ID!");
            return;
        }

        Date receiveDate = (Date) receiveDateSpinner.getValue();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String receiveDateStr = df.format(receiveDate);

        if (con == null) {
            return;
        }

        String sql = "INSERT INTO [operate].[Handle](StaffID, MessID, ReceiveDate, Feedback) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            stmt.setInt(2, messId);
            stmt.setString(3, receiveDateStr);
            stmt.setString(4, feedback);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Message handled successfully!");

            messIdField.setText("");
            feedbackArea.setText("");
            loadMessages();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to handle message", ex);
            JOptionPane.showMessageDialog(this, "Failed to handle message.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendReport() {
        String reportNoText = reportNoField.getText().trim();
        String issue = issueArea.getText().trim();

        if (reportNoText.isEmpty() || issue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        int reportNo;
        try {
            reportNo = Integer.parseInt(reportNoText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Report Number must be numeric!");
            return;
        }

        if (con == null) {
            return;
        }

        String sql = "INSERT INTO [operate].[Report](ReportNo, Issue, StaffID) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, reportNo);
            stmt.setString(2, issue);
            stmt.setInt(3, staffId);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Report sent successfully!");

            reportNoField.setText("");
            issueArea.setText("");
            loadReports();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to send report", ex);
            JOptionPane.showMessageDialog(this, "Failed to send report.", "Error", JOptionPane.ERROR_MESSAGE);
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
            Logger.getLogger(StaffOperatePage.class.getName()).log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new StaffOperatePage().setVisible(true));
    }
}

