package com.mycompany.inventorysystemdesktop.dao;

import com.mycompany.inventorysystemdesktop.models.Category;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    // إضافة تصنيف جديد
    public boolean add(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("خطأ في إضافة التصنيف: " + e.getMessage());
            return false;
        }
    }
    
    // تحديث تصنيف
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("خطأ في تحديث التصنيف: " + e.getMessage());
            return false;
        }
    }
    
    // حذف تصنيف
    public boolean delete(int id) {
        // أولاً نتحقق من عدم وجود مواد مرتبطة بهذا التصنيف
        if (hasRelatedMaterials(id)) {
            System.err.println("لا يمكن حذف التصنيف لوجود مواد مرتبطة به");
            return false;
        }
        
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("خطأ في حذف التصنيف: " + e.getMessage());
            return false;
        }
    }
    
    // التحقق من وجود مواد مرتبطة بالتصنيف
    private boolean hasRelatedMaterials(int categoryId) {
        String sql = "SELECT COUNT(*) FROM materials WHERE category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في التحقق من المواد المرتبطة: " + e.getMessage());
        }
        
        return false;
    }
    
    // الحصول على تصنيف بواسطة المعرف
    public Category getById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في استرجاع التصنيف: " + e.getMessage());
        }
        
        return null;
    }
    
    // الحصول على جميع التصنيفات
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في استرجاع التصنيفات: " + e.getMessage());
        }
        
        return categories;
    }
    
    // البحث عن التصنيفات
    public List<Category> search(String keyword) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE name LIKE ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في البحث عن التصنيفات: " + e.getMessage());
        }
        
        return categories;
    }
    
    // تحويل نتيجة الاستعلام إلى كائن Category
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setCreatedAt(rs.getTimestamp("created_at"));
        category.setUpdatedAt(rs.getTimestamp("updated_at"));
        return category;
    }
    
    public Category getByName(String name) {
    String sql = "SELECT * FROM categories WHERE name = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return mapResultSetToCategory(rs);
        }
        
    } catch (SQLException e) {
        System.err.println("خطأ في استرجاع التصنيف: " + e.getMessage());
    }
    
    return null;
}
    
    
    
}