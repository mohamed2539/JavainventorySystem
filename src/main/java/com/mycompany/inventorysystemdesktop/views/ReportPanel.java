package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.CategoryDAO;
import com.mycompany.inventorysystemdesktop.dao.MaterialDAO;
import com.mycompany.inventorysystemdesktop.dao.TransactionDAO;
import com.mycompany.inventorysystemdesktop.models.Category;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.models.Transaction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {
      private MaterialDAO materialDAO;
    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO; // إضافة CategoryDAO
    
    public ReportPanel() {
        materialDAO = new MaterialDAO();
        transactionDAO = new TransactionDAO();
        categoryDAO = new CategoryDAO(); // تهيئة CategoryDAO
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(createMaterialStatusChart());
        add(createTopMaterialsChart());
        add(createTransactionSummaryChart());
        add(createInventoryValueChart());
    }
    
    private JPanel createMaterialStatusChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            List<Material> materials = materialDAO.findAll();
            
            int lowStock = 0;
            int available = 0;
            
            for (Material material : materials) {
                if (material.getQuantity() <= material.getMinQuantity()) {
                    lowStock++;
                } else {
                    available++;
                }
            }
            
            dataset.setValue("متوفر", available);
            dataset.setValue("منخفض", lowStock);
            
        } catch (SQLException e) {
            System.err.println("خطأ في جلب بيانات المواد: " + e.getMessage());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "حالة المخزون",
            dataset,
            true,
            true,
            false
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("حالة المخزون"));
        return chartPanel;
    }
    
    private JPanel createTopMaterialsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            List<Material> materials = materialDAO.findAll();
            
            materials.sort((m1, m2) -> {
                double value1 = m1.getQuantity() * m1.getPrice();
                double value2 = m2.getQuantity() * m2.getPrice();
                return Double.compare(value2, value1);
            });
            
            for (int i = 0; i < Math.min(5, materials.size()); i++) {
                Material material = materials.get(i);
                dataset.addValue(
                    material.getQuantity() * material.getPrice(),
                    "القيمة",
                    material.getName()
                );
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في جلب بيانات المواد: " + e.getMessage());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "أعلى 5 مواد من حيث القيمة",
            "المادة",
            "القيمة",
            dataset
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("أعلى المواد قيمة"));
        return chartPanel;
    }
    
    private JPanel createTransactionSummaryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        List<Transaction> incoming = transactionDAO.getAll("incoming");
        List<Transaction> outgoing = transactionDAO.getAll("outgoing");
        
        double totalIncoming = incoming.stream()
            .mapToDouble(t -> t.getQuantity() * t.getUnitPrice())
            .sum();
            
        double totalOutgoing = outgoing.stream()
            .mapToDouble(t -> t.getQuantity() * t.getUnitPrice())
            .sum();
            
        dataset.addValue(totalIncoming, "القيمة", "الوارد");
        dataset.addValue(totalOutgoing, "القيمة", "المنصرف");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "ملخص المعاملات",
            "نوع المعاملة",
            "القيمة الإجمالية",
            dataset
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("ملخص المعاملات"));
        return chartPanel;
    }
    
    private JPanel createInventoryValueChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            List<Material> materials = materialDAO.findAll();
            
            Map<String, Double> categoryValues = materials.stream()
                .collect(Collectors.groupingBy(
                    m -> getCategoryName(m.getCategoryId()),
                    Collectors.summingDouble(m -> m.getQuantity() * m.getPrice())
                ));
                
            categoryValues.forEach(dataset::setValue);
            
        } catch (SQLException e) {
            System.err.println("خطأ في جلب بيانات المواد: " + e.getMessage());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "قيمة المخزون حسب التصنيف",
            dataset,
            true,
            true,
            false
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createTitledBorder("قيمة المخزون حسب التصنيف"));
        return chartPanel;
    }
    
    private String getCategoryName(int categoryId) {
        Category category = categoryDAO.getById(categoryId);
        return category != null ? category.getName() : "بدون تصنيف";
    }
}