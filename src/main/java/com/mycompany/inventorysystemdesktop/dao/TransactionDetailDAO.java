package com.mycompany.inventorysystemdesktop.dao;

import com.mycompany.inventorysystemdesktop.models.TransactionDetail;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDetailDAO {
    
    public boolean add(TransactionDetail detail, Connection conn) throws SQLException {
        String sql = "INSERT INTO transaction_details (transaction_id, material_id, quantity, unit_price) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, detail.getTransactionId());
            stmt.setInt(2, detail.getMaterialId());
            stmt.setDouble(3, detail.getQuantity());
            stmt.setDouble(4, detail.getUnitPrice());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    detail.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }
    
    public List<TransactionDetail> getByTransactionId(int transactionId) throws SQLException {
        List<TransactionDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM transaction_details WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                TransactionDetail detail = new TransactionDetail();
                detail.setId(rs.getInt("id"));
                detail.setTransactionId(rs.getInt("transaction_id"));
                detail.setMaterialId(rs.getInt("material_id"));
                detail.setQuantity(rs.getDouble("quantity"));
                detail.setUnitPrice(rs.getDouble("unit_price"));
                details.add(detail);
            }
        }
        
        return details;
    }
    
    public boolean deleteByTransactionId(int transactionId, Connection conn) throws SQLException {
        String sql = "DELETE FROM transaction_details WHERE transaction_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            return stmt.executeUpdate() > 0;
        }
    }
} 