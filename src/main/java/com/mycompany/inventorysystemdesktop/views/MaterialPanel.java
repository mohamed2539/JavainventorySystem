package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.CategoryDAO;
import com.mycompany.inventorysystemdesktop.dao.MaterialDAO;
import com.mycompany.inventorysystemdesktop.models.Category;
import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MaterialPanel extends JPanel {
    
    private JTextField searchField;
    private JTable materialTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private MaterialDAO materialDAO;
    private CategoryDAO categoryDAO;
    private final User currentUser;
    public MaterialPanel(User currentUser) {
        materialDAO = new MaterialDAO();
        categoryDAO = new CategoryDAO();
        this.currentUser = currentUser;
        initComponents();
        loadMaterials();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // لوحة البحث
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        searchField.setToolTipText("ابحث باسم المادة أو الكود");
        JButton searchButton = new JButton("بحث");
        searchButton.addActionListener(e -> searchMaterials());
        searchPanel.add(searchButton);
        searchPanel.add(searchField);
        
        // لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("إضافة مادة");
        editButton = new JButton("تعديل");
        deleteButton = new JButton("حذف");
        
        addButton.addActionListener(this::addMaterial);
        editButton.addActionListener(this::editMaterial);
        deleteButton.addActionListener(this::deleteMaterial);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // لوحة العلوية تجمع البحث والأزرار
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.EAST);
        topPanel.add(buttonPanel, BorderLayout.WEST);
        
        // جدول المواد
        String[] columns = {"الكود", "اسم المادة", "التصنيف", "الوحدة", "الكمية", "الحد الأدنى", "السعر", "الحالة"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        materialTable = new JTable(tableModel);
        materialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(materialTable);
        
        // إضافة المكونات إلى اللوحة الرئيسية
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadMaterials() {
        tableModel.setRowCount(0);
        List<Material> materials = materialDAO.getAll();
        
        for (Material material : materials) {
            Object[] row = {
                material.getCode(),
                material.getName(),
                material.getCategoryName(),
                material.getUnit(),
                material.getQuantity(),
                material.getMinQuantity(),
                material.getPrice(),
                material.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchMaterials() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Material> materials;
        
        if (keyword.isEmpty()) {
            materials = materialDAO.getAll();
        } else {
            materials = materialDAO.search(keyword, null, null);
        }
        
        for (Material material : materials) {
            Object[] row = {
                material.getCode(),
                material.getName(),
                material.getCategoryName(),
                material.getUnit(),
                material.getQuantity(),
                material.getMinQuantity(),
                material.getPrice(),
                material.getStatus()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addMaterial(ActionEvent e) {
        MaterialDialog dialog = new MaterialDialog(SwingUtilities.getWindowAncestor(this), "إضافة مادة جديدة");
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Material material = dialog.getMaterial();
            if (materialDAO.add(material)) {
                JOptionPane.showMessageDialog(this, "تمت إضافة المادة بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                loadMaterials();
            } else {
                JOptionPane.showMessageDialog(this, "فشل في إضافة المادة", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editMaterial(ActionEvent e) {
        int selectedRow = materialTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء اختيار مادة للتعديل", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String code = tableModel.getValueAt(selectedRow, 0).toString();
        Material material = materialDAO.getByCode(code);
        
        if (material != null) {
            MaterialDialog dialog = new MaterialDialog(SwingUtilities.getWindowAncestor(this), "تعديل مادة", material);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                material = dialog.getMaterial();
                if (materialDAO.update(material)) {
                    JOptionPane.showMessageDialog(this, "تم تحديث المادة بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                    loadMaterials();
                } else {
                    JOptionPane.showMessageDialog(this, "فشل في تحديث المادة", "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteMaterial(ActionEvent e) {
        int selectedRow = materialTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء اختيار مادة للحذف", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف هذه المادة؟",
                "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            String code = tableModel.getValueAt(selectedRow, 0).toString();
            Material material = materialDAO.getByCode(code);
            
            if (material != null && materialDAO.delete(material.getId())) {
                JOptionPane.showMessageDialog(this, "تم حذف المادة بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                loadMaterials();
            } else {
                JOptionPane.showMessageDialog(this, "فشل في حذف المادة", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}