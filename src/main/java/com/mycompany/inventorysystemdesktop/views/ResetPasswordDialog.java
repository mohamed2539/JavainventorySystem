package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ResetPasswordDialog extends JDialog {
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private final int userId;
    private final String username;
    private final UserDAO userDAO;
    
    public ResetPasswordDialog(Window owner, int userId, String username) {
        super(owner, "إعادة تعيين كلمة المرور", ModalityType.APPLICATION_MODAL);
        this.userId = userId;
        this.username = username;
        this.userDAO = new UserDAO();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // عنوان
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("إعادة تعيين كلمة المرور للمستخدم: " + username), gbc);
        
        // كلمة المرور الجديدة
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("كلمة المرور الجديدة:"), gbc);
        
        newPasswordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(newPasswordField, gbc);
        
        // تأكيد كلمة المرور
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("تأكيد كلمة المرور:"), gbc);
        
        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(confirmPasswordField, gbc);
        
        // أزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("حفظ");
        JButton cancelButton = new JButton("إلغاء");
        
        saveButton.addActionListener(this::resetPassword);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gbc);
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void resetPassword(ActionEvent e) {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("جميع الحقول مطلوبة");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("كلمات المرور غير متطابقة");
            return;
        }
        
        try {
            userDAO.resetPassword(userId, newPassword);
            JOptionPane.showMessageDialog(this,
                "تم إعادة تعيين كلمة المرور بنجاح",
                "نجاح",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            showError("خطأ في إعادة تعيين كلمة المرور: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
}