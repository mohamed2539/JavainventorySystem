package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.models.*;
import com.mycompany.inventorysystemdesktop.dao.*;
import com.mycompany.inventorysystemdesktop.utils.ExcelExporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;      //TransactionsPanel
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;
import java.util.Calendar;

public class TransactionsPanel extends JPanel {
   private final String transactionType; // "IN" للوارد، "OUT" للصرف
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private final TransactionDAO transactionDAO;
    private final MaterialDAO materialDAO;
    private final User currentUser;
    private JComboBox<String> typeFilter;
    private JDateChooser fromDate;
    private JDateChooser toDate;
    private JTextField searchField;
    
    public TransactionsPanel(User currentUser ,String type) {
        this.currentUser = currentUser;
        this.transactionType = type;
        this.transactionDAO = new TransactionDAO();
        this.materialDAO = new MaterialDAO();
        initComponents();
        loadTransactions();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // إنشاء لوحة البحث والفلترة
        add(createFilterPanel(), BorderLayout.NORTH);
        
        // إنشاء جدول المعاملات
        String[] columns = {
            "الرقم", "المادة", "النوع", "الكمية", "السعر", "الإجمالي",
            "رقم المرجع", "التاريخ", "الملاحظات", "المستخدم"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // إنشاء لوحة الأزرار
        add(createButtonPanel(), BorderLayout.SOUTH);
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
        searchButton.addActionListener(e -> loadTransactions());
        typeFilter.addActionListener(e -> loadTransactions());
        
        return filterPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("إضافة معاملة");
        JButton editButton = new JButton("تعديل");
        JButton deleteButton = new JButton("حذف");
        JButton printButton = new JButton("طباعة");
        JButton exportButton = new JButton("تصدير");
        JButton btnIssue = new JButton("صرف مواد");
        
        addButton.addActionListener(this::addTransaction);
        editButton.addActionListener(this::editTransaction);
        deleteButton.addActionListener(this::deleteTransaction);
        printButton.addActionListener(this::printTransaction);
        exportButton.addActionListener(this::exportTransactions);
        btnIssue.addActionListener(e -> showIssueDialog());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(btnIssue);
        
        return buttonPanel;
    }
    
   private void loadTransactions() {
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
            double total = transaction.getQuantity() * transaction.getUnitPrice();
            
            Object[] row = {
                transaction.getId(),
                material != null ? material.getName() : "غير معروف",
                transaction.getType().equals("IN") ? "وارد" : "صادر",
                transaction.getQuantity(),
                transaction.getUnitPrice(),
                total,
                transaction.getReferenceNo(),
                dateFormat.format(transaction.getTransactionDate()),
                transaction.getNotes(),
                transaction.getUserId()
            };
            tableModel.addRow(row);
        }
    } catch (Exception e) {
        showError("خطأ في تحميل المعاملات: " + e.getMessage());
    }
}
    
    private void updateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (Transaction transaction : transactions) {
            try {
                Material material = materialDAO.findById(transaction.getMaterialId());
                double total = transaction.getQuantity() * transaction.getUnitPrice();
                
                Object[] row = {
                    transaction.getId(),
                    material.getName(),
                    transaction.getType().equals("IN") ? "وارد" : "صادر",
                    transaction.getQuantity(),
                    transaction.getUnitPrice(),
                    total,
                    transaction.getReferenceNo(),
                    dateFormat.format(transaction.getTransactionDate()),
                    transaction.getNotes(),
                    transaction.getUserId()
                };
                tableModel.addRow(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void addTransaction(ActionEvent e) {
        try {
            List<Material> materials = materialDAO.findAll();
            if (materials.isEmpty()) {showWarning("لا توجد مواد متاحة. الرجاء إضافة مواد أولاً.");
                return;
            }
            
            TransactionDialog dialog = new TransactionDialog(SwingUtilities.getWindowAncestor(this),null,
                currentUser);
            
            dialog.setVisible(true);
            if (dialog.isTransactionSaved()) {
                loadTransactions();
            }
        } catch (Exception ex) {
            showError("خطأ في إضافة المعاملة: " + ex.getMessage());
        }
    }
    
    
     // إضافة زر لإضافة كمية
    private JButton createAddQuantityButton() {
        JButton addQuantityButton = new JButton("إضافة كمية");
        addQuantityButton.addActionListener(e -> showAddQuantityDialog());
        return addQuantityButton;
    }
    
    
    
     private void showAddQuantityDialog() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("الرجاء اختيار معاملة أولاً");
            return;
        }
        
        try {
            int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
            Transaction transaction = transactionDAO.findById(transactionId);
            
            if (transaction != null) {
                String input = JOptionPane.showInputDialog(this,
                    "أدخل الكمية المراد إضافتها:",
                    "إضافة كمية",
                    JOptionPane.QUESTION_MESSAGE);
                
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        double quantity = Double.parseDouble(input.trim());
                        if (quantity <= 0) {
                            showError("الرجاء إدخال كمية صحيحة أكبر من صفر");
                            return;
                        }
                        
                        // إنشاء معاملة جديدة بنفس البيانات ولكن بالكمية الجديدة
                        Transaction newTransaction = new Transaction();
                        newTransaction.setMaterialId(transaction.getMaterialId());
                        newTransaction.setType(transaction.getType());
                        newTransaction.setQuantity(quantity);
                        newTransaction.setUnitPrice(transaction.getUnitPrice());
                        newTransaction.setNotes("إضافة كمية للمعاملة " + transaction.getReferenceNo());
                        newTransaction.setUserId(currentUser.getId());
                        
                        if (transactionDAO.add(newTransaction)) {
                            showMessage("تم إضافة الكمية بنجاح");
                            loadTransactions();
                        }
                    } catch (NumberFormatException ex) {
                        showError("الرجاء إدخال رقم صحيح");
                    }
                }
            }
        } catch (Exception ex) {
            showError("خطأ في إضافة الكمية: " + ex.getMessage());
        }
    }

    private void showIssueDialog() {
        IssueDialog dialog = new IssueDialog((JFrame)SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadTransactions(); // تحديث الجدول بعد إغلاق النافذة
    }
    
    private void editTransaction(ActionEvent e) {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار معاملة للتعديل");
            return;
        }
        
        try {
            int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
            Transaction transaction = transactionDAO.findById(transactionId);
            
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
            showError("خطأ في تحميل بيانات المعاملة: " + ex.getMessage());
        }
    }
    
    private void deleteTransaction(ActionEvent e) {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار معاملة للحذف");
            return;
        }
        
        int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "هل أنت متأكد من حذف هذه المعاملة؟",
            "تأكيد الحذف",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                transactionDAO.delete(transactionId);
                loadTransactions();
            } catch (Exception ex) {
                showError("خطأ في حذف المعاملة: " + ex.getMessage());
            }
        }
    }
    
    private void printTransaction(ActionEvent e) {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("الرجاء اختيار معاملة للطباعة");
            return;
        }
        
        try {
            int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
            Transaction transaction = transactionDAO.findById(transactionId);
            new TransactionPrintDialog(SwingUtilities.getWindowAncestor(this), transaction).setVisible(true);
        } catch (Exception ex) {
            showError("خطأ في طباعة المعاملة: " + ex.getMessage());
        }
    }
    
    private void exportTransactions(ActionEvent e) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("حفظ التقرير");
            fileChooser.setSelectedFile(new java.io.File("transactions_report.xlsx"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getPath();
                if (!filePath.toLowerCase().endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }
                
                ExcelExporter.exportTransactions(
                    transactionsTable,
                    filePath,
                    fromDate.getDate(),
                    toDate.getDate()
                );
                
                JOptionPane.showMessageDialog(this,
                    "تم تصدير التقرير بنجاح",
                    "نجاح",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showError("خطأ في تصدير التقرير: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "نجاح",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "تنبيه",
            JOptionPane.WARNING_MESSAGE);
    }
    
    private void searchTransactions() {
        tableModel.setRowCount(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        try {
            String searchTerm = searchField.getText().trim();
            Date from = fromDate.getDate();
            Date to = toDate.getDate();
            
            List<Transaction> transactions = transactionDAO.search(searchTerm, transactionType, from, to);
            
            for (Transaction transaction : transactions) {
                Material material = materialDAO.findById(transaction.getMaterialId());
                
                Object[] row = {
                    transaction.getReferenceNo(),
                    material != null ? material.getName() : "غير معروف",
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
}
