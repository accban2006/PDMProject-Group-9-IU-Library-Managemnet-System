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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author Name
 */
public class LoginPage extends javax.swing.JFrame {

    /**
     * Creates new form SignUpPage
     */
    // Default constructor
    public LoginPage() {
        initComponents();
        Connect();

        setIconImage();
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private int id;
    private String uname;
    private String role;

    public LoginPage(int id, String username, String userrole) {
        this();
        this.id = id;
        this.uname = username;
        this.role = userrole;
    }

    // Set Icon method
    private void setIconImage() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/logo.png")));

    }

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    // Database connectivity
    public void Connect() {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://YOUR_SERVER_NAME:1433;databaseName=YOUR_DB_NAME;user=YOUR_USERNAME;password=YOUR_PASSWORD;trustServerCertificate=true");
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Validate Login credentials
    public boolean validateLogin() {
        String name = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword()).trim();
        String userType = userTypeCombo.getSelectedItem() != null
                ? userTypeCombo.getSelectedItem().toString()
                : "";

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "please enter your username!");
            return false;
        }

        if (pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "please enter your password!");
            return false;
        }

        if (userTypeCombo.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this, "please select a role!");
            return false;
        }

        return true;
    }

    // Login method
    public void login() {
        String name = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword()).trim();
        String utype = userTypeCombo.getSelectedItem().toString();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection("jdbc:sqlserver://LAPTOP-7M0AV8S0:1433;databaseName=LibraryManagementSystemGroup9;user=sa;password=T!24065n3A44;trustServerCertificate=true");
            pst = con.prepareStatement("select * from [auth].[Authentication] " +
                    "where Username=? and Password=? and Role=?");
            pst.setString(1, name);
            pst.setString(2, pwd);
            pst.setString(3, utype);
            rs = pst.executeQuery();

            if (rs.next()) {
                int id = -1; //initialize id to a safe default value
                String fetchedUtype = rs.getString("Displayname"); //assuming 'Displayname'
                //holds the role/type info
                //check if ReaderID is present (i.e., StaffID is NULL)
                if (rs.getObject("ReaderID") != null) {
                    id = rs.getInt("ReaderID");
                }
                // 2. Check if StaffID is present (i.e., ReaderID is NULL)
                else if (rs.getObject("StaffID") != null) {
                    id = rs.getInt("StaffID");
                }
                // Check if a valid ID was found
                if (id != -1) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    // Pass the generic ID (which holds either ReaderID or StaffID), username,
                    // and user type/role
                    new HomePage(id, name, utype).setVisible(true);
                    this.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect username or password");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Library Management System - Login");
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(1100, 700));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(520, 0));
        leftPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel welcomeHeader = new JLabel("WELCOME TO");
        welcomeHeader.setFont(new Font("Calibri", Font.BOLD, 36));
        welcomeHeader.setForeground(new Color(255, 51, 51));
        welcomeHeader.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(welcomeHeader, BorderLayout.NORTH);

        JLabel heroImage = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/icons/library-3.png.png")));
        heroImage.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(heroImage, BorderLayout.CENTER);

        JPanel creditsPanel = new JPanel(new GridBagLayout());
        creditsPanel.setBackground(Color.WHITE);
        creditsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 0, 10));
        GridBagConstraints creditsGbc = new GridBagConstraints();
        creditsGbc.gridx = 0;
        creditsGbc.gridy = 0;
        creditsGbc.anchor = GridBagConstraints.CENTER;
        creditsGbc.insets = new Insets(2, 5, 2, 5);

        JLabel developedByLabel = new JLabel("Developed by: ");
        developedByLabel.setFont(new Font("Calibri", Font.PLAIN, 14));
        developedByLabel.setForeground(new Color(51, 51, 51));
        creditsPanel.add(developedByLabel, creditsGbc);

        creditsGbc.gridx = 1;
        JLabel developerLabel = createLinkLabel("Naveenkumar J", "https://github.com/naveenkumar-j", new Color(0, 102, 204));
        developerLabel.setFont(new Font("Calibri", Font.BOLD, 14));
        creditsPanel.add(developerLabel, creditsGbc);

        creditsGbc.gridx = 0;
        creditsGbc.gridy = 1;
        creditsGbc.gridwidth = 2;
        JLabel brandLabel = createLinkLabel("EAGLE PROGRAMMER", "https://www.instagram.com/eagle_programming/?hl=en", new Color(255, 51, 51));
        brandLabel.setFont(new Font("Calibri", Font.BOLD, 14));
        creditsPanel.add(brandLabel, creditsGbc);

        leftPanel.add(creditsPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(51, 153, 255));
        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(60, 80, 60, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel formTitle = new JLabel("Login Page", SwingConstants.CENTER);
        formTitle.setFont(new Font("Tahoma", Font.BOLD, 32));
        formTitle.setForeground(Color.WHITE);
        formPanel.add(formTitle, gbc);

        gbc.gridy++;
        JLabel subTitle = new JLabel("Welcome to your account!", SwingConstants.CENTER);
        subTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
        subTitle.setForeground(new Color(255, 255, 255));
        formPanel.add(subTitle, gbc);

        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        usernameLabel.setForeground(Color.WHITE);
        formPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        usernameField = new JTextField(25);
        usernameField.setFont(new Font("Tahoma", Font.PLAIN, 16));
        usernameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        formPanel.add(usernameField, gbc);

        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        passwordLabel.setForeground(Color.WHITE);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        passwordField = new JPasswordField(25);
        passwordField.setFont(new Font("Tahoma", Font.PLAIN, 16));
        passwordField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        passwordField.addActionListener(e -> attemptLogin());
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        JLabel userTypeLabel = new JLabel("User Type");
        userTypeLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        userTypeLabel.setForeground(Color.WHITE);
        formPanel.add(userTypeLabel, gbc);

        gbc.gridy++;
        userTypeCombo = new JComboBox<>(new String[]{"Select", "Reader", "Staff"});
        userTypeCombo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        userTypeCombo.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        userTypeCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(userTypeCombo, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Tahoma", Font.BOLD, 18));
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());
        formPanel.add(loginButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 10, 0);
        JButton signUpButton = new JButton("Create an Account");
        signUpButton.setFont(new Font("Tahoma", Font.BOLD, 16));
        signUpButton.setBackground(new Color(255, 153, 51)); // Orange button
        signUpButton.setForeground(Color.WHITE);
        signUpButton.setFocusPainted(false);
        signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpButton.addActionListener(e -> openSignUpPage());
        formPanel.add(signUpButton, gbc);

        gbc.gridy++;
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
        exitButton.setBackground(new Color(255, 255, 255));
        exitButton.setForeground(new Color(51, 51, 51));
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> dispose());
        formPanel.add(exitButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(loginButton);
    }

    private void attemptLogin() {
        if (validateLogin()) {
            login();
        }
    }

    private JLabel createLinkLabel(String text, String url, Color color) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(color);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLink(url);
            }
        });
        return label;
    }

    private void navigateTo(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.setVisible(true);
        dispose();
    }

    private void openSignUpPage() {
        navigateTo(new SignUpPage(id, uname, role));
    }

    private void openLink(String url) {
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Unable to open links on this system.");
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(LoginPage.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Failed to open link: " + url);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton loginButton;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
