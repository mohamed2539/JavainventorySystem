package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.UserDAO;
import com.mycompany.inventorysystemdesktop.models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private final UserDAO userDAO;
    
    public LoginDialog(Frame parent) {
        super(parent, "تسجيل الدخول", true);
        this.userDAO = new UserDAO();
        initComponents();
        setupDialog();
    }
    
    private void initComponents() {
        // إعداد التخطيط
        setLayout(new BorderLayout(10, 10));
        
        // لوحة الحقول
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // إضافة حقل اسم المستخدم
        gbc.gridx = 1;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("اسم المستخدم:"), gbc);
        
        gbc.gridx = 0;
        usernameField = new JTextField(20);
        fieldsPanel.add(usernameField, gbc);
        
        // إضافة حقل كلمة المرور
        gbc.gridx = 1;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("كلمة المرور:"), gbc);
        
        gbc.gridx = 0;
        passwordField = new JPasswordField(20);
        fieldsPanel.add(passwordField, gbc);
        
        // لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("دخول");
        cancelButton = new JButton("إلغاء");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        // إضافة اللوحات إلى النافذة
        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // إضافة المستمعين
        loginButton.addActionListener(e -> login());
        cancelButton.addActionListener(e -> dispose());
        
        // إضافة مستمع للضغط على Enter
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void setupDialog() {
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("الرجاء إدخال اسم المستخدم وكلمة المرور");
            return;
        }
        
        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                if (!user.isActive()) {
                    showError("هذا الحساب غير نشط");
                    return;
                }
                
                // إغلاق نافذة تسجيل الدخول
                dispose();
                
                // فتح النافذة الرئيسية
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(user);
                    mainFrame.setVisible(true);
                });
            } else {
                showError("اسم المستخدم أو كلمة المرور غير صحيحة");
            }
        } catch (SQLException e) {
            showError("خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE
        );
    }
}