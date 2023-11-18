package com.example.bolti_koltes;

import java.io.Serializable;
import java.util.Objects;

public class ShoppingListItem implements Serializable {
    private Product product;
    private int amount;
    private boolean isChecked;
    private double salePer;

    public ShoppingListItem(Product product, int amount, boolean isChecked, double salePer)
    {
        this.product=product;
        this.amount=amount;
        this.isChecked=isChecked;
        this.salePer=salePer;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingListItem that = (ShoppingListItem) o;
        return Objects.equals(product, that.product) && salePer == that.salePer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, salePer);
    }

    public double getSalePer() {
        return salePer;
    }

    public void setSalePer(double salePer) {
        this.salePer = salePer;
    }


}
