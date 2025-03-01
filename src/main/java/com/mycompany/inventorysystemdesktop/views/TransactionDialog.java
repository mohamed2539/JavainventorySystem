package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.*;
import com.mycompany.inventorysystemdesktop.dao.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import com.toedter.calendar.JDateChooser;

public class TransactionDialog extends JDialog {
    private final Transaction transaction;
    private final User currentUser;
    private final TransactionDAO transactionDAO;
    private final MaterialDAO materialDAO;
    private boolean transactionSaved = false;
    
    private JComboBox<Material> materialCombo;
    private JComboBox<String> typeCombo;
    private JSpinner quantitySpinner;
    private JTextField unitPriceField;
    private JTextField referenceNoField;
    private JTextArea notesArea;
    private JDateChooser dateChooser;
    
    public TransactionDialog(Window owner, Transaction transaction, User currentUser) {
        super(owner, transaction == null ? "إضافة معاملة جديدة" : "تعديل معاملة", ModalityType.APPLICATION_MODAL);
        this.transaction = transaction;
        this.currentUser = currentUser;
        this.transactionDAO = new TransactionDAO();
        this.materialDAO = new MaterialDAO();
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // المادة
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("المادة:"), gbc);
        
        materialCombo = new JComboBox<>();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(materialCombo, gbc);
        
        // نوع المعاملة
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("النوع:"), gbc);
        
        typeCombo = new JComboBox<>(new String[]{"وارد", "صادر"});
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(typeCombo, gbc);
        
        // الكمية
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("الكمية:"), gbc);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1.0, 0.0, 1000000.0, 1.0);
        quantitySpinner = new JSpinner(spinnerModel);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(quantitySpinner, gbc);
        
        // سعر الوحدة
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("سعر الوحدة:"), gbc);
        
        unitPriceField = new JTextField(10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(unitPriceField, gbc);
        
        // رقم المرجع
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("رقم المرجع:"), gbc);
        
        referenceNoField = new JTextField(20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(referenceNoField, gbc);
        
        // التاريخ
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("التاريخ:"), gbc);
        
        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(dateChooser, gbc);
        
        // الملاحظات
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        add(new JLabel("ملاحظات:"), gbc);
        
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(notesScroll, gbc);
        
        // الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("حفظ");
        JButton cancelButton = new JButton("إلغاء");
        
        saveButton.addActionListener(this::saveTransaction);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gbc);
        
        // إضافة المستمعين
        materialCombo.addActionListener(e -> updateUnitPrice());
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void loadData() {
        try {
            // تحميل المواد
            List<Material> materials = materialDAO.findAll();
            for (Material material : materials) {
                materialCombo.addItem(material);
            }
            
            // تحميل بيانات المعاملة إذا كانت موجودة
            if (transaction != null) {
                Material material = materialDAO.findById(transaction.getMaterialId());
                materialCombo.setSelectedItem(material);
                
                typeCombo.setSelectedItem(transaction.getType().equals("IN") ? "وارد" : "صادر");
                quantitySpinner.setValue(transaction.getQuantity());
                unitPriceField.setText(String.valueOf(transaction.getUnitPrice()));
                referenceNoField.setText(transaction.getReferenceNo());
                dateChooser.setDate(transaction.getTransactionDate());
                notesArea.setText(transaction.getNotes());
                
                // تعطيل تغيير النوع للمعاملات الموجودة
                typeCombo.setEnabled(false);
            }
        } catch (Exception e) {
            showError("خطأ في تحميل البيانات: " + e.getMessage());
        }
    }
    
    private void updateUnitPrice() {
        Material selectedMaterial = (Material) materialCombo.getSelectedItem();
        if (selectedMaterial != null) {
            unitPriceField.setText(String.valueOf(selectedMaterial.getPrice()));
        }
    }
    
    private void saveTransaction(ActionEvent e) {
        try {
            // التحقق من صحة البيانات
            Material selectedMaterial = (Material) materialCombo.getSelectedItem();
            if (selectedMaterial == null) {
                showError("الرجاء اختيار المادة");
                return;
            }
            
            double quantity = (double) quantitySpinner.getValue();
            if (quantity <= 0) {
                showError("الكمية يجب أن تكون أكبر من صفر");
                return;
            }
            
            String type = typeCombo.getSelectedItem().toString().equals("وارد") ? "IN" : "OUT";
            
            // التحقق من توفر الكمية في حالة الصرف
            if (type.equals("OUT")) {
                double availableQuantity = selectedMaterial.getQuantity();
                if (transaction != null) {
                    availableQuantity += transaction.getQuantity(); // إضافة الكمية القديمة في حالة التعديل
                }
                if (quantity > availableQuantity) {
                    showError("الكمية المطلوبة غير متوفرة. الكمية المتوفرة: " + availableQuantity);
                    return;
                }
            }
            
            // حفظ المعاملة
            if (transaction == null) {
                Transaction newTransaction = new Transaction();
                newTransaction.setMaterialId(selectedMaterial.getId());
                newTransaction.setType(type);
                newTransaction.setQuantity(quantity);
                newTransaction.setUnitPrice(Double.parseDouble(unitPriceField.getText()));
                newTransaction.setReferenceNo(referenceNoField.getText());
                newTransaction.setTransactionDate(dateChooser.getDate());
                newTransaction.setNotes(notesArea.getText());
                newTransaction.setUserId(currentUser.getId());
                
                transactionDAO.create(newTransaction);
            } else {
                transaction.setQuantity(quantity);
                transaction.setUnitPrice(Double.parseDouble(unitPriceField.getText()));
                transaction.setReferenceNo(referenceNoField.getText());
                transaction.setTransactionDate(dateChooser.getDate());
                transaction.setNotes(notesArea.getText());
                
                transactionDAO.update(transaction);
            }
            
            transactionSaved = true;
            dispose();
            
        } catch (Exception ex) {
            showError("خطأ في حفظ المعاملة: " + ex.getMessage());
        }
    }
    
    public boolean isTransactionSaved() {
        return transactionSaved;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
}