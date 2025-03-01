package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.Category;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.dao.CategoryDAO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MaterialDialog extends JDialog {
    
    private Material material;
    private boolean confirmed = false;
    
    private JTextField codeField;
    private JTextField nameField;
    private JComboBox<Category> categoryCombo;
    private JTextField unitField;
    private JSpinner quantitySpinner;
    private JSpinner minQuantitySpinner;
    private JSpinner priceSpinner;
    
    public MaterialDialog(Window owner, String title) {
        this(owner, title, null);
    }
    
    public MaterialDialog(Window owner, String title, Material material) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.material = material != null ? material : new Material();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 450);
        setLocationRelativeTo(getOwner());
        
        // لوحة الحقول
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // الكود
        gbc.gridx = 1; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("الكود:"), gbc);
        gbc.gridx = 0;
        codeField = new JTextField(20);
        fieldsPanel.add(codeField, gbc);
        
        // الاسم
        gbc.gridx = 1; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("الاسم:"), gbc);
        gbc.gridx = 0;
        nameField = new JTextField(20);
        fieldsPanel.add(nameField, gbc);
        
        // التصنيف
        gbc.gridx = 1; gbc.gridy = 2;
        fieldsPanel.add(new JLabel("التصنيف:"), gbc);
        gbc.gridx = 0;
        categoryCombo = new JComboBox<>();
        loadCategories();
        fieldsPanel.add(categoryCombo, gbc);
        
        // الوحدة
        gbc.gridx = 1; gbc.gridy = 3;
        fieldsPanel.add(new JLabel("الوحدة:"), gbc);
        gbc.gridx = 0;
        unitField = new JTextField(20);
        fieldsPanel.add(unitField, gbc);
        
        // الكمية
        gbc.gridx = 1; gbc.gridy = 4;
        fieldsPanel.add(new JLabel("الكمية:"), gbc);
        gbc.gridx = 0;
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1.0);
        quantitySpinner = new JSpinner(quantityModel);
        fieldsPanel.add(quantitySpinner, gbc);
        
        // الحد الأدنى
        gbc.gridx = 1; gbc.gridy = 5;
        fieldsPanel.add(new JLabel("الحد الأدنى:"), gbc);
        gbc.gridx = 0;
        SpinnerNumberModel minQuantityModel = new SpinnerNumberModel(0.0, 0.0, 1000000.0, 1.0);
        minQuantitySpinner = new JSpinner(minQuantityModel);
        fieldsPanel.add(minQuantitySpinner, gbc);
        
        // السعر
        gbc.gridx = 1; gbc.gridy = 6;
        fieldsPanel.add(new JLabel("السعر:"), gbc);
        gbc.gridx = 0;
        SpinnerNumberModel priceModel = new SpinnerNumberModel(0.0, 0.0, 1000000.0, 0.5);
        priceSpinner = new JSpinner(priceModel);
        fieldsPanel.add(priceSpinner, gbc);
        
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
    
    private void loadCategories() {
        CategoryDAO categoryDAO = new CategoryDAO();
        List<Category> categories = categoryDAO.getAll();
        for (Category category : categories) {
            categoryCombo.addItem(category);
        }
    }
    
    private void loadData() {
        if (material.getId() != 0) {
            codeField.setText(material.getCode());
            nameField.setText(material.getName());
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                Category category = categoryCombo.getItemAt(i);
                if (category.getId() == material.getCategoryId()) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
            unitField.setText(material.getUnit());
            quantitySpinner.setValue(material.getQuantity());
            minQuantitySpinner.setValue(material.getMinQuantity());
            priceSpinner.setValue(material.getPrice());
        }
    }
    
    private boolean validateInput() {
        if (codeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "الرجاء إدخال الكود", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "الرجاء إدخال الاسم", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (categoryCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "الرجاء اختيار التصنيف", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (unitField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "الرجاء إدخال الوحدة", "خطأ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void saveData() {
        material.setCode(codeField.getText().trim());
        material.setName(nameField.getText().trim());
        Category selectedCategory = (Category) categoryCombo.getSelectedItem();
        material.setCategoryId(selectedCategory.getId());
        material.setUnit(unitField.getText().trim());
        material.setQuantity((Double) quantitySpinner.getValue());
        material.setMinQuantity((Double) minQuantitySpinner.getValue());
        material.setPrice((Double) priceSpinner.getValue());
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}