package com.mycompany.inventorysystemdesktop.utils;

import javax.swing.JOptionPane;
import java.awt.Component;

public class MessageBox {
    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showWarning(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    public static boolean showConfirm(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, 
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
} 