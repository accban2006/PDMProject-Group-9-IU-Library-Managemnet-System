package librarymanagementsystem;

import jframe.LoginPage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.sql.*;
import java.text.SimpleDateFormat;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/**
 * Main entry point for the Library Management System.
 * 
 * @author Name
 */
public class LibraryManagementSystem {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Set Nimbus look and feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LibraryManagementSystem.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Launch the application on the Event Dispatch Thread
        java.awt.EventQueue.invokeLater(() -> {
            new LoginPage().setVisible(true);
        });
               
    }
}