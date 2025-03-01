package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.MaterialDAO;
import com.mycompany.inventorysystemdesktop.dao.TransactionDAO;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.models.Transaction;
import com.mycompany.inventorysystemdesktop.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.border.TitledBorder;
import java.util.Calendar;
import java.util.Date;

public class DashboardPanel extends JPanel {
    private final User currentUser;
    private final MaterialDAO materialDAO;
    private final TransactionDAO transactionDAO;
    private final DecimalFormat decimalFormat;
    private DefaultTableModel transactionsTableModel;
    private DefaultTableModel alertsTableModel;
    
    public DashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        this.materialDAO = new MaterialDAO();
        this.transactionDAO = new TransactionDAO();
        this.decimalFormat = new DecimalFormat("#,##0.00");
        
        initComponents();
        loadDashboardData();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // إضافة البطاقات الإحصائية
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        add(createStatsCard("إجمالي المواد", "0", new Color(33, 150, 243)), gbc);
        
        gbc.gridx = 1;
        add(createStatsCard("المواد منخفضة المخزون", "0", new Color(244, 67, 54)), gbc);
        
        gbc.gridx = 2;
        add(createStatsCard("قيمة المخزون", "0.00", new Color(76, 175, 80)), gbc);
        
        // إضافة ملخص المعاملات الأخيرة
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        add(createTransactionsSummary(), gbc);
        
        // إضافة تنبيهات المخزون
        gbc.gridy = 2;
        add(createStockAlerts(), gbc);
    }
    
    private JPanel createStatsCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(250, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(24f));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createTransactionsSummary() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "ملخص المعاملات اليومية",
            TitledBorder.RIGHT,
            TitledBorder.TOP
        ));
        
        String[] columns = {"النوع", "المادة", "الكمية", "الإجمالي", "التاريخ"};
        transactionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(transactionsTableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStockAlerts() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED),
            "تنبيهات المخزون",
            TitledBorder.RIGHT,
            TitledBorder.TOP
        ));
        
        String[] columns = {"المادة", "الكمية الحالية", "الحد الأدنى"};
        alertsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(alertsTableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadDashboardData() {
        try {
            // تحميل إحصائيات المواد
            List<Material> materials = materialDAO.findAll();
            int totalMaterials = materials.size();
            int lowStockCount = 0;
            double totalValue = 0.0;
            
            for (Material material : materials) {
                if (material.getQuantity() <= material.getMinQuantity()) {
                    lowStockCount++;
                }
                totalValue += material.getQuantity() * material.getPrice();
            }
            
            // تحديث البطاقات الإحصائية
            updateStatsCard(0, String.valueOf(totalMaterials));
            updateStatsCard(1, String.valueOf(lowStockCount));
            updateStatsCard(2, decimalFormat.format(totalValue));
            
            // تحميل المعاملات اليومية
            loadTodayTransactions();
            
            // تحميل تنبيهات المخزون
            loadStockAlerts(materials);
            
        } catch (SQLException e) {
            showError("خطأ في تحميل بيانات لوحة المعلومات: " + e.getMessage());
        }
    }
    
    private void updateStatsCard(int index, String value) {
        Component card = getComponent(index);
        if (card instanceof JPanel) {
            Component valueLabel = ((JPanel) card).getComponent(1);
            if (valueLabel instanceof JLabel) {
                ((JLabel) valueLabel).setText(value);
            }
        }
    }
    
    private void loadTodayTransactions() {
        try {
            // تحديد فترة اليوم
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startDate = calendar.getTime();
            
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();
            
            // تحميل المعاملات
            List<Transaction> transactions = transactionDAO.search(null, null, startDate, endDate);
            
            // تحديث الجدول
            transactionsTableModel.setRowCount(0);
            
            for (Transaction transaction : transactions) {
                Material material = materialDAO.findById(transaction.getMaterialId());
                transactionsTableModel.addRow(new Object[]{
                    transaction.getType().equals("IN") ? "وارد" : "صادر",
                    material != null ? material.getName() : "غير معروف",
                    transaction.getQuantity(),
                    decimalFormat.format(transaction.getTotalPrice()),
                    new java.text.SimpleDateFormat("HH:mm").format(transaction.getTransactionDate())
                });
            }
            
        } catch (SQLException e) {
            showError("خطأ في تحميل المعاملات اليومية: " + e.getMessage());
        }
    }
    
    private void loadStockAlerts(List<Material> materials) {
        alertsTableModel.setRowCount(0);
        
        for (Material material : materials) {
            if (material.getQuantity() <= material.getMinQuantity()) {
                alertsTableModel.addRow(new Object[]{
                    material.getName(),
                    material.getQuantity(),
                    material.getMinQuantity()
                });
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    }
    
    // يمكنك إضافة دالة لتحديث البيانات بشكل دوري
    public void refreshData() {
        loadDashboardData();
    }
}