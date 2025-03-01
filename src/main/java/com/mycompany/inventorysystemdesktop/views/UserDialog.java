package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.User;
import com.mycompany.inventorysystemdesktop.dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UserDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JComboBox<String> roleCombo;
    private JCheckBox activeCheck;
    private User user;
    private boolean userSaved = false;
    private UserDAO userDAO;
    
    public UserDialog(Window owner, User user) {
        super(owner, user == null ? "إضافة مستخدم جديد" : "تعديل مستخدم", ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.userDAO = new UserDAO();
        initComponents();
        loadUserData();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // اسم المستخدم
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("اسم المستخدم:"), gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(usernameField, gbc);
        
        // كلمة المرور
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("كلمة المرور:"), gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(passwordField, gbc);
        
        // الاسم الكامل
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("الاسم الكامل:"), gbc);
        
        fullNameField = new JTextField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(fullNameField, gbc);
        
        // الصلاحية
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("الصلاحية:"), gbc);
        
        roleCombo = new JComboBox<>(new String[]{"ADMIN", "USER"});
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(roleCombo, gbc);
        
        // نشط
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        activeCheck = new JCheckBox("نشط");
        activeCheck.setSelected(true);
        add(activeCheck, gbc);
        
        // أزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("حفظ");
        JButton cancelButton = new JButton("إلغاء");
        
        saveButton.addActionListener(this::saveUser);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gbc);
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void loadUserData() {
        if (user != null) {
            usernameField.setText(user.getUsername());
            usernameField.setEnabled(false); // لا يمكن تغيير اسم المستخدم
            passwordField.setEnabled(false); // تغيير كلمة المرور يتم من خلال زر منفصل
            fullNameField.setText(user.getFullName());
            roleCombo.setSelectedItem(user.getRole());
            activeCheck.setSelected(user.isActive());
        }
    }
    
    private void saveUser(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        boolean active = activeCheck.isSelected();
        
        // التحقق من صحة البيانات
        if (username.isEmpty() || fullName.isEmpty()) {
            showError("جميع الحقول مطلوبة");
            return;
        }
        
        if (user == null && password.isEmpty()) {
            showError("كلمة المرور مطلوبة");
            return;
        }
        
        try {
            if (user == null) {
                // إضافة مستخدم جديد
                user = new User();
                user.setUsername(username);
                user.setPassword(password); // يجب تشفير كلمة المرور قبل الحفظ
                user.setFullName(fullName);
                user.setRole(role);
                user.setActive(active);
                userDAO.create(user);
            } else {
                // تحديث مستخدم موجود
                user.setFullName(fullName);
                user.setRole(role);
                user.setActive(active);
                userDAO.update(user);
            }
            
            userSaved = true;
            dispose();
            
        } catch (Exception ex) {
            showError("خطأ في حفظ المستخدم: " + ex.getMessage());
        }
    }
    
    public boolean isUserSaved() {
        return userSaved;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
}