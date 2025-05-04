package com.example.todooapp.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tests")
public class Test {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private long createdDate;
    private String imagePath;

    private double price;  // New property
    public Test(String name, double price) {
        this.name = name;
        this.price = price;
        this.createdDate = System.currentTimeMillis();
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Add getter and setter for price
    public double getPrice() {
        return price;
    }

    // ... existing getters and setters

    public void setPrice(double price) {
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}