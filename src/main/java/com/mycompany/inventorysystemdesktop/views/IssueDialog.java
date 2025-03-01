package com.mycompany.inventorysystemdesktop.views;

import com.formdev.flatlaf.FlatClientProperties;
import com.mycompany.inventorysystemdesktop.dao.MaterialDAO;
import com.mycompany.inventorysystemdesktop.dao.TransactionDAO;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.models.Transaction;
import com.mycompany.inventorysystemdesktop.models.TransactionDetail;
import com.mycompany.inventorysystemdesktop.utils.MessageBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class IssueDialog extends JDialog {
    private final JTextField txtCode;
    private final JTextField txtQuantity;
    private final JTextField txtNotes;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final MaterialDAO materialDAO;
    private final TransactionDAO transactionDAO;
    private final List<TransactionDetail> details;
    
    public IssueDialog(JFrame parent) {
        super(parent, "صرف مواد", true);
        this.materialDAO = new MaterialDAO();
        this.transactionDAO = new TransactionDAO();
        this.details = new ArrayList<>();
        
        // إنشاء نموذج الجدول
        String[] columns = {"الكود", "اسم المادة", "الوحدة", "الكمية"};
        this.tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // إنشاء المكونات
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        txtCode = new JTextField(15);
        txtQuantity = new JTextField(15);
        txtNotes = new JTextField(30);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        
        JButton btnAdd = new JButton("إضافة");
        JButton btnRemove = new JButton("حذف");
        JButton btnSave = new JButton("حفظ");
        JButton btnCancel = new JButton("إلغاء");
        
        // تنسيق المكونات
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        
        // إضافة المكونات
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("كود المادة:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(txtCode, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("الكمية:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(txtQuantity, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("ملاحظات:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(txtNotes, gbc);
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // إضافة الأحداث
        btnAdd.addActionListener(e -> addItem());
        btnRemove.addActionListener(e -> removeItem());
        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        
        txtCode.addActionListener(e -> findMaterial());
        
        // تنسيق النافذة
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // تطبيق التصميم الحديث
        getRootPane().putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        mainPanel.putClientProperty(FlatClientProperties.STYLE, "background: lighten(@background, 2%)");
    }
    
    private void findMaterial() {
        String code = txtCode.getText().trim();
        if (!code.isEmpty()) {
            Material material = materialDAO.getByCode(code);
            if (material != null) {
                txtQuantity.requestFocus();
            } else {
                MessageBox.showError(this, "خطأ", "لم يتم العثور على المادة!");
                txtCode.selectAll();
                txtCode.requestFocus();
            }
        }
    }
    
    private void addItem() {
        String code = txtCode.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        
        if (code.isEmpty() || quantityStr.isEmpty()) {
            MessageBox.showError(this, "خطأ", "الرجاء إدخال كود المادة والكمية!");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                MessageBox.showError(this, "خطأ", "الرجاء إدخال كمية صحيحة!");
                return;
            }
            
            Material material = materialDAO.getByCode(code);
            if (material == null) {
                MessageBox.showError(this, "خطأ", "لم يتم العثور على المادة!");
                return;
            }
            
            if (material.getQuantity() < quantity) {
                MessageBox.showError(this, "خطأ", "الكمية المتوفرة غير كافية!");
                return;
            }
            
            // إضافة المادة إلى الجدول
            Object[] row = {material.getCode(), material.getName(), material.getUnit(), quantity};
            tableModel.addRow(row);
            
            // إضافة التفاصيل
            TransactionDetail detail = new TransactionDetail();
            detail.setMaterialId(material.getId());
            detail.setQuantity(quantity);
            details.add(detail);
            
            // تنظيف الحقول
            txtCode.setText("");
            txtQuantity.setText("");
            txtCode.requestFocus();
            
        } catch (NumberFormatException e) {
            MessageBox.showError(this, "خطأ", "الرجاء إدخال كمية صحيحة!");
        }
    }
    
    private void removeItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow);
            details.remove(selectedRow);
        }
    }
    
    private void save() {
        if (details.isEmpty()) {
            MessageBox.showError(this, "خطأ", "الرجاء إضافة مواد للصرف!");
            return;
        }
        
        // إنشاء معاملة جديدة
        Transaction transaction = new Transaction();
        transaction.setType("صرف");
        transaction.setNotes(txtNotes.getText().trim());
        transaction.setDetails(details);
        
        // حفظ المعاملة
        if (transactionDAO.add(transaction)) {
            MessageBox.showInfo(this, "نجاح", "تم حفظ معاملة الصرف بنجاح!");
            dispose();
        } else {
            MessageBox.showError(this, "خطأ", "حدث خطأ أثناء حفظ المعاملة!");
        }
    }
} 