package com.example.bolti_koltes;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private String name;
    private String unit;
    private String barcode;
    private Category category;
    private double price;
    private double typicalPrice;
    private int ID;


    public Product(String name, Category category, double price, String unit, double typicalPrice)
    {
        this.name = name;
        this.category = category;
        this.price = price;
        this.unit = unit;
        this.typicalPrice = typicalPrice;
    }

    public Product(String name, String unit, String barcode, Category category, double price, double typicalPrice, int id)
    {
        this.name = name;
        this.unit = unit;
        this.barcode = barcode;
        this.category = category;
        this.price = price;
        this.typicalPrice = typicalPrice;
        this.ID = id;
    }

    int getID()
    {
        return ID;
    }
    @Override
    public String toString() {
        return name;
    }

    public String unfoldToString() {
        String str = getName();
        str += "(" + getUnit() + "): " + "[" + getCategory().toString() + "] BARCODE:" + getBarcode();
        str += "\nPrice: " + getPrice();
        str += "\nUnit: " + getUnit();

        return str;
    }

    Category getCategory()
    {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice()
    {
        return price;
    }

    public double getTypicalPrice()
    {
        return  typicalPrice;
    }

    public void setCategory(Category category){
        this.category=category;
    }

    public  void setId(int id)
    {
        this.ID=id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(category, product.category) && typicalPrice == product.typicalPrice &&
                Objects.equals(name, product.name) && Objects.equals(unit, product.unit) &&
                Objects.equals(barcode, product.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unit, barcode, category, price, typicalPrice);
    }

    public void setPrice(double price) {
        this.price=price;
    }

}
