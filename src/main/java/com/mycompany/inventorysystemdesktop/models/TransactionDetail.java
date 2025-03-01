package com.mycompany.inventorysystemdesktop.models;

public class TransactionDetail {
    private int id;
    private int transactionId;
    private int materialId;
    private double quantity;
    private double unitPrice;
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    
    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
} 