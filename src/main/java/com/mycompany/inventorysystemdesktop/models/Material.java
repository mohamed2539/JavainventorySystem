
package com.mycompany.inventorysystemdesktop.models;

import java.sql.Timestamp;

public class Material {
    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String categoryName; // للعرض فقط
    private String unit;
    private double quantity;
    private double minQuantity;
    private double price;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private double minimumQuantity;
    
    // Constructor
    public Material() {}
    
    // Constructor with parameters
    public Material(String code, String name, int categoryId, String unit, 
                   double quantity, double minQuantity, double price) {
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.unit = unit;
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.price = price;
        this.status = quantity <= minQuantity ? "منخفض" : "متوفر";
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.status = quantity <= minQuantity ? "منخفض" : "متوفر";
    }

    public double getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(double minQuantity) {
        this.minQuantity = minQuantity;
        this.status = quantity <= minQuantity ? "منخفض" : "متوفر";
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
 
    
    public double getMinimumQuantity() {
        return minimumQuantity;
    }
    
   
    
    public void setMinimumQuantity(double minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }
    

    
}