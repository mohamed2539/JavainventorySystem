package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.Category;
import javax.swing.*;
import java.awt.*;

public class CategoryDialog extends JDialog {
    
    private Category category;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextArea descriptionArea;
    
    public CategoryDialog(Window owner, String title) {
        this(owner, title, null);
    }
    
    public CategoryDialog(Window owner, String title, Category category) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.category = category != null ? category : new Category();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        
        // لوحة الحقول
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // الاسم
        gbc.gridx = 1; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("الاسم:"), gbc);
        gbc.gridx = 0;
        nameField = new JTextField(20);
        fieldsPanel.add(nameField, gbc);
        
        // الوصف
        gbc.gridx = 1; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("الوصف:"), gbc);
        gbc.gridx = 0;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        fieldsPanel.add(scrollPane, gbc);
        
        // لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("حفظ");
        JButton cancelButton = new JButton("إلغاء");
        
        saveButton.addActionListener(e -> {
            if (validateInput()) {
                saveData();
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // إضافة اللوحات إلى النافذة
        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        if (category.getId() != 0) {
            nameField.setText(category.getName());
            descriptionArea.setText(category.getDescription());
        }
    }
    
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "الرجاء إدخال اسم التصنيف", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void saveData() {
        category.setName(nameField.getText().trim());
        category.setDescription(descriptionArea.getText().trim());
    }
    
    public Category getCategory() {
        return category;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}