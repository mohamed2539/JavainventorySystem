package com.mycompany.inventorysystemdesktop.views;

import com.mycompany.inventorysystemdesktop.dao.CategoryDAO;
import com.mycompany.inventorysystemdesktop.models.Category;
import com.mycompany.inventorysystemdesktop.models.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class CategoryPanel extends JPanel {
    
    private JTextField searchField;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private CategoryDAO categoryDAO;
    private final User currentUser;
    public CategoryPanel(User currentUser) {
        categoryDAO = new CategoryDAO();
        this.currentUser = currentUser;
        initComponents();
        loadCategories();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // لوحة البحث
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        searchField.setToolTipText("ابحث باسم التصنيف");
        JButton searchButton = new JButton("بحث");
        searchButton.addActionListener(e -> searchCategories());
        searchPanel.add(searchButton);
        searchPanel.add(searchField);
        
        // لوحة الأزرار
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("إضافة تصنيف");
        editButton = new JButton("تعديل");
        deleteButton = new JButton("حذف");
        
        addButton.addActionListener(this::addCategory);
        editButton.addActionListener(this::editCategory);
        deleteButton.addActionListener(this::deleteCategory);
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // لوحة العلوية تجمع البحث والأزرار
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.EAST);
        topPanel.add(buttonPanel, BorderLayout.WEST);
        
        // جدول التصنيفات
        String[] columns = {"الاسم", "الوصف", "تاريخ الإنشاء"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        
        // إضافة المكونات إلى اللوحة الرئيسية
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = categoryDAO.getAll();
        
        for (Category category : categories) {
            Object[] row = {
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
            };
            tableModel.addRow(row);
        }
    }
    
    private void searchCategories() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Category> categories;
        
        if (keyword.isEmpty()) {
            categories = categoryDAO.getAll();
        } else {
            categories = categoryDAO.search(keyword);
        }
        
        for (Category category : categories) {
            Object[] row = {
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
            };
            tableModel.addRow(row);
        }
    }
    
    private void addCategory(ActionEvent e) {
        CategoryDialog dialog = new CategoryDialog(SwingUtilities.getWindowAncestor(this), "إضافة تصنيف جديد");
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Category category = dialog.getCategory();
            if (categoryDAO.add(category)) {
                JOptionPane.showMessageDialog(this, "تمت إضافة التصنيف بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "فشل في إضافة التصنيف", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editCategory(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء اختيار تصنيف للتعديل", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = tableModel.getValueAt(selectedRow, 0).toString();
        Category category = categoryDAO.getByName(name);
        
        if (category != null) {
            CategoryDialog dialog = new CategoryDialog(SwingUtilities.getWindowAncestor(this), "تعديل تصنيف", category);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                category = dialog.getCategory();
                if (categoryDAO.update(category)) {
                    JOptionPane.showMessageDialog(this, "تم تحديث التصنيف بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                    loadCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "فشل في تحديث التصنيف", "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteCategory(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "الرجاء اختيار تصنيف للحذف", "تنبيه", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف هذا التصنيف؟\nملاحظة: لا يمكن حذف التصنيف إذا كان هناك مواد مرتبطة به.",
                "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            String name = tableModel.getValueAt(selectedRow, 0).toString();
            Category category = categoryDAO.getByName(name);
            
            if (category != null && categoryDAO.delete(category.getId())) {
                JOptionPane.showMessageDialog(this, "تم حذف التصنيف بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "فشل في حذف التصنيف", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}