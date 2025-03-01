package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.User;
import com.mycompany.inventorysystemdesktop.dao.UserDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class UsersPanel extends JPanel {
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private UserDAO userDAO;
    private User currentUser;
    
    public UsersPanel(User currentUser) {
        this.currentUser = currentUser;
        this.userDAO = new UserDAO();
        initComponents();
        loadUsers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // إنشاء نموذج الجدول
        String[] columns = {"الرقم", "اسم المستخدم", "الاسم الكامل", "الصلاحية", "نشط"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // إنشاء الجدول
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        
        // إنشاء لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("إضافة مستخدم");
        JButton editButton = new JButton("تعديل");
        JButton deleteButton = new JButton("حذف");
        JButton resetPasswordButton = new JButton("إعادة تعيين كلمة المرور");
        
        // إضافة المستمعين للأزرار
        addButton.addActionListener(this::addUser);
        editButton.addActionListener(this::editUser);
        deleteButton.addActionListener(this::deleteUser);
        resetPasswordButton.addActionListener(this::resetPassword);
        
        // إضافة الأزرار إلى اللوحة
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(resetPasswordButton);
        
        // إضافة المكونات إلى اللوحة الرئيسية
        add(createSearchPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("بحث");
        
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText();
            // تنفيذ البحث
            try {
                List<User> users = userDAO.search(searchTerm);
                updateTable(users);
            } catch (Exception ex) {
                showError("خطأ في البحث: " + ex.getMessage());
            }
        });
        
        searchPanel.add(searchButton);
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("بحث:"));
        
        return searchPanel;
    }
    
    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            updateTable(users);
        } catch (Exception e) {
            showError("خطأ في تحميل المستخدمين: " + e.getMessage());
        }
    }
    
    private void updateTable(List<User> users) {
        tableModel.setRowCount(0);
        for (User user : users) {
            Object[] row = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive() ? "نعم" : "لا"
            };
            tableModel.addRow(row);
        }
    }
    
    private void addUser(ActionEvent e) {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isUserSaved()) {
            loadUsers();
        }
    }
    
    private void editUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار مستخدم للتعديل");
            return;
        }
        
        try {
            int userId = (int) usersTable.getValueAt(selectedRow, 0);
            User user = userDAO.findById(userId);
            UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), user);
            dialog.setVisible(true);
            if (dialog.isUserSaved()) {
                loadUsers();
            }
        } catch (Exception ex) {
            showError("خطأ في تحميل بيانات المستخدم: " + ex.getMessage());
        }
    }
    
    private void deleteUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار مستخدم للحذف");
            return;
        }
        
        int userId = (int) usersTable.getValueAt(selectedRow, 0);
        String username = (String) usersTable.getValueAt(selectedRow, 1);
        
        if (userId == currentUser.getId()) {
            showError("لا يمكن حذف المستخدم الحالي");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "هل أنت متأكد من حذف المستخدم " + username + "؟",
            "تأكيد الحذف",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.delete(userId);
                loadUsers();
            } catch (Exception ex) {
                showError("خطأ في حذف المستخدم: " + ex.getMessage());
            }
        }
    }
    
    private void resetPassword(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار مستخدم لإعادة تعيين كلمة المرور");
            return;
        }
        
        int userId = (int) usersTable.getValueAt(selectedRow, 0);
        String username = (String) usersTable.getValueAt(selectedRow, 1);
        
        ResetPasswordDialog dialog = new ResetPasswordDialog(
            SwingUtilities.getWindowAncestor(this),
            userId,
            username
        );
        dialog.setVisible(true);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
}