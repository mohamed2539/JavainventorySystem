package com.mycompany.inventorysystemdesktop.dao;

import com.mycompany.inventorysystemdesktop.models.Material;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {
    
    
    
public Material findById(int id) throws SQLException {
    String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                "LEFT JOIN categories c ON m.category_id = c.id " +
                "WHERE m.id = ?";
                
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, id);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Material material = new Material();
                material.setId(rs.getInt("id"));
                material.setCode(rs.getString("code"));
                material.setName(rs.getString("name"));
                material.setCategoryId(rs.getInt("category_id"));
                material.setUnit(rs.getString("unit"));
                material.setQuantity(rs.getDouble("quantity"));
                material.setMinQuantity(rs.getDouble("min_quantity"));
                material.setPrice(rs.getDouble("price"));
                material.setStatus(rs.getString("status"));
                return material;
            }
        }
    }
    return null;
}
    
    
    
    
    
    // إضافة مادة جديدة
  public boolean add(Material material) {
    String sql = "INSERT INTO materials (code, name, category_id, unit, quantity, min_quantity, price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        
        stmt.setString(1, material.getCode());
        stmt.setString(2, material.getName());
        stmt.setInt(3, material.getCategoryId());
        stmt.setString(4, material.getUnit());
        stmt.setDouble(5, material.getQuantity());
        stmt.setDouble(6, material.getMinQuantity());
        stmt.setDouble(7, material.getPrice());
        stmt.setString(8, material.getStatus());
        
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows > 0) {
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                material.setId(rs.getInt(1));
            }
            return true;
        }
        return false;
        
    } catch (SQLException e) {
        System.err.println("خطأ في إضافة المادة: " + e.getMessage());
        return false;
    }
}
    
    // تحديث مادة
    public boolean update(Material material) {
        String sql = "UPDATE materials SET code = ?, name = ?, category_id = ?, unit = ?, " +
                    "quantity = ?, min_quantity = ?, price = ?, status = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, material.getCode());
            stmt.setString(2, material.getName());
            stmt.setInt(3, material.getCategoryId());
            stmt.setString(4, material.getUnit());
            stmt.setDouble(5, material.getQuantity());
            stmt.setDouble(6, material.getMinQuantity());
            stmt.setDouble(7, material.getPrice());
            stmt.setString(8, material.getStatus());
            stmt.setInt(9, material.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("خطأ في تحديث المادة: " + e.getMessage());
            return false;
        }
    }
    
    // حذف مادة
    public boolean delete(int id) {
        String sql = "DELETE FROM materials WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("خطأ في حذف المادة: " + e.getMessage());
            return false;
        }
    }
    
    // الحصول على مادة بواسطة المعرف
    public Material getById(int id) {
        String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                    "LEFT JOIN categories c ON m.category_id = c.id " +
                    "WHERE m.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMaterial(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في استرجاع المادة: " + e.getMessage());
        }
        
        return null;
    }
    
    // الحصول على جميع المواد
    public List<Material> getAll() {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                    "LEFT JOIN categories c ON m.category_id = c.id " +
                    "ORDER BY m.name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                materials.add(mapResultSetToMaterial(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في استرجاع المواد: " + e.getMessage());
        }
        
        return materials;
    }
    
    // البحث عن المواد
    public List<Material> search(String keyword) {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                    "LEFT JOIN categories c ON m.category_id = c.id " +
                    "WHERE m.name LIKE ? OR m.code LIKE ? " +
                    "ORDER BY m.name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                materials.add(mapResultSetToMaterial(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("خطأ في البحث عن المواد: " + e.getMessage());
        }
        
        return materials;
    }
    
    // تحويل نتيجة الاستعلام إلى كائن Material
    private Material mapResultSetToMaterial(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setId(rs.getInt("id"));
        material.setCode(rs.getString("code"));
        material.setName(rs.getString("name"));
        material.setCategoryId(rs.getInt("category_id"));
        material.setCategoryName(rs.getString("category_name"));
        material.setUnit(rs.getString("unit"));
        material.setQuantity(rs.getDouble("quantity"));
        material.setMinQuantity(rs.getDouble("min_quantity"));
        material.setPrice(rs.getDouble("price"));
        material.setStatus(rs.getString("status"));
        material.setCreatedAt(rs.getTimestamp("created_at"));
        material.setUpdatedAt(rs.getTimestamp("updated_at"));
        return material;
    }
    
    // تحديث كمية المادة
    public boolean updateQuantity(int id, double quantity) {
        String sql = "UPDATE materials SET quantity = ?, " +
                    "status = CASE WHEN ? <= min_quantity THEN 'منخفض' ELSE 'متوفر' END, " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, quantity);
            stmt.setDouble(2, quantity);
            stmt.setInt(3, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("خطأ في تحديث كمية المادة: " + e.getMessage());
            return false;
        }
    }
    
    // الحصول على مادة بواسطة الكود
public Material getByCode(String code) {
    String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                "LEFT JOIN categories c ON m.category_id = c.id " +
                "WHERE m.code = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, code);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return mapResultSetToMaterial(rs);
        }
        
    } catch (SQLException e) {
        System.err.println("خطأ في استرجاع المادة: " + e.getMessage());
    }
    
    return null;
}
    
    
    
public List<Material> findAll() throws SQLException {
    List<Material> materials = new ArrayList<>();
    String sql = "SELECT m.*, c.name as category_name FROM materials m " +
                "LEFT JOIN categories c ON m.category_id = c.id " +
                "ORDER BY m.name";
                
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            Material material = new Material();
            material.setId(rs.getInt("id"));
            material.setCode(rs.getString("code"));
            material.setName(rs.getString("name"));
            material.setCategoryId(rs.getInt("category_id"));
            material.setUnit(rs.getString("unit"));
            material.setQuantity(rs.getDouble("quantity"));
            material.setMinQuantity(rs.getDouble("min_quantity"));
            material.setPrice(rs.getDouble("price"));
            material.setStatus(rs.getString("status"));
            materials.add(material);
        }
    }
    
    return materials;
}
    
    
   
    }
    
    


    
