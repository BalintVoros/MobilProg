package com.example.bolti_koltes;

import android.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class ShoppingList implements Serializable {
    private String name;
    private int ID;
    ArrayList<ShoppingListItem> products;

    public ShoppingList(String name, int ID)
    {
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    void setProducts(ArrayList<ShoppingListItem> list)
    {
        products = list;
    }

}
