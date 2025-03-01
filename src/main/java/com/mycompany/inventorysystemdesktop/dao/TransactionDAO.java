package com.mycompany.inventorysystemdesktop.dao;

import com.mycompany.inventorysystemdesktop.models.Transaction;
import com.mycompany.inventorysystemdesktop.models.TransactionDetail;
import com.mycompany.inventorysystemdesktop.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionDAO {
    private final TransactionDetailDAO detailDAO;
    private final MaterialDAO materialDAO;
    
    public TransactionDAO() {
        this.detailDAO = new TransactionDetailDAO();
        this.materialDAO = new MaterialDAO();
    }
    
    public boolean add(Transaction transaction) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // إضافة المعاملة الرئيسية
            String sql = "INSERT INTO transactions (type, notes, user_id, transaction_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, transaction.getType());
                stmt.setString(2, transaction.getNotes());
                stmt.setInt(3, transaction.getUserId());
                stmt.setTimestamp(4, new Timestamp(transaction.getTransactionDate().getTime()));
                
                if (stmt.executeUpdate() > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        transaction.setId(rs.getInt(1));
                        
                        // إضافة تفاصيل المعاملة
                        for (TransactionDetail detail : transaction.getDetails()) {
                            detail.setTransactionId(transaction.getId());
                            if (!detailDAO.add(detail, conn)) {
                                throw new SQLException("فشل في إضافة تفاصيل المعاملة");
                            }
                            
                            // تحديث كمية المادة
                            double newQuantity;
                            if (transaction.getType().equals("IN")) {
                                newQuantity = materialDAO.getById(detail.getMaterialId()).getQuantity() + detail.getQuantity();
                            } else {
                                newQuantity = materialDAO.getById(detail.getMaterialId()).getQuantity() - detail.getQuantity();
                            }
                            materialDAO.updateQuantity(detail.getMaterialId(), newQuantity);
                        }
                        
                        conn.commit();
                        return true;
                    }
                }
            }
            
            conn.rollback();
            return false;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("خطأ في التراجع عن العملية: " + ex.getMessage());
                }
            }
            System.err.println("خطأ في إضافة المعاملة: " + e.getMessage());
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("خطأ في إغلاق الاتصال: " + e.getMessage());
                }
            }
        }
    }
    
    public Transaction findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transaction.setDetails(detailDAO.getByTransactionId(id));
                return transaction;
            }
        }
        return null;
    }
    
    public List<Transaction> search(String keyword, String type, Date from, Date to) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.* FROM transactions t ");
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        
        if (type != null && !type.isEmpty()) {
            sql.append("AND t.type = ? ");
            params.add(type);
        }
        
        if (from != null) {
            sql.append("AND t.transaction_date >= ? ");
            params.add(new Timestamp(from.getTime()));
        }
        
        if (to != null) {
            sql.append("AND t.transaction_date <= ? ");
            params.add(new Timestamp(to.getTime()));
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (t.notes LIKE ? OR t.reference_no LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        
        sql.append("ORDER BY t.transaction_date DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transaction.setDetails(detailDAO.getByTransactionId(transaction.getId()));
                transactions.add(transaction);
            }
        }
        
        return transactions;
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setType(rs.getString("type"));
        transaction.setNotes(rs.getString("notes"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date"));
        transaction.setReferenceNo(rs.getString("reference_no"));
        return transaction;
    }
    
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // حذف تفاصيل المعاملة أولاً
            if (detailDAO.deleteByTransactionId(id, conn)) {
                // ثم حذف المعاملة نفسها
                String sql = "DELETE FROM transactions WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    if (stmt.executeUpdate() > 0) {
                        conn.commit();
                        return true;
                    }
                }
            }
            
            conn.rollback();
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("خطأ في إغلاق الاتصال: " + e.getMessage());
                }
            }
        }
    }
}
