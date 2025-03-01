package com.mycompany.inventorysystemdesktop.dao;

import com.mycompany.inventorysystemdesktop.models.Transaction;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {
    
    public void create(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (material_id, type, quantity, unit_price, reference_no, notes, created_at, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, transaction.getMaterialId());
            stmt.setString(2, transaction.getType());
            stmt.setDouble(3, transaction.getQuantity());
            stmt.setDouble(4, transaction.getUnitPrice());
            stmt.setString(5, transaction.getReferenceNo());
            stmt.setString(6, transaction.getNotes());
            stmt.setTimestamp(7, new Timestamp(transaction.getTransactionDate().getTime()));
            stmt.setInt(8, transaction.getUserId());
            
            stmt.executeUpdate();
            
            // تحديث كمية المادة
            updateMaterialQuantity(conn, transaction);
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public void update(Transaction transaction) throws SQLException {
        // استرجاع المعاملة القديمة
        Transaction oldTransaction = findById(transaction.getId());
        
        String sql = "UPDATE transactions SET quantity = ?, unit_price = ?, reference_no = ?, notes = ?, created_at = ? " +
                     "WHERE id = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, transaction.getQuantity());
            stmt.setDouble(2, transaction.getUnitPrice());
            stmt.setString(3, transaction.getReferenceNo());
            stmt.setString(4, transaction.getNotes());
            stmt.setTimestamp(5, new Timestamp(transaction.getTransactionDate().getTime()));
            stmt.setInt(6, transaction.getId());
            
            stmt.executeUpdate();
            
            // تحديث كمية المادة
            updateMaterialQuantityOnEdit(conn, oldTransaction, transaction);
        }
    }
    
 public boolean delete(String referenceNo) throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        
        // أولاً نجد المعاملة
        Transaction transaction = findByReferenceNo(referenceNo);
        if (transaction == null) {
            return false;
        }
        
        // نقوم بتحديث كمية المادة
        String updateMaterialSql = "UPDATE materials SET quantity = quantity " +
            (transaction.getType().equals("IN") ? "-" : "+") +
            " ? WHERE id = ?";
        
        stmt = conn.prepareStatement(updateMaterialSql);
        stmt.setDouble(1, transaction.getQuantity());
        stmt.setInt(2, transaction.getMaterialId());
        stmt.executeUpdate();
        
        // ثم نحذف المعاملة
        String deleteSql = "DELETE FROM transactions WHERE reference_no = ?";
        stmt = conn.prepareStatement(deleteSql);
        stmt.setString(1, referenceNo);
        
        int result = stmt.executeUpdate();
        conn.commit();
        return result > 0;
        
    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        throw e;
    } finally {
        if (stmt != null) {
            stmt.close();
        }
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
    
 public Transaction findById(int id) throws SQLException {
        String sql = "SELECT t.*, m.name as material_name, u.username as user_name " +
                    "FROM transactions t " +
                    "JOIN materials m ON t.material_id = m.id " +
                    "LEFT JOIN users u ON t.user_id = u.id " +
                    "WHERE t.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        }
        return null;
    }
    
  public List<Transaction> search(String searchTerm, String type, Date fromDate, Date toDate) {
    List<Transaction> transactions = new ArrayList<>();
    StringBuilder sql = new StringBuilder(
        "SELECT t.*, m.name as material_name " +
        "FROM transactions t " +
        "JOIN materials m ON t.material_id = m.id " +
        "WHERE 1=1"
    );
    List<Object> params = new ArrayList<>();
    
    if (searchTerm != null && !searchTerm.isEmpty()) {
        sql.append(" AND (m.name LIKE ? OR t.reference_no LIKE ? OR t.notes LIKE ?)");
        params.add("%" + searchTerm + "%");
        params.add("%" + searchTerm + "%");
        params.add("%" + searchTerm + "%");
    }
    
    if (type != null && !type.isEmpty()) {
        sql.append(" AND t.type = ?");
        params.add(type);
    }
    
    if (fromDate != null) {
        sql.append(" AND t.created_at >= ?");
        params.add(new Timestamp(fromDate.getTime()));
    }
    
    if (toDate != null) {
        sql.append(" AND t.created_at <= ?");
        params.add(new Timestamp(toDate.getTime()));
    }
    
    sql.append(" ORDER BY t.created_at DESC");
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return transactions;
}
   private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setMaterialId(rs.getInt("material_id"));
        transaction.setType(rs.getString("type"));
        transaction.setQuantity(rs.getDouble("quantity"));
        transaction.setUnitPrice(rs.getDouble("unit_price"));
        transaction.setReferenceNo(rs.getString("reference_no"));
        transaction.setNotes(rs.getString("notes"));
        transaction.setTransactionDate(rs.getTimestamp("created_at"));
        transaction.setUserId(rs.getInt("user_id"));
        return transaction;
    }
    
    private void updateMaterialQuantity(Connection conn, Transaction transaction) throws SQLException {
        String sql = "UPDATE materials SET quantity = quantity + ? WHERE id = ?";
        double quantityChange = transaction.getType().equals("IN") ? transaction.getQuantity() : -transaction.getQuantity();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantityChange);
            stmt.setInt(2, transaction.getMaterialId());
            stmt.executeUpdate();
        }
    }
    
    private void updateMaterialQuantityOnEdit(Connection conn, Transaction oldTransaction, Transaction newTransaction) throws SQLException {
        // عكس تأثير المعاملة القديمة
        double oldQuantityChange = oldTransaction.getType().equals("IN") ? -oldTransaction.getQuantity() : oldTransaction.getQuantity();
        
        // إضافة تأثير المعاملة الجديدة
        double newQuantityChange = newTransaction.getType().equals("IN") ? newTransaction.getQuantity() : -newTransaction.getQuantity();
        
        String sql = "UPDATE materials SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, oldQuantityChange + newQuantityChange);
            stmt.setInt(2, newTransaction.getMaterialId());
            stmt.executeUpdate();
        }
    }
 
    
    
        private void reverseTransactionEffect(Connection conn, Transaction transaction) throws SQLException {
        String sql = "UPDATE materials SET quantity = quantity + ? WHERE id = ?";
        double quantityChange = transaction.getType().equals("IN") ? -transaction.getQuantity() : transaction.getQuantity();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, quantityChange);
            stmt.setInt(2, transaction.getMaterialId());
            stmt.executeUpdate();
        }
    }
    
    // دوال إضافية للتقارير
    public double getTotalIncoming(Date fromDate, Date toDate) throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) as total FROM transactions " +
                    "WHERE type = 'IN' AND created_at BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, new Timestamp(fromDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(toDate.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    
    public double getTotalOutgoing(Date fromDate, Date toDate) throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) as total FROM transactions " +
                    "WHERE type = 'OUT' AND created_at BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, new Timestamp(fromDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(toDate.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    
    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapTransaction(rs));
                }
            }
        }
        
        return transactions;
    }
    
    
    
     public List<Transaction> getAll(String type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, m.name as material_name, u.username as user_name " +
                    "FROM transactions t " +
                    "JOIN materials m ON t.material_id = m.id " +
                    "LEFT JOIN users u ON t.user_id = u.id " +
                    "WHERE t.type = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = mapTransaction(rs);
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("خطأ في جلب المعاملات: " + e.getMessage());
        }
        
        return transactions;
    }
    
     
       public Transaction findByReferenceNo(String referenceNo) throws SQLException {
        String sql = "SELECT t.*, m.name as material_name FROM transactions t " +
                    "JOIN materials m ON t.material_id = m.id " +
                    "WHERE t.reference_no = ?";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, referenceNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTransaction(rs);
                }
            }
        }
        return null;
    }
     
    
     
}
