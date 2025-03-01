package com.mycompany.inventorysystemdesktop.models;

import java.util.Date;

public class Transaction {
    private int id;
    private int materialId;
    private String type; // IN, OUT
    private double quantity;
    private double unitPrice;
    private String referenceNo;
    private String notes;
    private Date transactionDate;
    private int userId;
    
    // Constructor
    public Transaction() {
        this.transactionDate = new Date();
    }
    
    // Helper methods
    public double getTotalPrice() {
        return quantity * unitPrice;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getMaterialId() {
        return materialId;
    }
    
    public String getType() {
        return type;
    }
    
    public double getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public String getReferenceNo() {
        return referenceNo;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public Date getTransactionDate() {
        return transactionDate;
    }
    
    public int getUserId() {
        return userId;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}