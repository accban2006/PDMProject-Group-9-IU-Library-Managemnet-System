package jframe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class HomePage extends JFrame {

    private static final Color NAV_BACKGROUND = new Color(45, 45, 45);
    private static final Color NAV_HOVER = NAV_BACKGROUND.brighter();
    private static final Color NAV_ACTIVE = new Color(70, 130, 180);
    private static final Color HEADER_BACKGROUND = new Color(51, 153, 255);
    private static final Color STAT_TEXT_COLOR = new Color(50, 50, 50);
    private static final Logger LOGGER = Logger.getLogger(HomePage.class.getName());

    private Connection con;

    private int id;
    private String displayName;
    private String userrole;

    private JTable jTable1;
    private JTable jTable2;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel bookTableModel;

    private JLabel lbl_noOfBooks;
    private JLabel lbl_noOfStudents;
    private JLabel lbl_noOfIssuedBooks;
    private JLabel lbl_noOfDefaulters;
    private JPanel panelPieChart;
    private JLabel welcomeValueLabel;
    private JLabel userTypeValueLabel;
    private JPanel navigationPanel;


    public HomePage() {
        initialize();
        Connect();
        if (con != null) {
            refreshData();
        }
//        updateUserContext();
    }

    public HomePage(int id, String displayName, String urole) {
        this();
        this.id = id;
        this.displayName = displayName;
        this.userrole = urole.trim();
        updateUserContext();
    }

    private void initialize() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1200, 780));

        add(buildHeader(), BorderLayout.NORTH);

        navigationPanel = new JPanel(new BorderLayout());
        add(navigationPanel, BorderLayout.WEST);

        add(buildContent(), BorderLayout.CENTER);

        setIconImageSafe();
        pack();
        setLocationRelativeTo(null);

        System.out.println("DEBUG userrole = [" + userrole + "]");

    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome, ");
        welcomeLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.WHITE);

        welcomeValueLabel = new JLabel("-");
        welcomeValueLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        welcomeValueLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("   Role: ");
        roleLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
        roleLabel.setForeground(Color.WHITE);

        userTypeValueLabel = new JLabel("Guest");
        userTypeValueLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        userTypeValueLabel.setForeground(Color.WHITE);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> System.exit(0));

        right.add(welcomeLabel);
        right.add(welcomeValueLabel);
        right.add(Box.createHorizontalStrut(16));
        right.add(roleLabel);
        right.add(userTypeValueLabel);
        right.add(Box.createHorizontalStrut(16));
        right.add(exitButton);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildNavigation() {
        JPanel nav = new JPanel();
        nav.setBackground(NAV_BACKGROUND);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(230, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        navTitle.setForeground(Color.WHITE);
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        nav.add(navTitle);
        nav.add(Box.createVerticalStrut(24));

        // ========== Buttons Visible for BOTH ==========
        nav.add(createNavButton("Dashboard", null));
        nav.add(Box.createVerticalStrut(12));

        // ========== Buttons Visible ONLY for Staff ==========
        if ("Staff".equalsIgnoreCase(userrole)) {
            nav.add(createNavButton("Manage Books", this::openManageBooks));
            nav.add(Box.createVerticalStrut(12));

            nav.add(createNavButton("Manage Reader", this::openManageStudents));
            nav.add(Box.createVerticalStrut(12));

            nav.add(createNavButton("Issue Book", this::openIssueBook));
            nav.add(Box.createVerticalStrut(12));

            nav.add(createNavButton("Return Book", this::openReturnBook));
            nav.add(Box.createVerticalStrut(12));

            nav.add(createNavButton("Handle Messages & Reports", this::openStaffOperate));
            nav.add(Box.createVerticalStrut(12));

            nav.add(createNavButton("View Records", this::openViewRecords));
            nav.add(Box.createVerticalStrut(12));
        }

        // ========== Buttons Visible ONLY for Reader ==========
        if ("Reader".equalsIgnoreCase(userrole)) {
            nav.add(createNavButton("Send Messages", this::openOperate));
            nav.add(Box.createVerticalStrut(12));
        }

        nav.add(Box.createVerticalGlue());
        nav.add(createNavButton("Logout", this::logout));

        return nav;
    }


    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(245, 245, 245));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);
        GridBagConstraints statsConstraints = new GridBagConstraints();
        statsConstraints.insets = new Insets(0, 0, 0, 16);
        statsConstraints.gridy = 0;
        statsConstraints.fill = GridBagConstraints.BOTH;
        statsConstraints.weighty = 1.0;

        lbl_noOfBooks = createStatLabel();
        lbl_noOfStudents = createStatLabel();
        lbl_noOfIssuedBooks = createStatLabel();
        lbl_noOfDefaulters = createStatLabel();

//        statsConstraints.gridx = 0;
//        statsConstraints.weightx = 0.25;
//        statsPanel.add(createStatCard("No. Of Books", lbl_noOfBooks, new Color(0, 123, 255)), statsConstraints);
//        statsConstraints.gridx = 1;
//        statsPanel.add(createStatCard("No. Of Students", lbl_noOfStudents, new Color(40, 167, 69)), statsConstraints);
//        statsConstraints.gridx = 2;
//        statsPanel.add(createStatCard("Issued Books", lbl_noOfIssuedBooks, new Color(255, 193, 7)), statsConstraints);
//        statsConstraints.gridx = 3;
//        statsConstraints.insets = new Insets(0, 0, 0, 0);
//        statsPanel.add(createStatCard("Defaulters", lbl_noOfDefaulters, new Color(220, 53, 69)), statsConstraints);
//
//        content.add(statsPanel, BorderLayout.NORTH);

//        studentTableModel = createStudentTableModel();
        bookTableModel = createBookTableModel();

//        jTable1 = new JTable(studentTableModel);
//        configureTable(jTable1);

        jTable2 = new JTable(bookTableModel);
        configureTable(jTable2);

        JPanel tablesPanel = new JPanel();
        tablesPanel.setOpaque(false);
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
//        tablesPanel.add(createTableSection("Student Details", jTable1));
        tablesPanel.add(Box.createVerticalStrut(16));
        tablesPanel.add(createTableSection("Book Details", jTable2));

//        panelPieChart = new JPanel(new BorderLayout());
//        panelPieChart.setPreferredSize(new Dimension(420, 0));
//        panelPieChart.setBackground(Color.WHITE);
//        panelPieChart.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), "Issued Book details"));
//        panelPieChart.add(new JLabel("No data available", SwingConstants.CENTER), BorderLayout.CENTER);
//
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints centerConstraints = new GridBagConstraints();
        centerConstraints.gridx = 0;
        centerConstraints.gridy = 0;
        centerConstraints.weightx = 0.6;
        centerConstraints.weighty = 1.0;
        centerConstraints.fill = GridBagConstraints.BOTH;
        centerConstraints.insets = new Insets(0, 0, 0, 16);
        centerPanel.add(tablesPanel, centerConstraints);

        centerConstraints.gridx = 1;
        centerConstraints.weightx = 0.4;
        centerConstraints.insets = new Insets(0, 0, 0, 0);
//        centerPanel.add(panelPieChart, centerConstraints);

        content.add(centerPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        JLabel developedBy = new JLabel("Developed by: ");
        developedBy.setFont(new Font("Tahoma", Font.PLAIN, 12));
        footer.add(developedBy);
        footer.add(createLinkLabel("Naveenkumar J", "https://github.com/naveenkumar-j"));
        content.add(footer, BorderLayout.SOUTH);

        return content;
    }

    private JButton createNavButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(action == null ? NAV_ACTIVE : NAV_BACKGROUND);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        if (action != null) {
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> action.run());
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(NAV_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(NAV_BACKGROUND);
                }
            });
        } else {
            button.setEnabled(false);
        }
        return button;
    }

    private JLabel createStatLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Calibri", Font.BOLD, 40));
        label.setForeground(STAT_TEXT_COLOR);
        return label;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 70, 70));

        valueLabel.setBackground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTableSection(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Tahoma", Font.BOLD, 16));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

//    private DefaultTableModel createStudentTableModel() {
//        return new DefaultTableModel(new Object[]{"Student ID", "Student Name", "Course", "Branch"}, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            }
//        };
//    }

    private DefaultTableModel createBookTableModel() {
        return new DefaultTableModel(new Object[]{"ISBN", "Title", "Genre", "Edition", "Plot", "Rating", "PublisherName", "YearOfPublication"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setAutoCreateRowSorter(true);
    }

    private JLabel createLinkLabel(String text, String url) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(new Color(0, 102, 204));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
            LOGGER.log(Level.SEVERE, "Failed to open link: " + url, ex);
            JOptionPane.showMessageDialog(this, "Failed to open link.");
        }
    }

    private void updateUserContext() {

        // Update header labels
        welcomeValueLabel.setText(displayName != null ? displayName : "Guest");
        userTypeValueLabel.setText(userrole != null ? userrole : "Guest");

        // REBUILD NAVIGATION
        navigationPanel.removeAll();
        navigationPanel.add(buildNavigation());
        navigationPanel.revalidate();
        navigationPanel.repaint();

        System.out.println("NAV rebuilt with role = " + userrole);
    }

    private void navigateTo(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setVisible(true);
        dispose();
    }

    private void openManageBooks() {
        navigateTo(new ManageBooks(id, displayName, userrole));
    }

    private void openManageStudents() {
        navigateTo(new ManageReader(id, displayName, userrole));
    }

    private void openIssueBook() {
        navigateTo(new IssueBook(id, displayName, userrole));
    }

    private void openReturnBook() {
        navigateTo(new ReturnBook(id, displayName, userrole));
    }

    private void openViewRecords() {
        navigateTo(new ViewAllRecord(id, displayName, userrole));
    }

    private void openOperate() {
        navigateTo(new OperatePage(id, displayName, userrole));
    }

    private void openStaffOperate() {
        navigateTo(new StaffOperatePage(id, displayName, userrole));
    }

    private void logout() {
        navigateTo(new LoginPage());
    }

    public final void Connect() {
        try {
            if (con != null && !con.isClosed()) {
                return;
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=LibraryManagementSystemGroup9;user=itcsiu24070;password=;trustServerCertificate=true");
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database", ex);
            JOptionPane.showMessageDialog(this, "Unable to connect to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        Book_Load();
//        Student_Load();
//        countStudentDetails();
//        countBookDetails();
//        countIssueBookDetails();
//        countDefaultersDetails();
//        showPieChart();
    }

    public void Book_Load() {
        if (con == null) {
            return;
        }
        bookTableModel.setRowCount(0);
        String sql = "select ISBN, title, genre, edition, plot, ratings, PublisherName, YearOfPublication from [lib].[book] b INNER JOIN [lib].[Publisher] p ON b.PublisherID = p.PublisherID;";
        try (PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                bookTableModel.addRow(new Object[]{
                        resultSet.getString("ISBN"),
                        resultSet.getString("title"),
                        resultSet.getString("genre"),
                        resultSet.getString("edition"),
                        resultSet.getString("plot"),
                        resultSet.getString("ratings"),
                        resultSet.getString("PublisherName"),
                        resultSet.getString("YearOfPublication")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load books", ex);
        }
    }

//    public void Student_Load() {
//        if (con == null) {
//            return;
//        }
//        studentTableModel.setRowCount(0);
//        String sql = "select student_id, student_name, course, branch from student_details";
//        try (PreparedStatement statement = con.prepareStatement(sql);
//             ResultSet resultSet = statement.executeQuery()) {
//            while (resultSet.next()) {
//                studentTableModel.addRow(new Object[]{
//                        resultSet.getString("student_id"),
//                        resultSet.getString("student_name"),
//                        resultSet.getString("course"),
//                        resultSet.getString("branch")
//                });
//            }
//        } catch (SQLException ex) {
//            LOGGER.log(Level.SEVERE, "Failed to load students", ex);
//        }
//    }

    public void countBookDetails() {
        if (con == null) {
            return;
        }
        String sql = "select count(*) from [lib].[Book]";
        try (PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                lbl_noOfBooks.setText(String.valueOf(resultSet.getInt(1)));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to count books", ex);
        }
    }

    public void countStudentDetails() {
        if (con == null) {
            return;
        }
        String sql = "select count(*) from student_details";
        try (PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                lbl_noOfStudents.setText(String.valueOf(resultSet.getInt(1)));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to count students", ex);
        }
    }

    public void countIssueBookDetails() {
        if (con == null) {
            return;
        }
        String sql = "select count(*) from issue_book_details where status = ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, "pending");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lbl_noOfIssuedBooks.setText(String.valueOf(resultSet.getInt(1)));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to count issued books", ex);
        }
    }

    public void countDefaultersDetails() {
        if (con == null) {
            return;
        }
        long now = System.currentTimeMillis();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String todayDate = df.format(now);
        String sql = "select count(*) from issue_book_details where due_date < ?";
        try (PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, todayDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    lbl_noOfDefaulters.setText(String.valueOf(resultSet.getInt(1)));
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to count defaulters", ex);
        }
    }

    public void showPieChart() {
        if (con == null) {
            return;
        }

        DefaultTableModel chartModel = new DefaultTableModel(new Object[]{"Book Name", "Issue Count"}, 0);
        String sql = "select book_name, count(*) as issue_count from issue_book_details group by book_name";
        try (PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                chartModel.addRow(new Object[]{
                    resultSet.getString("book_name"),
                    resultSet.getInt("issue_count")
                });
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to build chart data", ex);
        }

        JTable chartTable = new JTable(chartModel);
        chartTable.setFont(new Font("Tahoma", Font.PLAIN, 12));
        chartTable.setRowHeight(25);
        chartTable.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        chartTable.setFillsViewportHeight(true);
        chartTable.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(chartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panelPieChart.removeAll();
        panelPieChart.add(scrollPane, BorderLayout.CENTER);
        panelPieChart.revalidate();
        panelPieChart.repaint();
    }

    private void setIconImageSafe() {
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo.png")));
        } catch (Exception ignored) {
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
            LOGGER.log(Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new HomePage().setVisible(true));
    }
}




