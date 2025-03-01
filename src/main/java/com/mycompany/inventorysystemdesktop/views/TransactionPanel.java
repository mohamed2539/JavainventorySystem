package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.MaterialDAO;
import com.mycompany.inventorysystemdesktop.dao.TransactionDAO;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.models.Transaction;
import com.mycompany.inventorysystemdesktop.models.User;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TransactionPanel extends JPanel {
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private final TransactionDAO transactionDAO;
    private final MaterialDAO materialDAO;
    private final User currentUser;
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JDateChooser fromDate;
    private JDateChooser toDate;
    
    public TransactionPanel(User currentUser) {
        this.currentUser = currentUser;
        this.transactionDAO = new TransactionDAO();
        this.materialDAO = new MaterialDAO();
        initComponents();
        loadTransactions();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // إضافة لوحة الفلترة
        add(createFilterPanel(), BorderLayout.NORTH);
        
        // إعداد الجدول
        setupTable();
        
        // إضافة لوحة الأزرار
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    private void setupTable() {
        String[] columns = {
            "رقم المرجع",
            "المادة",
            "النوع",
            "الكمية",
            "سعر الوحدة",
            "الإجمالي",
            "الملاحظات",
            "التاريخ"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // تنسيق الجدول
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // فلتر نوع المعاملة
        gbc.gridx = 5;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("النوع:"), gbc);
        
        typeFilter = new JComboBox<>(new String[]{"الكل", "وارد", "صادر"});
        gbc.gridx = 4;
        filterPanel.add(typeFilter, gbc);
        
        // من تاريخ
        gbc.gridx = 3;
        filterPanel.add(new JLabel("من:"), gbc);
        
        fromDate = new JDateChooser();
        fromDate.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 2;
        filterPanel.add(fromDate, gbc);
        
        // إلى تاريخ
        gbc.gridx = 1;
        filterPanel.add(new JLabel("إلى:"), gbc);
        
        toDate = new JDateChooser();
        toDate.setPreferredSize(new Dimension(120, 25));
        gbc.gridx = 0;
        filterPanel.add(toDate, gbc);
        
        // حقل البحث
        gbc.gridx = 5;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("بحث:"), gbc);
        
        searchField = new JTextField(20);
        gbc.gridx = 4;
        gbc.gridwidth = 2;
        filterPanel.add(searchField, gbc);
        
        // زر البحث
        JButton searchButton = new JButton("بحث");
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        filterPanel.add(searchButton, gbc);
        
        // إضافة المستمعين
        searchButton.addActionListener(e -> searchTransactions());
        typeFilter.addActionListener(e -> searchTransactions());
        fromDate.addPropertyChangeListener("date", e -> searchTransactions());
        toDate.addPropertyChangeListener("date", e -> searchTransactions());
        
        return filterPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("إضافة معاملة");
        JButton editButton = new JButton("تعديل");
        JButton deleteButton = new JButton("حذف");
        JButton printButton = new JButton("طباعة");
        JButton exportButton = new JButton("تصدير");
        
        addButton.addActionListener(this::addTransaction);
        editButton.addActionListener(this::editTransaction);
        deleteButton.addActionListener(this::deleteTransaction);
        printButton.addActionListener(this::printTransaction);
        exportButton.addActionListener(this::exportTransaction);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        
        return buttonPanel;
    }
    
    private void searchTransactions() {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        try {
            String searchTerm = searchField.getText().trim();
            String type = typeFilter.getSelectedItem().toString();
            Date from = fromDate.getDate();
            Date to = toDate.getDate();
            
            if (type.equals("الكل")) {
                type = null;
            } else if (type.equals("وارد")) {
                type = "IN";
            } else {
                type = "OUT";
            }
            
            List<Transaction> transactions = transactionDAO.search(searchTerm, type, from, to);
            
            for (Transaction transaction : transactions) {
                Material material = materialDAO.findById(transaction.getMaterialId());
                
                Object[] row = {
                    transaction.getReferenceNo(),
                    material != null ? material.getName() : "غير معروف",
                    transaction.getType().equals("IN") ? "وارد" : "صادر",
                    transaction.getQuantity(),
                    transaction.getUnitPrice(),
                    transaction.getTotalPrice(),
                    transaction.getNotes(),
                    dateFormat.format(transaction.getTransactionDate())
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            showError("خطأ في تحميل المعاملات: " + e.getMessage());
        }
    }
    
    private void loadTransactions() {
        searchTransactions();
    }
    
    private void addTransaction(ActionEvent e) {
        try {
            List<Material> materials = materialDAO.findAll();
            if (materials.isEmpty()) {
                showWarning("لا توجد مواد متاحة. الرجاء إضافة مواد أولاً.");
                return;
            }
            
            TransactionDialog dialog = new TransactionDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                currentUser
            );
            
            dialog.setVisible(true);
            if (dialog.isTransactionSaved()) {
                loadTransactions();
            }
        } catch (Exception ex) {
            showError("خطأ في إضافة المعاملة: " + ex.getMessage());
        }
    }
    
    private void editTransaction(ActionEvent e) {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("الرجاء اختيار معاملة للتعديل");
            return;
        }
        
        try {
            String referenceNo = (String) transactionTable.getValueAt(selectedRow, 0);
            Transaction transaction = transactionDAO.findByReferenceNo(referenceNo);
            
            if (transaction != null) {
                TransactionDialog dialog = new TransactionDialog(
                    SwingUtilities.getWindowAncestor(this),
                    transaction,
                    currentUser
                );
                
                dialog.setVisible(true);
                if (dialog.isTransactionSaved()) {
                    loadTransactions();
                }
            }
        } catch (Exception ex) {
            showError("خطأ في تعديل المعاملة: " + ex.getMessage());
        }
    }
    
    private void deleteTransaction(ActionEvent e) {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("الرجاء اختيار معاملة للحذف");
            return;
        }
        
        try {
            String referenceNo = (String) transactionTable.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "هل أنت متأكد من حذف هذه المعاملة؟",
                "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (transactionDAO.delete(referenceNo)) {
                    loadTransactions();
                    showMessage("تم حذف المعاملة بنجاح");
                }
            }
        } catch (Exception ex) {
            showError("خطأ في حذف المعاملة: " + ex.getMessage());
        }
    }
    
    private void printTransaction(ActionEvent e) {
        // سيتم إضافة وظيفة الطباعة لاحقاً
        showMessage("سيتم إضافة وظيفة الطباعة قريباً");
    }
    
    private void exportTransaction(ActionEvent e) {
        // سيتم إضافة وظيفة التصدير لاحقاً
        showMessage("سيتم إضافة وظيفة التصدير قريباً");
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "خطأ", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "معلومات", JOptionPane.INFORMATION_MESSAGE);
    }
}